package com.sidebeam.bookmark.domain.service;

import com.sidebeam.bookmark.domain.model.Bookmark;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class BookMarkValidator {

    /**
     * Check for duplicate URLs across all bookmarks and log an error if any are found.
     * This is a validation step to ensure data integrity.
     */
    public void checkDuplicateUrls(List<Bookmark> bookmarks) {
        Map<String, List<Bookmark>> urlMap = new HashMap<>();

        // Group bookmarks by URL
        for (Bookmark bookmark : bookmarks) {
            String url = bookmark.getUrl();
            if (!urlMap.containsKey(url)) {
                urlMap.put(url, new ArrayList<>());
            }
            urlMap.get(url).add(bookmark);
        }

        // Check for duplicates
        boolean hasDuplicates = false;
        StringBuilder errorMessage = new StringBuilder("Duplicate URLs found:\n");

        for (Map.Entry<String, List<Bookmark>> entry : urlMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                hasDuplicates = true;
                errorMessage.append("URL: ").append(entry.getKey()).append("\n");
                for (Bookmark bookmark : entry.getValue()) {
                    errorMessage.append("  - ").append(bookmark.getName())
                            .append(" (").append(bookmark.getSourcePath()).append(")\n");
                }
            }
        }

        if (hasDuplicates) {
            log.error(errorMessage.toString());
            throw new IllegalStateException("Duplicate URLs found in bookmarks. See logs for details.");
        }
    }
}
