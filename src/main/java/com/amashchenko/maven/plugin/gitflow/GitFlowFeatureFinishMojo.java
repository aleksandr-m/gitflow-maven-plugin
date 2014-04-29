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
package com.amashchenko.maven.plugin.gitflow;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

/**
 * The git flow feature finish mojo.
 * 
 * @author Aleksandr Mashchenko
 * 
 */
@Mojo(name = "feature-finish", aggregator = true)
public class GitFlowFeatureFinishMojo extends AbstractGitFlowMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            // check uncommitted changes
            checkUncommittedChanges();

            // git for-each-ref --format='%(refname:short)' refs/heads/feature/*
            final String featureBranches = executeGitCommandReturn(
                    "for-each-ref", "--format=\"%(refname:short)\"",
                    "refs/heads/" + gitFlowConfig.getFeatureBranchPrefix()
                            + "*");

            if (StringUtils.isBlank(featureBranches)) {
                throw new MojoFailureException("There is no feature branches.");
            }

            String[] branches = featureBranches.split("\\r?\\n");

            List<String> numberedList = new ArrayList<String>();
            StringBuffer str = new StringBuffer(
                    "feature branch name to finish: [");
            for (int i = 0; i < branches.length; i++) {
                str.append((i + 1) + ". " + branches[i] + " ");
                numberedList.add("" + (i + 1));
            }
            str.append("]");

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

            // git checkout feature/...
            executeGitCommand("checkout", featureBranchName);

            // mvn clean install
            executeMvnCommand("clean", "install");

            // git checkout develop
            executeGitCommand("checkout", gitFlowConfig.getDevelopmentBranch());

            // git merge --no-ff feature/...
            executeGitCommand("merge", "--no-ff", featureBranchName);

            // get current project version from pom
            String currentVersion = getCurrentProjectVersion();

            String featureName = featureBranchName.replaceFirst(
                    gitFlowConfig.getFeatureBranchPrefix(), "");

            if (currentVersion.contains("-" + featureName)) {
                String version = currentVersion.replaceFirst("-" + featureName,
                        "");

                // mvn versions:set -DnewVersion=... -DgenerateBackupPoms=false
                executeMvnCommand(VERSIONS_MAVEN_PLUGIN + ":set",
                        "-DnewVersion=" + version, "-DgenerateBackupPoms=false");

                // git commit -a -m updating poms for development branch
                executeGitCommand("commit", "-a", "-m",
                        "updating poms for development branch");
            }

            // git branch -d feature/...
            executeGitCommand("branch", "-d", featureBranchName);
        } catch (CommandLineException e) {
            e.printStackTrace();
        }
    }
}
