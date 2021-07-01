package hu.blackbelt.judo.meta.keycloak.osgi.itest;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.cm.ConfigurationAdminOptions.newConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;

public class KeycloakKarafFeatureProvider {
    public static final String ZIP = "zip";

    public static final String FEATURE_CXF_JAXRS = "cxf-jaxrs";
    public static final String FEATURE_CXF_JACKSON = "cxf-jackson";
    public static final String FEATURE_SWAGGER_CORE = "cxf-rs-description-swagger2";

    public static MavenArtifactUrlReference apacheCxf() {
        return maven().groupId("org.apache.cxf.karaf").artifactId("apache-cxf").versionAsInProject().classifier("features").type("xml");
    }

    public static MavenArtifactUrlReference blackbeltOsgiUtils() {
        return maven().groupId("hu.blackbelt.osgi.utils").artifactId("features").versionAsInProject().classifier("features").type("xml");
    }

    public static MavenArtifactUrlReference blackbeltGoogle() {
        return maven().groupId("hu.blackbelt.karaf.features").artifactId("google-features").versionAsInProject().classifier("features").type("xml");
    }

    public static MavenArtifactUrlReference blackbeltBouncCastle() {
        return maven().groupId("hu.blackbelt.karaf.features").artifactId("bouncycastle-features").versionAsInProject().classifier("features").type("xml");
    }

    public static MavenArtifactUrlReference blackbeltApacheCommons() {
        return maven().groupId("hu.blackbelt.karaf.features").artifactId("apache-commons-features").versionAsInProject().classifier("features").type("xml");
    }

    public static MavenArtifactUrlReference blackbeltApacheHttpClient() {
        return maven().groupId("hu.blackbelt.karaf.features").artifactId("apache-httpclient-features").versionAsInProject().classifier("features").type("xml");
    }

    public static MavenArtifactUrlReference blackbeltApachePoi() {
        return maven().groupId("hu.blackbelt.karaf.features").artifactId("apache-poi-features").versionAsInProject().classifier("features").type("xml");
    }

    public static MavenArtifactUrlReference blackbeltEclipseEmf() {
        return maven().groupId("hu.blackbelt.karaf.features").artifactId("eclipse-emf-features").versionAsInProject().classifier("features").type("xml");
    }

    public static MavenArtifactUrlReference blackbeltEclipseEpsilon() {
        return maven().groupId("hu.blackbelt.karaf.features").artifactId("eclipse-epsilon-features").versionAsInProject().classifier("features").type("xml");
    }

    public static MavenArtifactUrlReference blackbeltTinybundles() {
        return maven().groupId("hu.blackbelt.karaf.features").artifactId("tinybundles-features").versionAsInProject().classifier("features").type("xml");
    }

    public static MavenArtifactUrlReference blackbeltAntlr() {
        return maven().groupId("hu.blackbelt.karaf.features").artifactId("antlr-features").versionAsInProject().classifier("features").type("xml");
    }

    public static MavenArtifactUrlReference blackbeltEpsilonRuntime() {
        return maven().groupId("hu.blackbelt.epsilon").artifactId("features").versionAsInProject().classifier("features").type("xml");
    }

    public static Option[] getRuntimeFeaturesForMetamodel(Class clazz) {
        return new Option[]{

                features(blackbeltBouncCastle()),

                features(blackbeltApacheCommons()),

                features(blackbeltApacheHttpClient()),

                features(blackbeltApachePoi()),

                features(blackbeltOsgiUtils(), "osgi-utils"),

                features(blackbeltGoogle()),

                features(blackbeltTinybundles(), "tinybundles"),

                features(blackbeltEclipseEmf()),

                features(blackbeltAntlr()),

                features(blackbeltEclipseEpsilon()),

                features(blackbeltEpsilonRuntime(), "epsilon-runtime"),

                features(apacheCxf(), FEATURE_SWAGGER_CORE, FEATURE_CXF_JACKSON, FEATURE_CXF_JAXRS),

                newConfiguration("hu.blackbelt.jaxrs.providers.JacksonProvider")
                        .put("JacksonProvider.SerializationFeature.INDENT_OUTPUT", "true").asOption()

        };
    }

}
