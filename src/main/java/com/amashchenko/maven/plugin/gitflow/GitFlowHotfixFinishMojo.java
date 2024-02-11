/*
 * Copyright 2014-2024 Aleksandr Mashchenko.
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
import org.codehaus.plexus.util.cli.CommandLineException;

/**
 * The git flow hotfix finish mojo.
 *
 */
@Mojo(name = "hotfix-finish", aggregator = true)
public class GitFlowHotfixFinishMojo extends AbstractGitFlowMojo {

    /** Whether to skip tagging the hotfix in Git. */
    @Parameter(property = "skipTag", defaultValue = "false")
    private boolean skipTag = false;

    /** Whether to keep hotfix branch after finish. */
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
     * Whether to push to the remote.
     *
     * @since 1.3.0
     */
    @Parameter(property = "pushRemote", defaultValue = "true")
    private boolean pushRemote;

    /**
     * Maven goals to execute in the hotfix branch before merging into the
     * production or support branch.
     *
     * @since 1.8.0
     */
    @Parameter(property = "preHotfixGoals")
    private String preHotfixGoals;

    /**
     * Maven goals to execute in the release or support branch after the hotfix.
     *
     * @since 1.8.0
     */
    @Parameter(property = "postHotfixGoals")
    private String postHotfixGoals;

    /**
     * Hotfix version to use in non-interactive mode.
     *
     * @since 1.9.0
     */
    @Parameter(property = "hotfixVersion")
    private String hotfixVersion;

    /**
     * Hotfix branch to use in non-interactive mode. Must start with hotfix branch
     * prefix. The hotfixBranch parameter will be used instead of
     * {@link #hotfixVersion} if both are set.
     *
     * @since 1.16.0
     */
    @Parameter(property = "hotfixBranch")
    private String hotfixBranch;

    /**
     * Whether to make a GPG-signed tag.
     *
     * @since 1.9.0
     */
    @Parameter(property = "gpgSignTag", defaultValue = "false")
    private boolean gpgSignTag = false;

    /**
     * Whether to use snapshot in hotfix.
     *
     * @since 1.10.0
     */
    @Parameter(property = "useSnapshotInHotfix", defaultValue = "false")
    private boolean useSnapshotInHotfix;

    /**
     * Whether to skip merging into the production branch.
     *
     * @since 1.12.0
     */
    @Parameter(property = "skipMergeProdBranch", defaultValue = "false")
    private boolean skipMergeProdBranch = false;

    /**
     * Whether to skip merging into the development branch.
     *
     * @since 1.12.0
     */
    @Parameter(property = "skipMergeDevBranch", defaultValue = "false")
    private boolean skipMergeDevBranch = false;

    /**
     * Controls which branch is merged to development branch. If set to
     * <code>true</code> then hotfix branch will be merged to development branch. If
     * set to <code>false</code> and tag is present ({@link #skipTag} is set to
     * <code>false</code>) then tag will be merged. If there is no tag then
     * production branch will be merged to development branch.
     *
     * @since 1.18.0
     */
    @Parameter(property = "noBackMergeHotfix", defaultValue = "false")
    private boolean noBackMergeHotfix = false;

    /** {@inheritDoc} */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        validateConfiguration(preHotfixGoals, postHotfixGoals);

