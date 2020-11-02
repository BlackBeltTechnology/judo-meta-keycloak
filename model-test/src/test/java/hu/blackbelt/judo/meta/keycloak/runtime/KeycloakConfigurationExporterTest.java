package hu.blackbelt.judo.meta.keycloak.runtime;

import hu.blackbelt.judo.meta.keycloak.Realm;
import hu.blackbelt.judo.meta.keycloak.runtime.exporter.KeycloakConfigurationExporter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;

import static hu.blackbelt.judo.meta.keycloak.util.builder.KeycloakBuilders.newClientBuilder;
import static hu.blackbelt.judo.meta.keycloak.util.builder.KeycloakBuilders.newRealmBuilder;

@Slf4j
public class KeycloakConfigurationExporterTest {

    private final String temporaryOutputPath = "target/test-classes/sample-keycloak-realm.json";

    @Test
    public void testConfigurationExporter() {
        final Realm realm = newRealmBuilder()
                .withId("sandbox")
                .withRealm("sandbox")
                .withEnabled(true)
                .withLoginWithEmailAllowed(true)
                .withClients(newClientBuilder()
                        .withName("Northwind-Internal")
                        .withClientId("Northwind-Internal")
                        .withEnabled(true)
                        .withDirectAccessGrantsEnabled(true)
                        .withBearerOnly(false)
                        .withPublicClient(false)
                        .withRedirectUris("http://localhost:8181/api/northwind/Internal/*")
                        .withSecret("apiSecret")
                        .build())
                .withClients(newClientBuilder()
                        .withName("Northwind-External")
                        .withClientId("Northwind-External")
                        .withEnabled(true)
                        .withDirectAccessGrantsEnabled(true)
                        .withBearerOnly(false)
                        .withPublicClient(true)
                        .withRedirectUris("http://localhost:8181/api/northwind/External/*")
                        .build())
                .build();

        final KeycloakConfigurationExporter exporter = new KeycloakConfigurationExporter(realm);
        final String json = exporter.getConfigurationAsString();
        log.info("Keycloak JSON configuration: {}", json);

        exporter.writeConfigurationToFile(new File(temporaryOutputPath));
    }
}
