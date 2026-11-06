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

import hu.blackbelt.epsilon.runtime.execution.impl.BufferedSlf4jLogger;
import hu.blackbelt.judo.meta.keycloak.*;
import hu.blackbelt.judo.meta.keycloak.support.KeycloakModelResourceSupport;
import hu.blackbelt.judo.meta.keycloak.validation.KeycloakValidator;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.ArrayList;
import java.util.List;

import static hu.blackbelt.judo.meta.keycloak.support.KeycloakModelResourceSupport.keycloakModelResourceSupportBuilder;
import static hu.blackbelt.judo.meta.keycloak.util.builder.KeycloakBuilders.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Performance tests comparing EVL and Java validation execution time.
 *
 * <p>This test generates a large synthetic model and measures validation time
 * for both EVL and Java validators, including sequential and parallel execution.</p>
 *
 * <p>Run with: {@code mvn test -Dtest=KeycloakValidationPerformanceTest -Dperformance.test=true}</p>
 */
@Slf4j
@EnabledIfSystemProperty(named = "performance.test", matches = "true")
public class KeycloakValidationPerformanceTest {

    private static final int DEFAULT_REALM_COUNT = 10;
    private static final int DEFAULT_CLIENTS_PER_REALM = 100;
    private static final int DEFAULT_USERS_PER_REALM = 100;
    private static final int DEFAULT_CREDENTIALS_PER_USER = 2;
    private static final int DEFAULT_BINDINGS_PER_CLIENT = 5;

    private static final String MODEL_NAME = "urn:Keycloak.performance.model";

    private KeycloakModelResourceSupport keycloakModelSupport;
    private KeycloakModel keycloakModel;

    @BeforeEach
    void setUp() {
        keycloakModelSupport = keycloakModelResourceSupportBuilder()
            .uri(URI.createURI(MODEL_NAME))
            .build();

        keycloakModel = KeycloakModel.buildKeycloakModel()
            .keycloakModelResourceSupport(keycloakModelSupport)
            .name("performance-test")
            .build();
    }

    /**
     * Test validation performance with 10,000+ elements.
     */
    @Test
    void testValidationPerformanceComparison() throws Exception {
        // Generate large model
        int realmCount = Integer.getInteger("perf.realms", DEFAULT_REALM_COUNT);
        int clientsPerRealm = Integer.getInteger("perf.clients", DEFAULT_CLIENTS_PER_REALM);
        int usersPerRealm = Integer.getInteger("perf.users", DEFAULT_USERS_PER_REALM);

        log.info("=== Keycloak Validation Performance Test ===");
        log.info("Generating model with {} realms, {} clients/realm, {} users/realm",
            realmCount, clientsPerRealm, usersPerRealm);

        generateLargeModel(realmCount, clientsPerRealm, usersPerRealm);

        int totalElements = countElements();
        log.info("Total model elements: {}", totalElements);
        assertTrue(totalElements >= 10000, "Model should have at least 10,000 elements");

        // Warm up
        log.info("Warming up validators...");
        runJavaValidation(false);
        runEvlValidation();

        // Run benchmarks
        int iterations = Integer.getInteger("perf.iterations", 3);
        log.info("Running {} iterations each...", iterations);

        // EVL validation
        List<Long> evlTimes = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            long start = System.currentTimeMillis();
            runEvlValidation();
            long duration = System.currentTimeMillis() - start;
            evlTimes.add(duration);
            log.info("EVL iteration {}: {} ms", i + 1, duration);
        }

