package fr.neosoft.aimanagersdemo.geoblocking.client;

import fr.neosoft.aimanagersdemo.geoblocking.GeoIpClient;
import org.springframework.stereotype.Component;

import java.util.Map;

/** Mock of the external GeoIP API so the demo runs standalone. */
@Component
public class GeoIpClientMock implements GeoIpClient {

    private final Map<String, String> countries = Map.of(
            "81.2.69.1", "FR",
            "8.8.8.8", "US",
            "5.5.5.5", "DE");

    @Override
    public String countryOf(String p_ip) {
        String country = countries.get(p_ip);
        if (country == null) throw new IllegalArgumentException("GeoIP lookup failed for " + p_ip);
        return country;
    }
}
