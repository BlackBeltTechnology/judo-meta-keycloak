package hu.blackbelt.judo.meta.keycloak.runtime;

/*-
 * #%L
 * Judo :: Keycloak :: Model :: Eclipse plugin
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

import static hu.blackbelt.epsilon.runtime.execution.ExecutionContext.executionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.contexts.EvlExecutionContext.evlExecutionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext.wrappedEmfModelContextBuilder;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.epsilon.common.util.UriUtil;

import hu.blackbelt.epsilon.runtime.execution.ExecutionContext;
import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.exceptions.ScriptExecutionException;

public class KeycloakEpsilonValidator {
	
	public static void validateKeycloak(Log log,
            KeycloakModel keycloakModel,
            URI scriptRoot) throws ScriptExecutionException, URISyntaxException {
		validateKeycloak(log, keycloakModel, scriptRoot, emptyList(), emptyList());
	}
	
	public static void validateKeycloak(Log log,
			KeycloakModel keycloakModel,
            URI scriptRoot,
            Collection<String> expectedErrors,
            Collection<String> expectedWarnings) throws ScriptExecutionException, URISyntaxException {
		ExecutionContext executionContext = executionContextBuilder()
                .log(log)
                .resourceSet(keycloakModel.getResourceSet())
                .metaModels(emptyList())
                .modelContexts(Arrays.asList(
                        wrappedEmfModelContextBuilder()
                                .log(log)
                                .name("Keycloak")
                                .validateModel(false)
                                .resource(keycloakModel.getResource())
                                .build()))
                .injectContexts(singletonMap("keycloakUtils", new KeycloakUtils(keycloakModel.getResourceSet())))
                .build();
		
		 try {
	            // run the model / metadata loading
	            executionContext.load();

	            // Transformation script
	            executionContext.executeProgram(
	                    evlExecutionContextBuilder()
	                            .source(UriUtil.resolve("keycloak.evl", scriptRoot))
	                            .expectedErrors(expectedErrors)
	                            .expectedWarnings(expectedWarnings)
	                            .build());

	        } finally {
	            executionContext.commit();
	            try {
	                executionContext.close();
	            } catch (Exception e) {}
	        }
	}
	
	public static URI calculateKeycloakValidationScriptURI() throws URISyntaxException {
        URI keycloakRoot = KeycloakModel.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        if (keycloakRoot.toString().endsWith(".jar")) {
            keycloakRoot = new URI("jar:" + keycloakRoot.toString() + "!/validations/");
        } else if (keycloakRoot.toString().startsWith("jar:bundle:")) {
            // bundle://37.0:0/validations/
            // jar:bundle://37.0:0/!/validations/keycloak.evl
            keycloakRoot = new URI(keycloakRoot.toString().substring(4, keycloakRoot.toString().indexOf("!")) + "validations/");
        } else {
            keycloakRoot = new URI(keycloakRoot.toString() + "/validations/");
        }
        return keycloakRoot;

    }

}
