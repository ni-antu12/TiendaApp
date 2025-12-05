from flask import Flask, request, jsonify
from flask_cors import CORS
import psycopg2
from psycopg2.extras import RealDictCursor
import os
import sys

# Agregar el directorio padre al path para importar config
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from config import DATABASE_URL

app = Flask(__name__)
CORS(app)

# Función para obtener conexión a la base de datos
def get_db_connection():
    try:
        conn = psycopg2.connect(DATABASE_URL)
        return conn
    except Exception as e:
        print(f"Error conectando a la base de datos: {e}")
        raise

# Inicializar base de datos
def init_db():
    conn = get_db_connection()
    cursor = conn.cursor()
    
    # DROP TABLES (reset completo, excepto users)
    print("Eliminando tablas de ventas...")
    cursor.execute("DROP TABLE IF EXISTS order_items CASCADE;")
    cursor.execute("DROP TABLE IF EXISTS sales CASCADE;")
    cursor.execute("DROP TABLE IF EXISTS cart_items CASCADE;")
    cursor.execute("DROP TABLE IF EXISTS orders CASCADE;")
    conn.commit()
    print("Tablas eliminadas")
    
    # Tabla de órdenes/pedidos
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS orders(
            id SERIAL PRIMARY KEY,
            user_id INTEGER NOT NULL,
            total DECIMAL(10, 2) NOT NULL,
            status VARCHAR(50) DEFAULT 'pending',
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
    """)
    
    # Tabla de items de carrito (carrito activo)
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS cart_items(
            id SERIAL PRIMARY KEY,
            user_id INTEGER NOT NULL,
            product_id INTEGER NOT NULL,
            quantity INTEGER NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            UNIQUE(user_id, product_id)
        );
    """)
    
    # Tabla de items de orden (productos de una orden)
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS order_items(
            id SERIAL PRIMARY KEY,
            order_id INTEGER NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
            product_id INTEGER NOT NULL,
            quantity INTEGER NOT NULL,
            price DECIMAL(10, 2) NOT NULL,
            subtotal DECIMAL(10, 2) NOT NULL
        );
    """)
    
    # Tabla de ventas (mantener compatibilidad)
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS sales(
            id SERIAL PRIMARY KEY,
            user_id INTEGER NOT NULL,
            product_id INTEGER NOT NULL,
            quantity INTEGER NOT NULL,
            total DECIMAL(10, 2) NOT NULL,
            order_id INTEGER REFERENCES orders(id),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
    """)
    
    conn.commit()
    cursor.close()
    conn.close()
    print("Tablas de ventas inicializadas correctamente")

# ========== CARRITO ==========

# Obtener carrito de un usuario
@app.get("/cart/<int:user_id>")
def get_cart(user_id):
    try:
        conn = get_db_connection()
        cursor = conn.cursor(cursor_factory=RealDictCursor)
        cursor.execute("""
            SELECT ci.*, p.name, p.price, p.image_url
            FROM cart_items ci
            JOIN products p ON ci.product_id = p.id
            WHERE ci.user_id = %s
            ORDER BY ci.created_at DESC
        """, (user_id,))
        items = cursor.fetchall()
        cursor.close()
        conn.close()
        
        result = []
        for item in items:
            result.append({
                "id": item["id"],
                "user_id": item["user_id"],
                "product_id": item["product_id"],
                "quantity": item["quantity"],
                "product_name": item["name"],
                "product_price": float(item["price"]),
                "product_image_url": item.get("image_url", "")
            })
        
        return jsonify(result)
    except Exception as e:
        return jsonify({"error": f"Error del servidor: {str(e)}"}), 500

