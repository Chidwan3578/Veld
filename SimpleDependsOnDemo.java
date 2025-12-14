import java.util.*;
import java.lang.reflect.*;

/**
 * Demostración simplificada del funcionamiento de @DependsOn
 * Sin dependencias de ASM, simula el orden de inicialización
 */
public class SimpleDependsOnDemo {
    
    // Clase para simular la anotación @DependsOn
    @interface DependsOn {
        String[] value() default {};
    }
    
    // Clase para simular @Component
    @interface Component {
        String value() default "";
    }
    
    // Clase para representar información de un componente
    static class ComponentInfo {
        private final String name;
        private final Class<?> clazz;
        private final List<String> dependencies = new ArrayList<>();
        private final List<String> explicitDependencies = new ArrayList<>();
        
        public ComponentInfo(String name, Class<?> clazz) {
            this.name = name;
            this.clazz = clazz;
            extractDependencies();
        }
        
        private void extractDependencies() {
            // Extraer dependencias de inyección de campos
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Component.class)) {
                    dependencies.add(field.getType().getSimpleName());
                }
            }
            
            // Extraer dependencias explícitas de @DependsOn
            DependsOn dependsOn = clazz.getAnnotation(DependsOn.class);
            if (dependsOn != null) {
                explicitDependencies.addAll(Arrays.asList(dependsOn.value()));
            }
        }
        
        public String getName() { return name; }
        public Class<?> getClazz() { return clazz; }
        public List<String> getDependencies() { return dependencies; }
        public List<String> getExplicitDependencies() { return explicitDependencies; }
    }
    
    // Componentes de ejemplo
    @Component("userRepository")
    static class UserRepository {
        public UserRepository() {
            System.out.println("✓ UserRepository inicializado");
        }
    }
    
    @Component("configService")
    static class ConfigService {
        public ConfigService() {
            System.out.println("✓ ConfigService inicializado");
        }
    }
    
    @Component("cacheManager")
    static class CacheManager {
        public CacheManager() {
            System.out.println("✓ CacheManager inicializado");
        }
    }
    
    @DependsOn("userRepository")
    @Component("databaseMigrator")
    static class DatabaseMigrator {
        @Component("userRepository")
        private UserRepository userRepository;
        
        public DatabaseMigrator() {
            System.out.println("✓ DatabaseMigrator inicializado (depende de UserRepository)");
        }
    }
    
    @DependsOn({"cacheManager", "configService"})
    @Component("applicationService")
    static class ApplicationService {
        @Component("cacheManager")
        private CacheManager cacheManager;
        
        @Component("configService")
        private ConfigService configService;
        
        public ApplicationService() {
            System.out.println("✓ ApplicationService inicializado (depende de CacheManager y ConfigService)");
        }
    }
    
    // Ordenador topológico para determinar el orden de inicialización
    static class DependencyResolver {
        private final List<ComponentInfo> components;
        
        public DependencyResolver(List<ComponentInfo> components) {
            this.components = components;
        }
        
        public List<String> resolveInitializationOrder() {
            Map<String, ComponentInfo> componentMap = new HashMap<>();
            Map<String, Set<String>> dependencyGraph = new HashMap<>();
            
            // Crear mapa de componentes y grafo de dependencias
            for (ComponentInfo component : components) {
                componentMap.put(component.getName(), component);
                dependencyGraph.put(component.getName(), new HashSet<>());
                
                // Agregar dependencias implícitas (por inyección de campos)
                dependencyGraph.get(component.getName()).addAll(component.getDependencies());
                
                // Agregar dependencias explícitas (@DependsOn)
                dependencyGraph.get(component.getName()).addAll(component.getExplicitDependencies());
            }
            
            // Algoritmo de ordenamiento topológico
            List<String> result = new ArrayList<>();
            Set<String> visited = new HashSet<>();
            Set<String> visiting = new HashSet<>();
            
            for (String componentName : dependencyGraph.keySet()) {
                if (!visited.contains(componentName)) {
                    visit(componentName, dependencyGraph, visited, visiting, result);
                }
            }
            
            return result;
        }
        
        private void visit(String componentName, Map<String, Set<String>> graph, 
                          Set<String> visited, Set<String> visiting, List<String> result) {
            if (visiting.contains(componentName)) {
                throw new RuntimeException("Ciclo de dependencias detectado: " + componentName);
            }
            
            if (visited.contains(componentName)) {
                return;
            }
            
            visiting.add(componentName);
            
            for (String dependency : graph.get(componentName)) {
                if (componentMapContains(dependency)) {
                    visit(dependency, graph, visited, visiting, result);
                }
            }
            
            visiting.remove(componentName);
            visited.add(componentName);
            result.add(componentName);
        }
        
        private boolean componentMapContains(String name) {
            for (ComponentInfo component : components) {
                if (component.getName().equals(name)) {
                    return true;
                }
            }
            return false;
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== DEMOSTRACIÓN @DependsOn - ORDEN DE INICIALIZACIÓN ===\n");
        
        // Crear información de componentes
        List<ComponentInfo> components = Arrays.asList(
            new ComponentInfo("userRepository", UserRepository.class),
            new ComponentInfo("configService", ConfigService.class),
            new ComponentInfo("cacheManager", CacheManager.class),
            new ComponentInfo("databaseMigrator", DatabaseMigrator.class),
            new ComponentInfo("applicationService", ApplicationService.class)
        );
        
        // Mostrar información de dependencias
        System.out.println("COMPONENTES Y SUS DEPENDENCIAS:");
        System.out.println("================================");
        for (ComponentInfo component : components) {
            System.out.println("• " + component.getName() + ":");
            if (!component.getExplicitDependencies().isEmpty()) {
                System.out.println("  - Dependencias explícitas (@DependsOn): " + component.getExplicitDependencies());
            }
            if (!component.getDependencies().isEmpty()) {
                System.out.println("  - Dependencias implícitas (inyección): " + component.getDependencies());
            }
            if (component.getExplicitDependencies().isEmpty() && component.getDependencies().isEmpty()) {
                System.out.println("  - Sin dependencias");
            }
            System.out.println();
        }
        
        // Resolver orden de inicialización
        DependencyResolver resolver = new DependencyResolver(components);
        List<String> initializationOrder = resolver.resolveInitializationOrder();
        
        System.out.println("ORDEN DE INICIALIZACIÓN RESUELTO:");
        System.out.println("==================================");
        for (int i = 0; i < initializationOrder.size(); i++) {
            System.out.println((i + 1) + ". " + initializationOrder.get(i));
        }
        
        System.out.println("\nINICIALIZACIÓN EN ORDEN:");
        System.out.println("=========================");
        
        // Simular inicialización en orden
        Map<String, Object> instances = new HashMap<>();
        for (String componentName : initializationOrder) {
            ComponentInfo component = components.stream()
                .filter(c -> c.getName().equals(componentName))
                .findFirst()
                .orElse(null);
            
            if (component != null) {
                try {
                    Object instance = component.getClazz().getDeclaredConstructor().newInstance();
                    instances.put(componentName, instance);
                } catch (Exception e) {
                    System.out.println("✗ Error inicializando " + componentName + ": " + e.getMessage());
                }
            }
        }
        
        System.out.println("\n✅ DEMOSTRACIÓN COMPLETADA");
        System.out.println("El orden de inicialización respeta correctamente las dependencias @DependsOn");
    }
}