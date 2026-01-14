package cinema.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProgramWorkflowTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    @Test
    void fullWorkflow_autoRejectWhenEnteringDecision() throws Exception {
        String createProgramJson = """
                {
                  \"name\": \"Test Program\",
                  \"description\": \"desc\",
                  \"startDate\": \"%s\",
                  \"endDate\": \"%s\"
                }
                """.formatted(LocalDate.now(), LocalDate.now().plusDays(2));

        String programBody = mvc.perform(post("/api/programs")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("user1", "user1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createProgramJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long programId = om.readTree(programBody).get("id").asLong();

        mvc.perform(post("/api/programs/" + programId + "/staff/staff1")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("user1", "user1")))
                .andExpect(status().isOk());

        mvc.perform(post("/api/programs/" + programId + "/state")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("user1", "user1"))
                        .param("newState", "SUBMISSION"))
                .andExpect(status().isOk());

        LocalDateTime st = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime et = st.plusMinutes(100);
        String createScreeningJson = """
                {
                  \"filmTitle\": \"Movie A\",
                  \"filmCast\": \"Cast\",
                  \"filmGenres\": \"Drama\",
                  \"filmDurationMinutes\": 90,
                  \"auditoriumName\": \"Hall 1\",
                  \"startTime\": \"%s\",
                  \"endTime\": \"%s\"
                }
                """.formatted(st, et);

        String screeningBody = mvc.perform(post("/api/programs/" + programId + "/screenings")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("submitter", "submitter"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createScreeningJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long screeningId = om.readTree(screeningBody).get("id").asLong();

        mvc.perform(post("/api/programs/" + programId + "/screenings/" + screeningId + "/submit")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("submitter", "submitter")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("SUBMITTED"));

        mvc.perform(post("/api/programs/" + programId + "/state")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("user1", "user1"))
                        .param("newState", "ASSIGNMENT"))
                .andExpect(status().isOk());

        mvc.perform(post("/api/programs/" + programId + "/screenings/" + screeningId + "/assign-handler")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("user1", "user1"))
                        .param("username", "staff1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.handlerUsername").value("staff1"));

        mvc.perform(post("/api/programs/" + programId + "/state")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("user1", "user1"))
                        .param("newState", "REVIEW"))
                .andExpect(status().isOk());

        mvc.perform(post("/api/programs/" + programId + "/screenings/" + screeningId + "/review")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("staff1", "staff1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\": 8, \"comments\": \"ok\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("REVIEWED"));

        mvc.perform(post("/api/programs/" + programId + "/state")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("user1", "user1"))
                        .param("newState", "SCHEDULING"))
                .andExpect(status().isOk());

        mvc.perform(post("/api/programs/" + programId + "/screenings/" + screeningId + "/approve")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("user1", "user1"))
                        .param("notes", "conditional"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("APPROVED"));

        mvc.perform(post("/api/programs/" + programId + "/state")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("user1", "user1"))
                        .param("newState", "FINAL_PUBLICATION"))
                .andExpect(status().isOk());

        mvc.perform(post("/api/programs/" + programId + "/state")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("user1", "user1"))
                        .param("newState", "DECISION"))
                .andExpect(status().isOk());

        String details = mvc.perform(get("/api/programs/" + programId + "/screenings/" + screeningId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("user1", "user1")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode node = om.readTree(details);
        org.junit.jupiter.api.Assertions.assertEquals("REJECTED", node.get("state").asText());
        org.junit.jupiter.api.Assertions.assertTrue(node.get("rejectionReason").asText().toLowerCase().contains("auto"));
    }

    @Test
    void cannotEnterReviewWhenUnassignedSubmittedExists() throws Exception {
        String createProgramJson = """
                {
                  \"name\": \"Test Program 2\",
                  \"description\": \"desc\",
                  \"startDate\": \"%s\",
                  \"endDate\": \"%s\"
                }
                """.formatted(LocalDate.now(), LocalDate.now().plusDays(2));

        String programBody = mvc.perform(post("/api/programs")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("user1", "user1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createProgramJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long programId = om.readTree(programBody).get("id").asLong();

        mvc.perform(post("/api/programs/" + programId + "/staff/staff1")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("user1", "user1")))
                .andExpect(status().isOk());

        mvc.perform(post("/api/programs/" + programId + "/state")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("user1", "user1"))
                        .param("newState", "SUBMISSION"))
                .andExpect(status().isOk());

        LocalDateTime st = LocalDateTime.now().plusDays(1).withHour(12).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime et = st.plusMinutes(100);
        String createScreeningJson = """
                {
                  \"filmTitle\": \"Movie B\",
                  \"filmCast\": \"Cast\",
                  \"filmGenres\": \"Comedy\",
                  \"filmDurationMinutes\": 90,
                  \"auditoriumName\": \"Hall 1\",
                  \"startTime\": \"%s\",
                  \"endTime\": \"%s\"
                }
                """.formatted(st, et);

        String screeningBody = mvc.perform(post("/api/programs/" + programId + "/screenings")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("submitter", "submitter"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createScreeningJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long screeningId = om.readTree(screeningBody).get("id").asLong();

        mvc.perform(post("/api/programs/" + programId + "/screenings/" + screeningId + "/submit")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("submitter", "submitter")))
                .andExpect(status().isOk());

        mvc.perform(post("/api/programs/" + programId + "/state")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("user1", "user1"))
                        .param("newState", "ASSIGNMENT"))
                .andExpect(status().isOk());

        mvc.perform(post("/api/programs/" + programId + "/state")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("user1", "user1"))
                        .param("newState", "REVIEW"))
                .andExpect(status().isConflict());
    }
}
