package ru.itmo.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import ru.itmo.security.user.AppUser;
import ru.itmo.security.user.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class SecureApiApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void protectedEndpointRejectsAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/data"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginReturnsJwtAndAllowsAccess() throws Exception {
        String token = login("student", "StrongPassword!2026");
        mockMvc.perform(get("/api/data").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").isArray());
    }

    @Test
    void sqlInjectionDoesNotBypassLogin() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"' OR 1=1 --","password":"anything"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void noteContentIsEscapedAgainstXss() throws Exception {
        String token = login("student", "StrongPassword!2026");
        mockMvc.perform(post("/api/notes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"<b>Title</b>","content":"<script>alert(1)</script>"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("&lt;b&gt;Title&lt;/b&gt;"))
                .andExpect(jsonPath("$.content")
                        .value("&lt;script&gt;alert(1)&lt;/script&gt;"));
    }

    @Test
    void passwordIsStoredAsBcryptHash() {
        AppUser user = userRepository.findByUsername("student").orElseThrow();
        assertThat(user.getPassword()).isNotEqualTo("StrongPassword!2026");
        assertThat(user.getPassword()).startsWith("$2");
        assertThat(passwordEncoder.matches("StrongPassword!2026", user.getPassword())).isTrue();
    }

    private String login(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new Credentials(username, password))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return json.get("accessToken").asText();
    }

    private record Credentials(String username, String password) {
    }
}
