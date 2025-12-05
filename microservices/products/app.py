from flask import Flask, jsonify, request
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
    
    # DROP TABLE products (reset completo)
    print("Eliminando tabla products...")
    cursor.execute("DROP TABLE IF EXISTS products CASCADE;")
    conn.commit()
    print("Tabla products eliminada")
    
    # Crear tabla products
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS products(
            id SERIAL PRIMARY KEY,
            name VARCHAR(255) NOT NULL,
            description TEXT,
            price DECIMAL(10, 2) NOT NULL,
            stock INTEGER DEFAULT 0,
            category VARCHAR(100),
            image_url TEXT,
            seller_name VARCHAR(255)
        );
    """)
    conn.commit()
    
    cursor.close()
    conn.close()
    print("Tabla products inicializada correctamente")

# Obtener todos los productos
@app.get("/products")
def get_products():
    try:
        conn = get_db_connection()
        cursor = conn.cursor(cursor_factory=RealDictCursor)
        cursor.execute("SELECT * FROM products ORDER BY id")
        products = cursor.fetchall()
        cursor.close()
        conn.close()
        
        # Convertir Decimal a float para JSON
        result = []
        for product in products:
            product_dict = dict(product)
            if product_dict.get('price'):
                product_dict['price'] = float(product_dict['price'])
            result.append(product_dict)
        
        return jsonify(result)
    except Exception as e:
        return jsonify({"error": f"Error del servidor: {str(e)}"}), 500

# Obtener un producto por ID
@app.get("/products/<int:product_id>")
def get_product(product_id):
    try:
        conn = get_db_connection()
        cursor = conn.cursor(cursor_factory=RealDictCursor)
        cursor.execute("SELECT * FROM products WHERE id = %s", (product_id,))
        product = cursor.fetchone()
        cursor.close()
        conn.close()
        
        if product:
            product_dict = dict(product)
            if product_dict.get('price'):
                product_dict['price'] = float(product_dict['price'])
            return jsonify(product_dict)
        else:
            return jsonify({"error": "Producto no encontrado"}), 404
    except Exception as e:
        return jsonify({"error": f"Error del servidor: {str(e)}"}), 500

# Crear un producto nuevo
@app.post("/products")
def create_product():
    try:
        data = request.get_json()
        
        if not data or "name" not in data or "price" not in data:
            return jsonify({"error": "Datos incompletos. Se requiere 'name' y 'price'"}), 400

        name = data.get("name")
        description = data.get("description", "")
        price = float(data.get("price"))
        stock = int(data.get("stock", 0))
        category = data.get("category", "")
        image_url = data.get("image_url", "")
        seller_name = data.get("seller_name", None)

        conn = get_db_connection()
        cursor = conn.cursor(cursor_factory=RealDictCursor)
        
        cursor.execute("""
            INSERT INTO products(name, description, price, stock, category, image_url, seller_name) 
            VALUES (%s, %s, %s, %s, %s, %s, %s) 
            RETURNING *
        """, (name, description, price, stock, category, image_url, seller_name))
        
        new_product = cursor.fetchone()
        conn.commit()
        cursor.close()
        conn.close()
        
        product_dict = dict(new_product)
        if product_dict.get('price'):
            product_dict['price'] = float(product_dict['price'])
        
        return jsonify(product_dict), 201
    except ValueError as e:
        return jsonify({"error": f"Error en los datos: {str(e)}"}), 400
    except Exception as e:
        return jsonify({"error": f"Error del servidor: {str(e)}"}), 500

# Actualizar un producto
@app.put("/products/<int:product_id>")
def update_product(product_id):
    try:
        data = request.get_json()
        
        if not data:
            return jsonify({"error": "Datos incompletos"}), 400

        # Construir la query dinámicamente con los campos proporcionados
        updates = []
        values = []
        
        if "name" in data:
            updates.append("name = %s")
            values.append(data["name"])
        if "description" in data:
            updates.append("description = %s")
            values.append(data["description"])
        if "price" in data:
            updates.append("price = %s")
            values.append(float(data["price"]))
        if "stock" in data:
            updates.append("stock = %s")
            values.append(int(data["stock"]))
        if "category" in data:
            updates.append("category = %s")
            values.append(data["category"])
        if "image_url" in data:
            updates.append("image_url = %s")
            values.append(data["image_url"])
        if "seller_name" in data:
            updates.append("seller_name = %s")
            values.append(data["seller_name"])
        
        if not updates:
            return jsonify({"error": "No hay campos para actualizar"}), 400
        
        values.append(product_id)
        query = f"UPDATE products SET {', '.join(updates)} WHERE id = %s RETURNING *"
        
        conn = get_db_connection()
        cursor = conn.cursor(cursor_factory=RealDictCursor)
        cursor.execute(query, values)
        updated_product = cursor.fetchone()
        
        if not updated_product:
            cursor.close()
            conn.close()
            return jsonify({"error": "Producto no encontrado"}), 404
        
        conn.commit()
        cursor.close()
        conn.close()
        
        product_dict = dict(updated_product)
        if product_dict.get('price'):
            product_dict['price'] = float(product_dict['price'])
        
        return jsonify(product_dict)
    except ValueError as e:
        return jsonify({"error": f"Error en los datos: {str(e)}"}), 400
    except Exception as e:
        return jsonify({"error": f"Error del servidor: {str(e)}"}), 500

# Eliminar un producto
@app.delete("/products/<int:product_id>")
def delete_product(product_id):
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        
        # Verificar si el producto existe
        cursor.execute("SELECT id FROM products WHERE id = %s", (product_id,))
        if not cursor.fetchone():
            cursor.close()
            conn.close()
            return jsonify({"error": "Producto no encontrado"}), 404
        
        # Eliminar el producto
        cursor.execute("DELETE FROM products WHERE id = %s", (product_id,))
        conn.commit()
        cursor.close()
        conn.close()
        
        return jsonify({"message": "Producto eliminado correctamente"}), 200
    except Exception as e:
        return jsonify({"error": f"Error del servidor: {str(e)}"}), 500

# Ejecutar servidor
if __name__ == "__main__":
    init_db()
    app.run(host="0.0.0.0", port=5002, debug=True)
