package com.codementor.api.question;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codementor.api.common.ApiExceptionHandler;
import com.codementor.api.config.JwtService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(QuestionController.class)
@Import(ApiExceptionHandler.class)
class QuestionControllerTest {
  @Autowired MockMvc mvc;
  @MockitoBean QuestionService service;
  @MockitoBean JwtService jwtService;
  @MockitoBean UserDetailsService userDetailsService;

  @Test
  void listBindsSearchFiltersSortAndPagination() throws Exception {
    var row = new QuestionDtos.QuestionSummary("LC-1", "two-sum", "Two Sum",
        QuestionDtos.Difficulty.BEGINNER, List.of("Hashing"), "LeetCode #1",
        "https://leetcode.com/problems/two-sum/", QuestionDtos.Status.TODO, false);
    when(service.search(eq("LC-1"), eq("Hashing"), eq("BEGINNER"), eq("difficulty"), eq(2), eq(16)))
        .thenReturn(new QuestionDtos.QuestionPage(List.of(row), 2, 12, 13, 2, false, true));

    mvc.perform(get("/api/questions")
            .param("q", "LC-1").param("topic", "Hashing").param("difficulty", "BEGINNER")
            .param("sort", "difficulty").param("page", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[0].id").value("LC-1"))
        .andExpect(jsonPath("$.items[0].slug").value("two-sum"))
        .andExpect(jsonPath("$.page").value(2))
        .andExpect(jsonPath("$.totalItems").value(13))
        .andExpect(jsonPath("$.hasPrevious").value(true));
  }

  @Test
  void detailRouteUsesStableSlugAndInvalidQuestionsReturnNotFound() throws Exception {
    when(service.get("missing-question")).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));
    mvc.perform(get("/api/questions/missing-question"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Question not found"));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "two-sum", "group-anagrams", "longest-substring-without-repeating-characters",
      "binary-search", "merge-intervals", "subsets", "reverse-linked-list",
      "valid-parentheses", "validate-binary-search-tree", "kth-largest-element-in-an-array",
      "number-of-islands", "jump-game", "coin-change", "implement-trie-prefix-tree",
      "single-number", "happy-number", "counting-bits", "median-of-two-sorted-arrays",
      "regular-expression-matching", "word-search-ii"
  })
  void everySeededSlugResolvesOnItsDetailRoute(String slug) throws Exception {
    when(service.get(slug)).thenReturn(new QuestionDtos.QuestionDetail("LC-1", slug, "Question",
        "Problem statement", List.of(), List.of(), QuestionDtos.Difficulty.BEGINNER,
        List.of("Arrays and Strings"), "LeetCode", "https://leetcode.com/",
        new QuestionDtos.Progress(QuestionDtos.Status.TODO, false, 0), List.of(), 3));

    mvc.perform(get("/api/questions/{slug}", slug))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.slug").value(slug))
        .andExpect(jsonPath("$.description").value("Problem statement"));
  }
}
