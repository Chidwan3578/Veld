# Veld Framework Documentation

![Veld Logo](https://img.shields.io/badge/Veld-Framework-brightgreen?style=for-the-badge&logo=java)

> **El framework de inyección de dependencias más rápido del ecosistema Java**

Veld es un framework de inyección de dependencias ligero y ultra-rápido diseñado para superar las limitaciones de Dagger en términos de velocidad de compilación y overhead runtime.

## 🚀 Características Principales

- ⚡ **Ultra-rápido**: 50x más rápido que Dagger en tiempo de compilación
- 🧵 **Thread-safe**: Completamente seguro para uso en entornos multi-thread
- 🎯 **Type-safe**: Inyección de dependencias tipada y segura
- 🔄 **Incremental**: Soporte para builds incrementales inteligente
- 🏗️ **Modular**: Arquitectura modular y extensible
- ☕ **Java 11+**: Compatible con Java 11 y versiones superiores
- 🌱 **Spring Boot**: Integración nativa con Spring Boot

## 📚 Documentación

### 🏃‍♂️ Guía Rápida
- [Inicio Rápido](getting-started.md) - Comienza en 5 minutos
- [Anotaciones Básicas](annotations.md) - Referencia completa de anotaciones
- [Ejemplos](examples/) - Ejemplos prácticos de uso

### 🔧 Guías Avanzadas
- [Arquitectura](architecture.md) - Arquitectura interna del framework
- [Optimización](optimization.md) - Guía de optimización de performance
- [Integración Spring Boot](spring-boot-integration.md) - Integración completa

### 📊 Benchmarks y Performance
- [Benchmarks](benchmarks.md) - Comparación de performance
- [Roadmap de Optimizaciones](../Veld_ROADMAP_OPTIMIZACION.md) - Plan de mejoras futuras

### 👥 Para Desarrolladores
- [Contributing](contributing.md) - Guía para contribuir
- [Development](development.md) - Configuración del entorno de desarrollo
- [API Reference](api-reference/) - Referencia de la API

## 🎯 Casos de Uso

### ✅ Casos Ideales para Veld
- **Aplicaciones que requieren startup rápido**
- **Microservicios con alto throughput**
- **Aplicaciones que necesitan builds rápidos**
- **Pro**
yectos con dependencias complejas- **Integración con Spring Boot**

### ❌ Casos Donde Considerar Alternativas
- **Proyectos simples sin inyección de dependencias**
- **Aplicaciones legacy que requieren Java 8**
- **Casos donde se prefiere configuración XML**

## ⚡ Performance vs Dagger

| Métrica | Dagger | Veld | Mejora |
|---------|--------|------|--------|
| Tiempo de compilación | 2-5s | <0.1s | **50x más rápido** |
| Overhead runtime | 5-10ms | <0.5ms | **20x más eficiente** |
| Memoria peak | ~50MB | <5MB | **10x menos uso** |
| Generated code | ~100KB | <10KB | **10x más pequeño** |

## 🏗️ Arquitectura del Proyecto

```
Veld Framework
├── veld-annotations/          # Anotaciones del framework
├── veld-runtime/              # Runtime core del framework
├── veld-aop/                  # Aspect-Oriented Programming
├── veld-processor/            # Annotation Processor
├── veld-weaver/               # Bytecode Weaver
├── veld-benchmark/            # Benchmarks y tests de performance
├── veld-example/              # Ejemplos básicos
├── veld-spring-boot-starter/  # Starter para Spring Boot
└── veld-spring-boot-example/  # Ejemplo con Spring Boot
```

## 🚀 Inicio Rápido

```java
// 1. Definir un componente
@Component
public class MyService {
    private final Repository repository;
    
    @Inject
    public MyService(Repository repository) {
        this.repository = repository;
    }
}

// 2. Usar el servicio
public class Main {
    public static void main(String[] args) {
        MyService service = Veld.inject(MyService.class);
        service.doSomething();
    }
}
```

Más detalles en [Inicio Rápido](getting-started.md)

## 📦 Instalación

### Maven
```xml
<dependency>
    <groupId>com.veld</groupId>
    <artifactId>veld-runtime</artifactId>
    <version>1.0.0-alpha.6</version>
</dependency>
```

### Gradle
```gradle
implementation 'com.veld:veld-runtime:1.0.0-alpha.6'
```

## 🤝 Contribuir

¡Las contribuciones son bienvenidas! Consulta nuestra [Guía de Contribución](contributing.md) para más información.

## 📄 Licencia

Este proyecto está bajo la Licencia Apache 2.0. Ver [LICENSE](../LICENSE) para más detalles.

## 📞 Soporte

- **GitHub Issues**: [Reportar bugs](https://github.com/yasmramos/Veld/issues)
- **Documentación**: [Wiki del proyecto](https://github.com/yasmramos/Veld/wiki)
- **Discusiones**: [GitHub Discussions](https://github.com/yasmramos/Veld/discussions)

---

<div align="center">
  <strong>Construido con ❤️ para la comunidad Java</strong>
</div>