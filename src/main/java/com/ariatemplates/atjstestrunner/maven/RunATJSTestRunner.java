/*
 * Copyright 2012 Amadeus s.a.s.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ariatemplates.atjstestrunner.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public abstract class RunATJSTestRunner extends RunNode {

    /**
     * Configuration file to use. More information about the format to use is
     * documented <a
     * href="https://github.com/ariatemplates/atjstestrunner-nodejs#usage"
     * >here</a>.
     *
     * @parameter
     */
    public File configFile;

    /**
     * Directory to use as the root of the web server. (Passed through
     * <code>--config.resources./</code> to <a
     * href="https://github.com/ariatemplates/atjstestrunner-nodejs#usage"
     * >atjstestrunner</a>)
     *
     * @parameter
     *            expression="${project.build.directory}/${project.build.finalName}"
     */
    public File webappDirectory;

    /**
     * Aria Templates bootstrap file. (Passed through
     * <code>--config.tests.aria-templates.bootstrap</code> to <a
     * href="https://github.com/ariatemplates/atjstestrunner-nodejs#usage"
     * >atjstestrunner</a>)
     *
     * @parameter expression="aria/aria-templates-${at.version}.js"
     */
    public String ariaTemplatesBootstrap;

    /**
     * Aria Templates test classpaths to include. (Passed through
     * <code>--config.tests.aria-templates.classpaths.includes</code> to <a
     * href="https://github.com/ariatemplates/atjstestrunner-nodejs#usage"
     * >atjstestrunner</a>)
     *
     * @parameter
     */
    public String[] ariaTemplatesClasspathsIncludes = new String[] { "MainTestSuite" };

    /**
     * Aria Templates test classpaths to exclude. (Passed through
     * <code>--config.tests.aria-templates.classpaths.excludes</code> to <a
     * href="https://github.com/ariatemplates/atjstestrunner-nodejs#usage"
     * >atjstestrunner</a>)
     *
     * @parameter
     */
    public String[] ariaTemplatesClasspathsExcludes;

    /**
     * Directory for the set of JUnit-style report files. (Passed through
     * <code>--config.test-reports.xml-directory</code> to <a
     * href="https://github.com/ariatemplates/atjstestrunner-nodejs#usage"
     * >atjstestrunner</a>)
     *
     * @parameter expression="${project.build.directory}/jstestdriver"
     */
    public File xmlReportsDirectory;

    /**
     * Single JUnit-style file report. (Passed through
     * <code>--config.test-reports.xml-file</code> to <a
     * href="https://github.com/ariatemplates/atjstestrunner-nodejs#usage"
     * >atjstestrunner</a>)
     *
     * @parameter expression="${project.build.directory}/atjstestsReport.xml"
     */
    public File xmlReportFile;

    /**
     * JSON file report. (Passed through
     * <code>--config.test-reports.json-file</code> to <a
     * href="https://github.com/ariatemplates/atjstestrunner-nodejs#usage"
     * >atjstestrunner</a>)
     *
     * @parameter expression="${project.build.directory}/atjstestsReport.json"
     */
    public File jsonReportFile;

    /**
     * JSON coverage file report. (Passed through
     * <code>--config.coverage-reports.json-file</code> to <a
     * href="https://github.com/ariatemplates/atjstestrunner-nodejs#usage"
     * >atjstestrunner</a>)
     *
     * @parameter
     *            expression="${project.build.directory}/atjstestsCoverageReport.json"
     */
    public File jsonCoverageReportFile;

    /**
     * <a href="http://ltp.sourceforge.net/coverage/lcov/geninfo.1.php">lcov</a>
     * coverage file report. (Passed through
     * <code>--config.coverage-reports.lcov-file</code> to <a
     * href="https://github.com/ariatemplates/atjstestrunner-nodejs#usage"
     * >atjstestrunner</a>)
     *
     * @parameter expression=
     *            "${project.build.directory}/jstestdriver/jsTestDriver.conf-coverage.dat"
     */
    public File lcovCoverageReportFile;

    /**
     * Path to the atjstestrunner directory. If not defined, atjstestrunner is
     * extracted from the the following maven artifact:
     * <code>com.ariatemplates.atjstestrunner:atjstestrunner-nodejs:zip:project</code>
     *
     * @parameter expression="${com.ariatemplates.atjstestrunner.path}"
     */
    public File atjstestrunnerPath;

    /**
     * Port for the internal web server. (Passed through <code>--port</code> to
     * <a href="https://github.com/ariatemplates/atjstestrunner-nodejs#usage"
     * >atjstestrunner</a>)
     *
     * @parameter
     */
    public Integer port;

    /**
     * Enables or disables colors. (If false, passes <code>--no-colors</code> to
     * <a href="https://github.com/ariatemplates/atjstestrunner-nodejs#usage"
     * >atjstestrunner</a>)
     *
     * @parameter
     */
    public boolean colors = false;

    private static final String PATH_IN_ATJSTESTRUNNER_DIRECTORY = "bin" + File.separator + "atjstestrunner.js";

    protected File atjstestrunnerJsMainFile;
    protected File phantomjsExecutable;

    public static Dependency getATJSTestRunnerDependency() {
        Dependency dependency = new Dependency();
        dependency.setGroupId("com.ariatemplates.atjstestrunner");
        dependency.setArtifactId("atjstestrunner-nodejs");
        dependency.setVersion(RunATJSTestRunner.class.getPackage().getImplementationVersion());
        dependency.setClassifier("project");
        dependency.setType("zip");
        return dependency;
    }

    protected File extractDependency(File property, Dependency dependency, String pathAfterProperty, String pathAfterDependency) {
        File res;
        try {
            if (property != null) {
                res = new File(property, pathAfterProperty);
            } else {
                ArtifactExtractor extractor = new ArtifactExtractor();
                extractor.setLog(this.getLog());
                String outputDirectory = extractor.inplaceExtractDependency(session.getLocalRepository(), dependency);
                res = new File(outputDirectory, pathAfterDependency);
            }
            if (!res.exists()) {
                throw new FileNotFoundException("Could not find file: " + res.getAbsolutePath());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find or extract " + dependency.getArtifactId(), e);
        }
        return res;
    }

    protected void extractATJSTestRunner() {
        atjstestrunnerJsMainFile = extractDependency(atjstestrunnerPath, getATJSTestRunnerDependency(), PATH_IN_ATJSTESTRUNNER_DIRECTORY, "atjstestrunner"
            + File.separator + PATH_IN_ATJSTESTRUNNER_DIRECTORY);
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        extractATJSTestRunner();
        super.execute();
    }

    protected void addMultipleOptions(List<String> optionsArray, String optionName, String[] array) {
        if (array != null) {
            for (String item : array) {
                optionsArray.add(optionName);
                optionsArray.add(item);
            }
        }
    }

    @Override
    protected List<String> getNodeArguments() {
        List<String> res = new LinkedList<String>();
        res.add(atjstestrunnerJsMainFile.getAbsolutePath());

        if (configFile != null) {
            res.add(configFile.getAbsolutePath());
        }

        if (port != null) {
            res.add("--port");
            res.add(port.toString());
        }

        if (!colors) {
            res.add("--no-colors");
        }

        res.add("--config.resources./");
        res.add(webappDirectory.getAbsolutePath());

        res.add("--config.test-reports.xml-directory");
        res.add(xmlReportsDirectory.getAbsolutePath());

        res.add("--config.test-reports.xml-file");
        res.add(xmlReportFile.getAbsolutePath());

        res.add("--config.test-reports.json-file");
        res.add(jsonReportFile.getAbsolutePath());

        res.add("--config.coverage-reports.json-file");
        res.add(jsonCoverageReportFile.getAbsolutePath());

        res.add("--config.coverage-reports.lcov-file");
        res.add(lcovCoverageReportFile.getAbsolutePath());

        res.add("--config.tests.aria-templates.bootstrap");
        res.add(ariaTemplatesBootstrap);

        addMultipleOptions(res, "--config.tests.aria-templates.classpaths.includes", ariaTemplatesClasspathsIncludes);
        addMultipleOptions(res, "--config.tests.aria-templates.classpaths.excludes", ariaTemplatesClasspathsExcludes);

        addExtraAtjstestrunnerOptions(res);

        res.addAll(arguments);

        return res;
    }

    protected void addExtraAtjstestrunnerOptions(List<String> list) {
    }

}