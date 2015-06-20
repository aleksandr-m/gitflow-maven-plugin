/*
 * Copyright 2014-2015 Aleksandr Mashchenko.
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
import org.apache.maven.shared.release.versions.DefaultVersionInfo;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

import com.amashchenko.maven.plugin.gitflow.i18n.CommitMessages;
import com.amashchenko.maven.plugin.gitflow.i18n.ErrorMessages;

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

    /** Whether to skip calling Maven test goal before merging the branch. */
    @Parameter(property = "skipTestProject", defaultValue = "false")
    private boolean skipTestProject = false;

    /** {@inheritDoc} */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            // check uncommitted changes
            checkUncommittedChanges();

            // git for-each-ref --format='%(refname:short)' refs/heads/release/*
            final String releaseBranches = gitFindBranches(gitFlowConfig
                    .getReleaseBranchPrefix());

            String releaseVersion = null;

            if (StringUtils.isBlank(releaseBranches)) {
                throw new MojoFailureException(msg.getMessage(ErrorMessages.no_release_branch_found));
            } else if (StringUtils.countMatches(releaseBranches,
                    gitFlowConfig.getReleaseBranchPrefix()) > 1) {
                throw new MojoFailureException(msg.getMessage(ErrorMessages.release_branch_not_unique));
            } else {
                releaseVersion = releaseBranches.trim().substring(
                        releaseBranches.lastIndexOf(gitFlowConfig
                                .getReleaseBranchPrefix())
                                + gitFlowConfig.getReleaseBranchPrefix()
                                        .length());
            }

            if (StringUtils.isBlank(releaseVersion)) {
                throw new MojoFailureException(msg.getMessage(ErrorMessages.release_branch_name_empty));
            }

            // git checkout release/...
            gitCheckout(releaseBranches.trim());

            if (!skipTestProject) {
                // mvn clean test
                mvnCleanTest();
            }

            // git checkout master
            gitCheckout(gitFlowConfig.getProductionBranch());

            // git merge --no-ff release/...
            gitMergeNoff(gitFlowConfig.getReleaseBranchPrefix()
                    + releaseVersion);

            if (!skipTag) {
                // git tag -a ...
                gitTag(gitFlowConfig.getVersionTagPrefix() + releaseVersion,
                		msg.getMessage(CommitMessages.tagging_release));
            }

            // git checkout develop
            gitCheckout(gitFlowConfig.getDevelopmentBranch());

            // git merge --no-ff release/...
            gitMergeNoff(gitFlowConfig.getReleaseBranchPrefix()
                    + releaseVersion);

            // get current project version from pom
            final String currentVersion = getCurrentProjectVersion();

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
                throw new MojoFailureException(msg.getMessage(ErrorMessages.next_snapshot_version_empty));
            }

            // mvn versions:set -DnewVersion=... -DgenerateBackupPoms=false
            mvnSetVersions(nextSnapshotVersion);

            // git commit -a -m updating poms for development
            gitCommit(msg.getMessage(CommitMessages.updating_pom_for_develop_version, nextSnapshotVersion));

            if (installProject) {
                // mvn clean install
                mvnCleanInstall();
            }

            if (!keepBranch) {
                // git branch -d release/...
                gitBranchDelete(gitFlowConfig.getReleaseBranchPrefix()
                        + releaseVersion);
            }
        } catch (CommandLineException e) {
            getLog().error(e);
        }
    }
}
