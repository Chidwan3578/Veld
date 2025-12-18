/**
 * Veld Example Module.
 * Provides examples for the Veld DI Framework.
 */
module veld.example {
    // Required JDK modules
    requires java.logging;
    
    // Required Veld modules
    requires io.github.yasmramos.veld.annotation;
    requires io.github.yasmramos.veld.runtime;
    requires io.github.yasmramos.veld.aop;
    requires io.github.yasmramos.veld.weaver;
    
    // Export example packages
    exports io.github.yasmramos.veld.example;
}