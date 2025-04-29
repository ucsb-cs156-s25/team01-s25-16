package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.ArticlesRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Article;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

@WebMvcTest(controllers = ArticlesController.class)
@Import(TestConfig.class)
public class ArticlesControllerTests extends ControllerTestCase {

    @MockBean
    ArticlesRepository articlesRepository;

    @MockBean
    UserRepository userRepository;

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/articles/all"))
            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        mockMvc.perform(get("/api/articles/all"))
            .andExpect(status().is(200));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_articles() throws Exception {
        // arrange
        LocalDateTime dateAdded1 = LocalDateTime.parse("2024-05-01T12:00:00");
        LocalDateTime dateAdded2 = LocalDateTime.parse("2024-06-01T15:30:00");

        Article article1 = Article.builder()
            .title("Test Title 1")
            .url("http://testurl1.com")
            .explanation("Test Explanation 1")
            .email("test1@example.com")
            .dateAdded(dateAdded1)
            .build();

        Article article2 = Article.builder()
            .title("Test Title 2")
            .url("http://testurl2.com")
            .explanation("Test Explanation 2")
            .email("test2@example.com")
            .dateAdded(dateAdded2)
            .build();

        ArrayList<Article> expectedArticles = new ArrayList<>();
        expectedArticles.addAll(Arrays.asList(article1, article2));

        when(articlesRepository.findAll()).thenReturn(expectedArticles);

        // act
        MvcResult response = mockMvc.perform(get("/api/articles/all"))
            .andExpect(status().isOk())
            .andReturn();

        // assert
        verify(articlesRepository, times(1)).findAll();
        String expectedJson = mapper.writeValueAsString(expectedArticles);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @Test
    public void logged_out_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/articles/post"))
            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/articles/post"))
            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_article() throws Exception {
        LocalDateTime dateAdded = LocalDateTime.parse("2024-05-01T12:00:00");

        Article article = Article.builder()
            .title("Test Title")
            .url("http://testurl.com")
            .explanation("Test Explanation")
            .email("test@example.com")
            .dateAdded(dateAdded)
            .build();

        when(articlesRepository.save(eq(article))).thenReturn(article);

        MvcResult response = mockMvc.perform(
                post("/api/articles/post?title=Test Title&url=http://testurl.com&explanation=Test Explanation&email=test@example.com&dateAdded=2024-05-01T12:00:00")
                        .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

        verify(articlesRepository, times(1)).save(article);
        String expectedJson = mapper.writeValueAsString(article);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }
}
