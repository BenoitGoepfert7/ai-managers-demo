package fr.neosoft.aimanagersdemo.access.persistence;

import fr.neosoft.aimanagersdemo.access.Subscriber;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Fake in-memory "database" of subscribers. */
@Repository
public class SubscriberRepo {

    private final Map<String, Subscriber> data = new HashMap<>(Map.of(
            "s1", new Subscriber("s1", "Alice", true, Set.of("NEWS", "SPORT", "MOVIES")),
            "s2", new Subscriber("s2", "Bob", true, Set.of("NEWS")),
            "s3", new Subscriber("s3", "Carol", false, Set.of("NEWS", "SPORT"))));

    public Optional<Subscriber> findById(String p_id) {
        return Optional.ofNullable(data.get(p_id));
    }

    public boolean grantChannel(String p_id, String p_channel) {
        Subscriber subscriber = data.get(p_id);
        if (subscriber == null) return false;

        Set<String> channels = new HashSet<>(subscriber.channels());
        channels.add(p_channel.toUpperCase());
        data.put(p_id, new Subscriber(subscriber.id(), subscriber.name(), subscriber.active(), Set.copyOf(channels)));
        return true;
    }
}
