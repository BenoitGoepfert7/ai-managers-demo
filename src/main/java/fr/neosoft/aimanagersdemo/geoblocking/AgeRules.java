package fr.neosoft.aimanagersdemo.geoblocking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.OptionalInt;

/** Minimum subscriber age per (category, country). */
@Component
public class AgeRules {

    private record Key(String category, String country) {
    }

    private final Map<Key, Integer> minimumAges;

    @Autowired
    public AgeRules() {
        this(Map.of(
                new Key("LATE_NIGHT_MOVIE", "US"), 21,
                new Key("LATE_NIGHT_MOVIE", "FR"), 18));
    }

    private AgeRules(Map<Key, Integer> p_minimumAges) {
        this.minimumAges = p_minimumAges;
    }

    /** Builds a rule set from "category:country" keys, e.g. "LATE_NIGHT_MOVIE:FR" -> 16. */
    public static AgeRules of(Map<String, Integer> p_rules) {
        var rules = new java.util.HashMap<Key, Integer>();
        p_rules.forEach((k, v) -> {
            var parts = k.split(":");
            rules.put(new Key(parts[0], parts[1]), v);
        });
        return new AgeRules(Map.copyOf(rules));
    }

    public OptionalInt minimumAge(String p_category, String p_country) {
        Integer age = minimumAges.get(new Key(p_category, p_country));
        return age == null ? OptionalInt.empty() : OptionalInt.of(age);
    }
}
