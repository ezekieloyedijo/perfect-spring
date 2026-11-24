package com.perfectspring.topic04_webmvc.server;

import jakarta.servlet.http.HttpServlet;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.nio.file.Files;

/**
 * Starts an embedded TOMCAT web server by hand.
 *
 * Story: Tomcat is the school BUILDING with doors open to visitors.
 *        We put ONE receptionist (a servlet) at the front desk.
 *
 * In Topic 7, Spring Boot does all of this for you automatically.
 */
public final class TomcatLauncher {

    private TomcatLauncher() {
    }

    /** Spring MVC way: create Spring's container + DispatcherServlet and plug it into Tomcat. */
    public static void startSpringMvc(int port, Class<?> springConfigClass) throws Exception {
        // 1. Create Spring's container for a web app, and give it our configuration.
        AnnotationConfigWebApplicationContext springContainer = new AnnotationConfigWebApplicationContext();
        springContainer.register(springConfigClass);

        // 2. Hire the receptionist: the DispatcherServlet, connected to the container.
        DispatcherServlet dispatcherServlet = new DispatcherServlet(springContainer);

        // 3. Seat her at the front desk for EVERY URL ("/").
        start(port, dispatcherServlet);
    }

    /** Plain way: put any servlet at the front desk (used in Example 1). */
    public static void start(int port, HttpServlet frontDeskServlet) throws Exception {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.setBaseDir(Files.createTempDirectory("tomcat-" + port).toString());
        tomcat.getConnector();                               // open the door (HTTP connector)

        Context webApp = tomcat.addContext("", null);        // the web app lives at "/"
        Tomcat.addServlet(webApp, "frontDesk", frontDeskServlet).setLoadOnStartup(1);
        webApp.addServletMappingDecoded("/", "frontDesk");   // every request goes to the front desk

        tomcat.start();
        System.out.println();
        System.out.println("=====================================================");
        System.out.println(" Server running at http://localhost:" + port);
        System.out.println(" Press Ctrl + C to stop.");
        System.out.println("=====================================================");
        tomcat.getServer().await();                          // keep running and wait for visitors
    }
}
