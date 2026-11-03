package fr.neosoft.aimanagersdemo.geoblocking;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/** Content item to category mapping. Items without category are not age-restricted. */
@Component
public class ContentCatalog {

    private final Map<String, String> categories = Map.of("late-night-movie-1", "LATE_NIGHT_MOVIE");

    public Optional<String> categoryOf(String p_contentId) {
        return Optional.ofNullable(categories.get(p_contentId));
    }
}
