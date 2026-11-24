package com.perfectspring.topic04_webmvc.project;

import com.perfectspring.topic04_webmvc.server.TomcatLauncher;

/**
 * PROJECT 4 — Homework Tracker REST API
 *
 * Run me, then open http://localhost:8084/homework in your browser,
 * or use the requests in topic04_webmvc/homework-api.http
 */
public class HomeworkApp {

    public static void main(String[] args) throws Exception {
        TomcatLauncher.startSpringMvc(8084, WebConfig.class);
    }
}
