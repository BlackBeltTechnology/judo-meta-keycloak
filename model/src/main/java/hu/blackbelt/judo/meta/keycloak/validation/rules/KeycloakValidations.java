package hu.blackbelt.judo.meta.keycloak.validation.rules;

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

import hu.blackbelt.judo.meta.keycloak.*;
import hu.blackbelt.judo.meta.keycloak.validation.KeycloakValidationConstants;
import hu.blackbelt.judo.zeta.annotation.Constraint;
import hu.blackbelt.judo.zeta.annotation.Critique;
import hu.blackbelt.judo.zeta.annotation.ValidationContext;
import hu.blackbelt.judo.zeta.validation.core.Severity;
import hu.blackbelt.judo.zeta.validation.core.ValidationResult;
import hu.blackbelt.judo.zeta.validation.core.ValidationRule;

import org.eclipse.emf.ecore.EObject;

import static hu.blackbelt.judo.meta.keycloak.validation.KeycloakValidationConstants.*;

/**
 * Validation rules for Keycloak metamodel elements.
 *
 * <p>This class contains all validation constraints and critiques for the Keycloak model.
 * Constraints are error-level validations that must pass, while critiques are warning-level
 * validations that are advisory.</p>
 *
 * <p>Note: Since there are no existing EVL constraints in the original keycloak.evl file,
 * these are new validation rules based on the metamodel structure and common best practices.</p>
 */
@ValidationContext(EObject.class)
public class KeycloakValidations {

    // ===== Realm Validations =====

    /**
     * Realm must have a non-empty realm name.
     */
    @Constraint(
        name = REALM_NAME_NOT_EMPTY,
        description = "Realm must have a non-empty realm name",
        message = "Realm must have a non-empty realm name"
    )
    public ValidationRule realmNameNotEmpty() {
        return (element, ctx) -> {
            if (!(element instanceof Realm)) {
                return ValidationResult.pass();
            }
            Realm self = (Realm) element;
            String realmName = self.getRealm();
            if (realmName == null || realmName.trim().isEmpty()) {
                return ValidationResult.fail(
                    REALM_NAME_NOT_EMPTY,
                    String.format(MSG_REALM_NAME_EMPTY, self.getId() != null ? self.getId() : "<no-id>"),
                    Severity.ERROR,
                    self
                );
            }
            return ValidationResult.pass();
        };
    }

    /**
     * Realm must have a non-empty ID.
     */
    @Constraint(
        name = REALM_ID_NOT_EMPTY,
        description = "Realm must have a non-empty ID",
        message = "Realm must have a non-empty ID"
    )
    public ValidationRule realmIdNotEmpty() {
        return (element, ctx) -> {
            if (!(element instanceof Realm)) {
                return ValidationResult.pass();
            }
            Realm self = (Realm) element;
            String id = self.getId();
            if (id == null || id.trim().isEmpty()) {
                return ValidationResult.fail(
                    REALM_ID_NOT_EMPTY,
                    MSG_REALM_ID_EMPTY,
                    Severity.ERROR,
                    self
                );
            }
            return ValidationResult.pass();
        };
    }

    // ===== Client Validations =====

    /**
     * Client must have a non-empty clientId.
     */
    @Constraint(
        name = CLIENT_ID_NOT_EMPTY,
        description = "Client must have a non-empty clientId",
        message = "Client must have a non-empty clientId"
    )
    public ValidationRule clientIdNotEmpty() {
        return (element, ctx) -> {
            if (!(element instanceof Client)) {
                return ValidationResult.pass();
            }
            Client self = (Client) element;
            String clientId = self.getClientId();
            if (clientId == null || clientId.trim().isEmpty()) {
                return ValidationResult.fail(
                    CLIENT_ID_NOT_EMPTY,
                    String.format(MSG_CLIENT_ID_EMPTY, self.getName() != null ? self.getName() : "<unnamed>"),
                    Severity.ERROR,
                    self
                );
            }
            return ValidationResult.pass();
        };
    }

    /**
     * Client must have a non-empty internal ID.
     */
    @Constraint(
        name = CLIENT_INTERNAL_ID_NOT_EMPTY,
        description = "Client must have a non-empty internal ID",
        message = "Client must have a non-empty internal ID"
    )
    public ValidationRule clientInternalIdNotEmpty() {
        return (element, ctx) -> {
            if (!(element instanceof Client)) {
                return ValidationResult.pass();
            }
            Client self = (Client) element;
            String id = self.getId();
            if (id == null || id.trim().isEmpty()) {
                return ValidationResult.fail(
                    CLIENT_INTERNAL_ID_NOT_EMPTY,
                    MSG_CLIENT_INTERNAL_ID_EMPTY,
                    Severity.ERROR,
                    self
                );
            }
            return ValidationResult.pass();
        };
    }

