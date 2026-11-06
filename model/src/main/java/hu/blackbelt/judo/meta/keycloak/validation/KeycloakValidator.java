package hu.blackbelt.judo.meta.keycloak.validation;

/*-
 * #%L
 * Judo :: Keycloak :: Model
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

import hu.blackbelt.judo.meta.keycloak.runtime.KeycloakModel;
import hu.blackbelt.judo.meta.keycloak.validation.rules.KeycloakValidations;
import hu.blackbelt.judo.zeta.common.ExtensionMethodRegistry;
import hu.blackbelt.judo.zeta.common.ModelProvider;
import hu.blackbelt.judo.zeta.validation.core.Severity;
import hu.blackbelt.judo.zeta.validation.core.ValidationContext;
import hu.blackbelt.judo.zeta.validation.core.ValidationExecutor;
import hu.blackbelt.judo.zeta.validation.core.ValidationRegistry;
import hu.blackbelt.judo.zeta.validation.core.ValidationResult;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.common.notify.Notifier;
import org.slf4j.Logger;

/**
 * Entry point for Java-based Keycloak model validation.
 *
 * <p>This validator provides a native Java alternative to EVL (Epsilon Validation Language)
 * validation with better IDE integration, debugging support, and performance.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * KeycloakValidator.validateKeycloak(log, keycloakModel);
 * }
 * </pre>
 */
public class KeycloakValidator {

    /**
     * Validate Keycloak model using Java validation rules.
     *
     * @param log the logger
     * @param keycloakModel the model to validate
     * @throws KeycloakValidationException if validation fails
     */
    public static void validateKeycloak(Logger log, KeycloakModel keycloakModel)
        throws KeycloakValidationException {
        validateKeycloak(log, keycloakModel, null, null, false);
    }

    /**
     * Validate Keycloak model with expected errors and warnings (for testing).
     *
     * @param log the logger
     * @param keycloakModel the model to validate
     * @param expectedErrors expected error constraint names
     * @param expectedWarnings expected warning constraint names
     * @throws KeycloakValidationException if validation fails
     */
    public static void validateKeycloak(
        Logger log,
        KeycloakModel keycloakModel,
        Collection<String> expectedErrors,
        Collection<String> expectedWarnings
    ) throws KeycloakValidationException {
        validateKeycloak(log, keycloakModel, expectedErrors, expectedWarnings, false);
    }

