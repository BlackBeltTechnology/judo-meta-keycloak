package hu.blackbelt.judo.meta.keycloak.runtime;

import hu.blackbelt.judo.meta.keycloak.support.KeycloakModelResourceSupport;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    /**
     * Get stream of source iterator.
     *
     * @param sourceIterator source iterator
     * @param parallel       flag controlling returned stream (serial or parallel)
     * @param <T>            type of source iterator
     * @return return serial (parallel = <code>false</code>) or parallel (parallel = <code>true</code>) stream
     */
    static <T> Stream<T> asStream(Iterator<T> sourceIterator, boolean parallel) {
        Iterable<T> iterable = () -> sourceIterator;
        return StreamSupport.stream(iterable.spliterator(), parallel);
    }

    /**
     * Get all model elements.
     *
     * @param <T> generic type of model elements
     * @return model elements
     */
    <T> Stream<T> all() {
        return asStream((Iterator<T>) resourceSet.getAllContents(), false);
    }

    /**
     * Get model elements with specific type
     *
     * @param clazz class of model element types
     * @param <T>   specific type
     * @return all elements with clazz type
     */
    public <T> Stream<T> all(final Class<T> clazz) {
        return all().filter(e -> clazz.isAssignableFrom(e.getClass())).map(e -> (T) e);
    }
}
