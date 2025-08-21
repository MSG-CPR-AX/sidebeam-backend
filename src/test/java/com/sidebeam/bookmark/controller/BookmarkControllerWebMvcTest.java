package com.sidebeam.bookmark.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sidebeam.bookmark.dto.BookmarkDto;
import com.sidebeam.bookmark.dto.CategoryNodeDto;
import com.sidebeam.bookmark.dto.PackageNodeDto;
import com.sidebeam.bookmark.service.BookmarkService;
import com.sidebeam.common.rest.response.GlobalResponseBodyAdvice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookmarkController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalResponseBodyAdvice.class, BookmarkControllerWebMvcTest.TestConfig.class, com.sidebeam.common.core.exception.GlobalExceptionHandler.class, BookmarkController.class})
class BookmarkControllerWebMvcTest {

    @Configuration
    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookmarkService bookmarkService;

    @Test
    @DisplayName("Given valid bookmarks when GET bookmarks then returns wrapped list")
    void givenValidBookmarks_whenGetBookmarks_thenReturnsWrappedList() throws Exception {
        // Given
        BookmarkDto dto = new BookmarkDto(
                "JetBrains",
                "https://www.jetbrains.com",
                "jetbrains.com",
                "DevTools",
                List.of(new PackageNodeDto("ide", List.of())),
                Map.of("lang", "java"),
                "repo/path/file.yml"
        );
        given(bookmarkService.retrieveAllBookmarks()).willReturn(List.of(dto));

        // When / Then
        mockMvc.perform(get("/bookmarks").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name").value("JetBrains"))
                .andExpect(jsonPath("$.data[0].url").value("https://www.jetbrains.com"));

        verify(bookmarkService).retrieveAllBookmarks();
    }

    @Test
    @DisplayName("Given empty bookmarks when GET bookmarks then returns empty wrapped list")
    void givenEmptyBookmarks_whenGetBookmarks_thenReturnsEmptyWrappedList() throws Exception {
        // Given
        given(bookmarkService.retrieveAllBookmarks()).willReturn(List.of());

        // When / Then
        mockMvc.perform(get("/bookmarks").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(0)));

        verify(bookmarkService).retrieveAllBookmarks();
    }

    @Test
    @DisplayName("Given service exception when GET bookmarks then returns 500 error")
    void givenServiceException_whenGetBookmarks_thenReturns500Error() throws Exception {
        // Given
        given(bookmarkService.retrieveAllBookmarks()).willThrow(new RuntimeException("Service error"));

        // When / Then
        mockMvc.perform(get("/bookmarks").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").exists())
                .andExpect(jsonPath("$.error.message").exists());

        verify(bookmarkService).retrieveAllBookmarks();
    }

    @Test
    @DisplayName("Given valid category tree when GET categories then returns wrapped tree")
    void givenValidCategoryTree_whenGetCategories_thenReturnsWrappedTree() throws Exception {
        // Given
        CategoryNodeDto root = new CategoryNodeDto("root", List.of(new CategoryNodeDto("child", List.of(), 1)), 1);
        given(bookmarkService.retrieveCategoryTree()).willReturn(root);

        // When / Then
        mockMvc.perform(get("/bookmarks/categories").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("root"))
                .andExpect(jsonPath("$.data.children", hasSize(1)))
                .andExpect(jsonPath("$.data.children[0].name").value("child"));

        verify(bookmarkService).retrieveCategoryTree();
    }

    @Test
    @DisplayName("Given service exception when GET categories then returns 500 error")
    void givenServiceException_whenGetCategories_thenReturns500Error() throws Exception {
        // Given
        given(bookmarkService.retrieveCategoryTree()).willThrow(new RuntimeException("Service error"));

        // When / Then
        mockMvc.perform(get("/bookmarks/categories").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").exists())
                .andExpect(jsonPath("$.error.message").exists());

        verify(bookmarkService).retrieveCategoryTree();
    }
}
