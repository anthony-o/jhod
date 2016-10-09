package com.github.anthony_o.jhod;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class LauncherTest {
    @Test(expected = NullPointerException.class)
    public void launchWithoutConfigRessourceTest() throws InterruptedException, IOException, URISyntaxException {
        Launcher.createBuilder().appObject(this).build().launchThenWaitForTerminationThenShutdown();
    }

    @Test(expected = IllegalArgumentException.class)
    public void launchWithoutAppObjectNorAppClassNorAppPathTest() throws InterruptedException, IOException, URISyntaxException {
        Launcher.createBuilder().resourceConfig(new ResourceConfig()).build().launchThenWaitForTerminationThenShutdown();
    }

    @Test(expected = NullPointerException.class)
    public void launchWithoutNwHomeDefinedTest() throws InterruptedException, IOException, URISyntaxException {
        Launcher.createBuilder()
                .resourceConfig(new ResourceConfig())
                .appObject(this)
                .build().launchThenWaitForTerminationThenShutdown();
    }
}
