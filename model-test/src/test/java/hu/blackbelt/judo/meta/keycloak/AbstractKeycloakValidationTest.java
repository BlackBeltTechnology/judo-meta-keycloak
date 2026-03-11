package hu.blackbelt.judo.meta.keycloak;

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

import hu.blackbelt.epsilon.runtime.execution.exceptions.EvlScriptExecutionException;
import hu.blackbelt.epsilon.runtime.execution.impl.BufferedSlf4jLogger;
import hu.blackbelt.judo.meta.keycloak.runtime.KeycloakEpsilonValidator;
import hu.blackbelt.judo.meta.keycloak.runtime.KeycloakModel;
import hu.blackbelt.judo.meta.keycloak.support.KeycloakModelResourceSupport;
import hu.blackbelt.judo.meta.keycloak.validation.KeycloakValidationException;
import hu.blackbelt.judo.meta.keycloak.validation.KeycloakValidator;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static hu.blackbelt.judo.meta.keycloak.support.KeycloakModelResourceSupport.keycloakModelResourceSupportBuilder;

/**
 * Abstract base class for Keycloak validation tests.
 *
 * <p>Provides infrastructure for running the same test cases against both
 * EVL and Java validators using JUnit 5 parameterized tests.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * class MyValidationTest extends AbstractKeycloakValidationTest {
 *
 *     @ParameterizedTest(name = "testMyConstraint [{0}]")
 *     @EnumSource(ValidatorType.class)
 *     void testMyConstraint(ValidatorType type) throws Exception {
 *         this.validatorType = type;
 *         initModel();
 *
 *         // Build model with error
 *         Realm realm = keycloakModelSupport.newRealmBuilder().withRealm("").build();
 *         keycloakModel.addContent(realm);
 *
 *         runValidation(
 *             ImmutableList.of("RealmNameNotEmpty"),
 *             ImmutableList.of()
 *         );
 *     }
 * }
 * }
 * </pre>
 */
@Slf4j
public abstract class AbstractKeycloakValidationTest {

    protected static final String MODEL_NAME = "urn:Keycloak.test.model";

    protected KeycloakModelResourceSupport keycloakModelSupport;
    protected KeycloakModel keycloakModel;
    protected ValidatorType validatorType;

    /**
     * Initialize a new empty Keycloak model for testing.
     *
     * <p>Call this at the beginning of each test method after setting validatorType.</p>
     */
    protected void initModel() {
        keycloakModelSupport = keycloakModelResourceSupportBuilder()
            .uri(URI.createURI(MODEL_NAME))
            .build();

        keycloakModel = KeycloakModel.buildKeycloakModel()
            .keycloakModelResourceSupport(keycloakModelSupport)
            .name("test")
            .build();
    }

    /**
     * Run validation using the currently selected validator type.
     *
     * @param expectedErrors expected error constraint names
     * @param expectedWarnings expected warning constraint names
     * @throws Exception if validation fails unexpectedly
     */
    protected void runValidation(
        Collection<String> expectedErrors,
        Collection<String> expectedWarnings
    ) throws Exception {
        switch (validatorType) {
            case EVL:
                runEvlValidation(expectedErrors, expectedWarnings);
                break;
            case JAVA:
                runJavaValidation(expectedErrors, expectedWarnings);
                break;
            default:
                throw new IllegalArgumentException("Unknown validator type: " + validatorType);
        }
    }

