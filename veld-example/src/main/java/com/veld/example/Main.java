package com.veld.example;

import com.veld.example.aop.CalculatorService;
import com.veld.example.aop.LoggingAspect;
import com.veld.example.aop.PerformanceAspect;
import com.veld.example.aop.ProductService;
import com.veld.example.events.NotificationEvent;
import com.veld.example.events.OrderCreatedEvent;
import com.veld.example.lifecycle.*;
import com.veld.aop.InterceptorRegistry;
import com.veld.aop.interceptor.LoggingInterceptor;
import com.veld.aop.interceptor.TimingInterceptor;
import com.veld.aop.interceptor.TransactionInterceptor;
import com.veld.aop.interceptor.ValidationInterceptor;
import com.veld.aop.proxy.ProxyFactory;
import com.veld.runtime.Provider;
import com.veld.runtime.VeldContainer;
import com.veld.runtime.event.EventBus;
import com.veld.runtime.lifecycle.LifecycleProcessor;

import java.util.Arrays;

/**
 * Main class demonstrating Veld DI framework capabilities.
 * 
 * This example shows:
 * 1. Constructor injection (UserRepositoryImpl, EmailNotification)
 * 2. Field injection (ConfigService, RequestContext)
 * 3. Method injection (UserService)
 * 4. @Singleton scope (LogService, ConfigService, UserRepositoryImpl, UserService)
 * 5. @Prototype scope (RequestContext, EmailNotification)
 * 6. @PostConstruct and @PreDestroy lifecycle callbacks
 * 7. Interface-based injection (IUserRepository -> UserRepositoryImpl)
 * 8. JSR-330 compatibility (javax.inject.*)
 * 9. Jakarta Inject compatibility (jakarta.inject.*)
 * 10. @Lazy initialization (ExpensiveService)
 * 11. Provider<T> injection (ReportGenerator)
 * 12. @Optional and Optional<T> injection (OptionalDemoService)
 * 13. @Conditional annotations (ConditionalDemoService)
 *     - @ConditionalOnProperty
 *     - @ConditionalOnClass
 *     - @ConditionalOnMissingBean
 * 14. @Profile annotations (ProfileDemoService)
 *     - Environment-specific components (dev, prod, test)
 *     - Profile negation (!prod)
 *     - Multiple profiles with OR logic
 * 15. @Value configuration injection (AppConfigService)
 *     - System properties
 *     - Environment variables
 *     - application.properties file
 *     - Default values
 *     - Type conversion (String, int, boolean, double, etc.)
 * 16. EventBus - Event-driven communication
 *     - @Subscribe annotation for event handlers
 *     - Sync and async event delivery
 *     - Priority-based ordering
 *     - Filter expressions
 *     - Event hierarchy support
 * 17. AOP (Aspect-Oriented Programming)
 *     - @Aspect for defining aspects
 *     - @Around, @Before, @After advice
 *     - Pointcut expressions with wildcards
 *     - @Interceptor and @AroundInvoke
 *     - Built-in interceptors: @Logged, @Timed, @Validated, @Transactional
 *     - ASM bytecode proxy generation
 * 18. Advanced Lifecycle Management
 *     - SmartLifecycle interface (phase-ordered start/stop)
 *     - InitializingBean / DisposableBean interfaces
 *     - BeanPostProcessor for custom bean modification
 *     - @PostInitialize (after all beans ready)
 *     - @OnStart / @OnStop for application lifecycle
 *     - @DependsOn for explicit initialization order
 *     - Lifecycle events: ContextRefreshed, Started, Stopped, Closed
 * 
 * Simple API: Just create a new VeldContainer() - that's it!
 * All bytecode generation happens at compile-time using ASM.
 */
