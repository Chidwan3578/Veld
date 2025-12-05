package com.veld.weaver;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Bytecode weaver that adds synthetic setter methods for private field injection.
 * 
 * <p>For each field annotated with @Inject or @Value, this weaver generates a
 * synthetic method {@code __di_set_<fieldName>} that allows the generated factory
 * to inject values without using reflection.
 * 
 * <p>Example transformation:
 * <pre>
 * // Original class
 * public class MyService {
 *     &#64;Inject
 *     private Repository repository;
 * }
 * 
 * // After weaving - synthetic method added
 * public class MyService {
 *     &#64;Inject
 *     private Repository repository;
 *     
 *     // Generated synthetic method
 *     public synthetic void __di_set_repository(Repository value) {
 *         this.repository = value;
 *     }
 * }
 * </pre>
 * 
 * <p>The generated factory then calls:
 * <pre>
 * instance.__di_set_repository(container.get(Repository.class));
 * </pre>
 * 
 * <p>This approach maintains the "zero-reflection" principle while supporting
 * private field injection.
 */
public class FieldInjectorWeaver {
    
    /** Prefix for generated injection setter methods */
    public static final String SETTER_PREFIX = "__di_set_";
    
    /** Annotations that mark fields for injection */
    private static final Set<String> INJECT_ANNOTATIONS = Set.of(
        "Lcom/veld/annotation/Inject;",
        "Ljavax/inject/Inject;",
        "Ljakarta/inject/Inject;",
        "Lcom/veld/annotation/Value;"
    );
    
    private final List<WeavingResult> results = new ArrayList<>();
    
    /**
     * Weaves all class files in the specified directory.
     * 
     * @param classesDirectory the directory containing compiled .class files
     * @return list of weaving results
     * @throws IOException if an I/O error occurs
     */
    public List<WeavingResult> weaveDirectory(Path classesDirectory) throws IOException {
        results.clear();
        
        if (!Files.exists(classesDirectory)) {
            return results;
        }
        
        Files.walk(classesDirectory)
            .filter(path -> path.toString().endsWith(".class"))
            .forEach(this::weaveClassFile);
        
        return new ArrayList<>(results);
    }
    
    /**
     * Weaves a single class file.
     * 
     * @param classFile path to the .class file
     */
    private void weaveClassFile(Path classFile) {
        try {
            byte[] originalBytes = Files.readAllBytes(classFile);
            WeavingResult result = weaveClass(originalBytes);
            
            if (result.wasModified()) {
                Files.write(classFile, result.getBytecode());
                results.add(result);
            }
        } catch (IOException e) {
            results.add(WeavingResult.error(classFile.toString(), e.getMessage()));
        }
    }
    
    /**
     * Weaves a class, adding synthetic setter methods for injectable fields.
     * 
     * @param classBytes the original class bytecode
     * @return the weaving result containing modified bytecode if changes were made
     */
    public WeavingResult weaveClass(byte[] classBytes) {
        ClassReader reader = new ClassReader(classBytes);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, 0);
        
        // Find fields that need injection setters
        List<FieldNode> injectableFields = findInjectableFields(classNode);
        
        if (injectableFields.isEmpty()) {
            return WeavingResult.unchanged(classNode.name);
        }
        
        // Check if setters already exist (avoid re-weaving)
        Set<String> existingMethods = new HashSet<>();
        for (MethodNode method : classNode.methods) {
            existingMethods.add(method.name);
        }
        
        List<String> addedSetters = new ArrayList<>();
        
        // Generate synthetic setters for each injectable field
        for (FieldNode field : injectableFields) {
            String setterName = SETTER_PREFIX + field.name;
            
            // Skip if setter already exists
            if (existingMethods.contains(setterName)) {
                continue;
            }
            
            MethodNode setter = generateSetter(classNode.name, field);
            classNode.methods.add(setter);
            addedSetters.add(setterName);
        }
        
        if (addedSetters.isEmpty()) {
            return WeavingResult.unchanged(classNode.name);
        }
        
