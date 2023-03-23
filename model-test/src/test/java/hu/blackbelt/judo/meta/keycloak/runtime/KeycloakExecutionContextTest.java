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

import com.google.common.collect.ImmutableList;
import hu.blackbelt.judo.meta.keycloak.runtime.KeycloakModel;
import hu.blackbelt.judo.meta.keycloak.support.KeycloakModelResourceSupport;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static hu.blackbelt.judo.meta.keycloak.support.KeycloakModelResourceSupport.keycloakModelResourceSupportBuilder;
import static hu.blackbelt.judo.meta.keycloak.util.builder.KeycloakBuilders.*;

class KeycloakExecutionContextTest {

    @Test
    @DisplayName("Create Keycloak model with builder pattern")
    void testKeycloakReflectiveCreated() throws Exception {

        String createdSourceModelName = "urn:keycloak.judo-meta-keycloak";

        KeycloakModelResourceSupport keycloakModelSupport = keycloakModelResourceSupportBuilder()
                .uri(URI.createFileURI(createdSourceModelName))
                .build();

        // Build model here
    }
}
