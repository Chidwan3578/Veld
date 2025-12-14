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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DatabaseMigrator - debe inicializarse ANTES que cualquier repositorio.
 * 
 * Este bean representa un componente crítico que debe estar listo
 * antes de que otros beans puedan usarlo (ej: UserRepository).
 */
@Singleton
@PostConstruct
public class DatabaseMigrator {
    
    private static final AtomicInteger migrationCount = new AtomicInteger(0);
    
    public DatabaseMigrator() {
        System.out.println("[DatabaseMigrator] Constructor called");
    }
    
    @PostConstruct
    public void runMigrations() {
        int count = migrationCount.incrementAndGet();
        System.out.println("[DatabaseMigrator] ✅ Running database migrations #" + count);
        
        try {
            // Simular tiempo de migración
            Thread.sleep(100);
            System.out.println("[DatabaseMigrator] ✅ Database migrations completed successfully");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Migration interrupted", e);
        }
    }
    
    public static int getMigrationCount() {
        return migrationCount.get();
    }
    
    public boolean isDatabaseReady() {
        return migrationCount.get() > 0;
    }
}