/*
 * Copyright 2014-2020 Aleksandr Mashchenko.
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

import java.util.HashMap;
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
 * The git flow feature start mojo.
 * 
 */
@Mojo(name = "feature-start", aggregator = true)
public class GitFlowFeatureStartMojo extends AbstractGitFlowMojo {

    /**
     * Whether to skip changing project version. Default is <code>false</code>
     * (the feature name will be appended to project version).
     * 
     * @since 1.0.5
     */
    @Parameter(property = "skipFeatureVersion", defaultValue = "false")
    private boolean skipFeatureVersion = false;

    /**
     * Regex pattern to enforce naming of the feature branches. Doesn't have
     * effect if not set or blank.
     * 
     * @since 1.5.0
     */
    @Parameter
    private String featureNamePattern;

    /**
     * Whether to push to the remote.
     * 
     * @since 1.6.0
     */
    @Parameter(property = "pushRemote", defaultValue = "false")
    private boolean pushRemote;

    /**
     * Feature name to use in non-interactive mode.
     * 
     * @since 1.9.0
     */
    @Parameter(property = "featureName")
    private String featureName;

    /** {@inheritDoc} */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        validateConfiguration();

        try {
            // set git flow configuration
            initGitFlowConfig();

            // check uncommitted changes
            checkUncommittedChanges();

            // fetch and check remote
            if (fetchRemote) {
                gitFetchRemoteAndCompare(gitFlowConfig.getDevelopmentBranch());
            }

            String featureBranchName = null;
            if (settings.isInteractiveMode()) {
                try {
                    while (StringUtils.isBlank(featureBranchName)) {
                        featureBranchName = prompter
                                .prompt("What is a name of feature branch? "
                                        + gitFlowConfig
                                                .getFeatureBranchPrefix());

                        if (!validateBranchName(featureBranchName,
                                featureNamePattern)) {
                            featureBranchName = null;
                        }
                    }
                } catch (PrompterException e) {
                    throw new MojoFailureException("feature-start", e);
                }
            } else if (validateBranchName(featureName, featureNamePattern)) {
                featureBranchName = featureName;
            }

            if (StringUtils.isBlank(featureBranchName)) {
                throw new MojoFailureException("Feature name is blank.");
            }

            featureBranchName = StringUtils.deleteWhitespace(featureBranchName);

            // git for-each-ref refs/heads/feature/...
            final boolean featureBranchExists = gitCheckBranchExists(
                    gitFlowConfig.getFeatureBranchPrefix() + featureBranchName);

            if (featureBranchExists) {
                throw new MojoFailureException(
                        "Feature branch with that name already exists. Cannot start feature.");
            }

            // git checkout -b ... develop
            gitCreateAndCheckout(
                    gitFlowConfig.getFeatureBranchPrefix() + featureBranchName,
                    gitFlowConfig.getDevelopmentBranch());

            if (!skipFeatureVersion && !tychoBuild) {
                // get current project version from pom
                final String currentVersion = getCurrentProjectVersion();

                final String version = new GitFlowVersionInfo(currentVersion)
                        .featureVersion(featureBranchName);

                if (StringUtils.isNotBlank(version)) {
                    // mvn versions:set -DnewVersion=...
                    // -DgenerateBackupPoms=false
                    mvnSetVersions(version);

                    Map<String, String> properties = new HashMap<String, String>();
                    properties.put("version", version);
                    properties.put("featureName", featureBranchName);

                    // git commit -a -m updating versions for feature branch
                    gitCommit(commitMessages.getFeatureStartMessage(),
                            properties);
                }
            }

            if (installProject) {
                // mvn clean install
                mvnCleanInstall();
            }

            if (pushRemote) {
                gitPush(gitFlowConfig.getFeatureBranchPrefix()
                        + featureBranchName, false);
            }
        } catch (CommandLineException e) {
            throw new MojoFailureException("feature-start", e);
        } catch (VersionParseException e) {
            throw new MojoFailureException("feature-start", e);
        }
    }

    private boolean validateBranchName(String name, String pattern)
            throws MojoFailureException, CommandLineException {
        boolean valid = true;
        if (StringUtils.isNotBlank(name) && validBranchName(name)) {
            if (StringUtils.isNotBlank(pattern) && !name.matches(pattern)) {
                getLog().warn("The name of the branch doesn't match '" + pattern
                        + "'.");
                valid = false;
            }
        } else {
            getLog().warn("The name of the branch is not valid.");
            valid = false;
        }
        return valid;
    }
}
