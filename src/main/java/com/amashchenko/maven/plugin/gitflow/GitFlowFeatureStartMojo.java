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

import static java.util.regex.Pattern.matches;
import static org.codehaus.plexus.util.StringUtils.isBlank;
import static org.codehaus.plexus.util.StringUtils.isNotBlank;

/**
 * The git flow feature start mojo.
 * 
 * @author Aleksandr Mashchenko
 * 
 */
@Mojo(name = "feature-start", aggregator = true)
public class GitFlowFeatureStartMojo extends AbstractGitFlowMojo {

    public static final String FEATURE_NAME_REGEX = ".*";
    /**
     * Whether to skip changing project version. Default is <code>false</code>
     * (the feature name will be appended to project version).
     * 
     * @since 1.0.5
     */
    @Parameter(property = "skipFeatureVersion", defaultValue = "false")
    private boolean skipFeatureVersion = false;

    /**
     * Regex used to enforce a naming convention for feature branches,
     * by default it allows everything: {@link #FEATURE_NAME_REGEX}
     */
    @Parameter(property = "featureBranchRegex", defaultValue = FEATURE_NAME_REGEX)
    String featureBranchRegex = FEATURE_NAME_REGEX;

    /** {@inheritDoc} */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            // set git flow configuration
            initGitFlowConfig();

            // check uncommitted changes
            checkUncommittedChanges();

            // fetch and check remote
            if (fetchRemote) {
                gitFetchRemoteAndCompare(gitFlowConfig.getDevelopmentBranch());
            }

            String featureName = null;
            try {
                while (isBlank(featureName)) {
                    featureName = prompter
                            .prompt("What is a name of feature branch? "
                                    + gitFlowConfig.getFeatureBranchPrefix());
                    if(isNotBlank(featureName)) {
                        if (!isBranchNameRegexCompliant(featureName)) {
                            getLog().warn("The name of the branch is not regex compliant, it does not match: " + featureBranchRegex);
                            featureName = null;
                            continue;
                        }
                        if (!validBranchName(featureName)) {
                            getLog().info("The name of the branch is not valid.");
                            featureName = null;
                        }
                    }
                }
            } catch (PrompterException e) {
                getLog().error(e);
            }

            featureName = StringUtils.deleteWhitespace(featureName);

            // git for-each-ref refs/heads/feature/...
            final boolean featureBranchExists = gitCheckBranchExists(gitFlowConfig
                    .getFeatureBranchPrefix() + featureName);

            if (featureBranchExists) {
                throw new MojoFailureException(
                        "Feature branch with that name already exists. Cannot start feature.");
            }

            // git checkout -b ... develop
            gitCreateAndCheckout(gitFlowConfig.getFeatureBranchPrefix()
                    + featureName, gitFlowConfig.getDevelopmentBranch());

            if (!skipFeatureVersion && !tychoBuild) {
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

                    // git commit -a -m updating versions for feature branch
                    gitCommit(commitMessages.getFeatureStartMessage());
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

    boolean isBranchNameRegexCompliant(String branchName){
        return matches(featureBranchRegex, branchName);
    }
}
