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
    @WithMockUser(roles = { "USER" })
@Test
public void test_that_logged_in_user_can_get_by_id_when_article_exists() throws Exception {
    // arrange
    LocalDateTime dateAdded = LocalDateTime.parse("2024-05-01T12:00:00");

    Article article = Article.builder()
        .title("Test Title")
        .url("http://testurl.com")
        .explanation("Test Explanation")
        .email("test@example.com")
        .dateAdded(dateAdded)
        .build();

    when(articlesRepository.findById(123L)).thenReturn(java.util.Optional.of(article));

    // act
    MvcResult response = mockMvc.perform(get("/api/articles?id=123"))
        .andExpect(status().isOk())
        .andReturn();

    // assert
    verify(articlesRepository, times(1)).findById(123L);
    String expectedJson = mapper.writeValueAsString(article);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
}

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_get_by_id_returns_404_when_article_does_not_exist() throws Exception {
        // arrange
        when(articlesRepository.findById(123L)).thenReturn(java.util.Optional.empty());

        // act
        MvcResult response = mockMvc.perform(get("/api/articles?id=123"))
            .andExpect(status().isNotFound())
            .andReturn();

        // assert
        verify(articlesRepository, times(1)).findById(123L);
        String responseString = response.getResponse().getContentAsString();
        Map<String, Object> json = mapper.readValue(responseString, Map.class);
        assertEquals("Article with id 123 not found", json.get("message"));
    }
    @WithMockUser(roles = { "ADMIN", "USER" })
@Test
public void test_that_logged_in_admin_can_update_an_existing_article() throws Exception {
    // arrange
    LocalDateTime dateAdded = LocalDateTime.parse("2024-05-01T12:00:00");

    Article originalArticle = Article.builder()
        .title("Original Title")
        .url("http://original.com")
        .explanation("Original Explanation")
        .email("original@example.com")
        .dateAdded(dateAdded)
        .build();

    Article updatedArticle = Article.builder()
        .title("Updated Title")
        .url("http://updated.com")
        .explanation("Updated Explanation")
        .email("updated@example.com")
        .dateAdded(LocalDateTime.parse("2024-06-01T15:30:00"))
        .build();

    when(articlesRepository.findById(123L)).thenReturn(java.util.Optional.of(originalArticle));

    // act
    MvcResult response = mockMvc.perform(
            put("/api/articles?id=123")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(mapper.writeValueAsString(updatedArticle))
                .with(csrf()))
        .andExpect(status().isOk())
        .andReturn();

    // assert
    verify(articlesRepository, times(1)).findById(123L);
    verify(articlesRepository, times(1)).save(any(Article.class));

    String responseString = response.getResponse().getContentAsString();
    String expectedJson = mapper.writeValueAsString(updatedArticle);
    assertEquals(expectedJson, responseString);
}

@WithMockUser(roles = { "ADMIN", "USER" })
@Test
public void test_that_logged_in_admin_gets_404_when_updating_nonexistent_article() throws Exception {
    // arrange
    when(articlesRepository.findById(123L)).thenReturn(java.util.Optional.empty());

    Article updatedArticle = Article.builder()
        .title("Updated Title")
        .url("http://updated.com")
        .explanation("Updated Explanation")
        .email("updated@example.com")
        .dateAdded(LocalDateTime.parse("2024-05-01T12:00:00"))
        .build();

    // act
    MvcResult response = mockMvc.perform(
            put("/api/articles?id=123")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(mapper.writeValueAsString(updatedArticle))
                .with(csrf()))
        .andExpect(status().isNotFound())
        .andReturn();

    // assert
    verify(articlesRepository, times(1)).findById(123L);

    String responseString = response.getResponse().getContentAsString();
    Map<String, Object> json = mapper.readValue(responseString, Map.class);
    assertEquals("Article with id 123 not found", json.get("message"));
}
@WithMockUser(roles = { "ADMIN", "USER" })
@Test
public void test_that_logged_in_admin_can_delete_an_existing_article() throws Exception {
    // arrange
    LocalDateTime dateAdded = LocalDateTime.parse("2024-05-01T12:00:00");

    Article article = Article.builder()
        .title("Test Title")
        .url("http://testurl.com")
        .explanation("Test Explanation")
        .email("test@example.com")
        .dateAdded(dateAdded)
        .build();

    when(articlesRepository.findById(123L)).thenReturn(java.util.Optional.of(article));

    // act
    MvcResult response = mockMvc.perform(
            delete("/api/articles?id=123")
                .with(csrf()))
        .andExpect(status().isOk())
        .andReturn();

    // assert
    verify(articlesRepository, times(1)).findById(123L);
    verify(articlesRepository, times(1)).delete(article);

    String responseString = response.getResponse().getContentAsString();
    assertEquals("record 123 deleted", responseString);
}

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void test_that_logged_in_admin_gets_404_when_deleting_nonexistent_article() throws Exception {
        // arrange
        when(articlesRepository.findById(123L)).thenReturn(java.util.Optional.empty());

        // act
        MvcResult response = mockMvc.perform(
                delete("/api/articles?id=123")
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

        // assert
        verify(articlesRepository, times(1)).findById(123L);

        String responseString = response.getResponse().getContentAsString();
        Map<String, Object> json = mapper.readValue(responseString, Map.class);
        assertEquals("Article with id 123 not found", json.get("message"));
    }

}
