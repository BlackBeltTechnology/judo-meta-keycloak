package hu.blackbelt.judo.meta.keycloak.osgi;

import hu.blackbelt.judo.meta.keycloak.runtime.KeycloakModel;
import hu.blackbelt.osgi.utils.osgi.api.BundleCallback;
import hu.blackbelt.osgi.utils.osgi.api.BundleTrackerManager;
import hu.blackbelt.osgi.utils.osgi.api.BundleUtil;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.VersionRange;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hu.blackbelt.judo.meta.keycloak.runtime.KeycloakModel.LoadArguments.keycloakLoadArgumentsBuilder;
import static hu.blackbelt.judo.meta.keycloak.runtime.KeycloakModel.loadKeycloakModel;
import static java.util.Optional.ofNullable;

@Component(immediate = true)
@Slf4j
@Designate(ocd = KeycloakModelBundleTracker.TrackerConfig.class)
public class KeycloakModelBundleTracker {

    public static final String KEYCLOAK_MODELS = "Keycloak-Models";

    @ObjectClassDefinition(name="Keycloak Model Bundle TTracker")
    public @interface TrackerConfig {
        @AttributeDefinition(
                name = "Tags",
                description = "Which tags are on the loaded model when there is no one defined in bundle"
        )
        String tags() default "";
    }

    @Reference
    BundleTrackerManager bundleTrackerManager;

    Map<String, ServiceRegistration<KeycloakModel>> keycloakModelRegistrations = new ConcurrentHashMap<>();

    Map<String, KeycloakModel> keycloakModels = new HashMap<>();

    TrackerConfig config;

    @Activate
    public void activate(final ComponentContext componentContext, final TrackerConfig trackerConfig) {
        this.config = trackerConfig;
        bundleTrackerManager.registerBundleCallback(this.getClass().getName(),
                new KeycloakRegisterCallback(componentContext.getBundleContext()),
                new KeycloakUnregisterCallback(),
                new KeycloakBundlePredicate());
    }

    @Deactivate
    public void deactivate(final ComponentContext componentContext) {
        bundleTrackerManager.unregisterBundleCallback(this.getClass().getName());
    }

    private static class KeycloakBundlePredicate implements Predicate<Bundle> {
        @Override
        public boolean test(Bundle trackedBundle) {
            return BundleUtil.hasHeader(trackedBundle, KEYCLOAK_MODELS);
        }
    }

    private class KeycloakRegisterCallback implements BundleCallback {

        BundleContext bundleContext;

        public KeycloakRegisterCallback(BundleContext bundleContext) {
            this.bundleContext = bundleContext;
        }


        @Override
        public void accept(Bundle trackedBundle) {
            List<Map<String, String>> entries = BundleUtil.getHeaderEntries(trackedBundle, KEYCLOAK_MODELS);


            for (Map<String, String> params : entries) {
                String key = params.get(KeycloakModel.NAME);
                if (keycloakModelRegistrations.containsKey(key)) {
                    log.error("Keycloak model already loaded: " + key);
                } else {
                    if (params.containsKey(KeycloakModel.META_VERSION_RANGE)) {
                        VersionRange versionRange = new VersionRange(params.get(KeycloakModel.META_VERSION_RANGE).replaceAll("\"", ""));
                        if (versionRange.includes(bundleContext.getBundle().getVersion())) {
                            // Unpack model
                            try {
                                KeycloakModel keycloakModel = loadKeycloakModel(keycloakLoadArgumentsBuilder()
                                        .inputStream(trackedBundle.getEntry(params.get("file")).openStream())
                                        .name(params.get(KeycloakModel.NAME))
                                        .version(trackedBundle.getVersion().toString())
                                        .checksum(Optional.ofNullable(params.get(KeycloakModel.CHECKSUM)).orElse("notset"))
                                        .tags(Stream.of(ofNullable(params.get(KeycloakModel.TAGS)).orElse(config.tags()).split(",")).collect(Collectors.toSet()))
                                        .acceptedMetaVersionRange(Optional.of(versionRange.toString()).orElse("[0,99)")));

                                log.info("Registering Keycloak model: " + keycloakModel);

                                ServiceRegistration<KeycloakModel> modelServiceRegistration = bundleContext.registerService(KeycloakModel.class, keycloakModel, keycloakModel.toDictionary());
                                keycloakModels.put(key, keycloakModel);
                                keycloakModelRegistrations.put(key, modelServiceRegistration);

                            } catch (IOException | KeycloakModel.KeycloakValidationException e) {
                                log.error("Could not load Psm model: " + params.get(KeycloakModel.NAME) + " from bundle: " + trackedBundle.getBundleId(), e);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public Thread process(Bundle bundle) {
            return null;
        }
    }

    private class KeycloakUnregisterCallback implements BundleCallback {

        @Override
        public void accept(Bundle trackedBundle) {
            List<Map<String, String>> entries = BundleUtil.getHeaderEntries(trackedBundle, KEYCLOAK_MODELS);
            for (Map<String, String> params : entries) {
                String key = params.get(KeycloakModel.NAME);

                if (keycloakModels.containsKey(key)) {
                    ServiceRegistration<KeycloakModel> modelServiceRegistration = keycloakModelRegistrations.get(key);

                    if (modelServiceRegistration != null) {
                        log.info("Unregistering Keycloak model: " + keycloakModels.get(key));
                        modelServiceRegistration.unregister();
                        keycloakModelRegistrations.remove(key);
                        keycloakModels.remove(key);
                    }
                } else {
                    log.error("Keycloak Model is not registered: " + key);
                }
            }
        }

        @Override
        public Thread process(Bundle bundle) {
            return null;
        }
    }

}
