package fr.neosoft.aimanagersdemo.geoblocking;

import fr.neosoft.aimanagersdemo.access.Subscriber;
import fr.neosoft.aimanagersdemo.access.persistence.SubscriberRepo;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static fr.neosoft.aimanagersdemo.geoblocking.GeoBlockingSvc.Decision.AGE_RESTRICTED;
import static fr.neosoft.aimanagersdemo.geoblocking.GeoBlockingSvc.Decision.AGE_UNKNOWN;
import static fr.neosoft.aimanagersdemo.geoblocking.GeoBlockingSvc.Decision.ALLOWED;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AgeRulesTest {

    static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-03T12:00:00Z"), ZoneOffset.UTC);
    static final String MOVIE = "late-night-movie-1";

    @Test
    void initialRules() {
        var rules = new AgeRules();
        assertEquals(21, rules.minimumAge("LATE_NIGHT_MOVIE", "US").getAsInt());
        assertEquals(18, rules.minimumAge("LATE_NIGHT_MOVIE", "FR").getAsInt());
        assertEquals(true, rules.minimumAge("LATE_NIGHT_MOVIE", "DE").isEmpty());
        assertEquals(true, rules.minimumAge("OTHER", "US").isEmpty());
    }

    private GeoBlockingSvc svc(AgeRules rules, LocalDate dob) {
        var repo = new SubscriberRepo() {
            @Override
            public Optional<Subscriber> findById(String id) {
                return Optional.of(new Subscriber(id, "T", true, Set.of(), dob));
            }
        };
        GeoIpClient geo = ip -> ip;
        return new GeoBlockingSvc(geo, new ContentCatalog(), rules, repo, CLOCK);
    }

    @Test
    void usBoundaryAt21() {
        var rules = new AgeRules();
        assertEquals(AGE_RESTRICTED, svc(rules, LocalDate.of(2005, 11, 4)).check(MOVIE, "US", "x")); // 20 y, turns 21 tomorrow
        assertEquals(ALLOWED, svc(rules, LocalDate.of(2005, 11, 3)).check(MOVIE, "US", "x")); // turns 21 today
    }

    @Test
    void franceBoundaryAt18() {
        var rules = new AgeRules();
        assertEquals(AGE_RESTRICTED, svc(rules, LocalDate.of(2008, 11, 4)).check(MOVIE, "FR", "x"));
        assertEquals(ALLOWED, svc(rules, LocalDate.of(2008, 11, 3)).check(MOVIE, "FR", "x"));
    }

    @Test
    void nullDateOfBirthIsUnknown() {
        assertEquals(AGE_UNKNOWN, svc(new AgeRules(), null).check(MOVIE, "US", "x"));
    }

    @Test
    void changedRuleAffectsOnlyItsPair() {
        var rules = AgeRules.of(Map.of("LATE_NIGHT_MOVIE:FR", 16, "LATE_NIGHT_MOVIE:US", 21));
        var seventeen = LocalDate.of(2009, 6, 1);
        assertEquals(ALLOWED, svc(rules, seventeen).check(MOVIE, "FR", "x"));
        assertEquals(AGE_RESTRICTED, svc(rules, seventeen).check(MOVIE, "US", "x"));
    }

    @Test
    void categoryWithoutRuleForCountryIsNotRestricted() {
        var rules = AgeRules.of(Map.of("LATE_NIGHT_MOVIE:US", 21));
        assertEquals(ALLOWED, svc(rules, LocalDate.of(2020, 1, 1)).check(MOVIE, "FR", "x"));
    }
}
