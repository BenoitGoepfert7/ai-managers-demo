package fr.neosoft.aimanagersdemo.access.persistence;

import fr.neosoft.aimanagersdemo.access.Subscriber;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Fake in-memory "database" of subscribers. */
@Repository
public class SubscriberRepo {

    private final Map<String, Subscriber> data = Map.of(
            "s1", new Subscriber("s1", "Alice", true, LocalDate.of(1990, 5, 20), Set.of("NEWS", "SPORT", "MOVIES")),
            "s2", new Subscriber("s2", "Bob", true, LocalDate.of(2012, 3, 10), Set.of("NEWS")),
            "s3", new Subscriber("s3", "Carol", false, LocalDate.of(1985, 11, 2), Set.of("NEWS", "SPORT")),
            "s4", new Subscriber("s4", "Dave", true, LocalDate.of(2007, 6, 15), Set.of("NEWS")));

    public Optional<Subscriber> findById(String p_id) {
        return Optional.ofNullable(data.get(p_id));
    }
}