public class Main {
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║           Veld DI Framework - Example Application        ║");
        System.out.println("║        Pure ASM Bytecode Generation - Simple API         ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();
        
        // Create the container - simple as that!
        // The registry is automatically discovered and loaded
        VeldContainer container = new VeldContainer();
        
        try {
            System.out.println("\n══════════════════════════════════════════════════════════");
            System.out.println("1. SINGLETON DEMONSTRATION");
            System.out.println("══════════════════════════════════════════════════════════");
            demonstrateSingleton(container);
            
            System.out.println("\n══════════════════════════════════════════════════════════");
            System.out.println("2. PROTOTYPE DEMONSTRATION");
            System.out.println("══════════════════════════════════════════════════════════");
            demonstratePrototype(container);
            
            System.out.println("\n══════════════════════════════════════════════════════════");
            System.out.println("3. DEPENDENCY INJECTION CHAIN");
            System.out.println("══════════════════════════════════════════════════════════");
            demonstrateInjectionChain(container);
            
            System.out.println("\n══════════════════════════════════════════════════════════");
            System.out.println("4. INTERFACE-BASED INJECTION");
            System.out.println("══════════════════════════════════════════════════════════");
            demonstrateInterfaceInjection(container);
            
            System.out.println("\n══════════════════════════════════════════════════════════");
            System.out.println("5. JSR-330 & JAKARTA INJECT COMPATIBILITY");
            System.out.println("══════════════════════════════════════════════════════════");
            demonstrateJsr330AndJakarta(container);
            
            System.out.println("\n══════════════════════════════════════════════════════════");
            System.out.println("6. @LAZY INITIALIZATION");
            System.out.println("══════════════════════════════════════════════════════════");
            demonstrateLazy(container);
            
            System.out.println("\n══════════════════════════════════════════════════════════");
            System.out.println("7. PROVIDER<T> INJECTION");
            System.out.println("══════════════════════════════════════════════════════════");
            demonstrateProvider(container);
            
            System.out.println("\n══════════════════════════════════════════════════════════");
            System.out.println("8. @OPTIONAL AND Optional<T> INJECTION");
            System.out.println("══════════════════════════════════════════════════════════");
            demonstrateOptionalInjection(container);
            
            System.out.println("\n══════════════════════════════════════════════════════════");
            System.out.println("9. @CONDITIONAL ANNOTATIONS");
            System.out.println("══════════════════════════════════════════════════════════");
            demonstrateConditional(container);
            
            System.out.println("\n══════════════════════════════════════════════════════════");
            System.out.println("10. @PROFILE ANNOTATIONS");
            System.out.println("══════════════════════════════════════════════════════════");
            demonstrateProfiles(container);
            
            System.out.println("\n══════════════════════════════════════════════════════════");
            System.out.println("11. @VALUE CONFIGURATION INJECTION");
            System.out.println("══════════════════════════════════════════════════════════");
            demonstrateValueInjection(container);
            
            System.out.println("\n══════════════════════════════════════════════════════════");
            System.out.println("12. EVENTBUS - EVENT-DRIVEN COMMUNICATION");
            System.out.println("══════════════════════════════════════════════════════════");
            demonstrateEventBus(container);
            
            System.out.println("\n══════════════════════════════════════════════════════════");
            System.out.println("13. AOP (ASPECT-ORIENTED PROGRAMMING)");
            System.out.println("══════════════════════════════════════════════════════════");
            demonstrateAop();
            
            System.out.println("\n══════════════════════════════════════════════════════════");
            System.out.println("14. ADVANCED LIFECYCLE MANAGEMENT");
            System.out.println("══════════════════════════════════════════════════════════");
            demonstrateAdvancedLifecycle();
            
            System.out.println("\n══════════════════════════════════════════════════════════");
            System.out.println("15. SERVICE USAGE");
            System.out.println("══════════════════════════════════════════════════════════");
            demonstrateServiceUsage(container);
            
        } finally {
            System.out.println("\n══════════════════════════════════════════════════════════");
            System.out.println("CONTAINER SHUTDOWN - @PreDestroy callbacks");
            System.out.println("══════════════════════════════════════════════════════════");
            container.close();
        }
        
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║              Example Completed Successfully!              ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
    }
    
    /**
     * Demonstrates singleton behavior - same instance returned each time.
     */
    private static void demonstrateSingleton(VeldContainer container) {
        System.out.println("\n→ Getting LogService twice (should be same instance):");
        LogService log1 = container.get(LogService.class);
        LogService log2 = container.get(LogService.class);
        
        System.out.println("  log1 hashCode: " + System.identityHashCode(log1));
        System.out.println("  log2 hashCode: " + System.identityHashCode(log2));
        System.out.println("  Same instance? " + (log1 == log2 ? "YES ✓" : "NO ✗"));
        
        System.out.println("\n→ Getting ConfigService twice (should be same instance):");
        ConfigService config1 = container.get(ConfigService.class);
        ConfigService config2 = container.get(ConfigService.class);
        
        System.out.println("  config1 hashCode: " + System.identityHashCode(config1));
        System.out.println("  config2 hashCode: " + System.identityHashCode(config2));
        System.out.println("  Same instance? " + (config1 == config2 ? "YES ✓" : "NO ✗"));
    }
    
    /**
     * Demonstrates prototype behavior - new instance created each time.
     */
    private static void demonstratePrototype(VeldContainer container) {
        System.out.println("\n→ Getting RequestContext three times (should be different instances):");
        RequestContext req1 = container.get(RequestContext.class);
        RequestContext req2 = container.get(RequestContext.class);
        RequestContext req3 = container.get(RequestContext.class);
        
        System.out.println("  req1: Instance #" + req1.getInstanceNumber() + ", ID: " + req1.getRequestId());
        System.out.println("  req2: Instance #" + req2.getInstanceNumber() + ", ID: " + req2.getRequestId());
        System.out.println("  req3: Instance #" + req3.getInstanceNumber() + ", ID: " + req3.getRequestId());
        System.out.println("  All different instances? " + 
            (req1 != req2 && req2 != req3 && req1 != req3 ? "YES ✓" : "NO ✗"));
        
        System.out.println("\n→ Getting EmailNotification twice (should be different instances):");
        EmailNotification email1 = container.get(EmailNotification.class);
        EmailNotification email2 = container.get(EmailNotification.class);
        
        System.out.println("  email1: Notification #" + email1.getNotificationNumber());
        System.out.println("  email2: Notification #" + email2.getNotificationNumber());
        System.out.println("  Different instances? " + (email1 != email2 ? "YES ✓" : "NO ✗"));
    }
    
    /**
     * Demonstrates how dependencies are injected through the chain.
     */
    private static void demonstrateInjectionChain(VeldContainer container) {
        System.out.println("\n→ UserService receives dependencies via method injection:");
        UserService userService = container.get(UserService.class);
        
        System.out.println("\n→ ConfigService receives LogService via field injection:");
        ConfigService configService = container.get(ConfigService.class);
        LogService injectedLog = configService.getLogService();
        System.out.println("  ConfigService has LogService? " + (injectedLog != null ? "YES ✓" : "NO ✗"));
        
        System.out.println("\n→ Verifying singleton consistency in injection chain:");
        LogService directLog = container.get(LogService.class);
        System.out.println("  LogService from container == LogService in ConfigService? " + 
            (directLog == injectedLog ? "YES ✓" : "NO ✗"));
    }
    
