package com.github.anthony_o.jhod;

import com.google.common.base.Preconditions;
import org.glassfish.grizzly.PortRange;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Main entry point for starting your jhod app.<br/>
 * You should create a {@link Launcher} using the embedded {@link Builder}: <pre> {@code
 *   Launcher.createBuilder()
 *       .appObject(this)
 *       .resourceConfig(new MyAppJerseyConfig())
 *       .build()
 * }</pre><br/>
 * You must call (and set) at least {@link Builder#resourceConfig(ResourceConfig)} and one of {@link Builder#appClass(Class)}, {@link Builder#appObject(Object)} or {@link Builder#appPath(Path)}.<br/>
 * The 'NW_HOME' environment variable must have also been set (and must point to the NW.js runtime root folder) before calling {@link Launcher#launch()}.<br/>
 * After having been launched (via {@link Launcher#launch()}), the port on which the Jersey server has started is accessible via the file you set by calling {@link Builder#appRelativeJsPortPath(Path)} (which defaults to {@code "js/tempPort.js"}).<br/>
 * This file should be used by your HTML app (coded in your {@code app} folder) in order to communicate between your HTML GUI and your Java Jersey code.<br/>
 * After jhod has started, you should call {@link Service#waitForTerminationThenShutdown()} in order to wait indefinitely for the user to close the NW.js window, and shutdown the Jersey server.
 */
public class Launcher {
    private ResourceConfig resourceConfig;
    private URI uri = new URI("http://localhost/api");
    private Path appRelativeJsPortPath = Paths.get("js", "tempPort.js");
    private Object appObject;
    private Class<?> appClass;
    private Path appPath;


    public static class Builder {
        private Launcher launcher = new Launcher();

        private Builder() throws URISyntaxException {
        }

        /**
         * Sets the base {@code uri} for accessing your JAX-RS mapped services (defaults to {@code http://localhost/api} if you don't call this method).
         * @param uri           URI on which the Jersey web application will be deployed. Only first path segment will be
         *                      used as context path, the rest as well as the port will be ignored.
         * @return this {@link Builder}
         */
        public Builder uri(URI uri) {
            Objects.requireNonNull(uri);
            launcher.uri = uri;
            return this;
        }

        /**
         * Sets the Jersey {@link ResourceConfig}. This is a mandatory step before calling {@link #build()} as this is required by Jersey.
         * @param resourceConfig your app Jersey {@link ResourceConfig}
         * @return this {@link Builder}
         */
        public Builder resourceConfig(ResourceConfig resourceConfig) {
            Objects.requireNonNull(resourceConfig);
            launcher.resourceConfig = resourceConfig;
            return this;
        }

        /**
         * Sets the path (relative to your {@code app} directory) to the file in which jhod should write the port on which Jersey has started.<br/>
         * This is not mandatory to set and defaults to {@code "js/tempPort.js"}.
         * @param appRelativeJsPortPath path (relative to your {@code app} directory) to the file in which jhod should write the port on which Jersey has started
         * @return this {@link Builder}
         */
        public Builder appRelativeJsPortPath(Path appRelativeJsPortPath) {
            launcher.appRelativeJsPortPath = appRelativeJsPortPath;
            return this;
        }

        /**
         * Sets a class which is a part of your Java jhod code in order for jhod to find the {@code appPath}.<br/>
         * Either one of {@link #appClass(Class)}, {@link #appObject(Object)} or {@link #appPath(Path)} must be called & set.
         * @param appClass a class which is a part of your Java jhod code
         * @return this {@link Builder}
         */
        public Builder appClass(Class<?> appClass) {
            launcher.appClass = appClass;
            return this;
        }

        /**
         * Sets an {@link Object} which class is a part of your Java jhod code in order for jhod to find the {@code appPath}.<br/>
         * Either one of {@link #appClass(Class)}, {@link #appObject(Object)} or {@link #appPath(Path)} must be called & set.
         * @param appObject an object which class is a part of your Java jhod code
         * @return this {@link Builder}
         */
        public Builder appObject(Object appObject) {
            launcher.appObject = appObject;
            return this;
        }

        /**
         * Sets the {@link Path} to your {@code app} directory, which contains your HTML app jhod code.<br/>
         * Either one of {@link #appClass(Class)}, {@link #appObject(Object)} or {@link #appPath(Path)} must be called & set.
         * @param appPath the {@link Path} to your {@code app} directory
         * @return this {@link Builder}
         */
        public Builder appPath(Path appPath) {
            launcher.appPath = appPath;
            return this;
        }

        /**
         * Builds the {@link Launcher} parameterized by this {@link Builder} and return it.
         * @return the parameterized {@link Launcher} by this {@link Builder}
         */
        public Launcher build() {
            return launcher;
        }
    }

    /**
     * Creates a new {@link Builder}.
     * @return the new {@link Builder}
     */
    public static Builder createBuilder() {
        try {
            return new Builder();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Launch your jhod app. It will:<ul>
     * <li>Firstly start the Jersey server;</li>
     * <li>Then write the TCP port on which it has started in the file described by what was set with {@link Builder#appRelativeJsPortPath(Path)}</li>
     * <li>And finally start your NW.js app.</li>
     * </ul>
     * Be aware that the 'NW_HOME' environment variable must have been set (and must point to the NW.js runtime root folder) before calling this method.
     * @return the jhod {@link Service} which contains all the running objects
     * @throws IOException
     * @throws URISyntaxException
     */
    public Service launch() throws IOException, URISyntaxException {
        Objects.requireNonNull(resourceConfig, "'resourceConfig' must have been set.");
        Objects.requireNonNull(uri, "'uri' must have been set.");
        Preconditions.checkArgument(appPath != null || appObject != null || appClass != null, "Either one of 'appPath', 'appObject' or 'appClass' must have been set.");
        String nwHome = System.getenv("NW_HOME");
        Objects.requireNonNull(nwHome, "The 'NW_HOME' environment variable must have been set (and must point to the NW.js runtime root folder) before trying to call this 'launch()' method.");

        if (appPath == null) {
            if (appClass == null) {
                appClass = appObject.getClass();
            }
            Path sourcePath = Paths.get(appClass.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent(); // getting the jar location path thanks to http://stackoverflow.com/a/320595/535203
            appPath = sourcePath.resolve("app");

            if (!appPath.toFile().exists()) {
                // dev : it actually resolves to target (because getClass().getProtectionDomain().getCodeSource().getLocation() resolves to target/classes in dev), so we must append ../src/main/app
                appPath = sourcePath.getParent().resolve(Paths.get("src", "main", "app"));
            }
        }

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, resourceConfig, false);
        NetworkListener grizzlyListener = server.getListener("grizzly");

        // Set a range instead a single port
        grizzlyListener = new NetworkListener(grizzlyListener.getName(), grizzlyListener.getHost(), new PortRange(49152, 65535)); // low & high thanks to http://stackoverflow.com/a/2675399/535203
        server.addListener(grizzlyListener);

        server.start();

        File tempPortJsFile = appPath.resolve(appRelativeJsPortPath).toFile();
        try (PrintWriter writer = new PrintWriter(tempPortJsFile)) {
            writer.println("var serverPort = "+grizzlyListener.getPort()+";");
        }

        // Now launch nw
        Process nwProcess = Runtime.getRuntime().exec(Paths.get(nwHome, "nw") + " " + appPath);

        return new Service(server, nwProcess);
    }

    /**
     * Shortcut to {@link #launch()} then {@link Service#waitForTerminationThenShutdown()}.
     * @throws IOException
     * @throws URISyntaxException
     * @throws InterruptedException
     */
    public void launchThenWaitForTerminationThenShutdown() throws IOException, URISyntaxException, InterruptedException {
        launch().waitForTerminationThenShutdown();
    }

    private Launcher() throws URISyntaxException {
        // Forbid to create Launcher instances
    }
}
