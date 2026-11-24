package com.perfectspring.topic08_cloud.project.enrollment;

/**
 * The enrollment service's OWN copy of the shape of the catalog's JSON.
 * (Services don't share Java classes: they only agree on the JSON "contract".)
 */
public record CatalogCourse(String code, String title, String teacher, int servedByCatalogPort) {
}