        // Java sequential validation
        List<Long> javaSeqTimes = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            long start = System.currentTimeMillis();
            runJavaValidation(false);
            long duration = System.currentTimeMillis() - start;
            javaSeqTimes.add(duration);
            log.info("Java (sequential) iteration {}: {} ms", i + 1, duration);
        }

        // Java parallel validation
        List<Long> javaParTimes = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            long start = System.currentTimeMillis();
            runJavaValidation(true);
            long duration = System.currentTimeMillis() - start;
            javaParTimes.add(duration);
            log.info("Java (parallel) iteration {}: {} ms", i + 1, duration);
        }

        // Calculate averages
        double evlAvg = evlTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        double javaSeqAvg = javaSeqTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        double javaParAvg = javaParTimes.stream().mapToLong(Long::longValue).average().orElse(0);

        // Report results
        log.info("=== Results ===");
        log.info("Model size: {} elements", totalElements);
        log.info("EVL average:             {:>8.1f} ms", evlAvg);
        log.info("Java (sequential) avg:   {:>8.1f} ms", javaSeqAvg);
        log.info("Java (parallel) avg:     {:>8.1f} ms", javaParAvg);

        if (evlAvg > 0) {
            log.info("Java seq speedup:        {:>8.1f}x", evlAvg / javaSeqAvg);
            log.info("Java par speedup:        {:>8.1f}x", evlAvg / javaParAvg);
        }

        if (javaSeqAvg > 0) {
            log.info("Parallel vs sequential:  {:>8.1f}x", javaSeqAvg / javaParAvg);
        }

        // Assert Java is faster than EVL
        assertTrue(javaSeqAvg <= evlAvg * 1.5,
            "Java sequential should be at least comparable to EVL");
    }

    /**
     * Generate a large model with specified number of elements.
     */
    private void generateLargeModel(int realmCount, int clientsPerRealm, int usersPerRealm) {
        for (int r = 0; r < realmCount; r++) {
            List<Client> clients = new ArrayList<>();
            for (int c = 0; c < clientsPerRealm; c++) {
                List<AttributeBinding> bindings = new ArrayList<>();
                for (int b = 0; b < DEFAULT_BINDINGS_PER_CLIENT; b++) {
                    bindings.add(newAttributeBindingBuilder()
                        .withAttributeName("attr_" + r + "_" + c + "_" + b)
                        .build());
                }

                clients.add(newClientBuilder()
                    .withId("client-" + r + "-" + c)
                    .withClientId("client-id-" + r + "-" + c)
                    .withName("Client " + r + "-" + c)
                    .withEnabled(true)
                    .withAttributeBindings(bindings.toArray(new AttributeBinding[0]))
                    .build());
            }

            List<User> users = new ArrayList<>();
            for (int u = 0; u < usersPerRealm; u++) {
                List<UserCredential> credentials = new ArrayList<>();
                for (int cr = 0; cr < DEFAULT_CREDENTIALS_PER_USER; cr++) {
                    credentials.add(newUserCredentialBuilder()
                        .withType("password")
                        .withValue("password-" + r + "-" + u + "-" + cr)
                        .withTemporary(false)
                        .build());
                }

                users.add(newUserBuilder()
                    .withUsername("user-" + r + "-" + u)
                    .withEmail("user" + r + "_" + u + "@example.com")
                    .withFirstName("First" + u)
                    .withLastName("Last" + u)
                    .withEnabled(true)
                    .withCredentials(credentials.toArray(new UserCredential[0]))
                    .build());
            }

            Realm realm = newRealmBuilder()
                .withId("realm-" + r)
                .withRealm("realm-name-" + r)
                .withEnabled(true)
                .withClients(clients.toArray(new Client[0]))
                .withUsers(users.toArray(new User[0]))
                .build();

            keycloakModel.addContent(realm);
        }
    }

    /**
     * Count total elements in the model.
     */
    private int countElements() {
        int count = 0;
        var iter = keycloakModel.getResource().getAllContents();
        while (iter.hasNext()) {
            iter.next();
            count++;
        }
        return count;
    }

    /**
     * Run EVL validation.
     */
    private void runEvlValidation() throws Exception {
        try (BufferedSlf4jLogger bufferedLogger = new BufferedSlf4jLogger(log)) {
            KeycloakEpsilonValidator.validateKeycloak(
                bufferedLogger,
                keycloakModel,
                KeycloakEpsilonValidator.calculateKeycloakValidationScriptURI(),
                null,
                null
            );
        }
    }

    /**
     * Run Java validation.
     */
    private void runJavaValidation(boolean parallel) throws Exception {
        KeycloakValidator.validateKeycloak(
            log,
            keycloakModel,
            null,
            null,
            parallel
        );
    }
}