    /**
     * Demonstrates interface-based injection.
     * IUserRepository is an interface, UserRepositoryImpl is the implementation.
     * Veld automatically resolves the interface to its implementation.
     */
    private static void demonstrateInterfaceInjection(VeldContainer container) {
        System.out.println("\n→ Injecting by INTERFACE (IUserRepository):");
        IUserRepository repoByInterface = container.get(IUserRepository.class);
        System.out.println("  Requested: IUserRepository.class");
        System.out.println("  Received:  " + repoByInterface.getClass().getSimpleName());
        System.out.println("  Is UserRepositoryImpl? " + 
            (repoByInterface instanceof UserRepositoryImpl ? "YES ✓" : "NO ✗"));
        
        System.out.println("\n→ Injecting by CONCRETE CLASS (UserRepositoryImpl):");
        UserRepositoryImpl repoByClass = container.get(UserRepositoryImpl.class);
        System.out.println("  Requested: UserRepositoryImpl.class");
        System.out.println("  Received:  " + repoByClass.getClass().getSimpleName());
        
        System.out.println("\n→ Verifying singleton consistency:");
        System.out.println("  Same instance? " + (repoByInterface == repoByClass ? "YES ✓" : "NO ✗"));
        System.out.println("  Both hashCodes: " + System.identityHashCode(repoByInterface) + 
            " == " + System.identityHashCode(repoByClass));
        
        System.out.println("\n→ UserService injects IUserRepository (interface):");
        System.out.println("  This demonstrates that services can depend on interfaces,");
        System.out.println("  and Veld resolves them to concrete implementations automatically.");
    }
    
    /**
     * Demonstrates JSR-330 (javax.inject) and Jakarta Inject (jakarta.inject) compatibility.
     */
    private static void demonstrateJsr330AndJakarta(VeldContainer container) {
        System.out.println("\n→ PaymentService uses javax.inject.* annotations:");
        System.out.println("  @javax.inject.Singleton for scope");
        System.out.println("  @javax.inject.Inject for constructor and method injection");
        PaymentService paymentService = container.get(PaymentService.class);
        System.out.println("  PaymentService obtained: " + (paymentService != null ? "YES" : "NO"));
        
        System.out.println("\n→ OrderService uses jakarta.inject.* annotations:");
        System.out.println("  @jakarta.inject.Singleton for scope");
        System.out.println("  @jakarta.inject.Inject for constructor and method injection");
        OrderService orderService = container.get(OrderService.class);
        System.out.println("  OrderService obtained: " + (orderService != null ? "YES" : "NO"));
        
        System.out.println("\n→ NotificationService uses MIXED annotations:");
        System.out.println("  @com.veld.annotation.Singleton for scope");
        System.out.println("  @javax.inject.Inject for constructor");
        System.out.println("  @jakarta.inject.Inject for method");
        System.out.println("  @com.veld.annotation.Inject for field");
        NotificationService notificationService = container.get(NotificationService.class);
        System.out.println("  NotificationService obtained: " + (notificationService != null ? "YES" : "NO"));
        
        System.out.println("\n→ Testing PaymentService functionality:");
        boolean valid = paymentService.validatePayment(500.0);
        System.out.println("  Payment of $500 valid? " + (valid ? "YES" : "NO"));
        paymentService.processPayment("ORD-001", 500.0);
        
        System.out.println("\n→ Testing OrderService with Jakarta annotations:");
        String orderId = orderService.createOrder(1L, "Veld Framework", 99.99);
        System.out.println("  Order created: " + orderId);
        
        System.out.println("\n→ Testing NotificationService with mixed annotations:");
        notificationService.sendWelcomeNotification("new-user@example.com");
        
        System.out.println("\n→ Verifying all services are singletons:");
        PaymentService payment2 = container.get(PaymentService.class);
        OrderService order2 = container.get(OrderService.class);
        NotificationService notif2 = container.get(NotificationService.class);
        
        System.out.println("  PaymentService singleton? " + (paymentService == payment2 ? "YES" : "NO"));
        System.out.println("  OrderService singleton? " + (orderService == order2 ? "YES" : "NO"));
        System.out.println("  NotificationService singleton? " + (notificationService == notif2 ? "YES" : "NO"));
    }
    
    /**
     * Demonstrates @Lazy initialization.
     * Components marked with @Lazy are not instantiated until first accessed.
     */
    private static void demonstrateLazy(VeldContainer container) {
        // Reset the counter for clean demo
        ExpensiveService.resetInstanceCount();
        
        System.out.println("\n→ ExpensiveService is marked with @Lazy");
        System.out.println("  It should NOT be created when the container starts.");
        System.out.println("  Current instance count: " + ExpensiveService.getInstanceCount());
        
        System.out.println("\n→ Now requesting ExpensiveService for the first time...");
        ExpensiveService expensive1 = container.get(ExpensiveService.class);
        System.out.println("  Instance count after first request: " + ExpensiveService.getInstanceCount());
        
        System.out.println("\n→ Requesting ExpensiveService again (should be same singleton)...");
        ExpensiveService expensive2 = container.get(ExpensiveService.class);
        System.out.println("  Instance count after second request: " + ExpensiveService.getInstanceCount());
        System.out.println("  Same instance? " + (expensive1 == expensive2 ? "YES ✓" : "NO ✗"));
        
        System.out.println("\n→ Using the service:");
        String result = expensive1.process("test data");
        System.out.println("  Result: " + result);
    }
    
