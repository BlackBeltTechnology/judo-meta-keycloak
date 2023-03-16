package hu.blackbelt.judo.meta.keycloak.runtime.exporter;

/*-
 * #%L
 * Judo :: Keycloak :: Model :: Eclipse plugin
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.blackbelt.judo.meta.keycloak.Client;
import hu.blackbelt.judo.meta.keycloak.Realm;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class KeycloakConfigurationExporter {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Realm realm;

    public KeycloakConfigurationExporter(final Realm realm) {
        this.realm = realm;
    }

    public String getConfigurationAsString() {
        try {
            return objectMapper.writeValueAsString(realmToMap(realm));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to writer Keycloak configuration as JSON", ex);
        }
    }

    public void writeConfigurationToFile(final File file) {
        try {
            objectMapper.writeValue(file, realmToMap(realm));
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to writer Keycloak configuration to file", ex);
        }
    }

    public void writeConfigurationToStream(final OutputStream outputStream) {
        try {
            objectMapper.writeValue(outputStream, realmToMap(realm));
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to writer Keycloak configuration to output stream", ex);
        }
    }

    private static Map<String, Object> realmToMap(final Realm realm) {
        final Map<String, Object> map = new TreeMap<>();

        if (realm.getId() != null) {
            map.put("id", realm.getId());
        }
        if (realm.getRealm() != null) {
            map.put("realm", realm.getRealm());
        }
        if (realm.getEnabled() != null) {
            map.put("enabled", realm.getEnabled());
        }
        if (realm.getLoginWithEmailAllowed() != null) {
            map.put("loginWithEmailAllowed", realm.getLoginWithEmailAllowed());
        }

        if (!realm.getClients().isEmpty()) {
            map.put("clients", realm.getClients().stream().map(c -> clientToMap(c)).collect(Collectors.toList()));
        }

        return map;
    }

    private static Map<String, Object> clientToMap(final Client client) {
        final Map<String, Object> map = new TreeMap<>();

        if (client.getId() != null) {
            map.put("id", client.getId());
        }
        if (client.getClientId() != null) {
            map.put("clientId", client.getClientId());
        }
        if (client.getName() != null) {
            map.put("name", client.getName());
        }
        if (client.getEnabled() != null) {
            map.put("enabled", client.getEnabled());
        }
        if (!client.getRedirectUris().isEmpty()) {
            map.putIfAbsent("redirectUris", client.getRedirectUris());
        }
        if (client.getBearerOnly() != null) {
            map.put("bearerOnly", client.getBearerOnly());
        }
        if (client.getPublicClient() != null) {
            map.put("publicClient", client.getPublicClient());
        }
        if (client.getDirectAccessGrantsEnabled() != null) {
            map.put("directAccessGrantsEnabled", client.getDirectAccessGrantsEnabled());
        }
        if (client.getSecret() != null) {
            map.put("secret", client.getSecret());
        }

        return map;
    }
}
