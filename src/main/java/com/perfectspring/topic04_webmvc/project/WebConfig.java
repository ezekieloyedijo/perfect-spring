package com.perfectspring.topic04_webmvc.project;

import com.perfectspring.common.JsonOnlyWebConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * @EnableWebMvc   -> switch on Spring MVC: request mappings, JSON converters (Jackson), etc.
 * @ComponentScan  -> find the controller, service, repository and error handler in this package.
 * @Import         -> answer in JSON only (see JsonOnlyWebConfig for the reason)
 */
@Configuration
@EnableWebMvc
@ComponentScan
@Import(JsonOnlyWebConfig.class)
public class WebConfig {
}
