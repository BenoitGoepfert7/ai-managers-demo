package fr.neosoft.aimanagersdemo.access;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Fake in-memory "database" of subscribers. */
@Repository
public class SubscriberRepository {

    private final Map<String, Subscriber> data = Map.of(
            "s1", new Subscriber("s1", "Alice", true, Set.of("NEWS", "SPORT", "MOVIES")),
            "s2", new Subscriber("s2", "Bob", true, Set.of("NEWS")),
            "s3", new Subscriber("s3", "Carol", false, Set.of("NEWS", "SPORT")));

    public Optional<Subscriber> findById(String p_id) {
        return Optional.ofNullable(data.get(p_id));
    }
}
