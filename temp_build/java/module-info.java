/**
 * Veld Processor Module.
 * Annotation processor that generates bytecode using ASM.
 */
module io.github.yasmramos.veld.processor {
    // Required JDK modules
    requires java.compiler;
    
    // Required Veld modules
    requires io.github.yasmramos.veld.annotation;
    requires io.github.yasmramos.veld.runtime;
    
    // ASM modules for bytecode generation
    requires org.objectweb.asm;
    
    // Export processor package
    exports io.github.yasmramos.veld.processor;
    
    // Provide annotation processor service
    provides javax.annotation.processing.Processor 
        with io.github.yasmramos.veld.processor.VeldProcessor;
}
