package fr.neosoft.aimanagersdemo.geoblocking;

import java.util.Set;

/** Content with its category; an empty country set means no content-specific country restriction. */
public record Content(String id, String category, Set<String> countries) {
}
