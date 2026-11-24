package com.perfectspring.topic08_cloud.project.enrollment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * THE SPEED-DIAL BUTTON. Just an interface: Spring Cloud OpenFeign writes the HTTP code.
 *
 * name = "catalog-service"   -> look this NAME up in Eureka (no address anywhere!) + load-balance
 * dismiss404 = true          -> if the catalog answers 404 "no such course", return null
 *                               (a normal answer, NOT a failure, so the circuit breaker isn't fooled)
 * fallback = ...             -> Plan B when the catalog is down or too slow (circuit breaker)
 */
@FeignClient(name = "catalog-service", dismiss404 = true, fallback = CatalogClientFallback.class)
public interface CatalogClient {

    @GetMapping("/catalog/courses/{code}")              // the same annotations you used in controllers
    CatalogCourse findCourse(@PathVariable("code") String code);
}
