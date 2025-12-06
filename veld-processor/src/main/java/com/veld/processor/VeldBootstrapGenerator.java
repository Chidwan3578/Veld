package com.veld.processor;

import com.veld.runtime.Scope;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.*;

/**
 * Generates the Veld bootstrap class as bytecode using ASM.
 * Uses <clinit> initialization for MAXIMUM performance - as fast as Dagger.
 * 
 * Optimizations:
 * - final static fields (enables JIT inlining)
 * - <clinit> initialization (JVM-guaranteed thread safety, zero runtime overhead)
 * - Topological sorting (correct dependency order)
 * - invokespecial for constructors (direct call, no virtual dispatch)
 */
public class VeldBootstrapGenerator implements Opcodes {
    
    private static final String VELD_CLASS = "com/veld/generated/Veld";
    private static final String REGISTRY_CLASS = "com/veld/generated/VeldRegistry";
    private static final String CONTAINER_CLASS = "com/veld/runtime/VeldContainer";
    private static final String COMPONENT_REGISTRY_CLASS = "com/veld/runtime/ComponentRegistry";
    
    private final List<ComponentInfo> components;
    
    public VeldBootstrapGenerator(List<ComponentInfo> components) {
        this.components = components;
    }
    
    public byte[] generate() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        
        // public final class Veld
        cw.visit(V17, ACC_PUBLIC | ACC_FINAL, VELD_CLASS, null, "java/lang/Object", null);
        
        // Separate singletons from prototypes
        List<ComponentInfo> singletons = new ArrayList<>();
        List<ComponentInfo> prototypes = new ArrayList<>();
        for (ComponentInfo comp : components) {
            if (comp.getScope() == Scope.SINGLETON) {
                singletons.add(comp);
            } else {
                prototypes.add(comp);
            }
        }
        
        // Generate FINAL static fields for singletons (enables JIT optimization)
        for (ComponentInfo comp : singletons) {
            cw.visitField(
                ACC_PRIVATE | ACC_STATIC | ACC_FINAL,
                getFieldName(comp),
                "L" + comp.getInternalName() + ";",
                null,
                null
            ).visitEnd();
        }
        
        // Generate <clinit> with topologically sorted initialization
        generateStaticInit(cw, singletons);
        
        // Private constructor
        generatePrivateConstructor(cw);
        
        // Generate ultra-fast getter for each singleton (just getstatic + areturn)
        for (ComponentInfo comp : singletons) {
            generateSingletonGetter(cw, comp);
        }
        
        // Generate prototype getters
        for (ComponentInfo comp : prototypes) {
            generatePrototypeGetter(cw, comp);
        }
        
        // Container factory methods
        generateCreateContainer(cw);
        generateCreateRegistry(cw);
        
