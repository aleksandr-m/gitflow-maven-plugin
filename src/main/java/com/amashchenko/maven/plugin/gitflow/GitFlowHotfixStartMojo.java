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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

/**
 * The git flow hotfix start mojo.
 * 
 */
@Mojo(name = "hotfix-start", aggregator = true)
public class GitFlowHotfixStartMojo extends AbstractGitFlowMojo {

    /**
     * Whether to push to the remote.
     * 
     * @since 1.6.0
     */
    @Parameter(property = "pushRemote", defaultValue = "false")
    private boolean pushRemote;

    /**
     * Branch to start hotfix in non-interactive mode. Production branch or one of
     * the support branches.
     * 
     * @since 1.9.0
     */
    @Parameter(property = "fromBranch")
    private String fromBranch;

    /**
     * Hotfix version to use in non-interactive mode.
     * 
     * @since 1.9.0
     */
    @Parameter(property = "hotfixVersion")
    private String hotfixVersion;

    /**
     * Whether to use snapshot in hotfix.
     * 
     * @since 1.10.0
     */
    @Parameter(property = "useSnapshotInHotfix", defaultValue = "false")
    private boolean useSnapshotInHotfix;

    /** {@inheritDoc} */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        validateConfiguration();

        try {
            // set git flow configuration
            initGitFlowConfig();

            // check uncommitted changes
            checkUncommittedChanges();

            String branchName = gitFlowConfig.getProductionBranch();

            // find support branches
            final String supportBranchesStr = gitFindBranches(gitFlowConfig.getSupportBranchPrefix(), false);

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
                    // add production branch to the list
                    branches[supportBranches.length] = gitFlowConfig
                            .getProductionBranch();

                    List<String> numberedList = new ArrayList<String>();
                    StringBuilder str = new StringBuilder("Branches:")
                            .append(LS);
                    for (int i = 0; i < branches.length; i++) {
                        str.append((i + 1) + ". " + branches[i] + LS);
                        numberedList.add(String.valueOf(i + 1));
                    }
                    str.append("Choose branch to hotfix");

                    String branchNumber = null;
                    try {
                        while (StringUtils.isBlank(branchNumber)) {
                            branchNumber = prompter.prompt(str.toString(),
                                    numberedList);
                        }
                    } catch (PrompterException e) {
                        throw new MojoFailureException("hotfix-start", e);
                    }

                    if (branchNumber != null) {
                        int num = Integer.parseInt(branchNumber);
                        branchName = branches[num - 1];
                    }

                    if (StringUtils.isBlank(branchName)) {
                        throw new MojoFailureException("Branch name is blank.");
                    }
                }
            } else if (StringUtils.isNotBlank(fromBranch)) {
                if (fromBranch.equals(gitFlowConfig.getProductionBranch()) || contains(supportBranches, fromBranch)) {
                    branchName = fromBranch;
                } else {
                    throw new MojoFailureException("The fromBranch is not production or support branch.");
                }
            }

            // need to be in master to get correct project version
            // git checkout master
            gitCheckout(branchName);

            // fetch and check remote
            if (fetchRemote) {
                gitFetchRemoteAndCompare(branchName);
            }

            // get current project version from pom
            final String currentVersion = getCurrentProjectVersion();

            // get default hotfix version
            final String defaultVersion = new GitFlowVersionInfo(currentVersion)
                    .hotfixVersion(tychoBuild);

            if (defaultVersion == null) {
                throw new MojoFailureException(
                        "Cannot get default project version.");
            }

            String version = null;
            if (settings.isInteractiveMode()) {
                try {
                    while (version == null) {
                        version = prompter
                                .prompt("What is the hotfix version? ["
                                        + defaultVersion + "]");

                        if (!"".equals(version)
                                && (!GitFlowVersionInfo.isValidVersion(version)
                                        || !validBranchName(version))) {
                            getLog().info("The version is not valid.");
                            version = null;
                        }
                    }
                } catch (PrompterException e) {
                    throw new MojoFailureException("hotfix-start", e);
                }
            } else {
                if (StringUtils.isNotBlank(hotfixVersion)
                        && (!GitFlowVersionInfo.isValidVersion(hotfixVersion)
                                || !validBranchName(hotfixVersion))) {
                    throw new MojoFailureException("The hotfix version '"
                            + hotfixVersion + "' is not valid.");
                } else {
                    version = hotfixVersion;
                }
            }

            if (StringUtils.isBlank(version)) {
                getLog().info("Version is blank. Using default version.");
                version = defaultVersion;
            }

            // to finish hotfix on support branch
            String branchVersionPart = version.replace('/', '_');

            String hotfixBranchName = gitFlowConfig.getHotfixBranchPrefix()
                    + branchVersionPart;
            if (!gitFlowConfig.getProductionBranch().equals(branchName)) {
                hotfixBranchName = gitFlowConfig.getHotfixBranchPrefix()
                        + branchName + "/" + branchVersionPart;
            }

            // git for-each-ref refs/heads/hotfix/...
            final boolean hotfixBranchExists = gitCheckBranchExists(
                    hotfixBranchName);

            if (hotfixBranchExists) {
                throw new MojoFailureException(
                        "Hotfix branch with that name already exists. Cannot start hotfix.");
            }

            // git checkout -b hotfix/... master
            gitCreateAndCheckout(hotfixBranchName, branchName);

            // execute if version changed
            if (!version.equals(currentVersion)) {
                String projectVersion = version;
                if (useSnapshotInHotfix && !ArtifactUtils.isSnapshot(version)) {
                    projectVersion = version + "-" + Artifact.SNAPSHOT_VERSION;
                }

                if (useSnapshotInHotfix && mavenSession.getUserProperties().get("useSnapshotInHotfix") != null) {
                    getLog().warn(
                            "The useSnapshotInHotfix parameter is set from the command line. Don't forget to use it in the finish goal as well."
                                    + " It is better to define it in the project's pom file.");
                }

                // mvn versions:set -DnewVersion=... -DgenerateBackupPoms=false
                mvnSetVersions(projectVersion);

                Map<String, String> properties = new HashMap<String, String>();
                properties.put("version", projectVersion);

                // git commit -a -m updating versions for hotfix
                gitCommit(commitMessages.getHotfixStartMessage(), properties);
            }

            if (installProject) {
                // mvn clean install
                mvnCleanInstall();
            }

            if (pushRemote) {
                gitPush(hotfixBranchName, false);
            }
        } catch (CommandLineException e) {
            throw new MojoFailureException("hotfix-start", e);
        } catch (VersionParseException e) {
            throw new MojoFailureException("hotfix-start", e);
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
