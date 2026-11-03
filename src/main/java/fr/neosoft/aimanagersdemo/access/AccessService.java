package fr.neosoft.aimanagersdemo.access;

import fr.neosoft.aimanagersdemo.access.persistence.SubscriberRepo;
import org.springframework.stereotype.Service;

@Service
public class AccessService {

    public enum Decision { GRANTED, UNKNOWN_SUBSCRIBER, SUBSCRIPTION_INACTIVE, CHANNEL_NOT_IN_PACKAGE, ACCESS_GRANT_FAILED }

    private final SubscriberRepo repository;

    public AccessService(SubscriberRepo p_repository) {
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

    public Decision grant(String p_subscriberId, String p_channel) {
        return repository.findById(p_subscriberId)
                .map(s -> {
                    if (!s.active()) return Decision.SUBSCRIPTION_INACTIVE;
                    char lastCharacter = p_subscriberId.charAt(p_subscriberId.length() - 1);
                    if (Character.isDigit(lastCharacter) && Character.digit(lastCharacter, 10) % 2 == 0) {
                        return Decision.ACCESS_GRANT_FAILED;
                    }
                    repository.grantChannel(p_subscriberId, p_channel);
                    return Decision.GRANTED;
                })
                .orElse(Decision.UNKNOWN_SUBSCRIBER);
    }
}
