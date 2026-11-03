package fr.neosoft.aimanagersdemo.access;

import java.time.LocalDate;
import java.util.Set;

public record Subscriber(String id, String name, boolean active, LocalDate birthDate, Set<String> channels) {
}
