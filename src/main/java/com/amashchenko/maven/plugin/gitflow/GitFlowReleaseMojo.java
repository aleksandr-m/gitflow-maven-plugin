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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.release.versions.DefaultVersionInfo;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

/**
 * The git flow release mojo.
 *
 * @author Aleksandr Mashchenko
 * @since 1.2.0
 */
@Mojo(name = "release", aggregator = true)
public class GitFlowReleaseMojo extends AbstractGitFlowMojo {

    /** Whether to skip tagging the release in Git. */
    @Parameter(property = "skipTag", defaultValue = "false")
    private boolean skipTag = false;

    /**
     * Whether to skip calling Maven test goal before releasing.
     *
     * @since 1.0.5
     */
    @Parameter(property = "skipTestProject", defaultValue = "false")
    private boolean skipTestProject = false;

    /**
     * Whether to rebase branch or merge. If <code>true</code> then rebase will
     * be performed.
     *
     * @since 1.2.3
     */
    @Parameter(property = "releaseRebase", defaultValue = "false")
    private boolean releaseRebase = false;

    /**
     * Whether to use <code>--no-ff</code> option when merging.
     *
     * @since 1.2.3
     */
    @Parameter(property = "releaseMergeNoFF", defaultValue = "true")
    private boolean releaseMergeNoFF = true;

    /**
     * Whether to use <code>--ff-only</code> option when merging.
     *
     * @since 1.4.0
     */
    @Parameter(property = "releaseMergeFFOnly", defaultValue = "false")
    private boolean releaseMergeFFOnly = false;

    /**
     * Release version to use instead of the default next release version in non
     * interactive mode.
     *
     * @since 1.3.1
     */
    @Parameter(property = "releaseVersion", defaultValue = "")
    private String releaseVersion = "";

    /**
     * Whether to skip deploying created tag to nexus, assuming tag was created.
     *
     * @since 1.4.2
     */
    @Parameter(property = "skipMvnDeploy", defaultValue = "true")
    private boolean skipMvnDeploy = true;

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

                if (notSameProdDevName()) {
                    // checkout from remote if doesn't exist
                    gitFetchRemoteAndCreate(gitFlowConfig.getProductionBranch());

                    // fetch and check remote
                    gitFetchRemoteAndCompare(gitFlowConfig
                            .getProductionBranch());
                }
            }

            // need to be in develop to check snapshots and to get correct
            // project version
            // git checkout develop
            gitCheckout(gitFlowConfig.getDevelopmentBranch());

            // check snapshots dependencies
            if (!allowSnapshots) {
                checkSnapshotDependencies();
            }

            if (!skipTestProject) {
                // mvn clean test
                mvnCleanTest();
            }

            // get current project version from pom
            final String currentVersion = getCurrentProjectVersion();

            String defaultVersion = null;
            if (tychoBuild) {
                defaultVersion = currentVersion;
            } else {
                // get default release version
                try {
                    final DefaultVersionInfo versionInfo = new DefaultVersionInfo(
                            currentVersion);
                    defaultVersion = versionInfo.getReleaseVersionString();
                } catch (VersionParseException e) {
                    if (getLog().isDebugEnabled()) {
                        getLog().debug(e);
                    }
                }
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
                                && (!validVersion(version) || !validBranchName(version))) {
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

            // execute if version changed
            if (!version.equals(currentVersion)) {
                // mvn set version
                mvnSetVersions(version);

                // git commit -a -m updating versions for release
                gitCommit(commitMessages.getReleaseStartMessage());
            }

            if (notSameProdDevName()) {
                // git checkout master
                gitCheckout(gitFlowConfig.getProductionBranch());

                gitMerge(gitFlowConfig.getDevelopmentBranch(), releaseRebase,
                        releaseMergeNoFF, releaseMergeFFOnly);
            }

            String tagVersion = null;

            if (!skipTag) {
                if (tychoBuild && ArtifactUtils.isSnapshot(version)) {
                    version = version.replace("-" + Artifact.SNAPSHOT_VERSION,
                            "");
                }

                tagVersion = gitFlowConfig.getVersionTagPrefix() + version;

                // git tag -a ...
                gitTag(tagVersion, commitMessages.getTagReleaseMessage());
            }

            if (notSameProdDevName()) {
                // git checkout develop
                gitCheckout(gitFlowConfig.getDevelopmentBranch());
            }

            String nextSnapshotVersion = null;
            // get next snapshot version
            try {
                final DefaultVersionInfo versionInfo = new DefaultVersionInfo(
                        version);
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

            // mvn set version
            mvnSetVersions(nextSnapshotVersion);

            // git commit -a -m updating for next development version
            gitCommit(commitMessages.getReleaseFinishMessage());

            if (installProject) {
                // mvn clean install
                mvnCleanInstall();
            }

            if (pushRemote) {
                gitPush(gitFlowConfig.getProductionBranch(), !skipTag);
                if (notSameProdDevName()) {
                    gitPush(gitFlowConfig.getDevelopmentBranch(), !skipTag);
                }
            }

            if (!skipTag && !skipMvnDeploy) {
                mvnDeploy(tagVersion);
            }
        } catch (CommandLineException e) {
            getLog().error(e);
        }
    }
}