    /**
     * Demonstrates Provider<T> injection.
     * Provider allows lazy access and on-demand instance creation.
     */
    private static void demonstrateProvider(VeldContainer container) {
        System.out.println("\n→ ReportGenerator uses Provider<RequestContext>");
        System.out.println("  Provider allows getting new instances of @Prototype components on demand");
        
        ReportGenerator reportGen = container.get(ReportGenerator.class);
        
        System.out.println("\n→ Generating multiple reports (each gets a fresh RequestContext):");
        String report1 = reportGen.generateReport("Sales");
        String report2 = reportGen.generateReport("Inventory");
        String report3 = reportGen.generateReport("Financial");
        
        System.out.println("  " + report1);
        System.out.println("  " + report2);
        System.out.println("  " + report3);
        
        reportGen.demonstrateMultipleContexts();
        
        System.out.println("\n→ Using container.getProvider() directly:");
        Provider<LogService> logProvider = container.getProvider(LogService.class);
        System.out.println("  Got Provider<LogService>");
        
        LogService log1 = logProvider.get();
        LogService log2 = logProvider.get();
        System.out.println("  Provider.get() returns singleton: " + (log1 == log2 ? "YES ✓" : "NO ✗"));
        
        Provider<RequestContext> ctxProvider = container.getProvider(RequestContext.class);
        RequestContext ctx1 = ctxProvider.get();
        RequestContext ctx2 = ctxProvider.get();
        System.out.println("  Provider.get() creates new prototype: " + (ctx1 != ctx2 ? "YES ✓" : "NO ✗"));
    }
    
    /**
     * Demonstrates optional dependency injection.
     * Dependencies marked with @Optional or typed as Optional<T> don't fail if missing.
     */
    private static void demonstrateOptionalInjection(VeldContainer container) {
        System.out.println("\n→ OptionalDemoService has optional dependencies:");
        System.out.println("  - @Optional CacheService (not registered - will be null)");
        System.out.println("  - Optional<MetricsService> (not registered - will be empty)");
        System.out.println("  - LogService (required - will be injected)");
        
        System.out.println("\n→ Getting OptionalDemoService...");
        OptionalDemoService optionalDemo = container.get(OptionalDemoService.class);
        
        System.out.println("\n→ Using the service (gracefully handles missing dependencies):");
        optionalDemo.doWork();
        
        System.out.println("\n→ Testing container.tryGet() for non-existent component:");
        CacheService cache = container.tryGet(CacheService.class);
        System.out.println("  container.tryGet(CacheService.class): " + 
            (cache == null ? "null (expected)" : "found"));
        
        System.out.println("\n→ Testing container.getOptional() for non-existent component:");
        java.util.Optional<MetricsService> metrics = container.getOptional(MetricsService.class);
        System.out.println("  container.getOptional(MetricsService.class): " + 
            (metrics.isEmpty() ? "Optional.empty() (expected)" : "found"));
        
        System.out.println("\n→ Testing container.getOptional() for existing component:");
        java.util.Optional<LogService> logOpt = container.getOptional(LogService.class);
        System.out.println("  container.getOptional(LogService.class): " + 
            (logOpt.isPresent() ? "Optional[LogService] (expected)" : "empty"));
        
        System.out.println("\n→ Summary: Optional injection allows graceful handling of");
        System.out.println("  missing dependencies without throwing exceptions!");
    }
    
    /**
     * Demonstrates conditional component registration.
     * Components can be conditionally registered based on:
     * - System properties (@ConditionalOnProperty)
     * - Classpath presence (@ConditionalOnClass)
     * - Missing beans (@ConditionalOnMissingBean)
     */
    private static void demonstrateConditional(VeldContainer container) {
        System.out.println("\n→ Conditional Registration Demo:");
        System.out.println("  Components are registered based on conditions evaluated at runtime.");
        
        // Show excluded components
        System.out.println("\n→ Components excluded due to failing conditions:");
        java.util.List<String> excluded = container.getExcludedComponents();
        if (excluded.isEmpty()) {
            System.out.println("  (no components excluded)");
        } else {
            for (String name : excluded) {
                System.out.println("  - " + name);
            }
        }
        
        // Test @ConditionalOnMissingBean - DefaultDatabaseService
        System.out.println("\n→ @ConditionalOnMissingBean Demo:");
        System.out.println("  DefaultDatabaseService is registered only if no other DatabaseService exists.");
        boolean hasDbService = container.contains(DatabaseService.class);
        System.out.println("  DatabaseService available? " + (hasDbService ? "YES" : "NO"));
        if (hasDbService) {
            DatabaseService db = container.get(DatabaseService.class);
            System.out.println("  Using: " + db.getClass().getSimpleName());
            System.out.println("  Connection info: " + db.getConnectionInfo());
        }
        
        // Test @ConditionalOnProperty - DebugService
        System.out.println("\n→ @ConditionalOnProperty Demo:");
        System.out.println("  DebugService requires: -Dapp.debug=true or APP_DEBUG=true");
        String debugProp = System.getProperty("app.debug", System.getenv("APP_DEBUG"));
        System.out.println("  Current app.debug value: " + (debugProp != null ? debugProp : "<not set>"));
        boolean hasDebug = container.contains(DebugService.class);
        System.out.println("  DebugService available? " + (hasDebug ? "YES" : "NO"));
        if (hasDebug) {
            DebugService debug = container.get(DebugService.class);
            debug.logDebug("Conditional registration is working!");
        }
        
        // Test @ConditionalOnProperty - FeatureXService
        System.out.println("\n→ @ConditionalOnProperty Demo (Feature Flag):");
        System.out.println("  FeatureXService requires: feature.x.enabled to exist");
        String featureX = System.getProperty("feature.x.enabled", System.getenv("FEATURE_X_ENABLED"));
        System.out.println("  Current feature.x.enabled value: " + (featureX != null ? featureX : "<not set>"));
        boolean hasFeatureX = container.contains(FeatureXService.class);
        System.out.println("  FeatureXService available? " + (hasFeatureX ? "YES" : "NO"));
        
        // Test @ConditionalOnClass - JacksonJsonService
        System.out.println("\n→ @ConditionalOnClass Demo:");
        System.out.println("  JacksonJsonService requires: com.fasterxml.jackson.databind.ObjectMapper");
        boolean jacksonOnClasspath = false;
        try {
            Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
            jacksonOnClasspath = true;
        } catch (ClassNotFoundException e) {
            // Not on classpath
        }
        System.out.println("  Jackson on classpath? " + (jacksonOnClasspath ? "YES" : "NO"));
        boolean hasJackson = container.contains(JacksonJsonService.class);
        System.out.println("  JacksonJsonService available? " + (hasJackson ? "YES" : "NO"));
        
        // ConditionalDemoService demonstrates using these conditionally-registered services
        System.out.println("\n→ ConditionalDemoService integrates with conditional beans:");
        ConditionalDemoService conditionalDemo = container.get(ConditionalDemoService.class);
        conditionalDemo.runDemo();
        
        System.out.println("\n→ Summary: @Conditional annotations enable auto-configuration");
        System.out.println("  by registering beans only when specific conditions are met.");
    }
    