# Agregar item al carrito
@app.post("/cart")
def add_to_cart():
    try:
        data = request.json
        if not data or "user_id" not in data or "product_id" not in data or "quantity" not in data:
            return jsonify({"error": "Datos incompletos"}), 400
        
        user_id = int(data["user_id"])
        product_id = int(data["product_id"])
        quantity = int(data["quantity"])
        
        conn = get_db_connection()
        cursor = conn.cursor(cursor_factory=RealDictCursor)
        
        # VALIDACIÓN: Verificar que el usuario no esté comprando su propio producto
        cursor.execute("""
            SELECT p.seller_name, u.username as buyer_username
            FROM products p
            CROSS JOIN users u
            WHERE p.id = %s AND u.id = %s
        """, (product_id, user_id))
        
        validation = cursor.fetchone()
        if validation:
            seller_name = validation.get("seller_name")
            buyer_username = validation.get("buyer_username")
            
            if seller_name and buyer_username and seller_name == buyer_username:
                cursor.close()
                conn.close()
                return jsonify({"error": "No puedes comprar tus propios productos"}), 403
        
        # Verificar si ya existe en el carrito
        cursor.execute("SELECT * FROM cart_items WHERE user_id = %s AND product_id = %s", 
                      (user_id, product_id))
        existing = cursor.fetchone()
        
        if existing:
            # Actualizar cantidad
            new_quantity = existing["quantity"] + quantity
            cursor.execute("""
                UPDATE cart_items 
                SET quantity = %s 
                WHERE user_id = %s AND product_id = %s
                RETURNING *
            """, (new_quantity, user_id, product_id))
        else:
            # Insertar nuevo
            cursor.execute("""
                INSERT INTO cart_items(user_id, product_id, quantity) 
                VALUES (%s, %s, %s) 
                RETURNING *
            """, (user_id, product_id, quantity))
        
        
        item = cursor.fetchone()
        conn.commit()
        cursor.close()
        conn.close()
        
        return jsonify({"message": "Item agregado al carrito", "id": item["id"]}), 201
    except Exception as e:
        return jsonify({"error": f"Error del servidor: {str(e)}"}), 500

# Actualizar cantidad en carrito
@app.put("/cart/<int:item_id>")
def update_cart_item(item_id):
    try:
        data = request.json
        if not data or "quantity" not in data:
            return jsonify({"error": "Datos incompletos"}), 400
        
        quantity = int(data["quantity"])
        
        if quantity <= 0:
            # Eliminar item
            conn = get_db_connection()
            cursor = conn.cursor()
            cursor.execute("DELETE FROM cart_items WHERE id = %s", (item_id,))
            conn.commit()
            cursor.close()
            conn.close()
            return jsonify({"message": "Item eliminado del carrito"}), 200
        
        conn = get_db_connection()
        cursor = conn.cursor(cursor_factory=RealDictCursor)
        cursor.execute("""
            UPDATE cart_items 
            SET quantity = %s 
            WHERE id = %s 
            RETURNING *
        """, (quantity, item_id))
        
        item = cursor.fetchone()
        if not item:
            cursor.close()
            conn.close()
            return jsonify({"error": "Item no encontrado"}), 404
        
        conn.commit()
        cursor.close()
        conn.close()
        
        return jsonify({"message": "Carrito actualizado", "id": item["id"]})
    except Exception as e:
        return jsonify({"error": f"Error del servidor: {str(e)}"}), 500

# Eliminar item del carrito
@app.delete("/cart/<int:item_id>")
def delete_cart_item(item_id):
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute("DELETE FROM cart_items WHERE id = %s", (item_id,))
        conn.commit()
        deleted = cursor.rowcount
        cursor.close()
        conn.close()
        
        if deleted > 0:
            return jsonify({"message": "Item eliminado del carrito"}), 200
        else:
            return jsonify({"error": "Item no encontrado"}), 404
    except Exception as e:
        return jsonify({"error": f"Error del servidor: {str(e)}"}), 500

# Limpiar carrito de un usuario
@app.delete("/cart/user/<int:user_id>")
def clear_cart(user_id):
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute("DELETE FROM cart_items WHERE user_id = %s", (user_id,))
        conn.commit()
        cursor.close()
        conn.close()
        return jsonify({"message": "Carrito limpiado"}), 200
    except Exception as e:
        return jsonify({"error": f"Error del servidor: {str(e)}"}), 500

# ========== ÓRDENES ==========

