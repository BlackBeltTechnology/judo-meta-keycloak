package hu.blackbelt.judo.meta.keycloak.runtime;

/*-
 * #%L
 * Judo :: Keycloak :: Model :: Test
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

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
