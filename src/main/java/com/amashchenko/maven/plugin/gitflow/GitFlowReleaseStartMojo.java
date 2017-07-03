/*
 * Copyright 2014-2017 Aleksandr Mashchenko.
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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

/**
 * The git flow release start mojo.
 * 
 */
@Mojo(name = "release-start", aggregator = true)
public class GitFlowReleaseStartMojo extends AbstractGitFlowMojo {

    /**
     * Whether to use the same name of the release branch for every release.
     * Default is <code>false</code>, i.e. project version will be added to
     * release branch prefix. <br/>
     * <br/>
     * 
     * Note: By itself the default releaseBranchPrefix is not a valid branch
     * name. You must change it when setting sameBranchName to <code>true</code>
     * .
     * 
     * @since 1.2.0
     */
    @Parameter(property = "sameBranchName", defaultValue = "false")
    private boolean sameBranchName = false;

    /**
     * Whether to allow SNAPSHOT versions in dependencies.
     * 
     * @since 1.2.2
     */
    @Parameter(property = "allowSnapshots", defaultValue = "false")
    private boolean allowSnapshots = false;

    /**
     * Release version to use instead of the default next release version in non
     * interactive mode.
     * 
     * @since 1.3.1
     */
    @Parameter(property = "releaseVersion", defaultValue = "")
    private String releaseVersion = "";

    /**
     * Whether to push to the remote.
     * 
     * @since 1.6.0
     */
    @Parameter(property = "pushRemote", defaultValue = "false")
    private boolean pushRemote;

    /** {@inheritDoc} */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            // set git flow configuration
            initGitFlowConfig();

            // check uncommitted changes
            checkUncommittedChanges();

            // git for-each-ref --count=1 refs/heads/release/*
            final String releaseBranch = gitFindBranches(
                    gitFlowConfig.getReleaseBranchPrefix(), true);

            if (StringUtils.isNotBlank(releaseBranch)) {
                throw new MojoFailureException(
                        "Release branch already exists. Cannot start release.");
            }

            if (fetchRemote) {
                // checkout from remote if doesn't exist
                gitFetchRemoteAndCreate(gitFlowConfig.getDevelopmentBranch());

                // fetch and check remote
                gitFetchRemoteAndCompare(gitFlowConfig.getDevelopmentBranch());
            }

            // need to be in develop to check snapshots and to get
            // correct
            // project version
            gitCheckout(gitFlowConfig.getDevelopmentBranch());

            // check snapshots dependencies
            if (!allowSnapshots) {
                checkSnapshotDependencies();
            }

            // get current project version from pom
            final String currentVersion = getCurrentProjectVersion();

            String defaultVersion = null;
            if (tychoBuild) {
                defaultVersion = currentVersion;
            } else {
                // get default release version
                defaultVersion = new GitFlowVersionInfo(currentVersion)
                        .getReleaseVersionString();
            }

            if (defaultVersion == null) {
                throw new MojoFailureException(
                        "Cannot get default project version.");
            }

            String version = null;
            if (settings.isInteractiveMode()) {
                try {
                    while (version == null) {
                        version = prompter.prompt("What is release version? ["
                                + defaultVersion + "]");

                        if (!"".equals(version)
                                && (!GitFlowVersionInfo.isValidVersion(version) || !validBranchName(version))) {
                            getLog().info("The version is not valid.");
                            version = null;
                        }
                    }
                } catch (PrompterException e) {
                    getLog().error(e);
                }
            } else {
                version = releaseVersion;
            }

            if (StringUtils.isBlank(version)) {
                version = defaultVersion;
            }

            String branchName = gitFlowConfig.getReleaseBranchPrefix();
            if (!sameBranchName) {
                branchName += version;
            }

            // git checkout -b release/... develop
            gitCreateAndCheckout(branchName,
                    gitFlowConfig.getDevelopmentBranch());

            // execute if version changed
            if (!version.equals(currentVersion)) {
                // mvn versions:set -DnewVersion=... -DgenerateBackupPoms=false
                mvnSetVersions(version);

                // git commit -a -m updating versions for release
                gitCommit(commitMessages.getReleaseStartMessage());
            }

            if (installProject) {
                // mvn clean install
                mvnCleanInstall();
            }

            if (pushRemote) {
                gitPush(branchName, false);
            }
        } catch (CommandLineException e) {
            getLog().error(e);
        } catch (VersionParseException e) {
            getLog().error(e);
        }
    }
}
