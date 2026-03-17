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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Exception thrown when Keycloak model validation fails.
 *
 * <p>Contains details about validation errors and warnings encountered during validation.</p>
 */
public class KeycloakValidationException extends Exception {

    private final List<String> errors;
    private final List<String> warnings;

    /**
     * Create a validation exception with a message.
     *
     * @param message the error message
     */
    public KeycloakValidationException(String message) {
        super(message);
        this.errors = Collections.emptyList();
        this.warnings = Collections.emptyList();
    }

    /**
     * Create a validation exception with details about errors and warnings.
     *
     * @param message the error message
     * @param errors list of error messages
     * @param warnings list of warning messages
     */
    public KeycloakValidationException(String message, Collection<String> errors, Collection<String> warnings) {
        super(message);
        this.errors = errors != null ? List.copyOf(errors) : Collections.emptyList();
        this.warnings = warnings != null ? List.copyOf(warnings) : Collections.emptyList();
    }

    /**
     * Get the list of error messages.
     *
     * @return immutable list of errors
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     * Get the list of warning messages.
     *
     * @return immutable list of warnings
     */
    public List<String> getWarnings() {
        return warnings;
    }

    /**
     * Get unexpected errors (errors found but not expected).
     * Used for test diagnostics.
     *
     * @return list of unexpected errors
     */
    public List<String> getUnexpectedErrors() {
        return errors;
    }

    /**
     * Get unexpected warnings (warnings found but not expected).
     * Used for test diagnostics.
     *
     * @return list of unexpected warnings
     */
    public List<String> getUnexpectedWarnings() {
        return warnings;
    }
}