    /**
     * Run EVL-based validation.
     *
     * <p>Note: The epsilon-runtime library compares expected errors using full format
     * "ConstraintName|Message", which differs from our constraint-name-only approach.
     * Therefore, we transform expected errors to match EVL's format by extracting
     * the constraint names from found errors.</p>
     */
    private void runEvlValidation(
        Collection<String> expectedErrors,
        Collection<String> expectedWarnings
    ) throws Exception {
        Set<String> foundErrorNames = new HashSet<>();
        Set<String> foundWarningNames = new HashSet<>();

        try (BufferedSlf4jLogger bufferedLogger = new BufferedSlf4jLogger(log)) {
            // Run validation with empty expected sets to force exception on any violation
            // This ensures we catch both errors and warnings
            KeycloakEpsilonValidator.validateKeycloak(
                bufferedLogger,
                keycloakModel,
                KeycloakEpsilonValidator.calculateKeycloakValidationScriptURI(),
                Collections.emptyList(),
                Collections.emptyList()
            );
            // EVL validation passed with no errors or warnings
        } catch (EvlScriptExecutionException ex) {
            // Extract constraint names from UnsatisfiedConstraint objects
            foundErrorNames = ex.getUnsatisfiedErrors().stream()
                .map(uc -> uc.getConstraint().getName())
                .collect(java.util.stream.Collectors.toSet());
            foundWarningNames = ex.getUnsatisfiedWarnings().stream()
                .map(uc -> uc.getConstraint().getName())
                .collect(java.util.stream.Collectors.toSet());
        }

        // Compare found vs expected
        Set<String> expectedErrorSet = expectedErrors != null
            ? new HashSet<>(expectedErrors) : Collections.emptySet();
        Set<String> expectedWarningSet = expectedWarnings != null
            ? new HashSet<>(expectedWarnings) : Collections.emptySet();

        Set<String> missingErrors = new HashSet<>(expectedErrorSet);
        missingErrors.removeAll(foundErrorNames);

        Set<String> unexpectedErrors = new HashSet<>(foundErrorNames);
        unexpectedErrors.removeAll(expectedErrorSet);

        Set<String> missingWarnings = new HashSet<>(expectedWarningSet);
        missingWarnings.removeAll(foundWarningNames);

        Set<String> unexpectedWarnings = new HashSet<>(foundWarningNames);
        unexpectedWarnings.removeAll(expectedWarningSet);

        if (!missingErrors.isEmpty() || !unexpectedErrors.isEmpty() ||
            !missingWarnings.isEmpty() || !unexpectedWarnings.isEmpty()) {
            log.error("EVL validation mismatch");
            log.error("\u001B[31m - expected errors: {}\u001B[0m", expectedErrors);
            log.error("\u001B[31m - found errors: {}\u001B[0m", foundErrorNames);
            log.error("\u001B[31m - missing errors: {}\u001B[0m", missingErrors);
            log.error("\u001B[31m - unexpected errors: {}\u001B[0m", unexpectedErrors);
            log.error("\u001B[33m - expected warnings: {}\u001B[0m", expectedWarnings);
            log.error("\u001B[33m - found warnings: {}\u001B[0m", foundWarningNames);
            log.error("\u001B[33m - missing warnings: {}\u001B[0m", missingWarnings);
            log.error("\u001B[33m - unexpected warnings: {}\u001B[0m", unexpectedWarnings);
            throw new AssertionError("EVL validation mismatch - see log for details");
        }
    }

    /**
     * Run Java-based validation.
     */
    private void runJavaValidation(
        Collection<String> expectedErrors,
        Collection<String> expectedWarnings
    ) throws Exception {
        try {
            KeycloakValidator.validateKeycloak(
                log,
                keycloakModel,
                expectedErrors,
                expectedWarnings
            );
        } catch (KeycloakValidationException ex) {
            log.error("Java validation failed", ex);
            log.error("\u001B[31m - expected errors: {}\u001B[0m", expectedErrors);
            log.error("\u001B[31m - actual errors: {}\u001B[0m", ex.getErrors());
            log.error("\u001B[33m - expected warnings: {}\u001B[0m", expectedWarnings);
            log.error("\u001B[33m - actual warnings: {}\u001B[0m", ex.getWarnings());
            throw ex;
        }
    }

    /**
     * Get the logger for subclasses.
     */
    protected Logger getLog() {
        return log;
    }
}
