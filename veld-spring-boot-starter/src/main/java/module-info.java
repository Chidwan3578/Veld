/**
 * Veld Spring Boot Starter Module.
 * Provides Spring Boot auto-configuration for the Veld DI Framework.
 */
module veld.spring.boot.starter {
    // Required JDK modules
    requires java.logging;
    
    // Spring Boot modules
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.core;
    requires spring.context;
    
    // Required Veld modules
    requires io.github.yasmramos.veld.annotation;
    requires io.github.yasmramos.veld.runtime;
    
    // Export starter packages
    exports io.github.yasmramos.veld.spring.boot;
    exports io.github.yasmramos.veld.spring.boot.autoconfigure;
}