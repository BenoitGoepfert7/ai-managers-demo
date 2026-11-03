package fr.neosoft.aimanagersdemo.geoblocking;

import fr.neosoft.aimanagersdemo.access.persistence.SubscriberRepo;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class GeoBlockingSvc {

    public enum Decision { ALLOWED, COUNTRY_BLOCKED, CATEGORY_BLOCKED, AGE_RESTRICTED, UNKNOWN_SUBSCRIBER }

    private final GeoIpClient geoIp;
    private final SubscriberRepo subscribers;
    private final Clock clock;

    private final Map<String, Content> catalog = Map.of(
            "world-cup-final", new Content("world-cup-final", "SPORT", Set.of("FR", "DE")),
            "news-live", new Content("news-live", "NEWS", Set.of()),
            "late-night-movie", new Content("late-night-movie", "ADULT", Set.of()));

    private final Map<String, CountryRule> rules = Map.of(
            "FR", new CountryRule(Set.of("SPORT", "NEWS", "ADULT"), Map.of("ADULT", 18)),
            "DE", new CountryRule(Set.of("SPORT", "NEWS"), Map.of()),
            "US", new CountryRule(Set.of("NEWS", "ADULT"), Map.of("ADULT", 21)));

    public GeoBlockingSvc(GeoIpClient p_geoIp, SubscriberRepo p_subscribers, Clock p_clock) {
        this.geoIp = p_geoIp;
        this.subscribers = p_subscribers;
        this.clock = p_clock;
    }

    /** @param p_subscriberId optional; without it age-restricted content is blocked */
    public Decision check(String p_contentId, String p_ip, String p_subscriberId) {
        String country = geoIp.countryOf(p_ip);

        Optional<Integer> age = Optional.empty();
        if (p_subscriberId != null) {
            var subscriber = subscribers.findById(p_subscriberId);
            if (subscriber.isEmpty()) return Decision.UNKNOWN_SUBSCRIBER;
            age = Optional.of(Period.between(subscriber.get().birthDate(), LocalDate.now(clock)).getYears());
        }

        Content content = catalog.get(p_contentId);
        CountryRule rule = rules.get(country);
        if (content == null || rule == null) return Decision.COUNTRY_BLOCKED;
        if (!content.countries().isEmpty() && !content.countries().contains(country)) return Decision.COUNTRY_BLOCKED;
        if (!rule.allowedCategories().contains(content.category())) return Decision.CATEGORY_BLOCKED;

        Integer minAge = rule.minAgeByCategory().get(content.category());
        if (minAge != null && age.map(a -> a < minAge).orElse(true)) return Decision.AGE_RESTRICTED;
        return Decision.ALLOWED;
    }

    public boolean isAllowed(String contentId, String ip) {
        return check(contentId, ip, null) == Decision.ALLOWED;
    }
}
