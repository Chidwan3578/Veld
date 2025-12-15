package io.github.yasmramos.veld.example.dependsOn;

import io.github.yasmramos.veld.annotation.Component;
import io.github.yasmramos.veld.annotation.PostConstruct;
import io.github.yasmramos.veld.annotation.DependsOn;

/**
 * Servicio de base de datos - componente base sin dependencias.
 * Proporciona conectividad y operaciones de base de datos.
 */
@Component("databaseService")
public class DatabaseService {
    
    private String connectionString = "jdbc:h2:mem:example";
    private boolean connected = false;
    
    @PostConstruct
    public void init() {
        System.out.println("    ✅ DatabaseService inicializado - Conexión de BD establecida");
        System.out.println("       Connection: " + connectionString);
        this.connected = true;
    }
    
    public void executeQuery(String sql) {
        if (connected) {
            System.out.println("       Ejecutando SQL: " + sql);
        } else {
            System.out.println("       ❌ No hay conexión a BD");
        }
    }
    
    public void saveData(String data) {
        if (connected) {
            System.out.println("       Guardando datos: " + data);
        } else {
            System.out.println("       ❌ No hay conexión a BD para guardar");
        }
    }
    
    public boolean isConnected() {
        return connected;
    }
}