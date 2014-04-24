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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.shared.release.versions.DefaultVersionInfo;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

@Mojo(name = "release-start", aggregator = true)
public class GitFlowReleaseStartMojo extends AbstractGitFlowMojo {

    @Component
    private Prompter prompter;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {

            String defaultVersion = "1.0.0";
            // get default release version
            try {
                DefaultVersionInfo versionInfo = new DefaultVersionInfo(
                        project.getVersion());
                defaultVersion = versionInfo.getReleaseVersionString();
            } catch (VersionParseException e) {
                if (getLog().isDebugEnabled()) {
                    getLog().debug(e);
                }
            }

            String version = null;
            try {
                version = prompter.prompt("release version [" + defaultVersion
                        + "]");
            } catch (PrompterException e) {
                getLog().error(e);
            }

            if (StringUtils.isBlank(version)) {
                version = defaultVersion;
            }

            // git branch --list release/*
            final String releaseBranches = executeGitCommandReturn("branch",
                    "--list", "release/*");

            if (StringUtils.isNotBlank(releaseBranches)) {
                throw new MojoFailureException(
                        "Release branch already exists. Cannot start release.");
            }

            // git checkout -b release/... develop
            executeGitCommand("checkout", "-b", "release/" + version, "develop");

            // mvn versions:set -DnewVersion=... -DgenerateBackupPoms=false
            executeMvnCommand(VERSIONS_MAVEN_PLUGIN + ":set", "-DnewVersion="
                    + version, "-DgenerateBackupPoms=false");

            // git commit -a -m updating poms for release
            executeGitCommand("commit", "-a", "-m", "updating poms for release");
        } catch (CommandLineException e) {
            e.printStackTrace();
        }
    }
}
