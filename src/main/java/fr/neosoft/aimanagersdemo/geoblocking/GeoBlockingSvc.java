package fr.neosoft.aimanagersdemo.geoblocking;

import fr.neosoft.aimanagersdemo.access.persistence.SubscriberRepo;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

@Service
public class GeoBlockingSvc {

    public enum Decision { ALLOWED, COUNTRY_NOT_ALLOWED, AGE_RESTRICTED, AGE_UNKNOWN }

    private final GeoIpClient geoIp;
    private final ContentCatalog catalog;
    private final AgeRules ageRules;
    private final SubscriberRepo subscribers;
    private final Clock clock;
    private final Map<String, Set<String>> allowedCountries = Map.of(
            "world-cup-final", Set.of("FR", "DE"),
            "news-live", Set.of("FR", "DE", "US"),
            "late-night-movie-1", Set.of("FR", "US"));

    public GeoBlockingSvc(GeoIpClient p_geoIp, ContentCatalog p_catalog, AgeRules p_ageRules,
                          SubscriberRepo p_subscribers, Clock p_clock) {
        this.geoIp = p_geoIp;
        this.catalog = p_catalog;
        this.ageRules = p_ageRules;
        this.subscribers = p_subscribers;
        this.clock = p_clock;
    }

    public Decision check(String contentId, String ip, String subscriberId) {
        String country = geoIp.countryOf(ip);
        if (!allowedCountries.getOrDefault(contentId, Set.of()).contains(country)) {
            return Decision.COUNTRY_NOT_ALLOWED;
        }
        var minimumAge = catalog.categoryOf(contentId)
                .map(category -> ageRules.minimumAge(category, country))
                .orElse(OptionalInt.empty());
        if (minimumAge.isEmpty()) return Decision.ALLOWED;

        var dateOfBirth = subscriberId == null ? null
                : subscribers.findById(subscriberId).map(s -> s.dateOfBirth()).orElse(null);
        if (dateOfBirth == null) return Decision.AGE_UNKNOWN;
        int age = Period.between(dateOfBirth, LocalDate.now(clock)).getYears();
        return age >= minimumAge.getAsInt() ? Decision.ALLOWED : Decision.AGE_RESTRICTED;
    }
}
