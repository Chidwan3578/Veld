# Referencia de Anotaciones Veld

Esta guía proporciona una referencia completa de todas las anotaciones disponibles en Veld Framework.

## 📋 Tabla de Contenidos

- [Anotaciones de Componentes](#componentes)
- [Anotaciones de Inyección](#inyección)
- [Anotaciones de Configuración](#configuración)
- [Anotaciones de Scope](#scope)
- [Anotaciones de AOP](#aop)
- [Anotaciones de Eventos](#eventos)
- [Anotaciones de Condicionales](#condicionales)

## 🔧 Componentes

### `@Component`

Anotación principal para definir componentes gestionados por Veld.

```java
@Component
public class MyService {
    // Esta clase será gestionada por Veld
}
```

**Parámetros**:
- `String name()` - Nombre opcional del componente
- `Class<?>[] modules()` - Módulos adicionales a importar

**Ejemplo**:
```java
@Component(name = "myService", modules = {DatabaseModule.class})
public class DatabaseService {
    // Componente con nombre personalizado y módulo
}
```

### `@Module`

Define un módulo que proporciona configuraciones y componentes.

```java
@Module
public class DatabaseModule {
    @Provides
    public DataSource provideDataSource() {
        return new HikariDataSource(config);
    }
}
```

### `@Provides`

Define un método que proporciona una instancia de un tipo específico.

```java
@Module
public class ConfigModule {
    
    @Provides
    @Singleton
    public Config provideConfig() {
        return new AppConfig();
    }
    
    @Provides
    public Repository provideRepository(Config config) {
        return new RepositoryImpl(config);
    }
}
```

## 💉 Inyección

### `@Inject`

Anotación para inyección de dependencias.

#### Inyección de Constructor (Recomendada)

```java
@Component
public class UserService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    
    @Inject
    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }
}
```

#### Inyección de Campo

```java
@Component
public class ComplexService {
    @Inject
    private UserRepository userRepository;
    
    @Inject
    private EmailService emailService;
}
```

#### Inyección de Método

```java
@Component
public class ConfigurableService {
    private Config config;
    
    @Inject
    public void setConfig(Config config) {
        this.config = config;
    }
}
```

### `@Named`

Especifica un nombre para distinguir entre múltiples implementaciones.

```java
// Interface
public interface MessageSender {
    void send(String message);
}

// Implementaciones
@Component
@Named("email")
public class EmailSender implements MessageSender {
    @Override
    public void send(String message) {
        // Lógica de email
    }
}

@Component
@Named("sms")
public class SmsSender implements MessageSender {
    @Override
    public void send(String message) {
        // Lógica de SMS
    }
}

// Uso
@Component
public class NotificationService {
    private final MessageSender emailSender;
    private final MessageSender smsSender;
    
    @Inject
    public NotificationService(
        @Named("email") MessageSender emailSender,
        @Named("sms") MessageSender smsSender
    ) {
        this.emailSender = emailSender;
        this.smsSender = smsSender;
    }
}
```

### `@Optional`

Marca una dependencia como opcional.

```java
@Component
public class FlexibleService {
    private final AuditService auditService;
    
    @Inject
    public FlexibleService(@Optional AuditService auditService) {
        this.auditService = auditService; // Puede ser null
    }
    
    public void doSomething() {
        if (auditService != null) {
            auditService.log("Action performed");
        }
        // Lógica principal
    }
}
```

### `@Lazy`

Retrasa la inicialización de una dependencia hasta que se necesite.

```java
@Component
public class HeavyService {
    private final ExpensiveDependency expensive;
    
    @Inject
    public HeavyService(@Lazy ExpensiveDependency expensive) {
        this.expensive = expensive; // No se inicializa hasta el primer uso
    }
    
    public void performOperation() {
        expensive.execute(); // Aquí se inicializa
    }
}
```

## ⚙️ Configuración

### `@Value`

Inyecta valores de configuración.

```java
@Component
public class ConfiguredService {
    
    @Value("${app.name}")
    private String appName;
    
    @Value("${app.version:1.0}")
    private String appVersion;
    
    @Value("${database.url}")
    private String dbUrl;
    
    @Value("${database.username}")
    private String dbUsername;
    
    @Value("${database.password}")
    private String dbPassword;
}
```

**Uso en aplicación**:
```java
@Component
@PropertySource("classpath:application.properties")
public class Application {
    // Las propiedades serán inyectadas automáticamente
}
```

### `@Profile`

Define componentes que solo se activan en ciertos perfiles.

```java
@Component
@Profile("development")
public class DevDataSource implements DataSource {
    // Solo disponible en perfil development
}

@Component
@Profile("production")
public class ProdDataSource implements DataSource {
    // Solo disponible en perfil production
}
```

## 🎯 Scope

### `@Singleton`

Define un scope de singleton (una instancia por aplicación).

```java
@Component
@Singleton
public class CacheManager {
    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    
    public void put(String key, Object value) {
        cache.put(key, value);
    }
    
    public Object get(String key) {
        return cache.get(key);
    }
}
```

### `@Prototype`

Define un scope de prototype (nueva instancia por cada inyección).

```java
@Component
@Prototype
public class RequestHandler {
    private final String requestId = UUID.randomUUID().toString();
    
    public String getRequestId() {
        return requestId;
    }
}

// Cada inyección tendrá un ID diferente
@Component
public class HandlerManager {
    private final RequestHandler handler1;
    private final RequestHandler handler2;
    
    @Inject
    public HandlerManager(RequestHandler handler1, RequestHandler handler2) {
        this.handler1 = handler1;
        this.handler2 = handler2;
        // handler1.getRequestId() != handler2.getRequestId()
    }
}
```

## 🔄 AOP (Aspect-Oriented Programming)

### `@Aspect`

Define una clase como aspecto.

```java
@Aspect
@Component
public class LoggingAspect {
    
    @Around("@annotation(LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        
        Object result = joinPoint.proceed();
        
        long end = System.currentTimeMillis();
        System.out.println(joinPoint.getSignature() + " executed in " + (end - start) + "ms");
        
        return result;
    }
}
```

### `@Before`

Ejecuta antes del join point.

```java
@Aspect
@Component
public class SecurityAspect {
    
    @Before("@annotation(RequireRole)")
    public void checkSecurity(JoinPoint joinPoint) {
        RequireRole annotation = joinPoint.getMethod().getAnnotation(RequireRole.class);
        String[] requiredRoles = annotation.value();
        
        // Verificar si el usuario actual tiene los roles requeridos
        if (!hasRequiredRoles(requiredRoles)) {
            throw new SecurityException("Insufficient permissions");
        }
    }
}
```

### `@After`

Ejecuta después del join point (independientemente del resultado).

```java
@Aspect
@Component
public class CleanupAspect {
    
    @After("@annotation(Cleanup)")
    public void cleanup(JoinPoint joinPoint) {
        System.out.println("Cleaning up after: " + joinPoint.getSignature());
        // Lógica de limpieza
    }
}
```

### `@AfterReturning`

Ejecuta después del join point solo si este es exitoso.

```java
@Aspect
@Component
public class ResultAspect {
    
    @AfterReturning(value = "@annotation(LogResult)", returning = "result")
    public void logResult(JoinPoint joinPoint, Object result) {
        System.out.println("Method " + joinPoint.getSignature() + " returned: " + result);
    }
}
```

### `@AfterThrowing`

Ejecuta si el join point lanza una excepción.

```java
@Aspect
@Component
public class ExceptionHandlingAspect {
    
    @AfterThrowing(value = "@annotation(HandleException)", throwing = "ex")
    public void handleException(JoinPoint joinPoint, Exception ex) {
        System.out.println("Exception in " + joinPoint.getSignature() + ": " + ex.getMessage());
        // Lógica de manejo de excepción
    }
}
```

### `@Around`

Envuelve el join point.

```java
@Aspect
@Component
public class RetryAspect {
    
    @Around("@annotation(Retry)")
    public Object retry(ProceedingJoinPoint joinPoint) throws Throwable {
        Retry retry = joinPoint.getMethod().getAnnotation(Retry.class);
        int maxAttempts = retry.value();
        
        for (int i = 0; i < maxAttempts; i++) {
            try {
                return joinPoint.proceed();
            } catch (Exception e) {
                if (i == maxAttempts - 1) {
                    throw e;
                }
                Thread.sleep(retry.delay());
            }
        }
        throw new IllegalStateException("Should not reach here");
    }
}
```

### `@Pointcut`

Define un pointcut reutilizable.

```java
@Aspect
@Component
public class ServicePointcuts {
    
    @Pointcut("execution(* com.example.service.*.*(..))")
    public void serviceMethods() {}
    
    @Pointcut("execution(* com.example.repository.*.*(..))")
    public void repositoryMethods() {}
    
    @Pointcut("serviceMethods() || repositoryMethods()")
    public void dataLayerMethods() {}
}

// Uso
@Aspect
@Component
public class PerformanceMonitor {
    
    @Around("ServicePointcuts.serviceMethods()")
    public Object monitorServicePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long end = System.currentTimeMillis();
        
        System.out.println(joinPoint.getSignature() + " took " + (end - start) + "ms");
        return result;
    }
}
```

## 📡 Eventos

### `@Subscribe`

Define un método como suscriptor de eventos.

```java
@Component
public class EventListener {
    
    @Subscribe
    public void onUserCreated(UserCreatedEvent event) {
        System.out.println("User created: " + event.getUsername());
        // Lógica de respuesta al evento
    }
    
    @Subscribe
    public void onOrderPlaced(OrderPlacedEvent event) {
        System.out.println("Order placed: " + event.getOrderId());
        // Enviar confirmación de email
    }
}
```

### `@EventComponent`

Marca una clase como componente de eventos.

```java
@EventComponent
@Component
public class EventPublisher {
    
    @Inject
    private EventBus eventBus;
    
    public void publishUserCreated(String username) {
        eventBus.post(new UserCreatedEvent(username));
    }
    
    public void publishOrderPlaced(String orderId) {
        eventBus.post(new OrderPlacedEvent(orderId));
    }
}
```

## 🔍 Condicionales

### `@ConditionalOnClass`

Activa el componente solo si existe la clase especificada.

```java
@Component
@ConditionalOnClass(name = "com.fasterxml.jackson.databind.ObjectMapper")
public class JacksonJsonService implements JsonService {
    // Solo se activa si Jackson está en el classpath
}
```

### `@ConditionalOnMissingBean`

Activa el componente solo si no existe ya un bean del mismo tipo.

```java
@Configuration
public class AutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public JsonService jsonService() {
        return new GsonJsonService();
    }
}
```

### `@ConditionalOnProperty`

Activa el componente basado en una propiedad de configuración.

```java
@Component
@ConditionalOnProperty(name = "app.cache.enabled", havingValue = "true")
public class CacheService {
    // Solo se activa si app.cache.enabled=true
}
```

## 🏷️ Metas Anotaciones

### `@Component` como Meta-Anotación

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Repository {
    String name() default "";
}

// Uso
@Repository(name = "userRepo")
public class UserRepositoryImpl implements UserRepository {
    // Automaticamente sera un componente
}
```

### `@Singleton` como Meta-Anotación

```java
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Singleton
public @interface Cached {
    // Componente cacheado con metadatos adicionales
}
```

## 📝 Ejemplos de Uso Avanzado

### Configuración Compleja con Módulos

```java
@Module
public class DatabaseModule {
    
    @Provides
    @Singleton
    public DataSource provideDataSource(@Value("${db.url}") String url,
                                       @Value("${db.username}") String username,
                                       @Value("${db.password}") String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        return new HikariDataSource(config);
    }
    
    @Provides
    public UserRepository provideUserRepository(DataSource dataSource) {
        return new JdbcUserRepository(dataSource);
    }
}

@Component(modules = {DatabaseModule.class})
public class DatabaseService {
    private final UserRepository userRepository;
    private final DataSource dataSource;
    
    @Inject
    public DatabaseService(UserRepository userRepository, DataSource dataSource) {
        this.userRepository = userRepository;
        this.dataSource = dataSource;
    }
}
```

### AOP Avanzado con Múltiples Aspectos

```java
@Aspect
@Component
public class TransactionAspect {
    
    @Around("@annotation(Transactional)")
    public Object manageTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("Starting transaction for: " + joinPoint.getSignature());
        
        try {
            Object result = joinPoint.proceed();
            System.out.println("Committing transaction for: " + joinPoint.getSignature());
            return result;
        } catch (Exception e) {
            System.out.println("Rolling back transaction for: " + joinPoint.getSignature());
            throw e;
        }
    }
}

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactional {
    String value() default "";
}

// Uso
@Component
public class UserService {
    
    @Transactional
    public void createUser(String username, String email) {
        // La transacción será gestionada automáticamente
        userRepository.save(username, email);
    }
}
```

## 🔗 Ver También

- [Guía de Inicio Rápido](getting-started.md)
- [Integración con Spring Boot](spring-boot-integration.md)
- [Guía de AOP](aop-guide.md)
- [Ejemplos de Código](examples/)

---

**💡 Tip**: Para obtener la mejor performance, prefiere la inyección de constructor sobre la inyección de campo y usa `@Singleton` para componentes que son seguros de compartir entre threads.