    /**
     * Demonstrates @Profile annotations for environment-specific components.
     */
    private static void demonstrateProfiles(VeldContainer container) {
        System.out.println("\n→ Profile-Based Component Registration Demo:");
        System.out.println("  Components can be registered based on active profiles.");
        
        // Show active profiles
        System.out.println("\n→ Active Profiles:");
        java.util.Set<String> profiles = container.getActiveProfiles();
        System.out.println("  " + profiles);
        
        // Explain how to activate profiles
        System.out.println("\n→ How to activate profiles:");
        System.out.println("  1. System property: -Dveld.profiles.active=dev");
        System.out.println("  2. Environment variable: VELD_PROFILES_ACTIVE=dev");
        System.out.println("  3. Programmatically: VeldContainer.withProfiles(\"dev\")");
        
        // Check which profile-specific components are available
        System.out.println("\n→ Profile-Specific Components Status:");
        
        // DataSource implementations
        boolean hasDevDs = container.contains(DevDataSource.class);
        boolean hasProdDs = container.contains(ProdDataSource.class);
        boolean hasTestDs = container.contains(TestDataSource.class);
        
        System.out.println("  DevDataSource (@Profile(\"dev\")): " + (hasDevDs ? "REGISTERED" : "excluded"));
        System.out.println("  ProdDataSource (@Profile(\"prod\")): " + (hasProdDs ? "REGISTERED" : "excluded"));
        System.out.println("  TestDataSource (@Profile(\"test\")): " + (hasTestDs ? "REGISTERED" : "excluded"));
        
        // VerboseLoggingService
        boolean hasVerboseLog = container.contains(VerboseLoggingService.class);
        System.out.println("  VerboseLoggingService (@Profile({\"dev\", \"test\"})): " + 
                          (hasVerboseLog ? "REGISTERED" : "excluded"));
        
        // MockPaymentGateway
        boolean hasMockPayment = container.contains(MockPaymentGateway.class);
        System.out.println("  MockPaymentGateway (@Profile(\"!prod\")): " + 
                          (hasMockPayment ? "REGISTERED" : "excluded"));
        
        // Check DataSource interface availability
        System.out.println("\n→ DataSource Interface Resolution:");
        boolean hasDataSource = container.contains(DataSource.class);
        if (hasDataSource) {
            DataSource ds = container.get(DataSource.class);
            System.out.println("  DataSource implementation: " + ds.getClass().getSimpleName());
            System.out.println("  Connection URL: " + ds.getConnectionUrl());
        } else {
            System.out.println("  No DataSource available for current profile");
            System.out.println("  Hint: Try running with -Dveld.profiles.active=dev");
        }
        
        // ProfileDemoService demonstration
        System.out.println("\n→ ProfileDemoService integrates with profile-specific beans:");
        ProfileDemoService profileDemo = container.get(ProfileDemoService.class);
        profileDemo.runAllDemos();
        
        System.out.println("\n→ Summary: @Profile enables environment-specific configuration.");
        System.out.println("  - Use @Profile(\"dev\") for development-only components");
        System.out.println("  - Use @Profile(\"prod\") for production-only components");
        System.out.println("  - Use @Profile({\"dev\", \"test\"}) for multiple profiles (OR)");
        System.out.println("  - Use @Profile(\"!prod\") for negation (NOT prod)");
    }
    
    /**
     * Demonstrates @Value configuration injection.
     */
    private static void demonstrateValueInjection(VeldContainer container) {
        System.out.println("\n→ @Value Configuration Injection Demo:");
        System.out.println("  Values are resolved from multiple sources:");
        System.out.println("  1. System properties (-Dproperty=value)");
        System.out.println("  2. Environment variables");
        System.out.println("  3. application.properties file");
        System.out.println("  4. Default values in annotation");
        
        System.out.println("\n→ AppConfigService uses @Value for all configuration:");
        AppConfigService appConfig = container.get(AppConfigService.class);
        
        System.out.println("\n→ Configuration Values Retrieved:");
        System.out.println("  App Name: " + appConfig.getAppName());
        System.out.println("  Version: " + appConfig.getAppVersion());
        System.out.println("  Environment: " + appConfig.getEnvironment());
        System.out.println("  Server Port: " + appConfig.getServerPort());
        System.out.println("  Debug Mode: " + appConfig.isDebugMode());
        System.out.println("  Max Connections: " + appConfig.getMaxConnections());
        System.out.println("  Request Timeout: " + appConfig.getRequestTimeout() + "s");
        System.out.println("  API URL: " + appConfig.getApiBaseUrl());
        
        System.out.println("\n→ Config Summary:");
        System.out.println("  " + appConfig.getConfigSummary());
        
        System.out.println("\n→ @Value Annotation Examples:");
        System.out.println("  @Value(\"${app.name:Veld Application}\")  - With default value");
        System.out.println("  @Value(\"${server.port:8080}\")           - Integer conversion");
        System.out.println("  @Value(\"${app.debug:false}\")            - Boolean conversion");
        System.out.println("  @Value(\"${request.timeout:30.0}\")       - Double conversion");
        
        System.out.println("\n→ Override values with system properties:");
        System.out.println("  java -Dserver.port=3000 -Dapp.environment=production ...");
        
        System.out.println("\n→ Override values with environment variables:");
        System.out.println("  export SERVER_PORT=3000");
        System.out.println("  export APP_ENVIRONMENT=production");
    }
    
