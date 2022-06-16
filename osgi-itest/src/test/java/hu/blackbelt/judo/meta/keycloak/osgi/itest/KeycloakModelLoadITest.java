package hu.blackbelt.judo.meta.keycloak.osgi.itest;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.exceptions.ScriptExecutionException;
import hu.blackbelt.epsilon.runtime.execution.impl.BufferedSlf4jLogger;
import hu.blackbelt.judo.meta.keycloak.runtime.KeycloakEpsilonValidator;
import hu.blackbelt.judo.meta.keycloak.runtime.KeycloakModel;
import hu.blackbelt.judo.meta.keycloak.runtime.KeycloakModel.KeycloakValidationException;
import hu.blackbelt.judo.meta.keycloak.runtime.KeycloakModel.SaveArguments;
import hu.blackbelt.osgi.utils.osgi.api.BundleTrackerManager;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.log.LogService;

import javax.inject.Inject;
import java.io.*;
import java.net.URISyntaxException;

import static hu.blackbelt.judo.meta.keycloak.osgi.itest.KarafFeatureProvider.karafConfig;
import static hu.blackbelt.judo.meta.keycloak.runtime.KeycloakEpsilonValidator.calculateKeycloakValidationScriptURI;
import static hu.blackbelt.judo.meta.keycloak.runtime.KeycloakEpsilonValidator.validateKeycloak;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;
import static org.ops4j.pax.tinybundles.core.TinyBundles.withBnd;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@Slf4j
public class KeycloakModelLoadITest {

    private static final String DEMO = "northwind-keycloak";

    @Inject
    protected BundleTrackerManager bundleTrackerManager;

    @Inject
    BundleContext bundleContext;

    @Inject
    KeycloakModel keycloakModel;

    @Configuration
    public Option[] config() throws IOException, KeycloakValidationException {

        return combine(karafConfig(this.getClass()),
                mavenBundle(maven()
                        .groupId("hu.blackbelt.judo.meta")
                        .artifactId("hu.blackbelt.judo.meta.keycloak.osgi")
                        .versionAsInProject()),
                getProvisonModelBundle());
    }

    public Option getProvisonModelBundle() throws IOException, KeycloakValidationException {
        return provision(
                getKeycloakModelBundle()
        );
    }

    private InputStream getKeycloakModelBundle() throws IOException, KeycloakValidationException {
        KeycloakModel keycloakModel = KeycloakModel.buildKeycloakModel()
                .name(DEMO)
                .uri(URI.createFileURI("test.model"))
                .build();

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        keycloakModel.saveKeycloakModel(SaveArguments.keycloakSaveArgumentsBuilder().outputStream(os));
        return bundle()
                .add( "model/" + DEMO + ".judo-meta-keycloak",
                        new ByteArrayInputStream(os.toByteArray()))
                .set( Constants.BUNDLE_MANIFESTVERSION, "2")
                .set( Constants.BUNDLE_SYMBOLICNAME, DEMO + "-keycloak" )
                .set( "Keycloak-Models", "file=model/" + DEMO + ".judo-meta-keycloak;version=1.0.0;name=" + DEMO + ";checksum=notset;meta-version-range=\"[1.0.0,2)\"")
                .build( withBnd());
    }

    @Test
    public void testModelValidation() throws Exception {
        try (Log bufferedLogger = new BufferedSlf4jLogger(log)) {
            validateKeycloak(bufferedLogger, keycloakModel, calculateKeycloakValidationScriptURI());
        }
    }
}
