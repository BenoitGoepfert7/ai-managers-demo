package fr.neosoft.aimanagersdemo.access;

import org.springframework.stereotype.Service;

@Service
public class AccessService {

    public enum Decision { GRANTED, UNKNOWN_SUBSCRIBER, SUBSCRIPTION_INACTIVE, CHANNEL_NOT_IN_PACKAGE }

    private final SubscriberRepository repository;

    public AccessService(SubscriberRepository p_repository) {
        this.repository = p_repository;
    }

    public Decision check(String p_subscriberId, String p_channel) {
        return repository.findById(p_subscriberId)
                .map(s -> {
                    if (!s.active()) return Decision.SUBSCRIPTION_INACTIVE;
                    return s.channels().contains(p_channel.toUpperCase())
                            ? Decision.GRANTED : Decision.CHANNEL_NOT_IN_PACKAGE;
                })
                .orElse(Decision.UNKNOWN_SUBSCRIBER);
    }
}
