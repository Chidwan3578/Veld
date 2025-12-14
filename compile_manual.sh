#!/bin/bash

# Script para compilar manualmente los módulos de Veld Framework
# Como no podemos usar Maven por problemas de SSL, compilamos directamente con javac

JAVA_HOME=/workspace/jdk-11.0.2
CLASSES_DIR=temp_build/classes
SRC_DIR=temp_build/java

echo "Compilando Veld Framework manualmente..."

# Crear directorio para clases compiladas
mkdir -p $CLASSES_DIR

# 1. Compilar veld-annotations (solo anotaciones)
echo "Compilando veld-annotations..."
$JAVA_HOME/bin/javac -d $CLASSES_DIR \
    $SRC_DIR/io/github/yasmramos/veld/annotation/*.java

if [ $? -ne 0 ]; then
    echo "Error compilando veld-annotations"
    exit 1
fi

# 2. Compilar veld-runtime (incluyendo clases de condición)
echo "Compilando veld-runtime..."
$JAVA_HOME/bin/javac -d $CLASSES_DIR -cp $CLASSES_DIR \
    $SRC_DIR/io/github/yasmramos/veld/runtime/*.java \
    $SRC_DIR/io/github/yasmramos/veld/runtime/condition/*.java \
    $SRC_DIR/io/github/yasmramos/veld/Veld.java

if [ $? -ne 0 ]; then
    echo "Error compilando veld-runtime"
    exit 1
fi

# 3. Compilar veld-processor
echo "Compilando veld-processor..."
$JAVA_HOME/bin/javac -d $CLASSES_DIR -cp $CLASSES_DIR \
    $SRC_DIR/io/github/yasmramos/veld/processor/*.java

if [ $? -ne 0 ]; then
    echo "Error compilando veld-processor"
    exit 1
fi

# 4. Compilar veld-weaver
echo "Compilando veld-weaver..."
$JAVA_HOME/bin/javac -d $CLASSES_DIR -cp $CLASSES_DIR \
    $SRC_DIR/io/github/yasmramos/veld/weaver/*.java

if [ $? -ne 0 ]; then
    echo "Error compilando veld-weaver"
    exit 1
fi

# 5. Compilar el ejemplo de DependsOn
echo "Compilando ejemplos-dependson-test..."
$JAVA_HOME/bin/javac -d $CLASSES_DIR -cp $CLASSES_DIR \
    $SRC_DIR/io/github/yasmramos/veld/example/dependsOn/*.java

if [ $? -ne 0 ]; then
    echo "Error compilando ejemplos-dependson-test"
    exit 1
fi

echo "Compilación completada exitosamente!"
echo "Clases compiladas en: $CLASSES_DIR"

# Listar las clases compiladas
echo "Clases compiladas:"
find $CLASSES_DIR -name "*.class" | head -20