        try {
            // check uncommitted changes
            checkUncommittedChanges();

            String hotfixBranchName = null;
            if (settings.isInteractiveMode()) {
                hotfixBranchName = promptBranchName();
            } else if (StringUtils.isNotBlank(hotfixBranch)) {
                if (!hotfixBranch.startsWith(gitFlowConfig.getHotfixBranchPrefix())) {
                    throw new MojoFailureException("The hotfixBranch parameter doesn't start with hotfix branch prefix.");
                }
                if (!gitCheckBranchExists(hotfixBranch)) {
                    throw new MojoFailureException("Hotfix branch with name '" + hotfixBranch + "' doesn't exist. Cannot finish hotfix.");
                }
                hotfixBranchName = hotfixBranch;
            } else if (StringUtils.isNotBlank(hotfixVersion)) {
                final String branch = gitFlowConfig.getHotfixBranchPrefix() + hotfixVersion;
                if (!gitCheckBranchExists(branch)) {
                    throw new MojoFailureException("Hotfix branch with name '" + branch + "' doesn't exist. Cannot finish hotfix.");
                }
                hotfixBranchName = branch;
            } else if (StringUtils.isBlank(hotfixVersion)) {
                // git for-each-ref --format='%(refname:short)' refs/heads/hotfix/*
                hotfixBranchName = gitFindBranches(gitFlowConfig.getHotfixBranchPrefix(), false).trim();
                
                if (StringUtils.isBlank(hotfixBranchName)) {
                    if (fetchRemote) {
                        hotfixBranchName = gitFetchAndFindRemoteBranches(gitFlowConfig.getOrigin(),
                                gitFlowConfig.getHotfixBranchPrefix(), false).trim();
                        if (StringUtils.isBlank(hotfixBranchName)) {
                            throw new MojoFailureException("There is no remote or local hotfix branch.");
                        }

                        // remove remote name with slash from branch name
                        hotfixBranchName = hotfixBranchName.substring(gitFlowConfig.getOrigin().length() + 1);
                        
                        gitCreateAndCheckout(hotfixBranchName, gitFlowConfig.getOrigin() + "/" + hotfixBranchName);
                    } else {
                        throw new MojoFailureException("There is no hotfix branch.");
                    }
                }
                
                if (StringUtils.countMatches(hotfixBranchName, gitFlowConfig.getHotfixBranchPrefix()) > 1) {
                    throw new MojoFailureException(
                            "More than one remote hotfix branch exists. Cannot finish hotfix. Define branch name to finish hotfix.");
                }
            }

            if (StringUtils.isBlank(hotfixBranchName)) {
                throw new MojoFailureException("Hotfix branch name to finish is blank.");
            }

            // support branch hotfix
            String supportBranchName = null;
            boolean supportHotfix = hotfixBranchName
                    .startsWith(gitFlowConfig.getHotfixBranchPrefix() + gitFlowConfig.getSupportBranchPrefix());
            // get support branch name w/o version part
            if (supportHotfix) {
                supportBranchName = hotfixBranchName.substring(gitFlowConfig.getHotfixBranchPrefix().length());
                supportBranchName = supportBranchName.substring(0, supportBranchName.lastIndexOf('/'));
            }

            // fetch and check remote
            if (fetchRemote) {
                gitFetchRemoteAndCompareCreate(hotfixBranchName);

                if (supportBranchName != null) {
                    gitFetchRemoteAndCompareCreate(supportBranchName);
                } else {
                    if (notSameProdDevName()) {
                        gitFetchRemoteAndCompareCreate(gitFlowConfig.getDevelopmentBranch());
                    }
                    gitFetchRemoteAndCompareCreate(gitFlowConfig.getProductionBranch());

                    // release branch
                    String remoteReleases = gitFetchAndFindRemoteBranches(gitFlowConfig.getReleaseBranchPrefix(), false);
                    if (StringUtils.isNotBlank(remoteReleases)) {
                        // remove remote name with slash from branch name
                        String remoteRelease = remoteReleases.substring(gitFlowConfig.getOrigin().length() + 1);

                        if (StringUtils.countMatches(remoteRelease, gitFlowConfig.getReleaseBranchPrefix()) > 1) {
                            throw new MojoFailureException("More than one remote release branch exists. Cannot finish hotfix.");
                        }

                        gitFetchRemoteAndCompareCreate(remoteRelease);
                    }
                }
            }

            // git checkout hotfix/...
            gitCheckout(hotfixBranchName);

            if (!skipTestProject) {
                mvnCleanTest();
            }

            // maven goals before merge
            if (StringUtils.isNotBlank(preHotfixGoals)) {
                mvnRun(preHotfixGoals);
            }

            String currentHotfixVersion = getCurrentProjectVersion();

            Map<String, String> messageProperties = new HashMap<>();
            messageProperties.put("version", currentHotfixVersion);

            if (useSnapshotInHotfix && ArtifactUtils.isSnapshot(currentHotfixVersion)) {
                String commitVersion = currentHotfixVersion.replace("-" + Artifact.SNAPSHOT_VERSION, "");

                mvnSetVersions(commitVersion);

                messageProperties.put("version", commitVersion);

                gitCommit(commitMessages.getHotfixFinishMessage(), messageProperties);
            }

            if (supportBranchName != null) {
                gitCheckout(supportBranchName);
                // git merge --no-ff hotfix/...
                gitMergeNoff(hotfixBranchName, commitMessages.getHotfixFinishSupportMergeMessage(), messageProperties);
            } else if (!skipMergeProdBranch) {
                // git checkout production
                gitCheckout(gitFlowConfig.getProductionBranch());
                // git merge --no-ff hotfix/...
                gitMergeNoff(hotfixBranchName, commitMessages.getHotfixFinishMergeMessage(), messageProperties);
            }

            final String currentVersion = getCurrentProjectVersion();

            final String tagVersion = (tychoBuild || useSnapshotInHotfix) && ArtifactUtils.isSnapshot(currentVersion)
                    ? currentVersion.replace("-" + Artifact.SNAPSHOT_VERSION, "")
                    : currentVersion;
            if (!skipTag) {
                Map<String, String> properties = new HashMap<>();
                properties.put("version", tagVersion);

                // git tag -a ...
                gitTag(gitFlowConfig.getVersionTagPrefix() + tagVersion,
                        commitMessages.getTagHotfixMessage(), gpgSignTag, properties);
            }

            if (skipMergeProdBranch && (supportBranchName == null)) {
                // switch to production branch so hotfix branch can be deleted
                gitCheckout(gitFlowConfig.getProductionBranch());
            }

            // maven goals after merge
            if (StringUtils.isNotBlank(postHotfixGoals)) {
                mvnRun(postHotfixGoals);
            }

            // check whether release branch exists
            final String releaseBranch = gitFindBranches(gitFlowConfig.getReleaseBranchPrefix(), true);

            if (supportBranchName == null) {
                // if release branch exists merge hotfix changes into it
                if (StringUtils.isNotBlank(releaseBranch)) {
                    // git checkout release
                    gitCheckout(releaseBranch);
                    String releaseBranchVersion = getCurrentProjectVersion();

                    if (!currentVersion.equals(releaseBranchVersion)) {
                        // set version to avoid merge conflict
                        mvnSetVersions(currentVersion);
                        gitCommit(commitMessages.getUpdateReleaseToAvoidConflictsMessage());
                    }

                    messageProperties.put("version", currentVersion);

                    // git merge --no-ff hotfix/...
                    gitMergeNoff(hotfixBranchName, commitMessages.getHotfixFinishReleaseMergeMessage(),
                            messageProperties);

                    if (!currentVersion.equals(releaseBranchVersion)) {
                        mvnSetVersions(releaseBranchVersion);
                        gitCommit(commitMessages.getUpdateReleaseBackPreMergeStateMessage());
                    }
                } else if (!skipMergeDevBranch) {
                    GitFlowVersionInfo developVersionInfo = new GitFlowVersionInfo(
                            currentVersion, getVersionPolicy());
                    if (notSameProdDevName()) {
                        // git checkout develop
                        gitCheckout(gitFlowConfig.getDevelopmentBranch());

                        developVersionInfo = new GitFlowVersionInfo(getCurrentProjectVersion(), getVersionPolicy());

                        // set version to avoid merge conflict
                        mvnSetVersions(currentVersion);
                        gitCommit(commitMessages.getHotfixVersionUpdateMessage());

                        messageProperties.put("version", currentVersion);

                        final String refToMerge;
                        if (skipMergeProdBranch || noBackMergeHotfix) {
                            refToMerge = hotfixBranchName;
                        } else if (!skipTag) {
                            refToMerge = gitFlowConfig.getVersionTagPrefix() + tagVersion;
                        } else {
                            refToMerge = gitFlowConfig.getProductionBranch();
                        }
                        gitMergeNoff(refToMerge, commitMessages.getHotfixFinishDevMergeMessage(), messageProperties);

                        // which version to increment
                        GitFlowVersionInfo hotfixVersionInfo = new GitFlowVersionInfo(
                                currentVersion, getVersionPolicy());
                        if (developVersionInfo
                                .compareTo(hotfixVersionInfo) < 0) {
                            developVersionInfo = hotfixVersionInfo;
                        }
                    }

                    // get next snapshot version
                    final String nextSnapshotVersion = developVersionInfo.getSnapshotVersionString();

                    if (StringUtils.isBlank(nextSnapshotVersion)) {
                        throw new MojoFailureException(
                                "Next snapshot version is blank.");
                    }

                    mvnSetVersions(nextSnapshotVersion);

                    Map<String, String> properties = new HashMap<>();
                    properties.put("version", nextSnapshotVersion);

                    gitCommit(commitMessages.getHotfixFinishMessage(), properties);
                }
            }

            if (installProject) {
                mvnCleanInstall();
            }

            if (pushRemote) {
                if (supportBranchName != null) {
                    gitPush(supportBranchName, !skipTag);
                } else {
                    gitPush(gitFlowConfig.getProductionBranch(), !skipTag);

                    if (StringUtils.isNotBlank(releaseBranch)) {
                        gitPush(releaseBranch, !skipTag);
                    } else if (StringUtils.isBlank(releaseBranch)
                            && notSameProdDevName()) { // if no release branch
                        gitPush(gitFlowConfig.getDevelopmentBranch(), !skipTag);
                    }
                }

                // push tag
                if (!skipTag && skipMergeDevBranch && skipMergeProdBranch && StringUtils.isBlank(releaseBranch)) {
                    gitPush(gitFlowConfig.getVersionTagPrefix() + tagVersion, false);
                }

                if (!keepBranch) {
                    gitPushDelete(hotfixBranchName);
                }
            }

            if (!keepBranch) {
                if (skipMergeProdBranch) {
                    // force delete as upstream merge is skipped
                    gitBranchDeleteForce(hotfixBranchName);
                } else {
                    // git branch -d hotfix/...
                    gitBranchDelete(hotfixBranchName);
                }
            }
        } catch (Exception e) {
            throw new MojoFailureException("hotfix-finish", e);
        }
    }

    private String promptBranchName() throws MojoFailureException, CommandLineException {
        String hotfixBranches = gitFindBranches(gitFlowConfig.getHotfixBranchPrefix(), false);

        // find hotfix support branches
        if (!gitFlowConfig.getHotfixBranchPrefix().endsWith("/")) {
            String supportHotfixBranches = gitFindBranches(gitFlowConfig.getHotfixBranchPrefix() + "*/*", false);
            hotfixBranches = hotfixBranches + supportHotfixBranches;
        }

        if (StringUtils.isBlank(hotfixBranches)) {
            throw new MojoFailureException("There are no hotfix branches.");
        }

        String[] branches = hotfixBranches.split("\\r?\\n");

        return prompter.prompt(branches, null, "Hotfix branches:", "Choose hotfix branch to finish");
    }
}