# Crear orden (procesar compra)
@app.post("/orders")
def create_order():
    try:
        data = request.json
        if not data or "user_id" not in data:
            return jsonify({"error": "Datos incompletos"}), 400
        
        user_id = int(data["user_id"])
        
        conn = get_db_connection()
        cursor = conn.cursor(cursor_factory=RealDictCursor)
        
        # Obtener items del carrito
        cursor.execute("""
            SELECT ci.*, p.price 
            FROM cart_items ci
            JOIN products p ON ci.product_id = p.id
            WHERE ci.user_id = %s
        """, (user_id,))
        cart_items = cursor.fetchall()
        
        if not cart_items:
            cursor.close()
            conn.close()
            return jsonify({"error": "El carrito está vacío"}), 400
        
        # Calcular total
        total = sum(float(item["price"]) * item["quantity"] for item in cart_items)
        
        # Crear orden
        cursor.execute("""
            INSERT INTO orders(user_id, total, status) 
            VALUES (%s, %s, 'completed') 
            RETURNING *
        """, (user_id, total))
        order = cursor.fetchone()
        order_id = order["id"]
        
        # Crear order_items y sales
        for item in cart_items:
            product_id = item["product_id"]
            quantity = item["quantity"]
            price = float(item["price"])
            subtotal = price * quantity
            
            # Order item
            cursor.execute("""
                INSERT INTO order_items(order_id, product_id, quantity, price, subtotal) 
                VALUES (%s, %s, %s, %s, %s)
            """, (order_id, product_id, quantity, price, subtotal))
            
            # Sale - Registrar venta para el VENDEDOR del producto
            # Obtener seller_name del producto
            cursor.execute("SELECT seller_name FROM products WHERE id = %s", (product_id,))
            product_result = cursor.fetchone()
            
            if product_result and product_result["seller_name"]:
                seller_name = product_result["seller_name"]
                
                # Obtener user_id del seller
                cursor.execute('SELECT id FROM users WHERE username = %s', (seller_name,))
                seller_result = cursor.fetchone()
                
                if seller_result:
                    seller_id = seller_result["id"]
                    # Insertar en sales con el ID del VENDEDOR
                    cursor.execute("""
                        INSERT INTO sales(user_id, product_id, quantity, total, order_id) 
                        VALUES (%s, %s, %s, %s, %s)
                    """, (seller_id, product_id, quantity, subtotal, order_id))
        
        # Limpiar carrito
        cursor.execute("DELETE FROM cart_items WHERE user_id = %s", (user_id,))
        
        conn.commit()
        cursor.close()
        conn.close()
        
        return jsonify({
            "message": "Orden creada exitosamente",
            "order_id": order_id,
            "total": float(total)
        }), 201
    except Exception as e:
        return jsonify({"error": f"Error del servidor: {str(e)}"}), 500

# Obtener órdenes de un usuario
@app.get("/orders/user/<int:user_id>")
def get_user_orders(user_id):
    try:
        conn = get_db_connection()
        cursor = conn.cursor(cursor_factory=RealDictCursor)
        cursor.execute("""
            SELECT o.*, 
                   COUNT(oi.id) as items_count
            FROM orders o
            LEFT JOIN order_items oi ON o.id = oi.order_id
            WHERE o.user_id = %s
            GROUP BY o.id
            ORDER BY o.created_at DESC
        """, (user_id,))
        orders = cursor.fetchall()
        cursor.close()
        conn.close()
        
        result = []
        for order in orders:
            result.append({
                "id": order["id"],
                "user_id": order["user_id"],
                "total": float(order["total"]),
                "status": order["status"],
                "items_count": order["items_count"],
                "created_at": str(order["created_at"])
            })
        
        return jsonify(result)
    except Exception as e:
        return jsonify({"error": f"Error del servidor: {str(e)}"}), 500

# Obtener items de una orden
@app.get("/orders/<int:order_id>/items")
def get_order_items(order_id):
    try:
        conn = get_db_connection()
        cursor = conn.cursor(cursor_factory=RealDictCursor)
        cursor.execute("""
            SELECT oi.*, p.name, p.image_url
            FROM order_items oi
            JOIN products p ON oi.product_id = p.id
            WHERE oi.order_id = %s
        """, (order_id,))
        items = cursor.fetchall()
        cursor.close()
        conn.close()
        
        result = []
        for item in items:
            result.append({
                "id": item["id"],
                "order_id": item["order_id"],
                "product_id": item["product_id"],
                "product_name": item["name"],
                "product_image_url": item.get("image_url", ""),
                "quantity": item["quantity"],
                "price": float(item["price"]),
                "subtotal": float(item["subtotal"])
            })
        
        return jsonify(result)
    except Exception as e:
        return jsonify({"error": f"Error del servidor: {str(e)}"}), 500