        // Write modified class
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        
        return WeavingResult.modified(classNode.name, writer.toByteArray(), addedSetters);
    }
    
    /**
     * Finds all fields that have injection annotations.
     */
    private List<FieldNode> findInjectableFields(ClassNode classNode) {
        List<FieldNode> injectableFields = new ArrayList<>();
        
        for (FieldNode field : classNode.fields) {
            if (hasInjectAnnotation(field)) {
                // Only process private fields - public/protected/package-private
                // can use direct PUTFIELD
                if ((field.access & ACC_PRIVATE) != 0) {
                    injectableFields.add(field);
                }
            }
        }
        
        return injectableFields;
    }
    
    /**
     * Checks if a field has any injection annotation.
     */
    private boolean hasInjectAnnotation(FieldNode field) {
        if (field.visibleAnnotations != null) {
            for (AnnotationNode annotation : field.visibleAnnotations) {
                if (INJECT_ANNOTATIONS.contains(annotation.desc)) {
                    return true;
                }
            }
        }
        
        if (field.invisibleAnnotations != null) {
            for (AnnotationNode annotation : field.invisibleAnnotations) {
                if (INJECT_ANNOTATIONS.contains(annotation.desc)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Generates a synthetic setter method for a field.
     * 
     * <pre>
     * public synthetic void __di_set_fieldName(FieldType value) {
     *     this.fieldName = value;
     * }
     * </pre>
     */
    private MethodNode generateSetter(String classInternalName, FieldNode field) {
        String setterName = SETTER_PREFIX + field.name;
        String setterDesc = "(" + field.desc + ")V";
        
        MethodNode setter = new MethodNode(
            ACC_PUBLIC | ACC_SYNTHETIC,  // public synthetic
            setterName,
            setterDesc,
            null,  // no generic signature
            null   // no exceptions
        );
        
        // Generate method body:
        // this.field = value;
        // return;
        
        InsnList instructions = setter.instructions;
        
        // ALOAD 0 - load 'this'
        instructions.add(new VarInsnNode(ALOAD, 0));
        
        // Load the parameter based on its type
        int loadOpcode = getLoadOpcode(field.desc);
        instructions.add(new VarInsnNode(loadOpcode, 1));
        
        // PUTFIELD this.field = value
        instructions.add(new FieldInsnNode(
            PUTFIELD,
            classInternalName,
            field.name,
            field.desc
        ));
        
        // RETURN
        instructions.add(new InsnNode(RETURN));
        
        // Set max stack and locals (will be computed by ClassWriter)
        setter.maxStack = 2;
        setter.maxLocals = 2;
        
        return setter;
    }
    
    /**
     * Gets the appropriate load opcode for a type descriptor.
     */
    private int getLoadOpcode(String desc) {
        if (desc.length() == 1) {
            // Primitive types
            switch (desc.charAt(0)) {
                case 'Z': // boolean
                case 'B': // byte
                case 'C': // char
                case 'S': // short
                case 'I': // int
                    return ILOAD;
                case 'J': // long
                    return LLOAD;
                case 'F': // float
                    return FLOAD;
                case 'D': // double
                    return DLOAD;
                default:
                    return ALOAD;
            }
        }
        // Object or array type
        return ALOAD;
    }
    
    /**
     * Represents the result of weaving a class.
     */
    public static class WeavingResult {
        private final String className;
        private final byte[] bytecode;
        private final List<String> addedSetters;
        private final boolean modified;
        private final String errorMessage;
        
        private WeavingResult(String className, byte[] bytecode, List<String> addedSetters, 
                             boolean modified, String errorMessage) {
            this.className = className;
            this.bytecode = bytecode;
            this.addedSetters = addedSetters != null ? List.copyOf(addedSetters) : List.of();
            this.modified = modified;
            this.errorMessage = errorMessage;
        }
        
        public static WeavingResult unchanged(String className) {
            return new WeavingResult(className, null, null, false, null);
        }
        
        public static WeavingResult modified(String className, byte[] bytecode, List<String> addedSetters) {
            return new WeavingResult(className, bytecode, addedSetters, true, null);
        }
        
        public static WeavingResult error(String className, String errorMessage) {
            return new WeavingResult(className, null, null, false, errorMessage);
        }
        
        public String getClassName() {
            return className;
        }
        
        public byte[] getBytecode() {
            return bytecode;
        }
        
        public List<String> getAddedSetters() {
            return addedSetters;
        }
        
        public boolean wasModified() {
            return modified;
        }
        
        public boolean hasError() {
            return errorMessage != null;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        @Override
        public String toString() {
            if (hasError()) {
                return "WeavingResult[" + className + ", ERROR: " + errorMessage + "]";
            }
            if (modified) {
                return "WeavingResult[" + className + ", setters=" + addedSetters + "]";
            }
            return "WeavingResult[" + className + ", unchanged]";
        }
    }
}
