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
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.shared.release.versions.DefaultVersionInfo;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

@Mojo(name = "release-finish", aggregator = true)
public class GitFlowReleaseFinishMojo extends AbstractGitFlowMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            // git branch --list release/*
            final String releaseBranches = executeGitCommandReturn("branch",
                    "--list", "release/*");

            String releaseVersion = null;

            // TODO improve that
            if (StringUtils.isNotBlank(releaseBranches)
                    && StringUtils.countMatches(releaseBranches, "release/") > 1) {
                throw new MojoFailureException(
                        "More than one release branch exists. Cannot finish release.");
            } else {
                // remove * in case current branch is the release branch
                releaseVersion = releaseBranches.trim().substring(
                        releaseBranches.indexOf("release/") + 8);
            }

            if (StringUtils.isBlank(releaseVersion)) {
                throw new MojoFailureException("Release version is blank.");
            }

            // git checkout master
            executeGitCommand("checkout", "master");

            // git merge --no-ff release/...
            executeGitCommand("merge", "--no-ff", "release/" + releaseVersion);

            // TODO v
            // git tag -a ...
            executeGitCommand("tag", "-a", "v" + releaseVersion, "-m",
                    "tagging release");

            // git checkout develop
            executeGitCommand("checkout", "develop");

            // git merge --no-ff release/...
            executeGitCommand("merge", "--no-ff", "release/" + releaseVersion);

            String nextSnapshotVersion = null;
            // get next snapshot version
            try {
                DefaultVersionInfo versionInfo = new DefaultVersionInfo(
                        project.getVersion());
                nextSnapshotVersion = versionInfo.getNextVersion()
                        .getSnapshotVersionString();
            } catch (VersionParseException e) {
                if (getLog().isDebugEnabled()) {
                    getLog().debug(e);
                }
            }

            if (StringUtils.isBlank(nextSnapshotVersion)) {
                throw new MojoFailureException(
                        "Next snapshot version is blank.");
            }

            // mvn versions:set -DnewVersion=... -DgenerateBackupPoms=false
            executeMvnCommand(VERSIONS_MAVEN_PLUGIN + ":set", "-DnewVersion="
                    + nextSnapshotVersion, "-DgenerateBackupPoms=false");

            // git commit -a -m updating poms for ... release
            executeGitCommand("commit", "-a", "-m",
                    "updating poms for next development version");

            // git branch -d release/...
            executeGitCommand("branch", "-d", "release/" + releaseVersion);
        } catch (CommandLineException e) {
            e.printStackTrace();
        }
    }
}
