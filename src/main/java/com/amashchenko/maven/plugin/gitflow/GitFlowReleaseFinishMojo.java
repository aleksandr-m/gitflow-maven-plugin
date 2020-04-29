/*
 * Copyright 2014-2020 Aleksandr Mashchenko.
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
import org.codehaus.plexus.util.StringUtils;

/**
 * The git flow release finish mojo.
 *
 */
@Mojo(name = "release-finish", aggregator = true)
public class GitFlowReleaseFinishMojo extends AbstractGitFlowMojo {

    /** Whether to skip tagging the release in Git. */
    @Parameter(property = "skipTag", defaultValue = "false")
    private boolean skipTag = false;

    /** Whether to keep release branch after finish. */
    @Parameter(property = "keepBranch", defaultValue = "false")
    private boolean keepBranch = false;

    /**
     * Whether to skip calling Maven test goal before merging the branch.
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
     * Whether to commit development version when starting the release (vs when
     * finishing the release which is the default). Has effect only when there
     * are separate development and production branches.
     *
     * @since 1.7.0
     */
    @Parameter(property = "commitDevelopmentVersionAtStart", defaultValue = "false")
    private boolean commitDevelopmentVersionAtStart;

    /**
     * Maven goals to execute in the release branch before merging into the
     * production branch.
     *
     * @since 1.8.0
     */
    @Parameter(property = "preReleaseGoals")
    private String preReleaseGoals;

    /**
     * Maven goals to execute in the production branch after the release.
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

    /**
     * Whether to use snapshot in release.
     *
     * @since 1.10.0
     */
    @Parameter(property = "useSnapshotInRelease", defaultValue = "false")
    private boolean useSnapshotInRelease;

    /** {@inheritDoc} */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        validateConfiguration(preReleaseGoals, postReleaseGoals);

