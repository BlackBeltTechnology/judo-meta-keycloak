package hu.blackbelt.judo.meta.keycloak.runtime;

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
