package com.perfectspring.topic06_testing.project;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * WEB SLICE TEST (middle of the pyramid)
 *
 * @WebMvcTest starts ONLY the web layer: DispatcherServlet, our controller, JSON conversion.
 * GradingService is NOT created, so we put a mock into the container with @MockitoBean.
 * MockMvc = a pretend visitor. No real Tomcat, no real port.
 */
@WebMvcTest(ResultController.class)
class ResultControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    GradingService gradingService;

    @Test
    void returnsTheResultCardAsJson() throws Exception {
        when(gradingService.resultFor(1L)).thenReturn(new ResultCard(1L, "Ada", 85.0, "A", true));

        mockMvc.perform(get("/results/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ada"))
                .andExpect(jsonPath("$.average").value(85.0))
                .andExpect(jsonPath("$.grade").value("A"))
                .andExpect(jsonPath("$.passed").value(true));
    }

    @Test
    void textInsteadOfANumberIsABadRequest() throws Exception {
        mockMvc.perform(get("/results/abc"))
                .andExpect(status().isBadRequest());           // Spring MVC rejects it before our code runs
    }
}
