# Microservicios - TiendaApp

Este directorio contiene los tres microservicios del proyecto, todos conectados a Neon PostgreSQL.

## Configuración

Todos los microservicios usan la misma base de datos Neon PostgreSQL. La configuración está en `config.py`.

## Microservicios

### 1. Microservicio de Usuarios (Puerto 5001)
- **POST** `/register` - Registrar usuarios
- **POST** `/login` - Iniciar sesión

### 2. Microservicio de Productos (Puerto 5002)
- **GET** `/products` - Obtener todos los productos
- **GET** `/products/<id>` - Obtener un producto por ID
- **POST** `/products` - Crear producto
- **PUT** `/products/<id>` - Actualizar producto
- **DELETE** `/products/<id>` - Eliminar producto

### 3. Microservicio de Ventas (Puerto 5003)
- **POST** `/sales` - Registrar venta
- **GET** `/sales` - Consultar todas las ventas

## Instalación

Para cada microservicio, instala las dependencias:

```bash
# Microservicio de Usuarios
cd microservices/users
pip install -r requirements.txt

# Microservicio de Productos
cd microservices/products
pip install -r requirements.txt

# Microservicio de Ventas
cd microservices/sales
pip install -r requirements.txt
```

## Ejecución

### Opción 1: Script Automático (Recomendado)

Ejecuta el script que abrirá automáticamente 3 terminales, una para cada microservicio:

```bash
cd microservices
start-all.bat
```

Esto iniciará:
- **Users Service** en el puerto 5001
- **Products Service** en el puerto 5002
- **Sales Service** en el puerto 5003

**Para detener todos los servicios:**

```bash
cd microservices
stop-all.bat
```

### Opción 2: Manual

Abre tres terminales diferentes y ejecuta cada microservicio:

**Terminal 1 - Usuarios:**
```bash
cd microservices/users
python app.py
```

**Terminal 2 - Productos:**
```bash
cd microservices/products
python app.py
```

**Terminal 3 - Ventas:**
```bash
cd microservices/sales
python app.py
```

## Base de Datos

Los microservicios se conectan automáticamente a Neon PostgreSQL y crean las tablas necesarias al iniciar:
- `users` - Tabla de usuarios
- `products` - Tabla de productos
- `sales` - Tabla de ventas

## Notas

- Todos los microservicios tienen CORS habilitado para permitir conexiones desde la app Android
- Las tablas se crean automáticamente si no existen
- Los errores se manejan y retornan mensajes JSON apropiados

