package com.perfectspring.topic08_cloud.project.enrollment;

import org.springframework.stereotype.Component;

/**
 * PLAN B (the vending machine when the canteen is closed).
 *
 * Called by the circuit breaker when the catalog call FAILS (service down, too slow, error),
 * or when the breaker is OPEN and doesn't even try.
 * Instead of crashing, we return a special "unknown" course, and the enrollment becomes PENDING.
 */
@Component
public class CatalogClientFallback implements CatalogClient {

    public static final String UNAVAILABLE = "CATALOG-UNAVAILABLE";

    @Override
    public CatalogCourse findCourse(String code) {
        System.out.println("[enrollment] catalog unavailable -> using FALLBACK for " + code);
        return new CatalogCourse(code, UNAVAILABLE, null, 0);
    }
}
