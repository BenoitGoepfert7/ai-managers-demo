package fr.neosoft.aimanagersdemo.access;

import org.springframework.stereotype.Service;

@Service
public class AccessService {

    public enum Decision { GRANTED, UNKNOWN_SUBSCRIBER, SUBSCRIPTION_INACTIVE, CHANNEL_NOT_IN_PACKAGE }

    private final SubscriberRepository repository;

    public AccessService(SubscriberRepository repository) {
        this.repository = repository;
    }

    public Decision check(String subscriberId, String channel) {
        return repository.findById(subscriberId)
                .map(s -> {
                    if (!s.active()) return Decision.SUBSCRIPTION_INACTIVE;
                    return s.channels().contains(channel.toUpperCase())
                            ? Decision.GRANTED : Decision.CHANNEL_NOT_IN_PACKAGE;
                })
                .orElse(Decision.UNKNOWN_SUBSCRIBER);
    }
}
