#!/bin/bash
echo "========================================"
echo "JavaFX Installation Checker"
echo "========================================"
echo

echo "[1] Checking Java version..."
java -version
echo

echo "[2] Checking available Java modules..."
if java --list-modules | grep javafx > /dev/null; then
    echo "✅ JavaFX modules found!"
    java --list-modules | grep javafx
else
    echo "❌ No JavaFX modules found"
fi
echo

echo "[3] Testing JavaFX availability..."
if java --module-path . --add-modules javafx.controls --version 2>/dev/null; then
    echo "✅ JavaFX can be loaded successfully!"
else
    echo "❌ JavaFX cannot be loaded"
fi
echo

echo "[4] Testing simple JavaFX application..."
cat > JavaFXTest.java << 'EOF'
import javafx.application.Application;
import javafx.stage.Stage;
public class JavaFXTest extends Application {
    public void start(Stage stage) {
        System.out.println("JavaFX is working!");
        System.exit(0);
    }
    public static void main(String[] args) { launch(args); }
}
EOF

if javac --module-path . --add-modules javafx.controls JavaFXTest.java 2>/dev/null; then
    echo "✅ JavaFX compilation successful!"
    if java --module-path . --add-modules javafx.controls JavaFXTest 2>/dev/null; then
        echo "✅ JavaFX runtime test successful!"
    else
        echo "❌ JavaFX runtime test failed"
    fi
else
    echo "❌ JavaFX compilation failed"
fi

rm -f JavaFXTest.java JavaFXTest.class 2>/dev/null

echo
echo "========================================"
echo "Summary:"
echo "========================================"
echo "If you see ✅ marks above, JavaFX is properly installed."
echo "If you see ❌ marks, JavaFX is not available."
echo
echo "To test P2P Chat GUI:"
echo "  run: scripts/start-gui.sh"
echo
