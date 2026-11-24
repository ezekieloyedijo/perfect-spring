package com.perfectspring.topic06_testing.project;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * FULL INTEGRATION TEST (higher up the pyramid): the dress rehearsal.
 *
 * @SpringBootTest starts the WHOLE app (real controller + real service + real repository).
 * Nothing is mocked. Slower, but proves all the pieces work together.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ResultCheckerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void adaGetsAnAWithTheRealData() throws Exception {
        mockMvc.perform(get("/results/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ada"))
                .andExpect(jsonPath("$.average").value(85.0))
                .andExpect(jsonPath("$.grade").value("A"));
    }

    @Test
    void musaFails() throws Exception {
        mockMvc.perform(get("/results/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grade").value("F"))
                .andExpect(jsonPath("$.passed").value(false));
    }

    @Test
    void unknownStudentIs404() throws Exception {
        mockMvc.perform(get("/results/999"))
                .andExpect(status().isNotFound());
    }
}
