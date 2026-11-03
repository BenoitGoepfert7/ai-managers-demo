package fr.neosoft.aimanagersdemo.geoblocking;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class GeoBlockingService {

    private final GeoIpClient geoIp;
    private final Map<String, Set<String>> allowedCountries = Map.of(
            "world-cup-final", Set.of("FR", "DE"),
            "news-live", Set.of("FR", "DE", "US"));

    public GeoBlockingService(GeoIpClient p_geoIp) {
        this.geoIp = p_geoIp;
    }

    public boolean isAllowed(String p_contentId, String p_ip) {
        return allowedCountries.getOrDefault(p_contentId, Set.of()).contains(geoIp.countryOf(p_ip));
    }
}
