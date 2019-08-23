/*
 * Copyright 2014-2019 Aleksandr Mashchenko.
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

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.util.StringUtils;

/**
 * The git flow release mojo.
 * 
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
     * Whether to allow SNAPSHOT versions in dependencies.
     * 
     * @since 1.2.2
     */
    @Parameter(property = "allowSnapshots", defaultValue = "false")
    private boolean allowSnapshots = false;

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
     * Whether to push to the remote.
     * 
     * @since 1.3.0
     */
    @Parameter(property = "pushRemote", defaultValue = "true")
    private boolean pushRemote;

    /**
     * Release version to use instead of the default next release version in non
     * interactive mode.
     * 
     * @since 1.3.1
     */
    @Parameter(property = "releaseVersion", defaultValue = "")
    private String releaseVersion = "";

    /**
     * Whether to use <code>--ff-only</code> option when merging.
     * 
     * @since 1.4.0
     */
    @Parameter(property = "releaseMergeFFOnly", defaultValue = "false")
    private boolean releaseMergeFFOnly = false;

    /**
     * Whether to remove qualifiers from the next development version.
     * 
     * @since 1.6.0
     */
    @Parameter(property = "digitsOnlyDevVersion", defaultValue = "false")
    private boolean digitsOnlyDevVersion = false;

    /**
     * Development version to use instead of the default next development
     * version in non interactive mode.
     * 
     * @since 1.6.0
     */
    @Parameter(property = "developmentVersion", defaultValue = "")
    private String developmentVersion = "";

    /**
     * Which digit to increment in the next development version. Starts from
     * zero.
     * 
     * @since 1.6.0
     */
    @Parameter(property = "versionDigitToIncrement")
    private Integer versionDigitToIncrement;

    /**
     * Maven goals to execute before the release.
     * 
     * @since 1.8.0
     */
    @Parameter(property = "preReleaseGoals")
    private String preReleaseGoals;

    /**
     * Maven goals to execute after the release.
     * 
     * @since 1.8.0
     */
    @Parameter(property = "postReleaseGoals")
    private String postReleaseGoals;

    /**
     * Whether to make a GPG-signed tag.
     * 
     * @since 1.9.0
     */
    @Parameter(property = "gpgSignTag", defaultValue = "false")
    private boolean gpgSignTag = false;

    /** {@inheritDoc} */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        validateConfiguration(preReleaseGoals, postReleaseGoals);

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
                    throw new MojoFailureException("release", e);
                }
            } else {
                version = releaseVersion;
            }

            if (StringUtils.isBlank(version)) {
                getLog().info("Version is blank. Using default version.");
                version = defaultVersion;
            }

            // maven goals before release
            if (StringUtils.isNotBlank(preReleaseGoals)) {
                mvnRun(preReleaseGoals);
            }

            Map<String, String> messageProperties = new HashMap<String, String>();
            messageProperties.put("version", version);

            // execute if version changed
            if (!version.equals(currentVersion)) {
                // mvn set version
                mvnSetVersions(version);

                // git commit -a -m updating versions for release
                gitCommit(commitMessages.getReleaseStartMessage(), messageProperties);
            }

            if (notSameProdDevName()) {
                // git checkout master
                gitCheckout(gitFlowConfig.getProductionBranch());

                gitMerge(gitFlowConfig.getDevelopmentBranch(), releaseRebase,
                        releaseMergeNoFF, releaseMergeFFOnly, commitMessages.getReleaseFinishMergeMessage(),
                        messageProperties);
            }

            if (!skipTag) {
                if (tychoBuild && ArtifactUtils.isSnapshot(version)) {
                    version = version.replace("-" + Artifact.SNAPSHOT_VERSION, "");
                }

                messageProperties.put("version", version);

                // git tag -a ...
                gitTag(gitFlowConfig.getVersionTagPrefix() + version,
                        commitMessages.getTagReleaseMessage(), gpgSignTag, messageProperties);
            }

            // maven goals after release
            if (StringUtils.isNotBlank(postReleaseGoals)) {
                mvnRun(postReleaseGoals);
            }

            if (notSameProdDevName()) {
                // git checkout develop
                gitCheckout(gitFlowConfig.getDevelopmentBranch());
            }

            // get next snapshot version
            final String nextSnapshotVersion;
            if (!settings.isInteractiveMode()
                    && StringUtils.isNotBlank(developmentVersion)) {
                nextSnapshotVersion = developmentVersion;
            } else {
                GitFlowVersionInfo versionInfo = new GitFlowVersionInfo(version);
                if (digitsOnlyDevVersion) {
                    versionInfo = versionInfo.digitsVersionInfo();
                }

                nextSnapshotVersion = versionInfo
                        .nextSnapshotVersion(versionDigitToIncrement);
            }

            if (StringUtils.isBlank(nextSnapshotVersion)) {
                throw new MojoFailureException(
                        "Next snapshot version is blank.");
            }

            // mvn set version
            mvnSetVersions(nextSnapshotVersion);

            messageProperties.put("version", nextSnapshotVersion);

            // git commit -a -m updating for next development version
            gitCommit(commitMessages.getReleaseFinishMessage(), messageProperties);

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
        } catch (Exception e) {
            throw new MojoFailureException("release", e);
        }
    }
}
