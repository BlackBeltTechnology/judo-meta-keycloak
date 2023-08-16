package hu.blackbelt.judo.meta.keycloak.runtime;

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

import hu.blackbelt.judo.meta.keycloak.*;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.BasicEList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static hu.blackbelt.judo.meta.keycloak.util.builder.KeycloakBuilders.*;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@Slf4j
public class KeycloakUtilsTest {

    private static final String TARGET_TEST_CLASSES = "target/test-classes";

    private KeycloakModel keycloakModel;
    private KeycloakUtils keycloakUtils;

    @BeforeEach
    public void setup() {
        keycloakModel = KeycloakModel.buildKeycloakModel()
                .name("TestKeycloakModel")
                .build();
               
        keycloakUtils = new KeycloakUtils(keycloakModel.getResourceSet());
    }

    private void saveKeycloakModel(final String testName) {
        try {
            keycloakModel.saveKeycloakModel(KeycloakModel.SaveArguments.keycloakSaveArgumentsBuilder()
                    .file(new File(TARGET_TEST_CLASSES, format("%s-keycloak.xml", testName)))
                    .build());
        } catch (IOException e) {
            log.warn("Unable to save keycloak model");
        } catch (KeycloakModel.KeycloakValidationException e) {
            fail("Keycloak model is not valid", e);
        }
    }

    @Test
    public void testClientCreate() {
        keycloakModel.addContent(
                newRealmBuilder().withClients(newClientBuilder().withClientId("clientId").build()).build()
        );
        
        assertEquals("clientId", keycloakModel.getKeycloakModelResourceSupport().getStreamOfKeycloakClient().findAny().orElseThrow().getClientId());
        


    }

}
