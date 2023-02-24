package hu.blackbelt.judo.meta.keycloak.osgi.itest;

/*-
 * #%L
 * Judo :: Keycloak :: Model :: OSGi :: Integration Test
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
                .add( "model/" + keycloakModel.getName() + "-keycloak.model",
                        new ByteArrayInputStream(os.toByteArray()))
                .set( Constants.BUNDLE_MANIFESTVERSION, "2")
                .set( Constants.BUNDLE_SYMBOLICNAME, keycloakModel.getName() + "-keycloak" )
                .set( "Keycloak-Models", "name=" + keycloakModel.getName() + ";file=model/" + keycloakModel.getName() + "-keycloak.model")
                .build( withBnd());
    }

    @Test
    public void testModelValidation() throws Exception {
        try (Log bufferedLogger = new BufferedSlf4jLogger(log)) {
            validateKeycloak(bufferedLogger, keycloakModel, calculateKeycloakValidationScriptURI());
        }
    }
}
