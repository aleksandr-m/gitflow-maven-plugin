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

import java.util.ArrayList;
import java.util.List;

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

    /** {@inheritDoc} */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            // set git flow configuration
            initGitFlowConfig();

            // check uncommitted changes
            checkUncommittedChanges();

            String branchName = gitFlowConfig.getProductionBranch();

            final String supportBranches = gitFindBranches(
                    gitFlowConfig.getSupportBranchPrefix(), false);

            if (StringUtils.isNotBlank(supportBranches)) {
                final String[] tmpBranches = supportBranches.split("\\r?\\n");

                String[] branches = new String[tmpBranches.length + 1];
                for (int i = 0; i < tmpBranches.length; i++) {
                    branches[i] = tmpBranches[i];
                }
                // add production branch to the list
                branches[tmpBranches.length] = gitFlowConfig
                        .getProductionBranch();

                List<String> numberedList = new ArrayList<String>();
                StringBuilder str = new StringBuilder("Branches:").append(LS);
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
                    getLog().error(e);
                }

                if (branchNumber != null) {
                    int num = Integer.parseInt(branchNumber);
                    branchName = branches[num - 1];
                }

                if (StringUtils.isBlank(branchName)) {
                    throw new MojoFailureException("Branch name is blank.");
                }
            }
            //

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
            try {
                while (version == null) {
                    version = prompter.prompt("What is the hotfix version? ["
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

            if (StringUtils.isBlank(version)) {
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
            final boolean hotfixBranchExists = gitCheckBranchExists(hotfixBranchName);

            if (hotfixBranchExists) {
                throw new MojoFailureException(
                        "Hotfix branch with that name already exists. Cannot start hotfix.");
            }

            // git checkout -b hotfix/... master
            gitCreateAndCheckout(hotfixBranchName, branchName);

            // execute if version changed
            if (!version.equals(currentVersion)) {
                // mvn versions:set -DnewVersion=... -DgenerateBackupPoms=false
                mvnSetVersions(version);

                // git commit -a -m updating versions for hotfix
                gitCommit(commitMessages.getHotfixStartMessage());
            }

            if (installProject) {
                // mvn clean install
                mvnCleanInstall();
            }

            if (pushRemote) {
                gitPush(hotfixBranchName, false);
            }
        } catch (CommandLineException e) {
            getLog().error(e);
        } catch (VersionParseException e) {
            getLog().error(e);
        }
    }
}
