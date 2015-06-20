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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

import com.amashchenko.maven.plugin.gitflow.i18n.CommitMessages;
import com.amashchenko.maven.plugin.gitflow.i18n.ErrorMessages;
import com.amashchenko.maven.plugin.gitflow.i18n.PromptMessages;

/**
 * The git flow feature finish mojo.
 *
 * @author Aleksandr Mashchenko
 *
 */
@Mojo(name = "feature-finish", aggregator = true)
public class GitFlowFeatureFinishMojo extends AbstractGitFlowMojo {

    /** Whether to keep feature branch after finish. */
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

            // git for-each-ref --format='%(refname:short)' refs/heads/feature/*
            final String featureBranches = gitFindBranches(gitFlowConfig
                    .getFeatureBranchPrefix());

            if (StringUtils.isBlank(featureBranches)) {
                throw new MojoFailureException(msg.getMessage(ErrorMessages.no_feature_branch_found));
            }

            final String[] branches = featureBranches.split("\\r?\\n");

            List<String> numberedList = new ArrayList<String>();
            StringBuilder str = new StringBuilder(
            		msg.getMessage(PromptMessages.feature_branch_list_header));
            str.append(LS);
            for (int i = 0; i < branches.length; i++) {
            	str.append(i+1).append(". ").append(branches[i]).append(LS);
                numberedList.add(String.valueOf(i + 1));
            }
            str.append(msg.getMessage(PromptMessages.feature_branch_number_to_finish_prompt));

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
                throw new MojoFailureException(msg.getMessage(ErrorMessages.feature_branch_name_empty));
            }

            // git checkout feature/...
            gitCheckout(featureBranchName);

            if (!skipTestProject) {
                // mvn clean test
                mvnCleanTest();
            }

            // git checkout develop
            gitCheckout(gitFlowConfig.getDevelopmentBranch());

            // git merge --no-ff feature/...
            gitMergeNoff(featureBranchName);

            // get current project version from pom
            final String currentVersion = getCurrentProjectVersion();

            final String featureName = featureBranchName.replaceFirst(
                    gitFlowConfig.getFeatureBranchPrefix(), "");

            if (currentVersion.contains("-" + featureName)) {
                final String version = currentVersion.replaceFirst("-"
                        + featureName, "");

                // mvn versions:set -DnewVersion=... -DgenerateBackupPoms=false
                mvnSetVersions(version);

                // git commit -a -m updating poms for development branch
                gitCommit(msg.getMessage(CommitMessages.updating_pom_for_develop_branch, version));
            }

            if (installProject) {
                // mvn clean install
                mvnCleanInstall();
            }

            if (!keepBranch) {
                // git branch -d feature/...
                gitBranchDelete(featureBranchName);
            }
        } catch (CommandLineException e) {
            getLog().error(e);
        }
    }
}
