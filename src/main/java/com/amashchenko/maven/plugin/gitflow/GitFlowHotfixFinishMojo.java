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

import java.util.ArrayList;
import java.util.List;

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
 * The git flow hotfix finish mojo.
 * 
 * @author Aleksandr Mashchenko
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

    /** Whether to skip calling Maven test goal before merging the branch. */
    @Parameter(property = "skipTestProject", defaultValue = "false")
    private boolean skipTestProject = false;

    /** {@inheritDoc} */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            // check uncommitted changes
            checkUncommittedChanges();

            // git for-each-ref --format='%(refname:short)' refs/heads/hotfix/*
            final String hotfixBranches = gitFindBranches(gitFlowConfig
                    .getHotfixBranchPrefix());

            if (StringUtils.isBlank(hotfixBranches)) {
                throw new MojoFailureException("There is no hotfix branches.");
            }

            String[] branches = hotfixBranches.split("\\r?\\n");

            List<String> numberedList = new ArrayList<String>();
            StringBuilder str = new StringBuilder("Hotfix branches:")
                    .append(LS);
            for (int i = 0; i < branches.length; i++) {
                str.append((i + 1) + ". " + branches[i] + LS);
                numberedList.add(String.valueOf(i + 1));
            }
            str.append("Choose hotfix branch to finish");

            String hotfixNumber = null;
            try {
                while (StringUtils.isBlank(hotfixNumber)) {
                    hotfixNumber = prompter
                            .prompt(str.toString(), numberedList);
                }
            } catch (PrompterException e) {
                getLog().error(e);
            }

            String hotfixBranchName = null;
            if (hotfixNumber != null) {
                int num = Integer.parseInt(hotfixNumber);
                hotfixBranchName = branches[num - 1];
            }

            if (StringUtils.isBlank(hotfixBranchName)) {
                throw new MojoFailureException(
                        "Hotfix name to finish is blank.");
            }

            // git checkout hotfix/...
            gitCheckout(hotfixBranchName);

            if (!skipTestProject) {
                // mvn clean test
                mvnCleanTest();
            }

            // git checkout master
            gitCheckout(gitFlowConfig.getProductionBranch());

            // git merge --no-ff hotfix/...
            gitMergeNoff(hotfixBranchName);

            if (!skipTag) {
                String tagVersion = getCurrentProjectVersion();
                if (tychoBuild && ArtifactUtils.isSnapshot(tagVersion)) {
                    tagVersion = tagVersion.replace("-"
                            + Artifact.SNAPSHOT_VERSION, "");
                }

                // git tag -a ...
                gitTag(gitFlowConfig.getVersionTagPrefix() + tagVersion,
                        "tagging hotfix");
            }

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
                gitCheckout(releaseBranch);
                // git merge --no-ff hotfix/...
                gitMergeNoff(hotfixBranchName);
            } else {
                // git checkout develop
                gitCheckout(gitFlowConfig.getDevelopmentBranch());

                // git merge --no-ff hotfix/...
                gitMergeNoff(hotfixBranchName);

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
                    throw new MojoFailureException(
                            "Next snapshot version is blank.");
                }

                // mvn versions:set -DnewVersion=... -DgenerateBackupPoms=false
                mvnSetVersions(nextSnapshotVersion);

                // git commit -a -m updating for next development version
                gitCommit("updating for next development version");
            }

            if (installProject) {
                // mvn clean install
                mvnCleanInstall();
            }

            if (!keepBranch) {
                // git branch -d hotfix/...
                gitBranchDelete(hotfixBranchName);
            }
        } catch (CommandLineException e) {
            getLog().error(e);
        }
    }
}
