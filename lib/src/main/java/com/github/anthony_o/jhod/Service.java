package com.github.anthony_o.jhod;

import org.glassfish.grizzly.http.server.HttpServer;

import java.util.concurrent.TimeUnit;

/**
 * This object contains all the running parts of your jhod app.<br>
 * From here, you will be able to access either the Jersey HTTP server (via {@link #getHttpServer()}) or the NW.js {@link Process} started by jhod (via {@link #getNwProcess()}).
 * It also contains the final method used to wait indefinitely for the user to close the NW.js window, and shutdown the Jersey server.
 */
public class Service {
    private HttpServer httpServer;
    private Process nwProcess;

    public Service(HttpServer httpServer, Process nwProcess) {
        this.httpServer = httpServer;
        this.nwProcess = nwProcess;
    }

    /**
     * Wait indefinitely for the user to close the NW.js window, then shutdown the Jersey server.
     * @throws InterruptedException
     */
    public void waitForTerminationThenShutdown() throws InterruptedException {
        nwProcess.waitFor();

        // Stop all
        httpServer.shutdown(2, TimeUnit.SECONDS);
        httpServer.shutdownNow();
    }

    /**
     * @return the Jersey HTTP server started by jhod
     */
    public HttpServer getHttpServer() {
        return httpServer;
    }

    /**
     * @return the NW.js {@link Process} started by jhod
     */
    public Process getNwProcess() {
        return nwProcess;
    }
}