# ========== VENTAS (Compatibilidad) ==========

# Registrar venta (mantener compatibilidad)
@app.post("/sales")
def create_sale():
    try:
        data = request.json
        if not data:
            return jsonify({"error": "Datos incompletos"}), 400
            
        if "user_id" not in data or "product_id" not in data or "quantity" not in data or "total" not in data:
            return jsonify({"error": "Datos incompletos. Se requiere user_id, product_id, quantity y total"}), 400

        user_id = int(data["user_id"])
        product_id = int(data["product_id"])
        quantity = int(data["quantity"])
        total = float(data["total"])
        order_id = data.get("order_id")

        conn = get_db_connection()
        cursor = conn.cursor(cursor_factory=RealDictCursor)

        cursor.execute("""
            INSERT INTO sales(user_id, product_id, quantity, total, order_id) 
            VALUES (%s, %s, %s, %s, %s) 
            RETURNING *
        """, (user_id, product_id, quantity, total, order_id))
        
        new_sale = cursor.fetchone()
        conn.commit()
        cursor.close()
        conn.close()

        sale_dict = dict(new_sale)
        if sale_dict.get('total'):
            sale_dict['total'] = float(sale_dict['total'])
        if sale_dict.get('created_at'):
            sale_dict['created_at'] = str(sale_dict['created_at'])

        return jsonify({"message": "Venta registrada", "id": sale_dict["id"], "sale": sale_dict}), 201
    except ValueError as e:
        return jsonify({"error": f"Error en los datos: {str(e)}"}), 400
    except Exception as e:
        return jsonify({"error": f"Error del servidor: {str(e)}"}), 500

# Obtener todas las ventas
@app.get("/sales")
def get_sales():
    try:
        conn = get_db_connection()
        cursor = conn.cursor(cursor_factory=RealDictCursor)
        cursor.execute("""
            SELECT s.*, o.id as order_id
            FROM sales s
            LEFT JOIN orders o ON s.order_id = o.id
            ORDER BY s.created_at DESC
        """)
        sales = cursor.fetchall()
        cursor.close()
        conn.close()

        result = []
        for sale in sales:
            sale_dict = dict(sale)
            if sale_dict.get('total'):
                sale_dict['total'] = float(sale_dict['total'])
            if sale_dict.get('created_at'):
                sale_dict['created_at'] = str(sale_dict['created_at'])
            result.append(sale_dict)

        return jsonify(result)
    except Exception as e:
        return jsonify({"error": f"Error del servidor: {str(e)}"}), 500

# Obtener ventas de un usuario específico (productos que vendió)
@app.get("/sales/user/<int:user_id>")
def get_user_sales(user_id):
    try:
        print(f"Getting sales for user_id: {user_id}")
        conn = get_db_connection()
        cursor = conn.cursor(cursor_factory=RealDictCursor)
        cursor.execute("""
            SELECT 
                s.id as sale_id,
                s.product_id,
                s.quantity,
                s.total,
                s.created_at,
                p.name as product_name,
                p.price as product_price,
                p.image_url as product_image,
                p.seller_name,
                p.stock as product_stock
            FROM sales s
            JOIN products p ON s.product_id = p.id
            WHERE s.user_id = %s
            ORDER BY s.created_at DESC
        """, (user_id,))
        
        sales = cursor.fetchall()
        print(f"Found {len(sales)} sales for user {user_id}")
        cursor.close()
        conn.close()
        
        result = []
        for sale in sales:
            sale_dict = dict(sale)
            if sale_dict.get('total'):
                sale_dict['total'] = float(sale_dict['total'])
            if sale_dict.get('created_at'):
                sale_dict['created_at'] = str(sale_dict['created_at'])
            result.append(sale_dict)
        
        return jsonify(result), 200
        
    except Exception as e:
        print(f"Error getting user sales: {str(e)}")
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    init_db()
    app.run(host="0.0.0.0", port=5003, debug=True)
