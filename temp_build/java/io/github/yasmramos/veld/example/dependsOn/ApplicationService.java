/*
 * Copyright 2025 Veld Framework
 *
 * Licensed under the License at
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

/**
 * ApplicationService - servicio principal que depende de múltiples beans.
 * 
 * Este bean demuestra el uso de @DependsOn con múltiples dependencias.
 * Debe inicializarse después de que tanto CacheManager como ConfigService
 * estén completamente listos.
 */
@Singleton
@DependsOn({"cacheManager", "configService"})  // Múltiples dependencias explícitas
@PostConstruct
public class ApplicationService {
    
    private final CacheManager cacheManager;
    private final ConfigService configService;
    
    // Constructor con inyección (dependencias implícitas)
    @Inject
    public ApplicationService(CacheManager cacheManager, ConfigService configService) {
        this.cacheManager = cacheManager;
        this.configService = configService;
        System.out.println("[ApplicationService] Constructor called with CacheManager and ConfigService");
    }
    
    @PostConstruct
    public void initializeApplication() {
        System.out.println("[ApplicationService] ✅ Initializing application");
        System.out.println("[ApplicationService] ✅ Using configuration: " + configService);
        System.out.println("[ApplicationService] ✅ Cache system ready: " + cacheManager.getHitCount() + " hits");
    }
    
    public void startApplication() {
        System.out.println("[ApplicationService] 🚀 Starting application: " + configService.getAppName());
        
        // Usar el caché
        cacheManager.put("startup", "Application started successfully");
        Object startup = cacheManager.get("startup");
        System.out.println("[ApplicationService] 💾 Cache test: " + startup);
        
        // Simular operaciones que requieren todas las dependencias
        performDataOperations();
    }
    
    private void performDataOperations() {
        System.out.println("[ApplicationService] 🔄 Performing data operations...");
        
        // Operaciones que requieren base de datos + caché + configuración
        String operation = "User authentication";
        cacheManager.put("last_operation", operation);
        System.out.println("[ApplicationService] ✅ Operation cached: " + operation);
        
        int cacheHits = cacheManager.getHitCount();
        int cacheMisses = cacheManager.getMissCount();
        System.out.println("[ApplicationService] 📊 Cache stats - Hits: " + cacheHits + ", Misses: " + cacheMisses);
    }
    
    public CacheManager getCacheManager() {
        return cacheManager;
    }
    
    public ConfigService getConfigService() {
        return configService;
    }
    
    @PreDestroy
    public void shutdown() {
        System.out.println("[ApplicationService] 🛑 Shutting down application");
        cacheManager.clear();
    }
}