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
 * UserRepository - depende de que DatabaseMigrator esté completo.
 * 
 * Este bean demuestra el uso de @DependsOn para declarar dependencias
 * explícitas que no requieren inyección directa.
 */
@Singleton
@DependsOn("databaseMigrator")  // ¡Dependencia explícita!
@PostConstruct
public class UserRepository {
    
    private final DatabaseMigrator databaseMigrator;
    
    // Constructor con inyección (también dependencia implícita)
    @Inject
    public UserRepository(DatabaseMigrator databaseMigrator) {
        this.databaseMigrator = databaseMigrator;
        System.out.println("[UserRepository] Constructor called with DatabaseMigrator");
    }
    
    @PostConstruct
    public void initializeRepository() {
        System.out.println("[UserRepository] ✅ Initializing user repository");
        
        // Verificar que la base de datos está lista
        if (databaseMigrator.isDatabaseReady()) {
            System.out.println("[UserRepository] ✅ Database is ready - repository can operate safely");
        } else {
            System.out.println("[UserRepository] ❌ Database is NOT ready - this shouldn't happen!");
        }
    }
    
    public DatabaseMigrator getDatabaseMigrator() {
        return databaseMigrator;
    }
    
    public void saveUser(String user) {
        System.out.println("[UserRepository] 💾 Saving user to database: " + user);
        // Simular operación de base de datos
    }
    
    public void findUsers() {
        System.out.println("[UserRepository] 🔍 Finding users in database");
        // Simular consulta de base de datos
    }
}