        try {
            // check uncommitted changes
            checkUncommittedChanges();

            // git for-each-ref --format='%(refname:short)' refs/heads/release/*
            String releaseBranch = gitFindBranches(gitFlowConfig.getReleaseBranchPrefix(), false).trim();

            if (StringUtils.isBlank(releaseBranch)) {
                if (fetchRemote) {
                    releaseBranch = gitFetchAndFindRemoteBranches(gitFlowConfig.getOrigin(),
                            gitFlowConfig.getReleaseBranchPrefix(), false).trim();
                    if (StringUtils.isBlank(releaseBranch)) {
                        throw new MojoFailureException("There is no remote or local release branch.");
                    }

                    // remove remote name with slash from branch name
                    releaseBranch = releaseBranch.substring(gitFlowConfig.getOrigin().length() + 1);

                    if (StringUtils.countMatches(releaseBranch, gitFlowConfig.getReleaseBranchPrefix()) > 1) {
                        throw new MojoFailureException(
                                "More than one remote release branch exists. Cannot finish release.");
                    }

                    gitCreateAndCheckout(releaseBranch, gitFlowConfig.getOrigin() + "/" + releaseBranch);
                } else {
                    throw new MojoFailureException("There is no release branch.");
                }
            }
            if (StringUtils.countMatches(releaseBranch, gitFlowConfig.getReleaseBranchPrefix()) > 1) {
                throw new MojoFailureException(
                        "More than one release branch exists. Cannot finish release.");
            }

            // check snapshots dependencies
            if (!allowSnapshots) {
                gitCheckout(releaseBranch);

                checkSnapshotDependencies();
            }

            if (fetchRemote) {
                // fetch and check remote
                gitFetchRemoteAndCompare(releaseBranch);

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

            // git checkout release/...
            gitCheckout(releaseBranch);

            if (!skipTestProject) {
                // mvn clean test
                mvnCleanTest();
            }

            // maven goals before merge
            if (StringUtils.isNotBlank(preReleaseGoals)) {
                mvnRun(preReleaseGoals);
            }

            String currentReleaseVersion = getCurrentProjectVersion();

            Map<String, String> messageProperties = new HashMap<String, String>();
            messageProperties.put("version", currentReleaseVersion);

            if (useSnapshotInRelease && ArtifactUtils.isSnapshot(currentReleaseVersion)) {
                String commitVersion = currentReleaseVersion.replace("-" + Artifact.SNAPSHOT_VERSION, "");

                mvnSetVersions(commitVersion);

                messageProperties.put("version", commitVersion);

                gitCommit(commitMessages.getReleaseFinishMessage(), messageProperties);
            }

            // git checkout master
            gitCheckout(gitFlowConfig.getProductionBranch());

            gitMerge(releaseBranch, releaseRebase, releaseMergeNoFF, releaseMergeFFOnly,
                    commitMessages.getReleaseFinishMergeMessage(), messageProperties);

            // get current project version from pom
            final String currentVersion = getCurrentProjectVersion();

            if (!skipTag) {
                String tagVersion = currentVersion;
                if ((tychoBuild || useSnapshotInRelease) && ArtifactUtils.isSnapshot(currentVersion)) {
                    tagVersion = currentVersion.replace("-"
                            + Artifact.SNAPSHOT_VERSION, "");
                }

                messageProperties.put("version", tagVersion);

                // git tag -a ...
                gitTag(gitFlowConfig.getVersionTagPrefix() + tagVersion,
                        commitMessages.getTagReleaseMessage(), gpgSignTag, messageProperties);
            }

            // maven goals after merge
            if (StringUtils.isNotBlank(postReleaseGoals)) {
                mvnRun(postReleaseGoals);
            }

            if (notSameProdDevName()) {
                // git checkout develop
                gitCheckout(gitFlowConfig.getDevelopmentBranch());

                // get develop version
                final String developReleaseVersion = getCurrentProjectVersion();
                if (commitDevelopmentVersionAtStart && useSnapshotInRelease) {
                    // updating develop poms to master version to avoid merge conflicts
                    mvnSetVersions(currentVersion);

                    // commit the changes
                    gitCommit(commitMessages.getUpdateDevToAvoidConflictsMessage());
                }

                // merge branch master into develop
                gitMerge(releaseBranch, releaseRebase, releaseMergeNoFF, false,
                        commitMessages.getReleaseFinishDevMergeMessage(), messageProperties);

                if (commitDevelopmentVersionAtStart && useSnapshotInRelease) {
                    // updating develop poms version back to pre merge state
                    mvnSetVersions(developReleaseVersion);

                    // commit the changes
                    gitCommit(commitMessages.getUpdateDevBackPreMergeStateMessage());
                }
            }

            if (commitDevelopmentVersionAtStart && !notSameProdDevName()) {
                getLog().warn(
                        "The commitDevelopmentVersionAtStart will not have effect. It can be enabled only when there are separate branches for development and production.");
                commitDevelopmentVersionAtStart = false;
            }

            if (!commitDevelopmentVersionAtStart) {
                // get next snapshot version
                final String nextSnapshotVersion;
                if (!settings.isInteractiveMode()
                        && StringUtils.isNotBlank(developmentVersion)) {
                    nextSnapshotVersion = developmentVersion;
                } else {
                    GitFlowVersionInfo versionInfo = new GitFlowVersionInfo(
                            currentVersion);
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

                // mvn versions:set -DnewVersion=... -DgenerateBackupPoms=false
                mvnSetVersions(nextSnapshotVersion);

                messageProperties.put("version", nextSnapshotVersion);

                // git commit -a -m updating for next development version
                gitCommit(commitMessages.getReleaseFinishMessage(), messageProperties);
            }

            if (installProject) {
                // mvn clean install
                mvnCleanInstall();
            }

            if (pushRemote) {
                gitPush(gitFlowConfig.getProductionBranch(), !skipTag);
                if (notSameProdDevName()) {
                    gitPush(gitFlowConfig.getDevelopmentBranch(), !skipTag);
                }

                if (!keepBranch) {
                    gitPushDelete(releaseBranch);
                }
            }

            if (!keepBranch) {
                // git branch -d release/...
                gitBranchDelete(releaseBranch);
            }
        } catch (Exception e) {
            throw new MojoFailureException("release-finish", e);
        }
    }
}
