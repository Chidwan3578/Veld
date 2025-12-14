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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CacheManager - gestor de caché simple.
 */
@Singleton
@PostConstruct
public class CacheManager {
    
    private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();
    private final AtomicInteger hitCount = new AtomicInteger(0);
    private final AtomicInteger missCount = new AtomicInteger(0);
    
    public CacheManager() {
        System.out.println("[CacheManager] Constructor called");
    }
    
    @PostConstruct
    public void startCache() {
        System.out.println("[CacheManager] ✅ Starting cache system");
    }
    
    public void put(String key, Object value) {
        cache.put(key, value);
    }
    
    public Object get(String key) {
        Object value = cache.get(key);
        if (value != null) {
            hitCount.incrementAndGet();
        } else {
            missCount.incrementAndGet();
        }
        return value;
    }
    
    public int getHitCount() {
        return hitCount.get();
    }
    
    public int getMissCount() {
        return missCount.get();
    }
    
    public void clear() {
        cache.clear();
        System.out.println("[CacheManager] 🧹 Cache cleared");
    }
}