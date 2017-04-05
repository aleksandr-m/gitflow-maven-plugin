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
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

/**
 * The git flow release finish mojo.
 *
 * @author Aleksandr Mashchenko
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
            // check uncommitted changes
            checkUncommittedChanges();

            // git for-each-ref --format='%(refname:short)' refs/heads/release/*
            final String releaseBranch = gitFindBranches(
                    gitFlowConfig.getReleaseBranchPrefix(), false).trim();

            if (StringUtils.isBlank(releaseBranch)) {
                throw new MojoFailureException("There is no release branch.");
            } else if (StringUtils.countMatches(releaseBranch,
                    gitFlowConfig.getReleaseBranchPrefix()) > 1) {
                throw new MojoFailureException(
                        "More than one release branch exists. Cannot finish release.");
            }

            // check snapshots dependencies
            if (!allowSnapshots) {
                gitCheckout(releaseBranch);

                checkSnapshotDependencies();
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

            if (!skipTestProject) {
                // git checkout release/...
                gitCheckout(releaseBranch);

                // mvn clean test
                mvnCleanTest();
            }

            // git checkout master
            gitCheckout(gitFlowConfig.getProductionBranch());

            gitMerge(releaseBranch, releaseRebase, releaseMergeNoFF,
                    releaseMergeFFOnly);

            // get current project version from pom
            final String currentVersion = getCurrentProjectVersion();
            String tagVersion = currentVersion;

            if (!skipTag) {
                if (tychoBuild && ArtifactUtils.isSnapshot(currentVersion)) {
                    tagVersion = currentVersion.replace("-"
                            + Artifact.SNAPSHOT_VERSION, "");
                }

                tagVersion = gitFlowConfig.getVersionTagPrefix() + tagVersion;

                // git tag -a ...
                gitTag(tagVersion, commitMessages.getTagReleaseMessage());
            }

            if (notSameProdDevName()) {
                // git checkout develop
                gitCheckout(gitFlowConfig.getDevelopmentBranch());

                gitMerge(releaseBranch, releaseRebase, releaseMergeNoFF,
                        releaseMergeFFOnly);
            }

            String nextSnapshotVersion = null;
            // get next snapshot version
            try {
                final DefaultVersionInfo versionInfo = new DefaultVersionInfo(
                        currentVersion);
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
            mvnSetVersions(nextSnapshotVersion);

            // git commit -a -m updating for next development version
            gitCommit(commitMessages.getReleaseFinishMessage());

            if (installProject) {
                // mvn clean install
                mvnCleanInstall();
            }

            if (!keepBranch) {
                // git branch -d release/...
                gitBranchDelete(releaseBranch);
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
