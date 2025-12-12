#!/bin/bash

echo "🔍 VERIFICACIÓN DE JMH BENCHMARKS COMPLETOS"
echo "==========================================="
echo ""

# Función para verificar configuración JMH correcta
check_jmh_configuration() {
    local workflow_file=".github/workflows/benchmarks.yml"
    local has_issues=false
    
    echo "📄 Verificando: $workflow_file"
    echo "------------------------------"
    
    # Verificar que use la clase correcta
    if grep -q "io.github.yasmramos.benchmark.BenchmarkRunner" "$workflow_file"; then
        echo "✅ CORRECTO: Usa clase correcta io.github.yasmramos.benchmark.BenchmarkRunner"
    else
        echo "❌ ERROR: No usa la clase correcta de BenchmarkRunner"
        has_issues=true
    fi
    
    # Verificar que no use clases inexistentes
    if grep -q "com.veld.benchmark.Phase1OptimizationBenchmark" "$workflow_file"; then
        echo "❌ ERROR: Aún referencia clase inexistente com.veld.benchmark.Phase1OptimizationBenchmark"
        has_issues=true
    else
        echo "✅ CORRECTO: No referencia clase inexistente"
    fi
    
    # Verificar que use JMH completo
    if grep -q "BenchmarkRunner.*Injection" "$workflow_file"; then
        echo "✅ CORRECTO: Ejecuta benchmarks JMH completos"
    else
        echo "⚠️  ADVERTENCIA: No ejecuta benchmarks JMH completos"
    fi
    
    # Verificar que compile con mvn
    if grep -q "mvn.*compile.*-q" "$workflow_file"; then
        echo "✅ CORRECTO: Usa Maven para compilar"
    else
        echo "⚠️  ADVERTENCIA: No usa Maven para compilar"
    fi
    
    # Verificar resultados de JMH
    if grep -q "benchmark-results.json" "$workflow_file"; then
        echo "✅ CORRECTO: Genera resultados JMH"
    else
        echo "⚠️  ADVERTENCIA: No genera resultados JMH"
    fi
    
    if [ "$has_issues" = false ]; then
        echo "✅ JMH benchmarks configurados correctamente"
    fi
    
    echo ""
    return $([ "$has_issues" = true ] && echo 1 || echo 0)
}

# Verificar archivo de workflow
if [ -f ".github/workflows/benchmarks.yml" ]; then
    if ! check_jmh_configuration; then
        echo "❌ SE ENCONTRARON PROBLEMAS EN EL WORKFLOW"
        exit 1
    fi
else
    echo "❌ Archivo de workflow no encontrado"
    exit 1
fi

echo "📋 CONFIGURACIÓN JMH ESPERADA"
echo "=============================="
echo "✅ Clase correcta: io.github.yasmramos.benchmark.BenchmarkRunner"
echo "✅ Benchmarks JMH completos (Injection, Startup, Throughput)"
echo "✅ Maven compilation para resolver dependencias"
echo "✅ Resultados JSON de JMH"
echo "✅ Sin referencias a clases inexistentes"

echo ""
echo "🏁 Verificación JMH completada exitosamente"