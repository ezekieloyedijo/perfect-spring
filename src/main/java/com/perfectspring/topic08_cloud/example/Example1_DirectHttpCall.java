package com.perfectspring.topic08_cloud.example;

import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * EXAMPLE 1 — ONE APP CALLS ANOTHER OVER HTTP, WITH A FIXED ADDRESS
 *
 * First start the catalog service (see topic08 README), then run this.
 *
 * It works... but look at the hard-coded "http://localhost:8081".
 *   - What if the catalog moves to another machine or port?
 *   - What if there are THREE copies of the catalog?
 * That's exactly why we need SERVICE DISCOVERY (Eureka) + OpenFeign.
 */
public class Example1_DirectHttpCall {

    /** The shape of the JSON we expect back. */
    record CatalogCourse(String code, String title, String teacher, int servedByCatalogPort) {
    }

    public static void main(String[] args) {
        RestClient client = RestClient.create();

        try {
            CatalogCourse course = client.get()
                    .uri("http://localhost:8081/catalog/courses/{code}", "CS101")   // the HARD-CODED address
                    .retrieve()
                    .body(CatalogCourse.class);

            System.out.println("The catalog service answered: " + course);
        } catch (RestClientException e) {
            System.out.println("Could not reach http://localhost:8081. Is CatalogServiceApp running?");
            System.out.println("   (" + e.getMessage() + ")");
            System.out.println("This is the problem: one fixed address = one single point of failure.");
        }
    }
}
