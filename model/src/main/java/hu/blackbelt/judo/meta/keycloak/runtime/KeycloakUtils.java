package hu.blackbelt.judo.meta.keycloak.runtime;

import hu.blackbelt.judo.meta.keycloak.support.KeycloakModelResourceSupport;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeycloakUtils {

    private static final Logger log = LoggerFactory.getLogger(KeycloakUtils.class);

    private ResourceSet resourceSet;

    private KeycloakModelResourceSupport keycloakModelResourceSupport;

    public KeycloakUtils(final ResourceSet resourceSet) {
        this.resourceSet = resourceSet;

        keycloakModelResourceSupport = KeycloakModelResourceSupport.keycloakModelResourceSupportBuilder()
                .resourceSet(resourceSet)
                .build();
    }
}
