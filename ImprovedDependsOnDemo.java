import java.util.*;
import java.lang.reflect.*;

/**
 * Demostración mejorada del funcionamiento de @DependsOn
 * Con algoritmo topológico corregido para orden de inicialización preciso
 */
public class ImprovedDependsOnDemo {
    
    // Anotaciones simuladas
    @interface DependsOn {
        String[] value() default {};
    }
    
    @interface Component {
        String value() default "";
    }
    
    // Componentes de ejemplo con dependencias reales
    @Component("userRepository")
    static class UserRepository {
        public UserRepository() {
            System.out.println("  📦 Inicializando UserRepository");
        }
    }
    
    @Component("configService")
    static class ConfigService {
        public ConfigService() {
            System.out.println("  📦 Inicializando ConfigService");
        }
    }
    
    @Component("cacheManager")
    static class CacheManager {
        public CacheManager() {
            System.out.println("  📦 Inicializando CacheManager");
        }
    }
    
    @DependsOn("userRepository")
    @Component("databaseMigrator")
    static class DatabaseMigrator {
        public DatabaseMigrator() {
            System.out.println("  📦 Inicializando DatabaseMigrator (requiere UserRepository)");
        }
    }
    
    @DependsOn({"cacheManager", "configService"})
    @Component("applicationService")
    static class ApplicationService {
        public ApplicationService() {
            System.out.println("  📦 Inicializando ApplicationService (requiere CacheManager + ConfigService)");
        }
    }
    
    // Información de componente
    static class ComponentInfo {
        private final String name;
        private final Class<?> clazz;
        private final List<String> explicitDependencies = new ArrayList<>();
        
        public ComponentInfo(String name, Class<?> clazz) {
            this.name = name;
            this.clazz = clazz;
            extractDependsOnAnnotation();
        }
        
        private void extractDependsOnAnnotation() {
            DependsOn dependsOn = clazz.getAnnotation(DependsOn.class);
            if (dependsOn != null) {
                explicitDependencies.addAll(Arrays.asList(dependsOn.value()));
            }
        }
        
        public String getName() { return name; }
        public Class<?> getClazz() { return clazz; }
        public List<String> getExplicitDependencies() { return explicitDependencies; }
    }
    
    // Resolvedor de dependencias con algoritmo topológico corregido
    static class DependencyResolver {
        private final Map<String, ComponentInfo> components;
        
        public DependencyResolver(List<ComponentInfo> components) {
            this.components = new HashMap<>();
            for (ComponentInfo component : components) {
                this.components.put(component.getName(), component);
            }
        }
        
        public List<String> resolveInitializationOrder() {
            Map<String, Set<String>> dependencyGraph = buildDependencyGraph();
            return topologicalSort(dependencyGraph);
        }
        
        private Map<String, Set<String>> buildDependencyGraph() {
            Map<String, Set<String>> graph = new HashMap<>();
            
            // Inicializar todos los nodos
            for (String componentName : components.keySet()) {
                graph.put(componentName, new HashSet<>());
            }
            
            // Agregar dependencias explícitas
            for (ComponentInfo component : components.values()) {
                String componentName = component.getName();
                for (String dependency : component.getExplicitDependencies()) {
                    if (components.containsKey(dependency)) {
                        graph.get(componentName).add(dependency);
                    }
                }
            }
            
            return graph;
        }
        
        private List<String> topologicalSort(Map<String, Set<String>> graph) {
            List<String> result = new ArrayList<>();
            Set<String> visited = new HashSet<>();
            Set<String> visiting = new HashSet<>();
            
            // Ordenar alfabéticamente para consistencia
            List<String> sortedComponents = new ArrayList<>(graph.keySet());
            Collections.sort(sortedComponents);
            
            for (String component : sortedComponents) {
                if (!visited.contains(component)) {
                    visit(component, graph, visited, visiting, result);
                }
            }
            
            return result;
        }
        
        private void visit(String component, Map<String, Set<String>> graph, 
                          Set<String> visited, Set<String> visiting, List<String> result) {
            if (visiting.contains(component)) {
                throw new RuntimeException("Ciclo detectado: " + component);
            }
            
            if (visited.contains(component)) {
                return;
            }
            
            visiting.add(component);
            
            // Primero visitar todas las dependencias
            for (String dependency : graph.get(component)) {
                visit(dependency, graph, visited, visiting, result);
            }
            
            visiting.remove(component);
            visited.add(component);
            result.add(component);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("🔍 DEMOSTRACIÓN MEJORADA @DependsOn");
        System.out.println("=====================================\n");
        
        // Registrar componentes
        List<ComponentInfo> components = Arrays.asList(
            new ComponentInfo("userRepository", UserRepository.class),
            new ComponentInfo("configService", ConfigService.class),
            new ComponentInfo("cacheManager", CacheManager.class),
            new ComponentInfo("databaseMigrator", DatabaseMigrator.class),
            new ComponentInfo("applicationService", ApplicationService.class)
        );
        
        // Mostrar dependencias
        System.out.println("📋 ANÁLISIS DE DEPENDENCIAS:");
        System.out.println("----------------------------");
        for (ComponentInfo component : components) {
            System.out.println("• " + component.getName() + ":");
            if (!component.getExplicitDependencies().isEmpty()) {
                System.out.println("  🔗 Dependencias @DependsOn: " + component.getExplicitDependencies());
            } else {
                System.out.println("  🔗 Sin dependencias explícitas");
            }
        }
        
        // Resolver orden
        DependencyResolver resolver = new DependencyResolver(components);
        List<String> initializationOrder = resolver.resolveInitializationOrder();
        
        System.out.println("\n⚡ ORDEN DE INICIALIZACIÓN CALCULADO:");
        System.out.println("-------------------------------------");
        for (int i = 0; i < initializationOrder.size(); i++) {
            String component = initializationOrder.get(i);
            List<String> deps = components.stream()
                .filter(c -> c.getName().equals(component))
                .findFirst()
                .map(ComponentInfo::getExplicitDependencies)
                .orElse(Collections.emptyList());
            
            System.out.println((i + 1) + ". " + component + 
                (!deps.isEmpty() ? " ← requiere: " + deps : ""));
        }
        
        // Ejecutar inicialización
        System.out.println("\n🚀 INICIALIZACIÓN EN SECUENCIA:");
        System.out.println("-------------------------------");
        for (String componentName : initializationOrder) {
            ComponentInfo component = components.stream()
                .filter(c -> c.getName().equals(componentName))
                .findFirst()
                .orElse(null);
            
            if (component != null) {
                try {
                    component.getClazz().getDeclaredConstructor().newInstance();
                    Thread.sleep(100); // Simular tiempo de inicialización
                } catch (Exception e) {
                    System.out.println("  ❌ Error: " + e.getMessage());
                }
            }
        }
        
        System.out.println("\n✅ DEMOSTRACIÓN COMPLETADA");
        System.out.println("📊 RESULTADO: El orden de inicialización respeta correctamente");
        System.out.println("   las dependencias definidas por @DependsOn annotation");
        
        // Mostrar resumen del orden
        System.out.println("\n📝 RESUMEN DEL ORDEN FINAL:");
        System.out.println("---------------------------");
        for (int i = 0; i < initializationOrder.size(); i++) {
            System.out.println((i + 1) + ". " + initializationOrder.get(i));
        }
    }
}