package com.perfectspring.topic07_boot.project;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full Spring Boot test of the Course Registration API (Topic 6 skills applied to Topic 7).
 * @DirtiesContext on a test that CHANGES data: "throw this app away afterwards and start a
 * fresh one (with a fresh database)", so the next test isn't affected by the change.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("boot")
class CourseRegistrationApiTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void startupDataIsLoaded() throws Exception {
        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void derivedQueryFindsByTitle() throws Exception {
        mockMvc.perform(get("/courses/search").param("title", "JAVA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("CS101"));
    }

    @Test
    @DirtiesContext
    void missingCapacityUsesTheSchoolDefault() throws Exception {
        mockMvc.perform(post("/courses").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"MTH201\",\"title\":\"Further Maths\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.capacity").value(30));          // from application-boot.properties
    }

    @Test
    void invalidCourseIsRejectedWith400() throws Exception {
        mockMvc.perform(post("/courses").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"\",\"title\":\"\",\"capacity\":500}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.title").value("title is required"))
                .andExpect(jsonPath("$.fields.capacity").value("capacity must be at most 200"));
    }

    @Test
    @DirtiesContext
    void fullCourseGives409() throws Exception {
        mockMvc.perform(post("/courses/3/enroll")).andExpect(status().isOk());
        mockMvc.perform(post("/courses/3/enroll")).andExpect(status().isOk())
                .andExpect(jsonPath("$.seatsLeft").value(0));
        mockMvc.perform(post("/courses/3/enroll")).andExpect(status().isConflict());
    }

    @Test
    void actuatorSaysTheAppIsUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
