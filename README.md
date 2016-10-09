# jhod - Java HTML on Desktop

**jhod** is a platform which enables you to write desktop applications with all Web technologies for the GUI, and use Java JVM for "backend" (or heavy) processes.

It is based on [NW.js](http://nwjs.io/) + [Jersey](http://jersey.java.net/). So you can use HTML5 for frontend (for example) and use JAX-RS APIs as entry points for your Java processes.

# How to use

Create a new [Maven](https://maven.apache.org/) project and add this dependency to your `pom.xml`:
```xml
<dependency>
    <groupId>com.github.anthony-o</groupId>
    <artifactId>jhod</artifactId>
    <version>0.1.0</version>
</dependency>
```

In addition to the classical Maven Java project file structure, create the directory `src/main/app` in which you will develop your NW.js app.

In order to launch your app, here is the code you should write:
```java
Launcher.createBuilder()
        .appObject(this)
        .resourceConfig(new MyAppJerseyConfig())
        .build()
        .launchThenWaitForTerminationThenShutdown();
```

`MyAppJerseyConfig` references a class of your project specifying your Jersey [`ResourceConfig`](https://jersey.java.net/nonav/apidocs/2.23.2/jersey/org/glassfish/jersey/server/ResourceConfig.html).

Now add `NW_HOME` to your environment variables (make this pointing to the directory of the NW.js runtime you previously installed).

You're now able to start your Java project like any others and see it launching your NW.js app which will communicate with your Java JAX-RS classes through a local Jersey server launched on a port specified in `app/js/tempPort.js` (by default).

In order to package it, you can add this to your `pom.xml` in the `<build><plugins>` section:
```xml
<plugin>
    <artifactId>maven-assembly-plugin</artifactId>
    <version>2.6</version>
    <configuration>
        <descriptor>src/assembly/distrib.xml</descriptor>
    </configuration>
    <executions>
        <execution>
            <id>create-archive</id>
            <phase>package</phase>
            <goals>
                <goal>single</goal>
            </goals>
        </execution>
    </executions>
</plugin>
<plugin>
    <!-- Thanks to http://stackoverflow.com/a/25116745/535203 -->
    <artifactId>maven-dependency-plugin</artifactId>
    <version>2.10</version>
    <executions>
        <execution>
            <id>copy-dependencies</id>
            <phase>prepare-package</phase>
            <goals>
                <goal>copy-dependencies</goal>
            </goals>
            <configuration>
                <outputDirectory>${project.build.directory}/lib</outputDirectory>
                <overWriteReleases>false</overWriteReleases>
                <overWriteSnapshots>false</overWriteSnapshots>
                <overWriteIfNewer>true</overWriteIfNewer>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Here is the `distrib.xml` file you can write:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<assembly
        xmlns="http://maven.apache.org/plugins/maven-assembly-plugin/assembly/1.1.2"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://maven.apache.org/plugins/maven-assembly-plugin/assembly/1.1.2 http://maven.apache.org/xsd/assembly-1.1.2.xsd">
    <id>distrib</id>
    <formats>
        <format>zip</format>
    </formats>
    <fileSets>
        <fileSet>
            <directory>${project.basedir}/src/main/app</directory>
            <outputDirectory>app</outputDirectory>
            <includes>
                <include>**/*</include>
            </includes>
        </fileSet>
        <fileSet>
            <directory>${project.build.directory}/lib</directory>
            <outputDirectory>lib</outputDirectory>
            <includes>
                <include>*.jar</include>
            </includes>
        </fileSet>
        <fileSet>
            <directory>${project.build.directory}</directory>
            <outputDirectory></outputDirectory>
            <includes>
                <include>*.jar</include>
            </includes>
        </fileSet>
    </fileSets>
</assembly>
```

# Installation
Download and install a JDK 8 (or JRE) & a NW.js SDK (or "normal" runtime), then clone this project and execute this:
```bash
cd jhod/lib
mvn install
```

# TODOs / Ideas

Here are ideas for the future of this platform:
 * Develop an executable which will be the main entry to launch apps and will download & install the needed JRE & NW.js runtime (displaying a progress bar to the user) before really launching the app (using [Go](https://golang.org/) + [ui](https://github.com/andlabs/ui) for example in order to create native executables)
   * Those runtimes should be installed in the user system applications folder, or if he/she has not the rights, in his/her personal applications folder in order to share the runtimes between jhod applications
 * Enables the developer to specify (in a JSON) the version of Java JRE & NW.js runtime he/she wants