        cw.visitEnd();
        return cw.toByteArray();
    }
    
    /**
     * Generates <clinit> that initializes all singletons in topological order.
     * JVM guarantees thread-safe initialization - no synchronization needed at runtime.
     */
    private void generateStaticInit(ClassWriter cw, List<ComponentInfo> singletons) {
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();
        
        // Topologically sort singletons so dependencies are initialized first
        List<ComponentInfo> sorted = topologicalSort(singletons);
        
        // Initialize each singleton in order
        for (ComponentInfo comp : sorted) {
            String fieldName = getFieldName(comp);
            String fieldType = "L" + comp.getInternalName() + ";";
            
            // new Component(dep1(), dep2(), ...)
            mv.visitTypeInsn(NEW, comp.getInternalName());
            mv.visitInsn(DUP);
            
            // Push constructor arguments
            StringBuilder constructorDesc = new StringBuilder("(");
            InjectionPoint constructor = comp.getConstructorInjection();
            List<InjectionPoint.Dependency> deps = constructor != null ? 
                constructor.getDependencies() : Collections.emptyList();
            
            for (InjectionPoint.Dependency dep : deps) {
                String depType = dep.getTypeName().replace('.', '/');
                constructorDesc.append("L").append(depType).append(";");
                
                ComponentInfo depComp = findComponentByType(depType);
                if (depComp != null && depComp.getScope() == Scope.SINGLETON) {
                    // Read from already-initialized field
                    mv.visitFieldInsn(GETSTATIC, VELD_CLASS, getFieldName(depComp), 
                        "L" + depComp.getInternalName() + ";");
                } else if (depComp != null) {
                    // Prototype - call getter
                    mv.visitMethodInsn(INVOKESTATIC, VELD_CLASS, getMethodName(depComp),
                        "()L" + depComp.getInternalName() + ";", false);
                } else {
                    mv.visitInsn(ACONST_NULL);
                }
            }
            constructorDesc.append(")V");
            
            // invokespecial for direct constructor call
            mv.visitMethodInsn(INVOKESPECIAL, comp.getInternalName(), "<init>", 
                constructorDesc.toString(), false);
            
            // Store in field
            mv.visitFieldInsn(PUTSTATIC, VELD_CLASS, fieldName, fieldType);
        }
        
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    /**
     * Topological sort: dependencies before dependents.
     */
    private List<ComponentInfo> topologicalSort(List<ComponentInfo> singletons) {
        Map<String, ComponentInfo> byType = new HashMap<>();
        for (ComponentInfo comp : singletons) {
            byType.put(comp.getInternalName(), comp);
            for (String iface : comp.getImplementedInterfacesInternal()) {
                byType.put(iface, comp);
            }
        }
        
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();
        List<ComponentInfo> result = new ArrayList<>();
        
        for (ComponentInfo comp : singletons) {
            visit(comp, byType, visited, visiting, result);
        }
        
        return result;
    }
    
    private void visit(ComponentInfo comp, Map<String, ComponentInfo> byType,
                       Set<String> visited, Set<String> visiting, List<ComponentInfo> result) {
        String key = comp.getInternalName();
        if (visited.contains(key)) return;
        if (visiting.contains(key)) return; // Circular dependency
        
        visiting.add(key);
        
        InjectionPoint constructor = comp.getConstructorInjection();
        if (constructor != null) {
            for (InjectionPoint.Dependency dep : constructor.getDependencies()) {
                String depType = dep.getTypeName().replace('.', '/');
                ComponentInfo depComp = byType.get(depType);
                if (depComp != null && depComp.getScope() == Scope.SINGLETON) {
                    visit(depComp, byType, visited, visiting, result);
                }
            }
        }
        
        visiting.remove(key);
        visited.add(key);
        result.add(comp);
    }
    
    private void generatePrivateConstructor(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PRIVATE, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    /**
     * Generates ULTRA-FAST singleton getter - just 2 bytecode instructions.
     * 
     * Generated bytecode:
     *   getstatic  Veld._myService : MyService
     *   areturn
     */
    private void generateSingletonGetter(ClassWriter cw, ComponentInfo comp) {
        String methodName = getMethodName(comp);
        String fieldName = getFieldName(comp);
        String returnType = "L" + comp.getInternalName() + ";";
        
        MethodVisitor mv = cw.visitMethod(
            ACC_PUBLIC | ACC_STATIC | ACC_FINAL,
            methodName,
            "()" + returnType,
            null,
            null
        );
        mv.visitCode();
        
        // Just read the field and return - 2 instructions, same as Dagger
        mv.visitFieldInsn(GETSTATIC, VELD_CLASS, fieldName, returnType);
        mv.visitInsn(ARETURN);
        
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private void generatePrototypeGetter(ClassWriter cw, ComponentInfo comp) {
        String methodName = getMethodName(comp);
        String returnType = "L" + comp.getInternalName() + ";";
        
        MethodVisitor mv = cw.visitMethod(
            ACC_PUBLIC | ACC_STATIC,
            methodName,
            "()" + returnType,
            null,
            null
        );
        mv.visitCode();
        
        mv.visitTypeInsn(NEW, comp.getInternalName());
        mv.visitInsn(DUP);
        
        StringBuilder constructorDesc = new StringBuilder("(");
        InjectionPoint constructor = comp.getConstructorInjection();
        List<InjectionPoint.Dependency> deps = constructor != null ? 
            constructor.getDependencies() : Collections.emptyList();
        
        for (InjectionPoint.Dependency dep : deps) {
            String depType = dep.getTypeName().replace('.', '/');
            constructorDesc.append("L").append(depType).append(";");
            
            ComponentInfo depComp = findComponentByType(depType);
            if (depComp != null) {
                mv.visitMethodInsn(INVOKESTATIC, VELD_CLASS, getMethodName(depComp),
                    "()L" + depComp.getInternalName() + ";", false);
            } else {
                mv.visitInsn(ACONST_NULL);
            }
        }
        constructorDesc.append(")V");
        
        mv.visitMethodInsn(INVOKESPECIAL, comp.getInternalName(), "<init>", 
            constructorDesc.toString(), false);
        mv.visitInsn(ARETURN);
        
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private ComponentInfo findComponentByType(String internalName) {
        for (ComponentInfo comp : components) {
            if (comp.getInternalName().equals(internalName)) {
                return comp;
            }
            for (String iface : comp.getImplementedInterfacesInternal()) {
                if (iface.equals(internalName)) {
                    return comp;
                }
            }
        }
        return null;
    }
    
    private String getMethodName(ComponentInfo comp) {
        String simpleName = comp.getClassName();
        int lastDot = simpleName.lastIndexOf('.');
        if (lastDot >= 0) {
            simpleName = simpleName.substring(lastDot + 1);
        }
        if (simpleName.length() > 1 && Character.isUpperCase(simpleName.charAt(1))) {
            return simpleName;
        }
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }
    
    private String getFieldName(ComponentInfo comp) {
        return "_" + getMethodName(comp);
    }
    
    private void generateCreateContainer(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(
            ACC_PUBLIC | ACC_STATIC,
            "createContainer",
            "()L" + CONTAINER_CLASS + ";",
            null,
            null
        );
        mv.visitCode();
        mv.visitTypeInsn(NEW, CONTAINER_CLASS);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESTATIC, VELD_CLASS, "createRegistry", 
            "()L" + COMPONENT_REGISTRY_CLASS + ";", false);
        mv.visitMethodInsn(INVOKESPECIAL, CONTAINER_CLASS, "<init>", 
            "(L" + COMPONENT_REGISTRY_CLASS + ";)V", false);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private void generateCreateRegistry(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(
            ACC_PUBLIC | ACC_STATIC,
            "createRegistry",
            "()L" + COMPONENT_REGISTRY_CLASS + ";",
            null,
            null
        );
        mv.visitCode();
        mv.visitTypeInsn(NEW, REGISTRY_CLASS);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, REGISTRY_CLASS, "<init>", "()V", false);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    public String getClassName() {
        return VELD_CLASS.replace('/', '.');
    }
}
