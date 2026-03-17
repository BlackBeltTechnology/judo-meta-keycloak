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
import hu.blackbelt.judo.meta.keycloak.*;
import hu.blackbelt.judo.meta.keycloak.validation.KeycloakValidationConstants;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static hu.blackbelt.judo.meta.keycloak.util.builder.KeycloakBuilders.*;
import static hu.blackbelt.judo.meta.keycloak.validation.KeycloakValidationConstants.*;

/**
 * Validation tests for Keycloak metamodel.
 *
 * <p>All tests run against both EVL and Java validators to ensure parity
 * between the two validation implementations.</p>
 */
@Slf4j
public class KeycloakValidationTest extends AbstractKeycloakValidationTest {

    // ===== Realm Validation Tests =====

    @ParameterizedTest(name = "testRealmNameNotEmpty [{0}]")
    @EnumSource(ValidatorType.class)
    void testRealmNameNotEmpty(ValidatorType type) throws Exception {
        this.validatorType = type;
        initModel();

        // Create realm with empty name
        Realm realm = newRealmBuilder()
            .withId("test-realm-id")
            .withRealm("")
            .build();
        keycloakModel.addContent(realm);

        runValidation(
            ImmutableList.of(REALM_NAME_NOT_EMPTY),
            ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testRealmIdNotEmpty [{0}]")
    @EnumSource(ValidatorType.class)
    void testRealmIdNotEmpty(ValidatorType type) throws Exception {
        this.validatorType = type;
        initModel();

        // Create realm with empty ID
        Realm realm = newRealmBuilder()
            .withId("")
            .withRealm("test-realm")
            .build();
        keycloakModel.addContent(realm);

        runValidation(
            ImmutableList.of(REALM_ID_NOT_EMPTY),
            ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testValidRealm [{0}]")
    @EnumSource(ValidatorType.class)
    void testValidRealm(ValidatorType type) throws Exception {
        this.validatorType = type;
        initModel();

        // Create valid realm
        Realm realm = newRealmBuilder()
            .withId("test-realm-id")
            .withRealm("test-realm")
            .withEnabled(true)
            .build();
        keycloakModel.addContent(realm);

        runValidation(
            ImmutableList.of(),
            ImmutableList.of()
        );
    }

    // ===== Client Validation Tests =====

    @ParameterizedTest(name = "testClientIdNotEmpty [{0}]")
    @EnumSource(ValidatorType.class)
    void testClientIdNotEmpty(ValidatorType type) throws Exception {
        this.validatorType = type;
        initModel();

        // Create client with empty clientId
        Client client = newClientBuilder()
            .withId("internal-id")
            .withClientId("")
            .withName("Test Client")
            .build();

        Realm realm = newRealmBuilder()
            .withId("test-realm-id")
            .withRealm("test-realm")
            .withClients(client)
            .build();
        keycloakModel.addContent(realm);

        runValidation(
            ImmutableList.of(CLIENT_ID_NOT_EMPTY),
            ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testClientInternalIdNotEmpty [{0}]")
    @EnumSource(ValidatorType.class)
    void testClientInternalIdNotEmpty(ValidatorType type) throws Exception {
        this.validatorType = type;
        initModel();

        // Create client with empty internal ID
        Client client = newClientBuilder()
            .withId("")
            .withClientId("test-client")
            .withName("Test Client")
            .build();

        Realm realm = newRealmBuilder()
            .withId("test-realm-id")
            .withRealm("test-realm")
            .withClients(client)
            .build();
        keycloakModel.addContent(realm);

        runValidation(
            ImmutableList.of(CLIENT_INTERNAL_ID_NOT_EMPTY),
            ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testClientNameNotEmpty [{0}]")
    @EnumSource(ValidatorType.class)
    void testClientNameNotEmpty(ValidatorType type) throws Exception {
        this.validatorType = type;
        initModel();

        // Create client with empty name (warning)
        Client client = newClientBuilder()
            .withId("internal-id")
            .withClientId("test-client")
            .withName("")
            .build();

        Realm realm = newRealmBuilder()
            .withId("test-realm-id")
            .withRealm("test-realm")
            .withClients(client)
            .build();
        keycloakModel.addContent(realm);

        runValidation(
            ImmutableList.of(),
            ImmutableList.of(CLIENT_NAME_NOT_EMPTY)
        );
    }

    // ===== User Validation Tests =====

    @ParameterizedTest(name = "testUserUsernameNotEmpty [{0}]")
    @EnumSource(ValidatorType.class)
    void testUserUsernameNotEmpty(ValidatorType type) throws Exception {
        this.validatorType = type;
        initModel();

        // Create user with empty username
        User user = newUserBuilder()
            .withUsername("")
            .withEmail("test@example.com")
            .build();

        Realm realm = newRealmBuilder()
            .withId("test-realm-id")
            .withRealm("test-realm")
            .withUsers(user)
            .build();
        keycloakModel.addContent(realm);

        runValidation(
            ImmutableList.of(USER_USERNAME_NOT_EMPTY),
            ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testUserEmailValidFormat [{0}]")
    @EnumSource(ValidatorType.class)
    void testUserEmailValidFormat(ValidatorType type) throws Exception {
        this.validatorType = type;
        initModel();

        // Create user with invalid email format (warning)
        User user = newUserBuilder()
            .withUsername("testuser")
            .withEmail("invalid-email")
            .build();

        Realm realm = newRealmBuilder()
            .withId("test-realm-id")
            .withRealm("test-realm")
            .withUsers(user)
            .build();
        keycloakModel.addContent(realm);

        runValidation(
            ImmutableList.of(),
            ImmutableList.of(USER_EMAIL_VALID_FORMAT)
        );
    }

    // ===== UserCredential Validation Tests =====

    @ParameterizedTest(name = "testUserCredentialTypeNotEmpty [{0}]")
    @EnumSource(ValidatorType.class)
    void testUserCredentialTypeNotEmpty(ValidatorType type) throws Exception {
        this.validatorType = type;
        initModel();

        // Create credential with empty type
        UserCredential credential = newUserCredentialBuilder()
            .withType("")
            .withValue("password123")
            .build();

        User user = newUserBuilder()
            .withUsername("testuser")
            .withCredentials(credential)
            .build();

        Realm realm = newRealmBuilder()
            .withId("test-realm-id")
            .withRealm("test-realm")
            .withUsers(user)
            .build();
        keycloakModel.addContent(realm);

        runValidation(
            ImmutableList.of(USER_CREDENTIAL_TYPE_NOT_EMPTY),
            ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testUserCredentialValueNotEmpty [{0}]")
    @EnumSource(ValidatorType.class)
    void testUserCredentialValueNotEmpty(ValidatorType type) throws Exception {
        this.validatorType = type;
        initModel();

        // Create credential with empty value
        UserCredential credential = newUserCredentialBuilder()
            .withType("password")
            .withValue("")
            .build();

        User user = newUserBuilder()
            .withUsername("testuser")
            .withCredentials(credential)
            .build();

        Realm realm = newRealmBuilder()
            .withId("test-realm-id")
            .withRealm("test-realm")
            .withUsers(user)
            .build();
        keycloakModel.addContent(realm);

        runValidation(
            ImmutableList.of(USER_CREDENTIAL_VALUE_NOT_EMPTY),
            ImmutableList.of()
        );
    }

    // ===== AttributeBinding Validation Tests =====

    @ParameterizedTest(name = "testAttributeBindingNameNotEmpty [{0}]")
    @EnumSource(ValidatorType.class)
    void testAttributeBindingNameNotEmpty(ValidatorType type) throws Exception {
        this.validatorType = type;
        initModel();

        // Create attribute binding with empty name
        AttributeBinding binding = newAttributeBindingBuilder()
            .withAttributeName("")
            .build();

        Client client = newClientBuilder()
            .withId("internal-id")
            .withClientId("test-client")
            .withName("Test Client")
            .withAttributeBindings(binding)
            .build();

        Realm realm = newRealmBuilder()
            .withId("test-realm-id")
            .withRealm("test-realm")
            .withClients(client)
            .build();
        keycloakModel.addContent(realm);

        runValidation(
            ImmutableList.of(ATTRIBUTE_BINDING_NAME_NOT_EMPTY),
            ImmutableList.of()
        );
    }

    // ===== Complex/Combined Tests =====

    @ParameterizedTest(name = "testValidCompleteModel [{0}]")
    @EnumSource(ValidatorType.class)
    void testValidCompleteModel(ValidatorType type) throws Exception {
        this.validatorType = type;
        initModel();

        // Create a complete valid model
        AttributeBinding binding = newAttributeBindingBuilder()
            .withAttributeName("customAttribute")
            .build();

        Client client = newClientBuilder()
            .withId("client-internal-id")
            .withClientId("my-app")
            .withName("My Application")
            .withEnabled(true)
            .withPublicClient(false)
            .withAttributeBindings(binding)
            .build();

        UserCredential credential = newUserCredentialBuilder()
            .withType("password")
            .withValue("secret123")
            .withTemporary(false)
            .build();

        User user = newUserBuilder()
            .withUsername("john.doe")
            .withEmail("john.doe@example.com")
            .withFirstName("John")
            .withLastName("Doe")
            .withEnabled(true)
            .withCredentials(credential)
            .build();

        Realm realm = newRealmBuilder()
            .withId("my-realm-id")
            .withRealm("my-realm")
            .withEnabled(true)
            .withLoginWithEmailAllowed(true)
            .withClients(client)
            .withUsers(user)
            .build();

        keycloakModel.addContent(realm);

        // Should have no errors or warnings
        runValidation(
            ImmutableList.of(),
            ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testMultipleErrors [{0}]")
    @EnumSource(ValidatorType.class)
    void testMultipleErrors(ValidatorType type) throws Exception {
        this.validatorType = type;
        initModel();

        // Create model with multiple errors
        Client client = newClientBuilder()
            .withId("")  // Error: empty internal ID
            .withClientId("")  // Error: empty clientId
            .withName("")  // Warning: empty name
            .build();

        Realm realm = newRealmBuilder()
            .withId("")  // Error: empty ID
            .withRealm("")  // Error: empty realm name
            .withClients(client)
            .build();

        keycloakModel.addContent(realm);

        runValidation(
            ImmutableList.of(
                REALM_ID_NOT_EMPTY,
                REALM_NAME_NOT_EMPTY,
                CLIENT_ID_NOT_EMPTY,
                CLIENT_INTERNAL_ID_NOT_EMPTY
            ),
            ImmutableList.of(CLIENT_NAME_NOT_EMPTY)
        );
    }
}