    /**
     * Demonstrates EventBus for event-driven communication.
     */
    private static void demonstrateEventBus(VeldContainer container) {
        System.out.println("\n→ EventBus - Decoupled component communication");
        System.out.println("  Components publish events without knowing who handles them.");
        System.out.println("  Subscribers receive events using @Subscribe annotation.\n");
        
        // Get the EventBus and register handlers
        EventBus eventBus = EventBus.getInstance();
        
        // Get handler components and register them
        System.out.println("→ Registering event handlers:");
        OrderEventHandler orderHandler = container.get(OrderEventHandler.class);
        NotificationHandler notificationHandler = container.get(NotificationHandler.class);
        
        eventBus.register(orderHandler);
        eventBus.register(notificationHandler);
        
        // Get the demo service that publishes events
        EventDemoService eventService = container.get(EventDemoService.class);
        
        // Demonstrate creating orders
        System.out.println("\n══════════════════════════════════════════════════════════");
        System.out.println("→ Publishing OrderCreatedEvent (regular order):");
        System.out.println("══════════════════════════════════════════════════════════");
        eventService.createOrder(
            299.99,
            "customer@example.com",
            Arrays.asList("Veld Framework License", "Premium Support"),
            "123 Main St, Tech City"
        );
        
        // Wait for async handlers
        try { Thread.sleep(200); } catch (InterruptedException e) { }
        
        System.out.println("\n══════════════════════════════════════════════════════════");
        System.out.println("→ Publishing OrderCreatedEvent (VIP order > $1000):");
        System.out.println("══════════════════════════════════════════════════════════");
        eventService.createOrder(
            2499.99,
            "vip@enterprise.com",
            Arrays.asList("Enterprise License", "24/7 Support", "Custom Training"),
            "456 Corporate Blvd"
        );
        
        // Wait for async handlers
        try { Thread.sleep(200); } catch (InterruptedException e) { }
        
        System.out.println("\n══════════════════════════════════════════════════════════");
        System.out.println("→ Publishing OrderCancelledEvent:");
        System.out.println("══════════════════════════════════════════════════════════");
        eventService.cancelOrder(
            "ORD-1001",
            299.99,
            "customer@example.com",
            "Customer requested cancellation",
            true
        );
        
        System.out.println("\n══════════════════════════════════════════════════════════");
        System.out.println("→ Publishing NotificationEvent (normal priority):");
        System.out.println("══════════════════════════════════════════════════════════");
        eventService.sendNotification(
            "System maintenance scheduled for tonight",
            NotificationEvent.Priority.NORMAL,
            "system"
        );
        
        System.out.println("\n══════════════════════════════════════════════════════════");
        System.out.println("→ Publishing NotificationEvent (URGENT priority):");
        System.out.println("══════════════════════════════════════════════════════════");
        eventService.sendNotification(
            "Critical security update required!",
            NotificationEvent.Priority.URGENT,
            "security"
        );
        
        // Show statistics
        System.out.println("\n══════════════════════════════════════════════════════════");
        System.out.println("→ EventBus Statistics:");
        System.out.println("══════════════════════════════════════════════════════════");
        System.out.println(eventBus.getStatistics());
        
        System.out.println("\n→ @Subscribe Features Demonstrated:");
        System.out.println("  - priority: Higher priority handlers run first");
        System.out.println("  - async=true: Handler runs in background thread");
        System.out.println("  - filter: Conditional handling (e.g., amount > 1000)");
        System.out.println("  - Event hierarchy: OrderEvent base class catches all order events");
    }
    
