package com.perfectspring.topic02_core.project;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Compare this with Topic 1's AppConfig: NO @Bean methods at all!
 *
 * @ComponentScan   -> "HR, walk through this package and register every class wearing a badge"
 * @PropertySource  -> "pin library.properties on the notice board so @Value can read it"
 */
@Configuration
@ComponentScan
@PropertySource("classpath:topic02/library.properties")
public class LibraryConfig {
}
