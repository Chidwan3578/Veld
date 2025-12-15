#!/bin/bash

# Script para probar la compilación y ejecución de ejemplos
# Útil para desarrollo local antes de enviar a CI/CD

set -e

echo "🧪 Testing Veld Examples Compilation and Execution"
echo "=================================================="

# Verificar si estamos en el directorio correcto
if [ ! -f "pom.xml" ]; then
    echo "❌ Error: No se encuentra pom.xml. Ejecutar desde el directorio raíz de Veld."
    exit 1
fi

echo "📋 Verificando estructura del proyecto..."
if [ ! -d "veld-example" ]; then
    echo "❌ Error: No se encuentra el directorio veld-example"
    exit 1
fi

if [ ! -f ".github/workflows/examples.yml" ]; then
    echo "❌ Error: No se encuentra el workflow examples.yml"
    exit 1
fi

echo "✅ Estructura del proyecto verificada"

echo ""
echo "🔨 Compilando módulos core de Veld..."
mvn clean compile -pl veld-annotations,veld-runtime,veld-processor,veld-weaver -am -DskipTests=true -q

echo "✅ Módulos core compilados"

echo ""
echo "🔨 Compilando módulo de ejemplos..."
mvn clean compile -pl veld-example -am -DskipTests=true

echo "✅ Módulo de ejemplos compilado"

echo ""
echo "📁 Verificando archivos compilados..."
if [ -d "veld-example/target/classes" ]; then
    echo "✅ Clases de ejemplos encontradas"
    echo "📊 Contenido del directorio de clases:"
    find veld-example/target/classes -name "*.class" | grep -E "(ConfigService|DatabaseService|UserRepository|EmailService|UserService|DependsOnDemo)" | sort
else
    echo "❌ No se encontraron clases compiladas"
    exit 1
fi

echo ""
echo "🚀 Ejecutando demostración de @DependsOn..."
echo "========================================="

# Ejecutar solo la parte de @DependsOn para una prueba rápida
cd veld-example
mvn exec:java -Dexec.mainClass="io.github.yasmramos.veld.example.dependsOn.DependsOnDemo" -q

echo ""
echo "✅ Ejecución de @DependsOn completada"

echo ""
echo "🎯 Resumen de la prueba:"
echo "========================"
echo "✅ Compilación exitosa"
echo "✅ Clases @DependsOn generadas"
echo "✅ Ejemplos ejecutables"
echo "✅ Workflow CI/CD configurado"

echo ""
echo "📝 Para ejecutar todos los ejemplos:"
echo "cd veld-example"
echo "mvn exec:java -Dexec.mainClass=\"io.github.yasmramos.veld.example.Main\""

echo ""
echo "🎉 ¡Todas las pruebas pasaron exitosamente!"