    /**
     * Demonstrates AOP (Aspect-Oriented Programming) features.
     */
    private static void demonstrateAop() {
        System.out.println("\n→ AOP - Aspect-Oriented Programming Demo");
        System.out.println("  Cross-cutting concerns handled declaratively.");
        System.out.println("  Aspects intercept method calls using pointcut expressions.");
        
        // Register aspects
        System.out.println("\n→ Registering Aspects:");
        InterceptorRegistry registry = InterceptorRegistry.getInstance();
        
        LoggingAspect loggingAspect = new LoggingAspect();
        PerformanceAspect performanceAspect = new PerformanceAspect();
        registry.registerAspect(loggingAspect);
        registry.registerAspect(performanceAspect);
        
        // Register built-in interceptors
        System.out.println("\n→ Registering Built-in Interceptors:");
        LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
        TimingInterceptor timingInterceptor = new TimingInterceptor();
        ValidationInterceptor validationInterceptor = new ValidationInterceptor();
        TransactionInterceptor transactionInterceptor = new TransactionInterceptor();
        
        registry.registerInterceptor(loggingInterceptor);
        registry.registerInterceptor(timingInterceptor);
        registry.registerInterceptor(validationInterceptor);
        registry.registerInterceptor(transactionInterceptor);
        
        // Create proxied services
        System.out.println("\n→ Creating AOP Proxies:");
        ProxyFactory proxyFactory = ProxyFactory.getInstance();
        
        CalculatorService calculator = proxyFactory.createProxy(CalculatorService.class);
        ProductService productService = proxyFactory.createProxy(ProductService.class);
        
        // Test CalculatorService (intercepted by LoggingAspect and PerformanceAspect)
        System.out.println("\n══════════════════════════════════════════════════════════");
        System.out.println("→ Testing CalculatorService (with @Around advice):");
        System.out.println("══════════════════════════════════════════════════════════");
        
        System.out.println("\n>>> Calling add(5, 3):");
        int sum = calculator.add(5, 3);
        System.out.println("Result: " + sum);
        
        System.out.println("\n>>> Calling multiply(7, 6):");
        int product = calculator.multiply(7, 6);
        System.out.println("Result: " + product);
        
        System.out.println("\n>>> Calling factorial(5):");
        long factorial = calculator.factorial(5);
        System.out.println("Result: " + factorial);
        
        System.out.println("\n>>> Calling divide(10, 0) - should throw exception:");
        try {
            calculator.divide(10, 0);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            System.out.println("Caught exception as expected: " + cause.getMessage());
        }
        
        // Test ProductService (intercepted by @Before, @After advice + @Transactional, @Logged)
        System.out.println("\n══════════════════════════════════════════════════════════");
        System.out.println("→ Testing ProductService (with @Before, @After, @Transactional):");
        System.out.println("══════════════════════════════════════════════════════════");
        
        System.out.println("\n>>> Creating products:");
        ProductService.Product p1 = productService.createProduct("VELD-001", "Veld Framework", 99.99);
        System.out.println("Created: " + p1);
        
        ProductService.Product p2 = productService.createProduct("VELD-002", "Veld Enterprise", 499.99);
        System.out.println("Created: " + p2);
        
        System.out.println("\n>>> Finding product:");
        ProductService.Product found = productService.findProduct("VELD-001");
        System.out.println("Found: " + found);
        
        System.out.println("\n>>> Updating price:");
        ProductService.Product updated = productService.updatePrice("VELD-001", 79.99);
        System.out.println("Updated: " + updated);
        
        System.out.println("\n>>> Deleting product:");
        boolean deleted = productService.deleteProduct("VELD-002");
        System.out.println("Deleted: " + deleted);
        
        System.out.println("\n>>> Finding non-existent product (should throw):");
        try {
            productService.findProduct("VELD-999");
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            System.out.println("Caught exception as expected: " + cause.getMessage());
        }
        
        // Print performance statistics
        PerformanceAspect.printStatistics();
        
        // Show registry statistics
        System.out.println("→ InterceptorRegistry Statistics:");
        System.out.println("  " + registry.getStatistics());
        
        System.out.println("\n→ AOP Features Demonstrated:");
        System.out.println("  - @Aspect: LoggingAspect, PerformanceAspect");
        System.out.println("  - @Around: Wraps method execution with before/after logic");
        System.out.println("  - @Before: Executes before target method");
        System.out.println("  - @After: Executes after target method (returning/throwing/finally)");
        System.out.println("  - Pointcut expressions: execution(* com.veld.example.aop.*.*(..))");
        System.out.println("  - @Interceptor + @AroundInvoke: CDI-style interception");
        System.out.println("  - @Logged, @Timed, @Validated, @Transactional: Built-in interceptors");
        System.out.println("  - ProxyFactory: ASM-based bytecode generation for proxies");
        
        // Clear statistics for next run
        PerformanceAspect.clearStatistics();
        TimingInterceptor.clearStatistics();
        registry.clear();
    }
    
