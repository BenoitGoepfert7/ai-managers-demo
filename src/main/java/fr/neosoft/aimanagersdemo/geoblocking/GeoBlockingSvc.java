package fr.neosoft.aimanagersdemo.geoblocking;

import fr.neosoft.aimanagersdemo.access.Subscriber;
import fr.neosoft.aimanagersdemo.access.persistence.SubscriberRepo;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.Map;
import java.util.Optional;

@Service
public class GeoBlockingSvc {

    private static final Map<String, ContentCategory> CONTENT_CATEGORIES = Map.of(
            "world-cup-final", ContentCategory.SPORT,
            "news-live", ContentCategory.NEWS,
            "action-movie", ContentCategory.MOVIES,
            "late-night", ContentCategory.ADULT);

    /** Minimum age (0 = no limit) per category and country; a missing country means blocked. */
    private static final Map<ContentCategory, Map<String, Integer>> MIN_AGES = Map.of(
            ContentCategory.SPORT, Map.of("FR", 0, "DE", 0),
            ContentCategory.NEWS, Map.of("FR", 0, "DE", 0, "US", 0),
            ContentCategory.MOVIES, Map.of("FR", 12, "DE", 16, "US", 13),
            ContentCategory.ADULT, Map.of("FR", 18, "DE", 18));

    private final GeoIpClient geoIp;
    private final SubscriberRepo subscribers;
    private final Clock clock;

    public GeoBlockingSvc(GeoIpClient geoIp, SubscriberRepo subscribers, Clock clock) {
        this.geoIp = geoIp;
        this.subscribers = subscribers;
        this.clock = clock;
    }

    public GeoDecision check(String contentId, String ip, Optional<String> subscriberId) {
        ContentCategory category = CONTENT_CATEGORIES.get(contentId);
        if (category == null) return GeoDecision.deny(DenialReason.UNKNOWN_CONTENT);
        Integer minAge = MIN_AGES.get(category).get(geoIp.countryOf(ip));
        if (minAge == null) return GeoDecision.deny(DenialReason.COUNTRY_BLOCKED);
        return minAge == 0 ? GeoDecision.allow() : checkAge(minAge, subscriberId);
    }

    private GeoDecision checkAge(int minAge, Optional<String> subscriberId) {
        if (subscriberId.isEmpty()) return GeoDecision.deny(DenialReason.SUBSCRIBER_REQUIRED);
        return subscribers.findById(subscriberId.get())
                .map(s -> ageOf(s) >= minAge ? GeoDecision.allow() : GeoDecision.deny(DenialReason.AGE_RESTRICTED))
                .orElse(GeoDecision.deny(DenialReason.UNKNOWN_SUBSCRIBER));
    }

    private int ageOf(Subscriber subscriber) {
        return Period.between(subscriber.birthDate(), LocalDate.now(clock)).getYears();
    }
}
