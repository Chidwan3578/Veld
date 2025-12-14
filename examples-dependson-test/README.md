# Veld Framework - @DependsOn Test Examples

Este módulo contiene ejemplos y pruebas para demostrar la funcionalidad de `@DependsOn` en el framework Veld.

## 📋 Funcionalidades Demostradas

### 1. **Dependencias Explícitas**
- `@DependsOn("beanName")` para dependencias únicas
- `@DependsOn({"bean1", "bean2", "bean3"})` para múltiples dependencias

### 2. **Orden de Inicialización**
- Beans con `@DependsOn` se inicializan DESPUÉS de sus dependencias
- Respeta tanto dependencias explícitas como implícitas (inyectadas)

### 3. **Resolución de Beans**
- Por `@Component` value o `@Named` value
- Por nombre de clase simple (lowercase)
- Por nombre completo de clase

### 4. **Validación Compile-Time**
- Advertencias si un bean `@DependsOn` no se encuentra
- Detección de ciclos que incluye dependencias explícitas

## 🏗️ Estructura del Proyecto

```
examples-dependson-test/
├── src/main/java/io/github/yasmramos/veld/example/dependsOn/
│   ├── DatabaseMigrator.java      # Bean crítico que debe inicializarse primero
│   ├── UserRepository.java        # Depende de DatabaseMigrator (explícitamente)
│   ├── CacheManager.java          # Bean independiente
│   ├── ConfigService.java         # Bean independiente
│   ├── ApplicationService.java    # Depende de CacheManager y ConfigService
│   ├── DependsOnDemo.java         # Demo principal que muestra la funcionalidad
│   └── Main.java                  # Punto de entrada para ejecutar el demo
├── pom.xml                        # Configuración Maven con annotation processing
└── README.md                      # Este archivo
```

## 🔄 Flujo de Inicialización Esperado

1. **DatabaseMigrator** (sin dependencias explícitas)
2. **CacheManager** (sin dependencias explícitas)
3. **ConfigService** (sin dependencias explícitas)
4. **UserRepository** (depends on "databaseMigrator")
5. **ApplicationService** (depends on {"cacheManager", "configService"})
6. **DependsOnDemo** (inyecta todos los anteriores)

## 🚀 Cómo Ejecutar

### Opción 1: Script Automático
```bash
# Ejecutar el script de prueba completo
bash test-dependson.sh
```

### Opción 2: Comandos Maven
```bash
# 1. Compilar módulos base
mvn clean install -pl veld-annotations,veld-runtime,veld-processor,veld-weaver -DskipTests

# 2. Compilar ejemplos con annotation processing
cd examples-dependson-test
mvn clean compile

# 3. Ejecutar weaver
mvn veld-weaver:weave

# 4. Ejecutar demo
mvn exec:java -Dexec.mainClass="io.github.yasmramos.veld.example.dependsOn.Main"
```

## 📊 Salida Esperada

```
🚀 VELD @DependsOn DEMONSTRATION
================================

[DatabaseMigrator] Constructor called
[DatabaseMigrator] ✅ Running database migrations #1
[DatabaseMigrator] ✅ Database migrations completed successfully

[CacheManager] Constructor called
[CacheManager] ✅ Starting cache system

[ConfigService] Constructor called
[ConfigService] ✅ Loading configuration
[ConfigService] ✅ Configuration loaded: Veld DependsOn Demo

[UserRepository] Constructor called with DatabaseMigrator
[UserRepository] ✅ Initializing user repository
[UserRepository] ✅ Database is ready - repository can operate safely

[ApplicationService] Constructor called with CacheManager and ConfigService
[ApplicationService] ✅ Initializing application
[ApplicationService] ✅ Using configuration: ConfigService{...}
[ApplicationService] ✅ Cache system ready: 0 hits

📋 VERIFICANDO ORDEN DE INICIALIZACIÓN:
✅ DatabaseMigrator: INITIALIZED
✅ CacheManager: INITIALIZED
✅ ConfigService: INITIALIZED
✅ UserRepository: INITIALIZED
✅ ApplicationService: INITIALIZED

🗄️ VERIFICANDO ESTADO DE LA BASE DE DATOS:
Database migrations count: 1
Database ready: true

🎯 DEMOSTRANDO FUNCIONALIDAD:
[UserRepository] 💾 Saving user to database: Alice
[UserRepository] 💾 Saving user to database: Bob
[UserRepository] 🔍 Finding users in database

[ApplicationService] 🚀 Starting application: Veld DependsOn Demo
[ApplicationService] 💾 Cache test: Application started successfully
[ApplicationService] 🔄 Performing data operations...
[ApplicationService] ✅ Operation cached: User authentication
[ApplicationService] 📊 Cache stats - Hits: 1, Misses: 1

✅ DEMO COMPLETADO EXITOSAMENTE
```

## 🧪 Casos de Prueba

### Test 1: Dependencia Simple
```java
@Singleton
@DependsOn("databaseMigrator")
public class UserRepository {
    // Se inicializa DESPUÉS de databaseMigrator
}
```

### Test 2: Múltiples Dependencias
```java
@Singleton
@DependsOn({"cacheManager", "configService"})
public class ApplicationService {
    // Se inicializa DESPUÉS de cacheManager Y configService
}
```

### Test 3: Dependencias Explícitas e Implícitas
```java
@Singleton
@DependsOn("databaseMigrator")
public class UserRepository {
    @Inject
    public UserRepository(DatabaseMigrator migrator) {
        // Tiene tanto dependencia explícita (@DependsOn)
        // como implícita (inyección en constructor)
    }
}
```

## 🔍 Validaciones

El sistema valida en compile-time:

1. **Bean Existente**: Si un bean en `@DependsOn` no existe, se muestra una advertencia
2. **Ciclos**: Detecta ciclos que incluyen dependencias explícitas
3. **Nombres**: Resuelve nombres de beans usando múltiples estrategias

## 📈 Métricas de Éxito

- ✅ Todos los beans se inicializan sin errores
- ✅ El orden de inicialización respeta `@DependsOn`
- ✅ Las dependencias explícitas e implícitas trabajan juntas
- ✅ No hay ciclos de dependencia
- ✅ El LifecycleProcessor se inicializa automáticamente

## 🛠️ Tecnologías Utilizadas

- **Java 11+**
- **Veld Framework** (annotation processing + bytecode weaving)
- **Maven** (build + annotation processing)
- **JUnit 5** (testing framework)

## 📝 Notas

- Este demo requiere que el proyecto Veld esté compilado completamente
- El weaver debe ejecutarse después de la compilación para generar `Veld.class`
- El orden de inicialización es crítico para la funcionalidad correcta