    /**
     * Client name should not be empty (warning).
     */
    @Critique(
        name = CLIENT_NAME_NOT_EMPTY,
        description = "Client should have a name for better identification",
        message = "Client should have a name"
    )
    public ValidationRule clientNameNotEmpty() {
        return (element, ctx) -> {
            if (!(element instanceof Client)) {
                return ValidationResult.pass();
            }
            Client self = (Client) element;
            String name = self.getName();
            if (name == null || name.trim().isEmpty()) {
                return ValidationResult.warn(
                    CLIENT_NAME_NOT_EMPTY,
                    String.format(MSG_CLIENT_NAME_EMPTY, self.getClientId() != null ? self.getClientId() : "<no-client-id>"),
                    self
                );
            }
            return ValidationResult.pass();
        };
    }

    // ===== User Validations =====

    /**
     * User must have a non-empty username.
     */
    @Constraint(
        name = USER_USERNAME_NOT_EMPTY,
        description = "User must have a non-empty username",
        message = "User must have a non-empty username"
    )
    public ValidationRule userUsernameNotEmpty() {
        return (element, ctx) -> {
            if (!(element instanceof User)) {
                return ValidationResult.pass();
            }
            User self = (User) element;
            String username = self.getUsername();
            if (username == null || username.trim().isEmpty()) {
                return ValidationResult.fail(
                    USER_USERNAME_NOT_EMPTY,
                    MSG_USER_USERNAME_EMPTY,
                    Severity.ERROR,
                    self
                );
            }
            return ValidationResult.pass();
        };
    }

    /**
     * User email should have valid format (warning).
     */
    @Critique(
        name = USER_EMAIL_VALID_FORMAT,
        description = "User email should have valid format",
        message = "User email should have valid format"
    )
    public ValidationRule userEmailValidFormat() {
        return (element, ctx) -> {
            if (!(element instanceof User)) {
                return ValidationResult.pass();
            }
            User self = (User) element;
            String email = self.getEmail();
            if (email != null && !email.isEmpty() && !isValidEmail(email)) {
                return ValidationResult.warn(
                    USER_EMAIL_VALID_FORMAT,
                    String.format(MSG_USER_EMAIL_INVALID, self.getUsername(), email),
                    self
                );
            }
            return ValidationResult.pass();
        };
    }

    // ===== UserCredential Validations =====

    /**
     * UserCredential must have a type.
     */
    @Constraint(
        name = USER_CREDENTIAL_TYPE_NOT_EMPTY,
        description = "UserCredential must have a type",
        message = "UserCredential must have a type"
    )
    public ValidationRule userCredentialTypeNotEmpty() {
        return (element, ctx) -> {
            if (!(element instanceof UserCredential)) {
                return ValidationResult.pass();
            }
            UserCredential self = (UserCredential) element;
            String type = self.getType();
            if (type == null || type.trim().isEmpty()) {
                return ValidationResult.fail(
                    USER_CREDENTIAL_TYPE_NOT_EMPTY,
                    MSG_USER_CREDENTIAL_TYPE_EMPTY,
                    Severity.ERROR,
                    self
                );
            }
            return ValidationResult.pass();
        };
    }

    /**
     * UserCredential must have a value.
     */
    @Constraint(
        name = USER_CREDENTIAL_VALUE_NOT_EMPTY,
        description = "UserCredential must have a value",
        message = "UserCredential must have a value"
    )
    public ValidationRule userCredentialValueNotEmpty() {
        return (element, ctx) -> {
            if (!(element instanceof UserCredential)) {
                return ValidationResult.pass();
            }
            UserCredential self = (UserCredential) element;
            String value = self.getValue();
            if (value == null || value.trim().isEmpty()) {
                return ValidationResult.fail(
                    USER_CREDENTIAL_VALUE_NOT_EMPTY,
                    String.format(MSG_USER_CREDENTIAL_VALUE_EMPTY,
                        self.getType() != null ? self.getType() : "<unknown>"),
                    Severity.ERROR,
                    self
                );
            }
            return ValidationResult.pass();
        };
    }

    // ===== AttributeBinding Validations =====

    /**
     * AttributeBinding must have an attributeName.
     */
    @Constraint(
        name = ATTRIBUTE_BINDING_NAME_NOT_EMPTY,
        description = "AttributeBinding must have an attributeName",
        message = "AttributeBinding must have an attributeName"
    )
    public ValidationRule attributeBindingNameNotEmpty() {
        return (element, ctx) -> {
            if (!(element instanceof AttributeBinding)) {
                return ValidationResult.pass();
            }
            AttributeBinding self = (AttributeBinding) element;
            String attributeName = self.getAttributeName();
            if (attributeName == null || attributeName.trim().isEmpty()) {
                return ValidationResult.fail(
                    ATTRIBUTE_BINDING_NAME_NOT_EMPTY,
                    MSG_ATTRIBUTE_BINDING_NAME_EMPTY,
                    Severity.ERROR,
                    self
                );
            }
            return ValidationResult.pass();
        };
    }

    // ===== Helper Methods =====

    /**
     * Simple email validation - checks for @ symbol and basic format.
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        // Simple regex for email validation
        return email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,}$");
    }
}
