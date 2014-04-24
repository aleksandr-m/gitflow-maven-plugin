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
package com.amashchenko.gitflow.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

public abstract class AbstractGitFlowMojo extends AbstractMojo {

    @Parameter(defaultValue = "${gitFlowConfig}")
    protected GitFlowConfig gitFlowConfig;

    private final String gitExec = "git"
            + (Os.isFamily(Os.FAMILY_WINDOWS) ? ".exe" : "");
    private final Commandline cmdGit = new Commandline(gitExec);

    private final String mvnExec = "mvn"
            + (Os.isFamily(Os.FAMILY_WINDOWS) ? ".bat" : "");
    private final Commandline cmdMvn = new Commandline(mvnExec);

    protected static final String VERSIONS_MAVEN_PLUGIN = "org.codehaus.mojo:versions-maven-plugin:2.1";

    @Component
    protected MavenProject project;

    protected String executeGitCommandReturn(final String... args)
            throws CommandLineException, MojoFailureException {
        return executeCommand(cmdGit, false, true, args);
    }

    protected void executeGitCommand(final String... args)
            throws CommandLineException, MojoFailureException {
        executeCommand(cmdGit, true, false, args);
    }

    protected void executeMvnCommand(final String... args)
            throws CommandLineException, MojoFailureException {
        executeCommand(cmdMvn, true, false, args);
    }

    private String executeCommand(final Commandline cmd, final boolean showOut,
            final boolean returnOut, final String... args)
            throws CommandLineException, MojoFailureException {
        getLog().info(cmd.getExecutable() + " " + StringUtils.join(args, " "));

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
