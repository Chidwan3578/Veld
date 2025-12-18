/**
 * Veld Benchmark Module.
 * Provides benchmark tests for the Veld DI Framework.
 */
module veld.benchmark {
    // Required JDK modules
    requires java.logging;
    
    // JMH benchmark framework
    requires org.openjdk.jmh;
    requires jdk.unsupported;
    
    // Required Veld modules
    requires io.github.yasmramos.veld.annotation;
    requires io.github.yasmramos.veld.runtime;
    requires io.github.yasmramos.veld.aop;
    requires io.github.yasmramos.veld.processor;
    
    // Export benchmark packages
    exports io.github.yasmramos.veld.benchmark;
}