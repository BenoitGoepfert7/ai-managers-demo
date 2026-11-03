package fr.neosoft.aimanagersdemo.geoblocking;

import java.util.Map;
import java.util.Set;

/** Categories allowed in a country, with an optional minimum age per category. */
public record CountryRule(Set<String> allowedCategories, Map<String, Integer> minAgeByCategory) {
}