    /**
     * Validate Keycloak model with all options.
     *
     * @param log the logger
     * @param keycloakModel the model to validate
     * @param expectedErrors expected error constraint names
     * @param expectedWarnings expected warning constraint names
     * @param parallel use parallel execution
     * @throws KeycloakValidationException if validation fails
     */
    public static void validateKeycloak(
        Logger log,
        KeycloakModel keycloakModel,
        Collection<String> expectedErrors,
        Collection<String> expectedWarnings,
        boolean parallel
    ) throws KeycloakValidationException {
        log.info("Starting Java-based Keycloak validation...");

        // Create validation infrastructure
        ValidationRegistry registry = new ValidationRegistry();
        ExtensionMethodRegistry extensionRegistry = new ExtensionMethodRegistry();

        // Create model provider adapter
        ModelProvider modelProvider = new ModelProvider() {
            @Override
            public <T extends EObject> Collection<T> getAllContents(ResourceSet resourceSet, Class<T> type) {
                List<T> result = new ArrayList<>();
                TreeIterator<Notifier> iterator = resourceSet.getAllContents();
                while (iterator.hasNext()) {
                    Notifier notifier = iterator.next();
                    if (type.isInstance(notifier)) {
                        result.add(type.cast(notifier));
                    }
                }
                return result;
            }
        };

        // Create validation context
        ValidationContext context = new ValidationContext(
            modelProvider,
            keycloakModel.getResourceSet(),
            extensionRegistry
        );

        // Register validation rule classes
        try {
            registry.register(KeycloakValidations.class);
            log.debug("Registered Keycloak validation rules");
        } catch (Exception e) {
            log.error("Failed to register Keycloak validations", e);
            throw new RuntimeException("Failed to register validation rules", e);
        }

        // Set registry in context for satisfies() support
        context.setValidationRegistry(registry);

        // Collect all model elements
        List<EObject> allElements = new ArrayList<>();
        keycloakModel.getResource().getAllContents().forEachRemaining(allElements::add);
        log.debug("Validating {} model elements", allElements.size());

        // Execute validation
        ValidationExecutor executor = new ValidationExecutor(registry, context, parallel);
        List<ValidationResult> results = executor.validate(allElements);

        // Separate errors and warnings
        List<ValidationResult> errors = results.stream()
            .filter(r -> r.isFailed() && r.getSeverity() == Severity.ERROR)
            .collect(Collectors.toList());
        List<ValidationResult> warnings = results.stream()
            .filter(r -> r.isFailed() && r.getSeverity() == Severity.WARNING)
            .collect(Collectors.toList());

        // Log results
        if (!errors.isEmpty()) {
            log.error("Validation errors ({}):", errors.size());
            errors.forEach(e -> log.error("  - [{}] {}", e.getConstraintName(), e.getMessage()));
        }
        if (!warnings.isEmpty()) {
            log.warn("Validation warnings ({}):", warnings.size());
            warnings.forEach(w -> log.warn("  - [{}] {}", w.getConstraintName(), w.getMessage()));
        }

        // Check expected errors/warnings for testing
        if (expectedErrors != null || expectedWarnings != null) {
            Set<String> actualErrors = errors.stream()
                .map(ValidationResult::getConstraintName)
                .collect(Collectors.toSet());
            Set<String> actualWarnings = warnings.stream()
                .map(ValidationResult::getConstraintName)
                .collect(Collectors.toSet());

            Set<String> expectedErrorSet = expectedErrors != null
                ? new HashSet<>(expectedErrors) : Collections.emptySet();
            Set<String> expectedWarningSet = expectedWarnings != null
                ? new HashSet<>(expectedWarnings) : Collections.emptySet();

            // Find unexpected and missing
            Set<String> unexpectedErrors = new HashSet<>(actualErrors);
            unexpectedErrors.removeAll(expectedErrorSet);

            Set<String> missingErrors = new HashSet<>(expectedErrorSet);
            missingErrors.removeAll(actualErrors);

            Set<String> unexpectedWarnings = new HashSet<>(actualWarnings);
            unexpectedWarnings.removeAll(expectedWarningSet);

            Set<String> missingWarnings = new HashSet<>(expectedWarningSet);
            missingWarnings.removeAll(actualWarnings);

            if (!unexpectedErrors.isEmpty() || !missingErrors.isEmpty() ||
                !unexpectedWarnings.isEmpty() || !missingWarnings.isEmpty()) {

                StringBuilder message = new StringBuilder("Validation result mismatch:\n");
                if (!unexpectedErrors.isEmpty()) {
                    message.append("  Unexpected errors: ").append(unexpectedErrors).append("\n");
                }
                if (!missingErrors.isEmpty()) {
                    message.append("  Missing errors: ").append(missingErrors).append("\n");
                }
                if (!unexpectedWarnings.isEmpty()) {
                    message.append("  Unexpected warnings: ").append(unexpectedWarnings).append("\n");
                }
                if (!missingWarnings.isEmpty()) {
                    message.append("  Missing warnings: ").append(missingWarnings).append("\n");
                }

                throw new KeycloakValidationException(
                    message.toString(),
                    errors.stream().map(ValidationResult::getMessage).collect(Collectors.toList()),
                    warnings.stream().map(ValidationResult::getMessage).collect(Collectors.toList())
                );
            }
        } else if (!errors.isEmpty()) {
            // No expected errors specified but errors found
            throw new KeycloakValidationException(
                "Validation failed with " + errors.size() + " error(s)",
                errors.stream().map(ValidationResult::getMessage).collect(Collectors.toList()),
                warnings.stream().map(ValidationResult::getMessage).collect(Collectors.toList())
            );
        }

        log.info("Java-based Keycloak validation completed successfully");
    }
}
