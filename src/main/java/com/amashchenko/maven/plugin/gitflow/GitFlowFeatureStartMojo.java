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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.release.versions.DefaultVersionInfo;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

import com.amashchenko.maven.plugin.gitflow.i18n.CommitMessages;
import com.amashchenko.maven.plugin.gitflow.i18n.ErrorMessages;
import com.amashchenko.maven.plugin.gitflow.i18n.PromptMessages;

/**
 * The git flow feature start mojo.
 *
 * @author Aleksandr Mashchenko
 *
 */
@Mojo(name = "feature-start", aggregator = true)
public class GitFlowFeatureStartMojo extends AbstractGitFlowMojo {

    /**
     * Whether to skip changing project version. Default is <code>false</code>
     * (the feature name will be appended to project version).
     */
	// FIXME switch to appendFeatureVersion
    @Parameter(property = "skipFeatureVersion", defaultValue = "false")
    private boolean skipFeatureVersion = false;

    /** {@inheritDoc} */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            // set git flow configuration
            initGitFlowConfig();

            // check uncommitted changes
            checkUncommittedChanges();

            String featureName = null;
            try {
                while (StringUtils.isBlank(featureName)) {
                    featureName = prompter
                            .prompt(msg.getMessage(PromptMessages.feature_branch_name_to_create_prompt)
                                    + gitFlowConfig.getFeatureBranchPrefix());
                }
            } catch (PrompterException e) {
                getLog().error(e);
            }

            featureName = StringUtils.join(StringUtils.split(featureName), "_");

			// git for-each-ref refs/heads/feature/...
            String pattern = "refs/heads/" + gitFlowConfig.getFeatureBranchPrefix() + featureName;
            final String featureBranch = executeGitCommandReturn("for-each-ref", pattern);

            if (StringUtils.isNotBlank(featureBranch)) {
                throw new MojoFailureException(msg.getMessage(ErrorMessages.feature_branch_name_duplicate));
            }

            // git checkout -b ... develop
            gitCreateAndCheckout(gitFlowConfig.getFeatureBranchPrefix()
                    + featureName, gitFlowConfig.getDevelopmentBranch());

            if (!skipFeatureVersion) {
                // get current project version from pom
                final String currentVersion = getCurrentProjectVersion();

                String version = null;
                try {
                    final DefaultVersionInfo versionInfo = new DefaultVersionInfo(
                            currentVersion);
                    version = versionInfo.getReleaseVersionString() + "-"
                            + featureName + "-" + Artifact.SNAPSHOT_VERSION;
                } catch (VersionParseException e) {
                    if (getLog().isDebugEnabled()) {
                        getLog().debug(e);
                    }
                }

                if (StringUtils.isNotBlank(version)) {
                    // mvn versions:set -DnewVersion=...
                    // -DgenerateBackupPoms=false
                    mvnSetVersions(version);

                    // git commit -a -m updating poms for feature branch
                    gitCommit(msg.getMessage(CommitMessages.updating_pom_for_feature_branch));
                }
            }

            if (installProject) {
                // mvn clean install
                mvnCleanInstall();
            }
        } catch (CommandLineException e) {
            getLog().error(e);
        }
    }
}
