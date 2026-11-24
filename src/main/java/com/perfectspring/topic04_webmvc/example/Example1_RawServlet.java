package com.perfectspring.topic04_webmvc.example;

import com.perfectspring.topic04_webmvc.server.TomcatLauncher;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * EXAMPLE 1 — A RAW SERVLET (no Spring MVC): the painful way
 *
 * Try in your browser:
 *   http://localhost:8041/hello?name=Ada
 *   http://localhost:8041/students/1
 *   http://localhost:8041/students/abc     (we must handle bad input ourselves)
 *
 * Look at how much plumbing WE must write: routing, parsing, JSON, status codes.
 */
public class Example1_RawServlet {

    private static final Map<Long, String> STUDENTS = Map.of(1L, "Ada", 2L, "Tunde");

    static class FrontDeskServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
            String path = request.getRequestURI();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            // ROUTING BY HAND: a growing if/else chain
            if (path.equals("/hello")) {
                String name = request.getParameter("name");                    // reading input by hand
                if (name == null) name = "stranger";
                response.getWriter().write("{\"message\":\"Hello, " + name + "!\"}");   // JSON by hand

            } else if (path.startsWith("/students/")) {
                String idText = path.substring("/students/".length());         // cutting the URL by hand
                try {
                    long id = Long.parseLong(idText);                           // converting by hand
                    String name = STUDENTS.get(id);
                    if (name == null) {
                        response.setStatus(404);                                // status codes by hand
                        response.getWriter().write("{\"error\":\"Student not found\"}");
                    } else {
                        response.getWriter().write("{\"id\":" + id + ",\"name\":\"" + name + "\"}");
                    }
                } catch (NumberFormatException e) {
                    response.setStatus(400);
                    response.getWriter().write("{\"error\":\"id must be a number\"}");
                }

            } else {
                response.setStatus(404);
                response.getWriter().write("{\"error\":\"No such page\"}");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        TomcatLauncher.start(8041, new FrontDeskServlet());
    }
}
