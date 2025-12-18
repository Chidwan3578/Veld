/**
 * Veld Spring Boot Example Module.
 * Provides Spring Boot examples for the Veld DI Framework.
 */
module veld.spring.boot.example {
    // Required JDK modules
    requires java.logging;
    
    // Spring Boot modules
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    
    // Required Veld modules
    requires io.github.yasmramos.veld.annotation;
    requires io.github.yasmramos.veld.runtime;
    requires veld.spring.boot.starter;
    
    // Export example packages
    exports io.github.yasmramos.veld.spring.boot.example;
}