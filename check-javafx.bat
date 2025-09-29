@echo off
echo ========================================
echo JavaFX Installation Checker
echo ========================================
echo.

echo [1] Checking Java version...
java -version
echo.

echo [2] Checking available Java modules...
java --list-modules | findstr javafx
if %ERRORLEVEL% EQU 0 (
    echo ✅ JavaFX modules found!
) else (
    echo ❌ No JavaFX modules found
)
echo.

echo [3] Testing JavaFX availability...
java --module-path . --add-modules javafx.controls --version 2>nul
if %ERRORLEVEL% EQU 0 (
    echo ✅ JavaFX can be loaded successfully!
) else (
    echo ❌ JavaFX cannot be loaded
)
echo.

echo [4] Testing simple JavaFX application...
echo import javafx.application.Application; > JavaFXTest.java
echo import javafx.stage.Stage; >> JavaFXTest.java
echo public class JavaFXTest extends Application { >> JavaFXTest.java
echo     public void start(Stage stage) { >> JavaFXTest.java
echo         System.out.println("JavaFX is working!"); >> JavaFXTest.java
echo         System.exit(0); >> JavaFXTest.java
echo     } >> JavaFXTest.java
echo     public static void main(String[] args) { launch(args); } >> JavaFXTest.java
echo } >> JavaFXTest.java

javac --module-path . --add-modules javafx.controls JavaFXTest.java 2>nul
if %ERRORLEVEL% EQU 0 (
    echo ✅ JavaFX compilation successful!
    java --module-path . --add-modules javafx.controls JavaFXTest 2>nul
    if %ERRORLEVEL% EQU 0 (
        echo ✅ JavaFX runtime test successful!
    ) else (
        echo ❌ JavaFX runtime test failed
    )
) else (
    echo ❌ JavaFX compilation failed
)

del JavaFXTest.java JavaFXTest.class 2>nul

echo.
echo ========================================
echo Summary:
echo ========================================
echo If you see ✅ marks above, JavaFX is properly installed.
echo If you see ❌ marks, JavaFX is not available.
echo.
echo To test P2P Chat GUI:
echo   run: start-gui-only.bat
echo.
pause
