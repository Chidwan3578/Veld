package com.veld.example;

import com.veld.annotation.Singleton;
import com.veld.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A simple metrics service.
 * This service is intentionally NOT registered as a component to demonstrate
 * Optional<T> injection.
 * 
 * Uncomment @Singleton to register it as a component.
 */
// @Singleton  // <- Uncomment to make it available
public class MetricsService {
    
    private final ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() {
        System.out.println("  MetricsService initialized");
    }
    
    public void recordEvent(String eventName) {
        counters.computeIfAbsent(eventName, k -> new AtomicLong()).incrementAndGet();
    }
    
    public long getCount(String eventName) {
        AtomicLong counter = counters.get(eventName);
        return counter != null ? counter.get() : 0;
    }
    
    public void printMetrics() {
        System.out.println("Metrics:");
        counters.forEach((name, count) -> 
            System.out.println("  " + name + ": " + count.get()));
    }
}
