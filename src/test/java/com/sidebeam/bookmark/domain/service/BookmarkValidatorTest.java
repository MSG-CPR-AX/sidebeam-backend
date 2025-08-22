package com.sidebeam.bookmark.domain.service;

import com.sidebeam.bookmark.config.property.ValidationProperties;
import com.sidebeam.bookmark.domain.model.Bookmark;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BookmarkValidatorTest {

    private ValidationProperties validationProperties;
    private BookmarkValidator bookmarkValidator;

    @BeforeEach
    void setUp() {
        validationProperties = new ValidationProperties();
        bookmarkValidator = new BookmarkValidator(validationProperties);
    }

    @Test
    void testCheckDuplicateUrls_WhenValidationEnabled_ShouldThrowException() {
        // Given: validation is enabled and strict
        validationProperties.getDuplicate().setEnabled(true);
        validationProperties.getDuplicate().setStrict(true);

        List<Bookmark> bookmarksWithDuplicates = Arrays.asList(
            createBookmark("Bookmark 1", "https://example.com", "path1"),
            createBookmark("Bookmark 2", "https://example.com", "path2") // duplicate URL
        );

        // When & Then: should throw exception
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> bookmarkValidator.checkDuplicateUrls(bookmarksWithDuplicates));
        
        assertTrue(exception.getMessage().contains("중복된 URL이 발견되었습니다"));
    }

    @Test
    void testCheckDuplicateUrls_WhenValidationDisabled_ShouldNotThrowException() {
        // Given: validation is disabled
        validationProperties.getDuplicate().setEnabled(false);

        List<Bookmark> bookmarksWithDuplicates = Arrays.asList(
            createBookmark("Bookmark 1", "https://example.com", "path1"),
            createBookmark("Bookmark 2", "https://example.com", "path2") // duplicate URL
        );

        // When & Then: should not throw exception
        assertDoesNotThrow(() -> bookmarkValidator.checkDuplicateUrls(bookmarksWithDuplicates));
    }

    @Test
    void testCheckDuplicateUrls_WhenStrictModeDisabled_ShouldNotThrowException() {
        // Given: validation is enabled but strict mode is disabled
        validationProperties.getDuplicate().setEnabled(true);
        validationProperties.getDuplicate().setStrict(false);

        List<Bookmark> bookmarksWithDuplicates = Arrays.asList(
            createBookmark("Bookmark 1", "https://example.com", "path1"),
            createBookmark("Bookmark 2", "https://example.com", "path2") // duplicate URL
        );

        // When & Then: should not throw exception (only log warning)
        assertDoesNotThrow(() -> bookmarkValidator.checkDuplicateUrls(bookmarksWithDuplicates));
    }

    @Test
    void testCheckDuplicateUrls_WithNoDuplicates_ShouldNotThrowException() {
        // Given: validation is enabled and strict
        validationProperties.getDuplicate().setEnabled(true);
        validationProperties.getDuplicate().setStrict(true);

        List<Bookmark> bookmarksWithoutDuplicates = Arrays.asList(
            createBookmark("Bookmark 1", "https://example1.com", "path1"),
            createBookmark("Bookmark 2", "https://example2.com", "path2")
        );

        // When & Then: should not throw exception
        assertDoesNotThrow(() -> bookmarkValidator.checkDuplicateUrls(bookmarksWithoutDuplicates));
    }

    private Bookmark createBookmark(String name, String url, String sourcePath) {
        Bookmark bookmark = new Bookmark();
        bookmark.setName(name);
        bookmark.setUrl(url);
        bookmark.setSourcePath(sourcePath);
        bookmark.setDomain("example.com");
        bookmark.setCategory("Test/Category");
        return bookmark;
    }
}