    /**
     * Demonstrates advanced lifecycle management features.
     */
    private static void demonstrateAdvancedLifecycle() {
        System.out.println("\n→ Advanced Lifecycle Management Demo");
        System.out.println("  Complete control over bean initialization, startup, and shutdown.");
        
        System.out.println("\n══════════════════════════════════════════════════════════");
        System.out.println("→ Creating LifecycleProcessor:");
        System.out.println("══════════════════════════════════════════════════════════");
        
        LifecycleProcessor lifecycleProcessor = new LifecycleProcessor();
        EventBus eventBus = EventBus.getInstance();
        lifecycleProcessor.setEventBus(eventBus);
        
        // Register BeanPostProcessor
        System.out.println("\n→ Registering BeanPostProcessor:");
        LoggingBeanPostProcessor postProcessor = new LoggingBeanPostProcessor();
        lifecycleProcessor.addBeanPostProcessor(postProcessor);
        System.out.println("  LoggingBeanPostProcessor registered");
        
        // Create and register lifecycle beans
        System.out.println("\n══════════════════════════════════════════════════════════");
        System.out.println("→ Registering Lifecycle Beans:");
        System.out.println("══════════════════════════════════════════════════════════");
        
        // SmartLifecycle - DatabaseConnection
        DatabaseConnection dbConnection = new DatabaseConnection();
        lifecycleProcessor.registerBean("databaseConnection", dbConnection);
        System.out.println("  Registered: databaseConnection (SmartLifecycle, phase=-1000)");
        
        // @PostInitialize - CacheWarmer
        CacheWarmer cacheWarmer = new CacheWarmer();
        lifecycleProcessor.registerBean("cacheWarmer", cacheWarmer);
        System.out.println("  Registered: cacheWarmer (@PostInitialize, @DependsOn)");
        
        // @OnStart/@OnStop - ScheduledTaskRunner
        ScheduledTaskRunner taskRunner = new ScheduledTaskRunner();
        lifecycleProcessor.registerBean("scheduledTaskRunner", taskRunner);
        System.out.println("  Registered: scheduledTaskRunner (@OnStart, @OnStop)");
        
        // InitializingBean/DisposableBean - MetricsService
        MetricsService metricsService = new MetricsService();
        lifecycleProcessor.registerBean("metricsService", metricsService);
        System.out.println("  Registered: metricsService (InitializingBean, DisposableBean)");
        
        // LifecycleEventListener
        LifecycleEventListener eventListener = new LifecycleEventListener();
        eventBus.register(eventListener);
        System.out.println("  Registered: lifecycleEventListener (@Subscribe for lifecycle events)");
        
        // Apply post-processing and initialization
        System.out.println("\n══════════════════════════════════════════════════════════");
        System.out.println("→ Post-Processing Beans:");
        System.out.println("══════════════════════════════════════════════════════════");
        
        lifecycleProcessor.postProcessBeforeInitialization(dbConnection, "databaseConnection");
        lifecycleProcessor.initializeBean(metricsService, "metricsService");
        lifecycleProcessor.postProcessAfterInitialization(dbConnection, "databaseConnection");
        lifecycleProcessor.postProcessAfterInitialization(metricsService, "metricsService");
        
        // Simulate container refresh
        System.out.println("\n══════════════════════════════════════════════════════════");
        System.out.println("→ Container Refresh (triggers @PostInitialize):");
        System.out.println("══════════════════════════════════════════════════════════");
        lifecycleProcessor.onRefresh(150); // Simulate 150ms init time
        
        // Start lifecycle
        System.out.println("\n══════════════════════════════════════════════════════════");
        System.out.println("→ Starting Lifecycle (triggers SmartLifecycle.start() and @OnStart):");
        System.out.println("══════════════════════════════════════════════════════════");
        lifecycleProcessor.start();
        
        // Simulate application running
        System.out.println("\n══════════════════════════════════════════════════════════");
        System.out.println("→ Application Running:");
        System.out.println("══════════════════════════════════════════════════════════");
        System.out.println("  LifecycleProcessor running: " + lifecycleProcessor.isRunning());
        System.out.println("  DatabaseConnection running: " + dbConnection.isRunning());
        System.out.println("  Cache warmed: " + cacheWarmer.isCacheWarmed());
        
        // Use MetricsService
        System.out.println("\n→ Using MetricsService:");
        metricsService.recordRequest(true);
        metricsService.recordRequest(true);
        metricsService.recordRequest(false);
        metricsService.recordCacheAccess(true);
        metricsService.recordCacheAccess(true);
        metricsService.recordCacheAccess(false);
        System.out.println("  Recorded 3 requests (2 success, 1 error)");
        System.out.println("  Recorded 3 cache accesses (2 hits, 1 miss)");
        
        // Wait for scheduled tasks
        System.out.println("\n→ Waiting for scheduled tasks (1 second)...");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("  Tasks executed: " + taskRunner.getTaskCount());
        
        // Show statistics
        System.out.println("\n→ LifecycleProcessor Statistics:");
        System.out.println("  " + lifecycleProcessor.getStatistics());
        
        // Stop lifecycle
        System.out.println("\n══════════════════════════════════════════════════════════");
        System.out.println("→ Stopping Lifecycle (triggers @OnStop and SmartLifecycle.stop()):");
        System.out.println("══════════════════════════════════════════════════════════");
        lifecycleProcessor.stop();
        
        // Destroy beans
        System.out.println("\n══════════════════════════════════════════════════════════");
        System.out.println("→ Destroying Beans (triggers DisposableBean.destroy()):");
        System.out.println("══════════════════════════════════════════════════════════");
        lifecycleProcessor.destroy();
        
        System.out.println("\n→ Advanced Lifecycle Features Demonstrated:");
        System.out.println("  - SmartLifecycle: Phase-ordered startup/shutdown (DatabaseConnection)");
        System.out.println("  - InitializingBean.afterPropertiesSet(): Post-injection init (MetricsService)");
        System.out.println("  - DisposableBean.destroy(): Cleanup on shutdown (MetricsService)");
        System.out.println("  - BeanPostProcessor: Before/after init hooks (LoggingBeanPostProcessor)");
        System.out.println("  - @PostInitialize: After all beans ready (CacheWarmer)");
        System.out.println("  - @OnStart/@OnStop: Application start/stop hooks (ScheduledTaskRunner)");
        System.out.println("  - @DependsOn: Explicit initialization order (CacheWarmer depends on DB)");
        System.out.println("  - Lifecycle Events: ContextRefreshed, Started, Stopped, Closed");
    }
    
    /**
     * Demonstrates actual usage of the services.
     */
    private static void demonstrateServiceUsage(VeldContainer container) {
        // Get services
        UserService userService = container.get(UserService.class);
        
        System.out.println("\n→ Listing existing users:");
        userService.listAllUsers();
        
        System.out.println("\n→ Looking up user by ID:");
        String user = userService.getUserName(1L);
        System.out.println("  User with ID 1: " + user);
        
        System.out.println("\n→ Creating new user:");
        userService.createUser(4L, "Diana");
        
        System.out.println("\n→ Listing users after creation:");
        userService.listAllUsers();
        
        System.out.println("\n→ Sending email notification (prototype):");
        EmailNotification notification = container.get(EmailNotification.class);
        notification
            .to("user@example.com")
            .withSubject("Welcome to Veld!")
            .withBody("Thank you for trying Veld DI Framework.")
            .send();
        
        System.out.println("\n→ Processing requests (prototype instances):");
        RequestContext request1 = container.get(RequestContext.class);
        request1.process("GET /users");
        
        RequestContext request2 = container.get(RequestContext.class);
        request2.process("POST /users");
    }
}
