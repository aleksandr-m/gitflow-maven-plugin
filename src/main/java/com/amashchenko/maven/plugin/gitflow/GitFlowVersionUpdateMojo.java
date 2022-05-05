/*
 * Copyright 2014-2022 Aleksandr Mashchenko.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

/**
 * Updates version in release or support branch, optionally tagging and pushing
 * it to the remote repository.
 * 
 * @since 1.18.0
 */
@Mojo(name = "version-update", aggregator = true)
public class GitFlowVersionUpdateMojo extends AbstractGitFlowMojo {
    /**
     * Whether to push to the remote.
     * 
     * @since 1.18.0
     */
    @Parameter(property = "pushRemote", defaultValue = "false")
    private boolean pushRemote;

    /**
     * Branch to start update in non-interactive mode. Release branch or one of the
     * support branches.
     * 
     * @since 1.18.0
     */
    @Parameter(property = "fromBranch")
    private String fromBranch;

    /**
     * The version to use in non-interactive mode.
     * 
     * @since 1.18.0
     */
    @Parameter(property = "updateVersion")
    private String updateVersion;

    /**
     * Which digit to increment in the next version. Starts from zero.
     *
     * @since 1.18.0
     */
    @Parameter(property = "updateVersionDigitToIncrement")
    private Integer updateVersionDigitToIncrement;

    /**
     * Whether to skip tagging the release in Git.
     * 
     * @since 1.18.0
     */
    @Parameter(property = "skipTag", defaultValue = "false")
    private boolean skipTag = false;

    /**
     * Whether to make a GPG-signed tag.
     *
     * @since 1.18.0
     */
    @Parameter(property = "gpgSignTag", defaultValue = "false")
    private boolean gpgSignTag = false;

    /** {@inheritDoc} */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        validateConfiguration();

        try {
            initGitFlowConfig();

            checkUncommittedChanges();

            final String releaseBranch = gitFindBranches(gitFlowConfig.getReleaseBranchPrefix(), false);

            String branchName = releaseBranch;

            // find support branches
            final String supportBranchesStr = gitFindBranches(gitFlowConfig.getSupportBranchPrefix(), false);

            if (StringUtils.isBlank(releaseBranch) && StringUtils.isBlank(supportBranchesStr)) {
                throw new MojoFailureException("There is no release or support branches.");
            }

            final String[] supportBranches;
            if (StringUtils.isNotBlank(supportBranchesStr)) {
                supportBranches = supportBranchesStr.split("\\r?\\n");
            } else {
                supportBranches = null;
            }

            if (settings.isInteractiveMode()) {
                if (supportBranches != null && supportBranches.length > 0) {
                    String[] branches = new String[supportBranches.length + 1];
                    for (int i = 0; i < supportBranches.length; i++) {
                        branches[i] = supportBranches[i];
                    }
                    if (StringUtils.isNotBlank(releaseBranch)) {
                        // add release branch to the list
                        branches[supportBranches.length] = releaseBranch;
                    }

                    List<String> numberedList = new ArrayList<>();
                    StringBuilder str = new StringBuilder("Branches:").append(LS);
                    for (int i = 0; i < branches.length; i++) {
                        str.append((i + 1) + ". " + branches[i] + LS);
                        numberedList.add(String.valueOf(i + 1));
                    }
                    str.append("Choose branch to update");

                    String branchNumber = null;
                    try {
                        while (StringUtils.isBlank(branchNumber)) {
                            branchNumber = prompter.prompt(str.toString(), numberedList);
                        }
                    } catch (PrompterException e) {
                        throw new MojoFailureException("version-update", e);
                    }

                    if (branchNumber != null) {
                        int num = Integer.parseInt(branchNumber);
                        branchName = branches[num - 1];
                    }
                }
            } else if (StringUtils.isNotBlank(fromBranch)) {
                if (fromBranch.equals(releaseBranch) || contains(supportBranches, fromBranch)) {
                    branchName = fromBranch;
                } else {
                    throw new MojoFailureException("The fromBranch is not release or support branch.");
                }
            }
            if (StringUtils.isBlank(branchName)) {
                throw new MojoFailureException("Branch name is blank.");
            }

            gitCheckout(branchName);

            // fetch and check remote
            if (fetchRemote) {
                gitFetchRemoteAndCompareCreate(branchName);
            }

            // get current project version from pom
            final String currentVersion = getCurrentProjectVersion();

            // get default next version
            final String defaultVersion = new GitFlowVersionInfo(currentVersion, getVersionPolicy()).hotfixVersion(tychoBuild,
                    updateVersionDigitToIncrement);

            if (defaultVersion == null) {
                throw new MojoFailureException("Cannot get default next version.");
            }

            String version = null;
            if (settings.isInteractiveMode()) {
                try {
                    while (version == null) {
                        version = prompter.prompt("What is the update version? [" + defaultVersion + "]");

                        if (!"".equals(version) && (!GitFlowVersionInfo.isValidVersion(version) || !validBranchName(version))) {
                            getLog().info("The version is not valid.");
                            version = null;
                        }
                    }
                } catch (PrompterException e) {
                    throw new MojoFailureException("version-update", e);
                }
            } else {
                if (StringUtils.isNotBlank(updateVersion)
                        && (!GitFlowVersionInfo.isValidVersion(updateVersion) || !validBranchName(updateVersion))) {
                    throw new MojoFailureException("The update version '" + updateVersion + "' is not valid.");
                } else {
                    version = updateVersion;
                }
            }

            if (StringUtils.isBlank(version)) {
                getLog().info("Version is blank. Using default version.");
                version = defaultVersion;
            }

            Map<String, String> messageProperties = new HashMap<>();

            // execute if version changed
            if (!version.equals(currentVersion)) {
                mvnSetVersions(version);

                messageProperties.put("version", version);

                gitCommit(commitMessages.getVersionUpdateMessage(), messageProperties);
            }

            if (!skipTag) {
                messageProperties.put("version", version);

                gitTag(gitFlowConfig.getVersionTagPrefix() + version, commitMessages.getTagVersionUpdateMessage(), gpgSignTag,
                        messageProperties);
            }

            if (installProject) {
                mvnCleanInstall();
            }

            if (pushRemote) {
                gitPush(branchName, !skipTag);
            }
        } catch (CommandLineException | VersionParseException e) {
            throw new MojoFailureException("version-update", e);
        }
    }

    private boolean contains(String[] arr, String str) {
        if (arr != null && str != null) {
            for (String a : arr) {
                if (str.equals(a)) {
                    return true;
                }
            }
        }
        return false;
    }
}
