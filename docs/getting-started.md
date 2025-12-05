# Inicio Rápido con Veld

Esta guía te permitirá comenzar a usar Veld en menos de 5 minutos.

## 📋 Prerrequisitos

- Java 11 o superior
- Maven 3.6+ o Gradle 6+
- Un IDE (IntelliJ IDEA, Eclipse, VS Code)

## 🚀 Instalación Rápida

### Con Maven

Agrega estas dependencias a tu `pom.xml`:

```xml
<dependencies>
    <!-- Veld Core -->
    <dependency>
        <groupId>com.veld</groupId>
        <artifactId>veld-runtime</artifactId>
        <version>1.0.0-alpha.6</version>
    </dependency>
    
    <!-- Veld Annotations -->
    <dependency>
        <groupId>com.veld</groupId>
        <artifactId>veld-annotations</artifactId>
        <version>1.0.0-alpha.6</version>
        <scope>provided</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <source>11</source>
                <target>11</target>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Con Gradle

```gradle
dependencies {
    implementation 'com.veld:veld-runtime:1.0.0-alpha.6'
    compileOnly 'com.veld:veld-annotations:1.0.0-alpha.6'
}
```

## 🎯 Tu Primera Aplicación con Veld

### Paso 1: Crear una Interfaz de Repositorio

```java
package com.example.repository;

public interface UserRepository {
    String findUserById(String id);
    void saveUser(String user);
}
```

### Paso 2: Implementar el Repositorio

```java
package com.example.repository;

import com.veld.annotation.Component;
import com.veld.annotation.Singleton;

@Component
@Singleton
public class InMemoryUserRepository implements UserRepository {
    private final Map<String, String> users = new ConcurrentHashMap<>();
    
    @Override
    public String findUserById(String id) {
        return users.get(id);
    }
    
    @Override
    public void saveUser(String user) {
        users.put(user, "User data for " + user);
    }
}
```

### Paso 3: Crear un Servicio

```java
package com.example.service;

import com.veld.annotation.Component;
import com.veld.annotation.Inject;
import com.example.repository.UserRepository;

@Component
public class UserService {
    private final UserRepository userRepository;
    
    @Inject
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public String getUser(String id) {
        return userRepository.findUserById(id);
    }
    
    public void registerUser(String username) {
        userRepository.saveUser(username);
    }
}
```

### Paso 4: Usar Veld en tu Aplicación

```java
package com.example;

import com.veld.annotation.Component;
import com.veld.service.UserService;
import com.veld.Veld;

@Component
public class Application {
    private final UserService userService;
    
    @Inject
    public Application(UserService userService) {
        this.userService = userService;
    }
    
    public void run() {
        // Usar el servicio con dependencias inyectadas automáticamente
        userService.registerUser("john_doe");
        String user = userService.getUser("john_doe");
        System.out.println("User: " + user);
    }
    
    public static void main(String[] args) {
        // Crear e iniciar la aplicación
        Application app = Veld.inject(Application.class);
        app.run();
    }
}
```

## 🎛️ Anotaciones Básicas

### `@Component`
Marca una clase como componente gestionado por Veld.

```java
@Component
public class MyService {
    // Veld gestionará esta clase
}
```

### `@Inject`
Inyecta dependencias en campos, constructores o métodos.

```java
@Component
public class ServiceWithDependency {
    private final Dependency dep;
    
    @Inject
    public ServiceWithDependency(Dependency dep) {
        this.dep = dep;
    }
}
```

### `@Singleton`
Define que solo debe existir una instancia del componente.

```java
@Component
@Singleton
public class SingletonService {
    // Solo una instancia será creada
}
```

### `@Prototype`
Define que una nueva instancia será creada cada vez.

```java
@Component
@Prototype
public class PrototypeService {
    // Una nueva instancia será creada cada vez que se solicite
}
```

## 🔧 Ejecución

### Compilar el proyecto:

```bash
mvn clean compile
```

### Ejecutar la aplicación:

```bash
java -cp target/classes:target/dependency/* com.example.Application
```

## 📁 Estructura del Proyecto

```
my-veld-app/
├── src/
│   └── main/
│       └── java/
│           └── com/example/
│               ├── Application.java
│               ├── repository/
│               │   ├── UserRepository.java
│               │   └── InMemoryUserRepository.java
│               └── service/
│                   └── UserService.java
└── pom.xml
```

## ⚡ Próximos Pasos

1. **Explora más anotaciones**: Consulta la [Referencia de Anotaciones](annotations.md)
2. **Aprende sobre AOP**: Lee la [Guía de AOP](aop-guide.md)
3. **Integra con Spring Boot**: Ver [Integración con Spring Boot](spring-boot-integration.md)
4. **Optimiza el rendimiento**: Consulta la [Guía de Optimización](optimization.md)

## 🐛 Problemas Comunes

### Error: "No se puede encontrar el componente"

**Solución**: Asegúrate de que tus clases estén anotadas con `@Component` y en el classpath.

### Error: "No se puede inyectar la dependencia"

**Solución**: Verifica que:
- La dependencia esté anotada con `@Component`
- Existe un constructor público (o anotado con `@Inject`)
- El tipo de dependencia esté disponible en el classpath

### Performance lenta

**Solución**: 
- Usa `@Singleton` para componentes que no cambian frecuentemente
- Evita inyección en el constructor de clases de alto nivel
- Considera usar `@Lazy` para dependencias que no se usan inmediatamente

## 💡 Tips de Rendimiento

1. **Prefiere inyección de constructor** sobre inyección de campo
2. **Usa `@Lazy`** para dependencias de arranque costosas
3. **Agrupa componentes relacionados** en módulos
4. **Evita circular dependencies** usando interfaces

## 🎓 Recursos Adicionales

- [Documentación Completa](README.md)
- [Ejemplos de Código](examples/)
- [Benchmarks](benchmarks.md)
- [Contribuir al Proyecto](contributing.md)

---

¡Felicitaciones! Ya tienes tu primera aplicación funcionando con Veld. 🎉