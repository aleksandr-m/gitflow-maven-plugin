/*
 * Copyright 2014 Aleksandr Mashchenko.
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
package com.amashchenko.gitflow.plugin;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.shared.release.versions.DefaultVersionInfo;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

@Mojo(name = "hotfix-finish", aggregator = true)
public class GitFlowHotfixFinishMojo extends AbstractGitFlowMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            // check uncommitted changes
            checkUncommittedChanges();

            // git for-each-ref --format='%(refname:short)' refs/heads/hotfix/*
            final String hotfixBranches = executeGitCommandReturn(
                    "for-each-ref", "--format=\"%(refname:short)\"",
                    "refs/heads/" + gitFlowConfig.getHotfixBranchPrefix() + "*");

            if (StringUtils.isBlank(hotfixBranches)) {
                throw new MojoFailureException("There is no hotfix branches.");
            }

            String[] branches = hotfixBranches.split("\\r?\\n");

            List<String> numberedList = new ArrayList<String>();
            StringBuffer str = new StringBuffer(
                    "hotfix branch name to finish: [");
            for (int i = 0; i < branches.length; i++) {
                str.append((i + 1) + ". " + branches[i] + " ");
                numberedList.add("" + (i + 1));
            }
            str.append("]");

            String hotfixNumber = null;
            try {
                while (StringUtils.isBlank(hotfixNumber)) {
                    hotfixNumber = prompter
                            .prompt(str.toString(), numberedList);
                }
            } catch (PrompterException e) {
                getLog().error(e);
            }

            String hotfixName = null;
            if (hotfixNumber != null) {
                int num = Integer.parseInt(hotfixNumber);
                hotfixName = branches[num - 1];
            }

            if (StringUtils.isBlank(hotfixName)) {
                throw new MojoFailureException(
                        "Hotfix name to finish is blank.");
            }

            // git checkout master
            executeGitCommand("checkout", gitFlowConfig.getProductionBranch());

            // git merge --no-ff hotfix/...
            executeGitCommand("merge", "--no-ff", hotfixName);

            // git tag -a ...
            executeGitCommand(
                    "tag",
                    "-a",
                    gitFlowConfig.getVersionTagPrefix()
                            + hotfixName.replaceFirst(
                                    gitFlowConfig.getHotfixBranchPrefix(), ""),
                    "-m", "tagging hotfix");

            // check whether release branch exists
            // git for-each-ref --count=1 --format="%(refname:short)"
            // refs/heads/release/*
            final String releaseBranch = executeGitCommandReturn(
                    "for-each-ref", "--count=1",
                    "--format=\"%(refname:short)\"", "refs/heads/"
                            + gitFlowConfig.getReleaseBranchPrefix() + "*");

            // if release branch exists merge hotfix changes into it
            if (StringUtils.isNotBlank(releaseBranch)) {
                // git checkout release
                executeGitCommand("checkout", releaseBranch);
                // git merge --no-ff hotfix/...
                executeGitCommand("merge", "--no-ff", hotfixName);
            } else {
                // git checkout develop
                executeGitCommand("checkout",
                        gitFlowConfig.getDevelopmentBranch());

                // git merge --no-ff hotfix/...
                executeGitCommand("merge", "--no-ff", hotfixName);

                String nextSnapshotVersion = null;
                // get next snapshot version
                try {
                    DefaultVersionInfo versionInfo = new DefaultVersionInfo(
                            project.getVersion());
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
                executeMvnCommand(VERSIONS_MAVEN_PLUGIN + ":set",
                        "-DnewVersion=" + nextSnapshotVersion,
                        "-DgenerateBackupPoms=false");

                // git commit -a -m updating poms for next development version
                executeGitCommand("commit", "-a", "-m",
                        "updating poms for next development version");
            }

            // git branch -d hotfix/...
            executeGitCommand("branch", "-d", hotfixName);
        } catch (CommandLineException e) {
            e.printStackTrace();
        }
    }
}
