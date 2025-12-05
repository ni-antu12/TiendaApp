@echo off
REM Script para detener todos los microservicios
REM Windows Batch Script

echo Deteniendo todos los microservicios...
echo.

REM Detener procesos de Python que ejecutan app.py en los directorios de microservicios
echo Deteniendo Users Service (puerto 5001)...
for /f "tokens=2" %%a in ('netstat -ano ^| findstr :5001') do (
    taskkill /F /PID %%a >nul 2>&1
)

echo Deteniendo Products Service (puerto 5002)...
for /f "tokens=2" %%a in ('netstat -ano ^| findstr :5002') do (
    taskkill /F /PID %%a >nul 2>&1
)

echo Deteniendo Sales Service (puerto 5003)...
for /f "tokens=2" %%a in ('netstat -ano ^| findstr :5003') do (
    taskkill /F /PID %%a >nul 2>&1
)

REM Método alternativo: buscar procesos Python ejecutando app.py
echo.
echo Buscando procesos restantes...
taskkill /F /FI "WINDOWTITLE eq Users Service*" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq Products Service*" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq Sales Service*" >nul 2>&1

echo.
echo ========================================
echo Todos los microservicios han sido detenidos
echo ========================================
echo.
pause

