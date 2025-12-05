# Endpoint para obtener productos vendidos por un usuario
@app.get("/sales/user/<int:user_id>")
def get_user_sales(user_id):
    """
    Obtiene los productos que un usuario ha vendido
    """
    try:
        conn = get_db_connection()
        cursor = conn.cursor(cursor_factory=RealDictCursor)
        
        # Obtener ventas del usuario con información del producto
        cursor.execute("""
            SELECT 
                s.id as sale_id,
                s.product_id,
                s.quantity,
                s.total,
                s.created_at,
                p.name as product_name,
                p.price as product_price,
                p.image_url as product_image
            FROM sales s
            JOIN products p ON s.product_id = p.id
            WHERE s.user_id = %s
            ORDER BY s.created_at DESC
        """, (user_id,))
        
        sales = cursor.fetchall()
        cursor.close()
        conn.close()
        
        return jsonify(sales), 200
        
    except Exception as e:
        print(f"Error getting user sales: {str(e)}")
        return jsonify({"error": str(e)}), 500
