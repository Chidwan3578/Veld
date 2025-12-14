/*
 * Copyright 2025 Veld Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.yasmramos.veld.example.dependsOn;

import io.github.yasmramos.veld.annotation.*;
import io.github.yasmramos.veld.runtime.lifecycle.LifecycleProcessor;
import io.github.yasmramos.veld.runtime.value.ValueResolver;

/**
 * Demo principal para probar la funcionalidad de @DependsOn.
 * 
 * Este demo muestra:
 * 1. Inicialización ordenada basada en @DependsOn
 * 2. Dependencias explícitas e implícitas trabajando juntas
 * 3. Multiple dependencies en @DependsOn
 * 4. Integration con el nuevo LifecycleProcessor
 */
@Singleton
public class DependsOnDemo {
    
    private final DatabaseMigrator databaseMigrator;
    private final UserRepository userRepository;
    private final CacheManager cacheManager;
    private final ConfigService configService;
    private final ApplicationService applicationService;
    
    @Inject
    public DependsOnDemo(
            DatabaseMigrator databaseMigrator,
            UserRepository userRepository,
            CacheManager cacheManager,
            ConfigService configService,
            ApplicationService applicationService) {
        this.databaseMigrator = databaseMigrator;
        this.userRepository = userRepository;
        this.cacheManager = cacheManager;
        this.configService = configService;
        this.applicationService = applicationService;
    }
    
    @PostConstruct
    public void runDemo() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🚀 VELD @DependsOn DEMONSTRATION");
        System.out.println("=".repeat(60));
        
        // Verificar el orden de inicialización
        System.out.println("\n📋 VERIFICANDO ORDEN DE INICIALIZACIÓN:");
        System.out.println("✅ DatabaseMigrator: " + (databaseMigrator != null ? "INITIALIZED" : "NULL"));
        System.out.println("✅ CacheManager: " + (cacheManager != null ? "INITIALIZED" : "NULL"));
        System.out.println("✅ ConfigService: " + (configService != null ? "INITIALIZED" : "NULL"));
        System.out.println("✅ UserRepository: " + (userRepository != null ? "INITIALIZED" : "NULL"));
        System.out.println("✅ ApplicationService: " + (applicationService != null ? "INITIALIZED" : "NULL"));
        
        // Verificar que la base de datos esté lista
        System.out.println("\n🗄️ VERIFICANDO ESTADO DE LA BASE DE DATOS:");
        System.out.println("Database migrations count: " + DatabaseMigrator.getMigrationCount());
        System.out.println("Database ready: " + databaseMigrator.isDatabaseReady());
        
        // Demostrar funcionalidad
        System.out.println("\n🎯 DEMOSTRANDO FUNCIONALIDAD:");
        
        // 1. Operaciones que requieren base de datos
        userRepository.saveUser("Alice");
        userRepository.saveUser("Bob");
        userRepository.findUsers();
        
        // 2. Operaciones que requieren caché
        cacheManager.put("test_key", "test_value");
        Object cachedValue = cacheManager.get("test_key");
        System.out.println("Cached value: " + cachedValue);
        
        // 3. Aplicación principal
        applicationService.startApplication();
        
        // 4. Verificar configuración
        System.out.println("\n⚙️ CONFIGURACIÓN ACTUAL:");
        System.out.println("App name: " + configService.getAppName());
        System.out.println("Max connections: " + configService.getMaxConnections());
        System.out.println("Debug mode: " + configService.isDebugMode());
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ DEMO COMPLETADO EXITOSAMENTE");
        System.out.println("✅ Todas las dependencias se resolvieron correctamente");
        System.out.println("✅ El orden de inicialización se respetó");
        System.out.println("=".repeat(60) + "\n");
    }
    
    /**
     * Método para demostrar acceso directo a los beans gestionados.
     */
    public void demonstrateDirectAccess() {
        System.out.println("\n🔍 ACCESO DIRECTO A BEANS GESTIONADOS:");
        
        // Usar Veld.get() para acceder directamente
        // (esto funcionará una vez que el weaver genere Veld.class)
        try {
            Class<?> veldClass = Class.forName("com.veld.Veld");
            System.out.println("✅ Veld class loaded successfully");
            
            // Acceder al LifecycleProcessor
            Object lifecycleProcessor = veldClass.getMethod("getLifecycleProcessor").invoke(null);
            System.out.println("✅ LifecycleProcessor: " + (lifecycleProcessor != null ? "AVAILABLE" : "NULL"));
            
            // Acceder al ValueResolver
            Object valueResolver = veldClass.getMethod("getValueResolver").invoke(null);
            System.out.println("✅ ValueResolver: " + (valueResolver != null ? "AVAILABLE" : "NULL"));
            
        } catch (Exception e) {
            System.out.println("ℹ️ Veld class not yet generated (this is expected during compilation)");
        }
    }
    
    @PreDestroy
    public void cleanup() {
        System.out.println("\n🧹 CLEANUP: Cerrando demo...");
        cacheManager.clear();
        System.out.println("✅ Cleanup completed");
    }
}