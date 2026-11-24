package com.perfectspring.topic05_security.project;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * BONUS (Topics 5 + 6): PROVING the security rules with tests.
 * Each test is a visitor at the gate. We check the gate's answer: 200, 401 or 403.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("security")
class SchoolPortalSecurityTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void noticeBoardIsOpenToEveryone() throws Exception {
        mockMvc.perform(get("/public/notice-board"))
                .andExpect(status().isOk());
    }

    @Test
    void strangerIsAskedWhoTheyAre() throws Exception {
        mockMvc.perform(get("/me"))
                .andExpect(status().isUnauthorized());                         // 401
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mockMvc.perform(get("/me").with(httpBasic("ada", "wrong")))
                .andExpect(status().isUnauthorized());                         // 401
    }

    @Test
    void studentSeesOnlyTheirOwnResults() throws Exception {
        mockMvc.perform(get("/results/my").with(httpBasic("ada", "ada123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.student").value("ada"));
    }

    @Test
    void studentCannotOpenAdminArea() throws Exception {
        mockMvc.perform(get("/admin/users").with(httpBasic("ada", "ada123")))
                .andExpect(status().isForbidden());                            // 403
    }

    @Test
    void adminCanOpenAdminArea() throws Exception {
        mockMvc.perform(get("/admin/users").with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk());
    }

    @Test
    void teacherCanRecordAResult() throws Exception {
        mockMvc.perform(post("/results").with(httpBasic("mrbello", "teach123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"student\":\"tunde\",\"subject\":\"Physics\",\"score\":81}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recordedBy").value("mrbello"));
    }

    @Test
    void studentCannotRecordAResult() throws Exception {
        mockMvc.perform(post("/results").with(httpBasic("ada", "ada123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"student\":\"ada\",\"subject\":\"Maths\",\"score\":100}"))
                .andExpect(status().isForbidden());                            // 403
    }
}
