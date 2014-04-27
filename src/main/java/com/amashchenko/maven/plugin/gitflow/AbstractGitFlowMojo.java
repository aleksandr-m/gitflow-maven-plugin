/*
 * Copyright 2014 Aleksandr Mashchenko.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.amashchenko.maven.plugin.gitflow;

import java.io.FileReader;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * Abstract git flow mojo.
 * 
 * @author Aleksandr Mashchenko
 * 
 */
public abstract class AbstractGitFlowMojo extends AbstractMojo {

    /** Git flow configuration. */
    @Parameter(defaultValue = "${gitFlowConfig}")
    protected GitFlowConfig gitFlowConfig;

    /**
     * The path to the Maven executable. Defaults to either "mvn" or "mvn.bat"
     * depending on the operating system.
     */
    @Parameter(property = "mvnExecutable")
    private String mvnExecutable;
    /**
     * The path to the Git executable. Defaults to either "git" or "git.exe"
     * depending on the operating system.
     */
    @Parameter(property = "gitExecutable")
    private String gitExecutable;

    /** Command line for Git executable. */
    private final Commandline cmdGit = new Commandline();
    /** Command line for Maven executable. */
    private final Commandline cmdMvn = new Commandline();

    /** Versions Maven plugin full name. */
    protected static final String VERSIONS_MAVEN_PLUGIN = "org.codehaus.mojo:versions-maven-plugin:2.1";

    /** Maven project. */
    @Component
    private MavenProject project;
    /** Default prompter. */
    @Component
    protected Prompter prompter;

    /**
     * Initializes command line executables.
     * 
     */
    private void initExecutables() {
        if (StringUtils.isBlank(cmdMvn.getExecutable())) {
            if (StringUtils.isBlank(mvnExecutable)) {
                mvnExecutable = "mvn"
                        + (Os.isFamily(Os.FAMILY_WINDOWS) ? ".bat" : "");
            }
            cmdMvn.setExecutable(mvnExecutable);
        }
        if (StringUtils.isBlank(cmdGit.getExecutable())) {
            if (StringUtils.isBlank(gitExecutable)) {
                gitExecutable = "git"
                        + (Os.isFamily(Os.FAMILY_WINDOWS) ? ".exe" : "");
            }
            cmdGit.setExecutable(gitExecutable);
        }
    }

    /**
     * Gets current project version from pom.xml file.
     * 
     * @return Current project version.
     * @throws MojoFailureException
     */
    protected String getCurrentProjectVersion() throws MojoFailureException {
        try {
            // read pom.xml
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader(project.getFile()
                    .getAbsoluteFile()));
            return model.getVersion();
        } catch (Exception e) {
            throw new MojoFailureException("", e);
        }
    }

    /**
     * Checks uncommitted changes.
     * 
     * @throws MojoFailureException
     */
    protected void checkUncommittedChanges() throws MojoFailureException {
        if (executeGitHasUncommitted()) {
            throw new MojoFailureException(
                    "You have some uncommitted files. Commit or discard local changes in order to proceed.");
        }
    }

    /**
     * Executes Git commands to check for uncommitted changes.
     * 
     * @return <code>true</code> when there are uncommitted changes,
     *         <code>false</code> otherwise.
     */
    protected boolean executeGitHasUncommitted() {
        try {
            // diff-index
            executeGitCommand("diff-index", "--quiet", "HEAD");

            // check untracked files
            String untracked = executeGitCommandReturn("ls-files", "--others",
                    "--exclude-standard", "--error-unmatch");
            if (StringUtils.isNotBlank(untracked)) {
                return true;
            }
        } catch (Exception e) {
            return true;
        }

        return false;
    }

    /**
     * Executes Git command and returns output.
     * 
     * @param args
     *            Git command line arguments.
     * @return Command output.
     * @throws CommandLineException
     * @throws MojoFailureException
     */
    protected String executeGitCommandReturn(final String... args)
            throws CommandLineException, MojoFailureException {
        return executeCommand(cmdGit, false, true, args);
    }

    /**
     * Executes Git command.
     * 
     * @param args
     *            Git command line arguments.
     * @throws CommandLineException
     * @throws MojoFailureException
     */
    protected void executeGitCommand(final String... args)
            throws CommandLineException, MojoFailureException {
        executeCommand(cmdGit, true, false, args);
    }

    /**
     * Executes Maven command.
     * 
     * @param args
     *            Maven command line arguments.
     * @throws CommandLineException
     * @throws MojoFailureException
     */
    protected void executeMvnCommand(final String... args)
            throws CommandLineException, MojoFailureException {
        executeCommand(cmdMvn, true, false, args);
    }

    /**
     * Executes command line.
     * 
     * @param cmd
     *            Command line.
     * @param showOut
     *            Whether to print output.
     * @param returnOut
     *            Whether to return output.
     * @param args
     *            Command line arguments.
     * @return Output of the command or empty String depending on the @param
     *         returnOut value.
     * @throws CommandLineException
     * @throws MojoFailureException
     */
    private String executeCommand(final Commandline cmd, final boolean showOut,
            final boolean returnOut, final String... args)
            throws CommandLineException, MojoFailureException {
        initExecutables();

        if (getLog().isDebugEnabled()) {
            getLog().debug(
                    cmd.getExecutable() + " " + StringUtils.join(args, " "));
        }

        cmd.clearArgs();
        cmd.addArguments(args);

        CommandLineUtils.StringStreamConsumer out = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer err = new CommandLineUtils.StringStreamConsumer();
        final int exitCode = CommandLineUtils.executeCommandLine(cmd, out, err);

        if (showOut) {
            final String output = out.getOutput();
            if (!StringUtils.isEmpty(output)) {
                getLog().info(output);
            }
        }

        if (exitCode != 0) {
            throw new MojoFailureException(err.getOutput());
        }

        String ret = "";
        if (returnOut) {
            ret = out.getOutput();
        }
        return ret;
    }
}
