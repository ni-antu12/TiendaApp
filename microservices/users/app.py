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
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS users(
            id SERIAL PRIMARY KEY,
            username VARCHAR(255) UNIQUE NOT NULL,
            name VARCHAR(255) NOT NULL,
            lastname VARCHAR(255) NOT NULL,
            email VARCHAR(255) UNIQUE NOT NULL,
            password VARCHAR(255) NOT NULL
        );
    """)
    conn.commit()
    cursor.close()
    conn.close()
    print("Tabla users inicializada correctamente")

# Registro de usuario
@app.post("/register")
def register():
    try:
        data = request.json
        if not data:
            return jsonify({"error": "Datos incompletos"}), 400
        
        # Validar campos requeridos
        required_fields = ["username", "name", "lastname", "email", "password"]
        missing_fields = [field for field in required_fields if field not in data or not data[field]]
        
        if missing_fields:
            return jsonify({"error": f"Campos faltantes: {', '.join(missing_fields)}"}), 400
            
        username = data["username"]
        name = data["name"]
        lastname = data["lastname"]
        email = data["email"]
        password = data["password"]

        conn = get_db_connection()
        cursor = conn.cursor(cursor_factory=RealDictCursor)

        try:
            cursor.execute("""
                INSERT INTO users(username, name, lastname, email, password) 
                VALUES (%s, %s, %s, %s, %s) 
                RETURNING *
            """, (username, name, lastname, email, password))
            
            new_user = cursor.fetchone()
            conn.commit()
            
            user_dict = dict(new_user)
            # No retornar la contraseña
            user_dict.pop('password', None)
            
            return jsonify({"message": "Usuario registrado", "user": user_dict}), 201
        except psycopg2.IntegrityError as e:
            conn.rollback()
            error_msg = str(e)
            if "username" in error_msg.lower():
                return jsonify({"error": "El nombre de usuario ya existe"}), 400
            elif "email" in error_msg.lower():
                return jsonify({"error": "El email ya está registrado"}), 400
            return jsonify({"error": "Usuario o email ya existe"}), 400
        except Exception as e:
            conn.rollback()
            return jsonify({"error": str(e)}), 500
        finally:
            cursor.close()
            conn.close()
    except Exception as e:
        return jsonify({"error": f"Error del servidor: {str(e)}"}), 500

# Login de usuario (puede usar username o email)
@app.post("/login")
def login():
    try:
        data = request.json
        if not data or "password" not in data:
            return jsonify({"error": "Datos incompletos"}), 400
        
        # Puede recibir username o email
        username_or_email = data.get("username") or data.get("email")
        if not username_or_email:
            return jsonify({"error": "Se requiere username o email"}), 400
            
        password = data["password"]

        conn = get_db_connection()
        cursor = conn.cursor(cursor_factory=RealDictCursor)

        # Buscar por username o email
        cursor.execute("""
            SELECT * FROM users 
            WHERE (username=%s OR email=%s) AND password=%s
        """, (username_or_email, username_or_email, password))
        
        user = cursor.fetchone()
        cursor.close()
        conn.close()

        if user:
            user_dict = dict(user)
            # No retornar la contraseña
            user_dict.pop('password', None)
            return jsonify({
                "message": "Login correcto", 
                "id": user_dict["id"],
                "user": user_dict
            })
        else:
            return jsonify({"error": "Credenciales inválidas"}), 401
    except Exception as e:
        return jsonify({"error": f"Error del servidor: {str(e)}"}), 500

if __name__ == "__main__":
    init_db()
    app.run(host="0.0.0.0", port=5001, debug=True)
