package hu.blackbelt.judo.meta.keycloak.runtime;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.exceptions.EvlScriptExecutionException;
import hu.blackbelt.epsilon.runtime.execution.impl.BufferedSlf4jLogger;
import hu.blackbelt.judo.meta.keycloak.support.KeycloakModelResourceSupport;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.BeforeEach;

import java.util.Collection;

import static hu.blackbelt.judo.meta.keycloak.support.KeycloakModelResourceSupport.keycloakModelResourceSupportBuilder;

@Slf4j
public class KeycloakValidationTest {

    private final String createdSourceModelName = "urn:Keycloak.model";

    KeycloakModelResourceSupport keycloakModelSupport;

    private KeycloakModel keycloakModel;

    @BeforeEach
    void setUp() {

        keycloakModelSupport = keycloakModelResourceSupportBuilder()
                .uri(URI.createURI(createdSourceModelName))
                .build();

        keycloakModel = KeycloakModel.buildKeycloakModel()
        		.keycloakModelResourceSupport(keycloakModelSupport)
                .name("test")
                .build();
    }

    private void runEpsilon (Collection<String> expectedErrors, Collection<String> expectedWarnings) throws Exception {
        try (Log bufferedLogger = new BufferedSlf4jLogger(log)) {
            KeycloakEpsilonValidator.validateKeycloak(bufferedLogger,
                    keycloakModel,
                    KeycloakEpsilonValidator.calculateKeycloakValidationScriptURI(),
                    expectedErrors,
                    expectedWarnings);
        } catch (EvlScriptExecutionException ex) {
            log.error("EVL failed", ex);
            log.error("\u001B[31m - expected errors: {}\u001B[0m", expectedErrors);
            log.error("\u001B[31m - unexpected errors: {}\u001B[0m", ex.getUnexpectedErrors());
            log.error("\u001B[31m - errors not found: {}\u001B[0m", ex.getErrorsNotFound());
            log.error("\u001B[33m - expected warnings: {}\u001B[0m", expectedWarnings);
            log.error("\u001B[33m - unexpected warnings: {}\u001B[0m", ex.getUnexpectedWarnings());
            log.error("\u001B[33m - warnings not found: {}\u001B[0m", ex.getWarningsNotFound());
            throw ex;
        }
    }
}
