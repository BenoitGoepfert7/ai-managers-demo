package fr.neosoft.aimanagersdemo.access;

import java.util.Set;

public record Subscriber(String id, String name, boolean active, Set<String> channels) {
}
