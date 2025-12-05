@echo off
REM Script para iniciar todos los microservicios en terminales separadas
REM Windows Batch Script - Instala dependencias si es necesario

echo ========================================
echo TiendaApp - Iniciando Microservicios
echo ========================================
echo.

REM Obtener el directorio del script
set SCRIPT_DIR=%~dp0

REM Verificar si Flask está instalado
echo Verificando dependencias...
python -c "import flask" >nul 2>&1
if errorlevel 1 (
    echo.
    echo Flask no está instalado. Instalando dependencias...
    echo.
    
    echo [1/3] Instalando dependencias para Users Service...
    cd /d "%SCRIPT_DIR%users"
    pip install -r requirements.txt >nul 2>&1
    if errorlevel 1 (
        echo ERROR: No se pudieron instalar las dependencias para Users
        pause
        exit /b 1
    )
    
    echo [2/3] Instalando dependencias para Products Service...
    cd /d "%SCRIPT_DIR%products"
    pip install -r requirements.txt >nul 2>&1
    if errorlevel 1 (
        echo ERROR: No se pudieron instalar las dependencias para Products
        pause
        exit /b 1
    )
    
    echo [3/3] Instalando dependencias para Sales Service...
    cd /d "%SCRIPT_DIR%sales"
    pip install -r requirements.txt >nul 2>&1
    if errorlevel 1 (
        echo ERROR: No se pudieron instalar las dependencias para Sales
        pause
        exit /b 1
    )
    
    echo.
    echo Dependencias instaladas correctamente.
    echo.
) else (
    echo Dependencias ya instaladas.
    echo.
)

REM Iniciar microservicio de Users (puerto 5001)
echo Iniciando microservicio Users en puerto 5001...
start "Users Service (5001)" cmd /k "cd /d %SCRIPT_DIR%users && python app.py"

REM Esperar un momento para que se inicie
timeout /t 2 /nobreak >nul

REM Iniciar microservicio de Products (puerto 5002)
echo Iniciando microservicio Products en puerto 5002...
start "Products Service (5002)" cmd /k "cd /d %SCRIPT_DIR%products && python app.py"

REM Esperar un momento para que se inicie
timeout /t 2 /nobreak >nul

REM Iniciar microservicio de Sales (puerto 5003)
echo Iniciando microservicio Sales en puerto 5003...
start "Sales Service (5003)" cmd /k "cd /d %SCRIPT_DIR%sales && python app.py"

echo.
echo ========================================
echo Todos los microservicios han sido iniciados
echo ========================================
echo.
echo Servicios activos:
echo   - Users:    http://localhost:5001
echo   - Products: http://localhost:5002
echo   - Sales:    http://localhost:5003
echo.
echo Presiona cualquier tecla para cerrar esta ventana...
echo (Los servicios seguirán ejecutándose en sus propias ventanas)
pause >nul
