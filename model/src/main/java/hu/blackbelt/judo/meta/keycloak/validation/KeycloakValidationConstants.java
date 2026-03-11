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

/**
 * Constants for Keycloak validation constraint names, guard method names, and message templates.
 *
 * <p>All validation rules should reference these constants rather than using inline strings
 * to ensure consistency and make refactoring easier.</p>
 */
public final class KeycloakValidationConstants {

    private KeycloakValidationConstants() {
        // Utility class - prevent instantiation
    }

    // ===== Realm Constraints =====

    /** Realm must have a non-empty realm name */
    public static final String REALM_NAME_NOT_EMPTY = "RealmNameNotEmpty";

    /** Realm must have a unique ID */
    public static final String REALM_ID_NOT_EMPTY = "RealmIdNotEmpty";

    // ===== Client Constraints =====

    /** Client must have a non-empty clientId */
    public static final String CLIENT_ID_NOT_EMPTY = "ClientIdNotEmpty";

    /** Client must have a unique ID */
    public static final String CLIENT_INTERNAL_ID_NOT_EMPTY = "ClientInternalIdNotEmpty";

    /** Client name should not be empty (warning) */
    public static final String CLIENT_NAME_NOT_EMPTY = "ClientNameNotEmpty";

    // ===== User Constraints =====

    /** User must have a non-empty username */
    public static final String USER_USERNAME_NOT_EMPTY = "UserUsernameNotEmpty";

    /** User email should be valid format (warning) */
    public static final String USER_EMAIL_VALID_FORMAT = "UserEmailValidFormat";

    // ===== UserCredential Constraints =====

    /** UserCredential must have a type */
    public static final String USER_CREDENTIAL_TYPE_NOT_EMPTY = "UserCredentialTypeNotEmpty";

    /** UserCredential must have a value */
    public static final String USER_CREDENTIAL_VALUE_NOT_EMPTY = "UserCredentialValueNotEmpty";

    // ===== AttributeBinding Constraints =====

    /** AttributeBinding must have an attributeName */
    public static final String ATTRIBUTE_BINDING_NAME_NOT_EMPTY = "AttributeBindingNameNotEmpty";

    // ===== Guard Method Names =====

    /** Guard: element has a container */
    public static final String GUARD_HAS_CONTAINER = "hasContainer";

    // ===== Message Templates =====

    public static final String MSG_REALM_NAME_EMPTY = "Realm '%s' must have a non-empty realm name";
    public static final String MSG_REALM_ID_EMPTY = "Realm must have a non-empty ID";
    public static final String MSG_CLIENT_ID_EMPTY = "Client '%s' must have a non-empty clientId";
    public static final String MSG_CLIENT_INTERNAL_ID_EMPTY = "Client must have a non-empty internal ID";
    public static final String MSG_CLIENT_NAME_EMPTY = "Client '%s' should have a name";
    public static final String MSG_USER_USERNAME_EMPTY = "User must have a non-empty username";
    public static final String MSG_USER_EMAIL_INVALID = "User '%s' has invalid email format: %s";
    public static final String MSG_USER_CREDENTIAL_TYPE_EMPTY = "UserCredential must have a type";
    public static final String MSG_USER_CREDENTIAL_VALUE_EMPTY = "UserCredential of type '%s' must have a value";
    public static final String MSG_ATTRIBUTE_BINDING_NAME_EMPTY = "AttributeBinding must have an attributeName";
}
