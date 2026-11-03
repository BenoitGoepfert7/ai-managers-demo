package fr.neosoft.aimanagersdemo.geoblocking;

/** Port to an external GeoIP provider. */
public interface GeoIpClient {
    String countryOf(String p_ip);
}
