/*
 * Copyright 2025 Veld Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this be used except in compliance with the License.
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
 * ConfigService - servicio de configuración.
 */
@Singleton
@PostConstruct
public class ConfigService {
    
    private String appName = "Veld DependsOn Demo";
    private int maxConnections = 100;
    private boolean debugMode = false;
    
    public ConfigService() {
        System.out.println("[ConfigService] Constructor called");
    }
    
    @PostConstruct
    public void loadConfiguration() {
        System.out.println("[ConfigService] ✅ Loading configuration");
        System.out.println("[ConfigService] ✅ Configuration loaded: " + getAppName());
    }
    
    public String getAppName() {
        return appName;
    }
    
    public void setAppName(String appName) {
        this.appName = appName;
    }
    
    public int getMaxConnections() {
        return maxConnections;
    }
    
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }
    
    public boolean isDebugMode() {
        return debugMode;
    }
    
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }
    
    @Override
    public String toString() {
        return "ConfigService{appName='" + appName + "', maxConnections=" + maxConnections + ", debugMode=" + debugMode + "}";
    }
}