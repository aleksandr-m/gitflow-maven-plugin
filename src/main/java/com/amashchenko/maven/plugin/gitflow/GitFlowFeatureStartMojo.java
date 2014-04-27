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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

@Mojo(name = "feature-start", aggregator = true)
public class GitFlowFeatureStartMojo extends AbstractGitFlowMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            // check uncommitted changes
            checkUncommittedChanges();

            String version = null;
            try {
                while (StringUtils.isBlank(version)) {
                    version = prompter
                            .prompt("What is a name of feature branch? "
                                    + gitFlowConfig.getFeatureBranchPrefix());
                }
            } catch (PrompterException e) {
                getLog().error(e);
            }

            // git for-each-ref refs/heads/feature/...
            final String featureBranch = executeGitCommandReturn(
                    "for-each-ref",
                    "refs/heads/" + gitFlowConfig.getFeatureBranchPrefix()
                            + version);

            if (StringUtils.isNotBlank(featureBranch)) {
                throw new MojoFailureException(
                        "Feature branch with that name already exists. Cannot start feature.");
            }

            // git checkout -b ... develop
            executeGitCommand("checkout", "-b",
                    gitFlowConfig.getFeatureBranchPrefix() + version,
                    gitFlowConfig.getDevelopmentBranch());

            // TODO
            /*
                        // mvn versions:set -DnewVersion=... -DgenerateBackupPoms=false
                        executeMvnCommand(VERSIONS_MAVEN_PLUGIN + ":set", "-DnewVersion="
                                + version, "-DgenerateBackupPoms=false");

            // git commit -a -m updating poms for ... release
            executeGitCommand("commit", "-a", "-m", "updating poms for release");
            */
        } catch (CommandLineException e) {
            e.printStackTrace();
        }
    }
}
