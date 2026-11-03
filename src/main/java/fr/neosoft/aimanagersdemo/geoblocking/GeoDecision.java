package fr.neosoft.aimanagersdemo.geoblocking;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeoDecision(boolean allowed, DenialReason reason) {

    public static GeoDecision allow() {
        return new GeoDecision(true, null);
    }

    public static GeoDecision deny(DenialReason reason) {
        return new GeoDecision(false, reason);
    }
}
