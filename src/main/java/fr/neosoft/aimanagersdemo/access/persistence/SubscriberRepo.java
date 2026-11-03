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
            "s1", new Subscriber("s1", "Alice", true, Set.of("NEWS", "SPORT", "MOVIES"), LocalDate.of(1990, 5, 20)),
            "s2", new Subscriber("s2", "Bob", true, Set.of("NEWS"), LocalDate.of(2012, 1, 15)),
            "s3", new Subscriber("s3", "Carol", false, Set.of("NEWS", "SPORT"), LocalDate.of(2008, 11, 4)));

    public Optional<Subscriber> findById(String p_id) {
        return Optional.ofNullable(data.get(p_id));
    }
}
