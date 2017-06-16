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
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

/**
 * The git flow feature finish mojo.
 * 
 */
@Mojo(name = "feature-finish", aggregator = true)
public class GitFlowFeatureFinishMojo extends AbstractGitFlowMojo {

    /** Whether to keep feature branch after finish. */
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
     * Whether to squash feature branch commits into a single commit upon
     * merging.
     * 
     * @since 1.2.3
     */
    @Parameter(property = "featureSquash", defaultValue = "false")
    private boolean featureSquash = false;

    /**
     * Whether to push to the remote.
     * 
     * @since 1.3.0
     */
    @Parameter(property = "pushRemote", defaultValue = "true")
    private boolean pushRemote;

    /** {@inheritDoc} */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            // check uncommitted changes
            checkUncommittedChanges();

            // git for-each-ref --format='%(refname:short)' refs/heads/feature/*
            final String featureBranches = gitFindBranches(
                    gitFlowConfig.getFeatureBranchPrefix(), false);

            if (StringUtils.isBlank(featureBranches)) {
                throw new MojoFailureException("There are no feature branches.");
            }

            final String[] branches = featureBranches.split("\\r?\\n");

            List<String> numberedList = new ArrayList<String>();
            StringBuilder str = new StringBuilder("Feature branches:")
                    .append(LS);
            for (int i = 0; i < branches.length; i++) {
                str.append((i + 1) + ". " + branches[i] + LS);
                numberedList.add(String.valueOf(i + 1));
            }
            str.append("Choose feature branch to finish");

            String featureNumber = null;
            try {
                while (StringUtils.isBlank(featureNumber)) {
                    featureNumber = prompter.prompt(str.toString(),
                            numberedList);
                }
            } catch (PrompterException e) {
                getLog().error(e);
            }

            String featureBranchName = null;
            if (featureNumber != null) {
                int num = Integer.parseInt(featureNumber);
                featureBranchName = branches[num - 1];
            }

            if (StringUtils.isBlank(featureBranchName)) {
                throw new MojoFailureException(
                        "Feature branch name to finish is blank.");
            }

            // fetch and check remote
            if (fetchRemote) {
                gitFetchRemoteAndCompare(featureBranchName);

                gitFetchRemoteAndCompare(gitFlowConfig.getDevelopmentBranch());
            }

            if (!skipTestProject) {
                // git checkout feature/...
                gitCheckout(featureBranchName);

                // mvn clean test
                mvnCleanTest();
            }

            // git checkout develop
            gitCheckout(gitFlowConfig.getDevelopmentBranch());

            if (featureSquash) {
                // git merge --squash feature/...
                gitMergeSquash(featureBranchName);
                gitCommit(featureBranchName);
            } else {
                // git merge --no-ff feature/...
                gitMergeNoff(featureBranchName);
            }

            // get current project version from pom
            final String currentVersion = getCurrentProjectVersion();

            final String featureName = featureBranchName.replaceFirst(
                    gitFlowConfig.getFeatureBranchPrefix(), "");

            if (currentVersion.contains("-" + featureName)) {
                final String version = currentVersion.replaceFirst("-"
                        + featureName, "");

                // mvn versions:set -DnewVersion=... -DgenerateBackupPoms=false
                mvnSetVersions(version);

                // git commit -a -m updating versions for development branch
                gitCommit(commitMessages.getFeatureFinishMessage());
            }

            if (installProject) {
                // mvn clean install
                mvnCleanInstall();
            }

            if (!keepBranch) {
                if (featureSquash) {
                    // git branch -D feature/...
                    gitBranchDeleteForce(featureBranchName);
                } else {
                    // git branch -d feature/...
                    gitBranchDelete(featureBranchName);
                }
            }

            if (pushRemote) {
                gitPush(gitFlowConfig.getDevelopmentBranch(), false);

                if (!keepBranch) {
                    gitPushDelete(featureBranchName);
                }
            }
        } catch (CommandLineException e) {
            getLog().error(e);
        }
    }
}
