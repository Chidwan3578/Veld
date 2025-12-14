/*
 * Copyright 2025 Veld Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
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
 * Clase principal para ejecutar el demo de @DependsOn.
 * 
 * Esta clase demuestra el uso del framework Veld con @DependsOn.
 */
public class Main {
    
    public static void main(String[] args) {
        System.out.println("Starting Veld @DependsOn Demo...");
        
        try {
            // Crear instancia del demo usando Veld.get()
            // (esto funcionará después de que el weaver genere Veld.class)
            DependsOnDemo demo = getDemoInstance();
            
            if (demo != null) {
                // Ejecutar el demo
                demo.runDemo();
                demo.demonstrateDirectAccess();
                
                // Simular trabajo de la aplicación
                Thread.sleep(2000);
                
                System.out.println("\n🏁 Demo completed successfully!");
            } else {
                System.out.println("❌ Could not create demo instance - Veld class not generated yet");
                System.out.println("💡 This is expected during compilation. Run after full build.");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Demo failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Shutdown Veld framework
            try {
                shutdownVeld();
            } catch (Exception e) {
                System.err.println("⚠️ Error during shutdown: " + e.getMessage());
            }
        }
    }
    
    /**
     * Obtiene una instancia del demo usando Veld.get().
     * Returns null si Veld.class no está disponible (durante compilación).
     */
    private static DependsOnDemo getDemoInstance() {
        try {
            // Intentar cargar Veld.class y obtener el demo
            Class<?> veldClass = Class.forName("com.veld.Veld");
            
            // Usar reflexión para llamar a Veld.get(DependsOnDemo.class)
            java.lang.reflect.Method getMethod = veldClass.getMethod("get", Class.class);
            Object demo = getMethod.invoke(null, DependsOnDemo.class);
            
            return (DependsOnDemo) demo;
            
        } catch (ClassNotFoundException e) {
            // Veld.class no está disponible - esto es normal durante compilación
            return null;
        } catch (Exception e) {
            System.err.println("Error getting demo instance: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Hace shutdown del framework Veld.
     */
    private static void shutdownVeld() {
        try {
            Class<?> veldClass = Class.forName("com.veld.Veld");
            java.lang.reflect.Method shutdownMethod = veldClass.getMethod("shutdown");
            shutdownMethod.invoke(null);
            System.out.println("✅ Veld framework shutdown completed");
        } catch (ClassNotFoundException e) {
            // Veld class no disponible - no hacer nada
        } catch (Exception e) {
            System.err.println("⚠️ Error during Veld shutdown: " + e.getMessage());
        }
    }
}