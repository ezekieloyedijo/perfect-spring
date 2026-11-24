package com.perfectspring.common;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * A small helper shared by the whole course project (not a lesson topic itself).
 *
 * WHY IT EXISTS:
 *   The Eureka libraries (Topic 8) bring an XML converter onto the classpath. Browsers say
 *   "I accept XML" in their requests, so without this class every app would answer in XML
 *   instead of JSON. Here we remove the XML converter from Spring MVC, so all apps speak JSON.
 *
 * HOW IT IS SWITCHED ON:
 *   - Spring Boot apps (Topics 5-8): it is registered as OUR OWN auto-configuration in
 *     src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
 *     That's exactly how Spring Boot's own auto-configurations are found (Topic 7, Concept 4)!
 *   - Plain Spring MVC (Topic 4): imported by hand with @Import(JsonOnlyWebConfig.class).
 */
@AutoConfiguration
public class JsonOnlyWebConfig implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.removeIf(converter -> converter instanceof MappingJackson2XmlHttpMessageConverter);
    }
}
