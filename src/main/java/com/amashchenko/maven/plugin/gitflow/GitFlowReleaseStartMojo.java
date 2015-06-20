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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.shared.release.versions.DefaultVersionInfo;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

import com.amashchenko.maven.plugin.gitflow.i18n.CommitMessages;
import com.amashchenko.maven.plugin.gitflow.i18n.ErrorMessages;
import com.amashchenko.maven.plugin.gitflow.i18n.PromptMessages;

/**
 * The git flow release start mojo.
 *
 * @author Aleksandr Mashchenko
 *
 */
@Mojo(name = "release-start", aggregator = true)
public class GitFlowReleaseStartMojo extends AbstractGitFlowMojo {

    /** {@inheritDoc} */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            // set git flow configuration
            initGitFlowConfig();

            // check uncommitted changes
            checkUncommittedChanges();

            // git for-each-ref --count=1 refs/heads/release/*
            String pattern = "refs/heads/" + gitFlowConfig.getReleaseBranchPrefix() + "*";
			final String releaseBranch = executeGitCommandReturn(
                    "for-each-ref", "--count=1", pattern);

            if (StringUtils.isNotBlank(releaseBranch)) {
                throw new MojoFailureException(msg.getMessage(ErrorMessages.release_branch_already_exists));
            }

            // need to be in develop to get correct project version
            // git checkout develop
            gitCheckout(gitFlowConfig.getDevelopmentBranch());

            // get current project version from pom
            final String currentVersion = getCurrentProjectVersion();

            String defaultVersion = "1.0.0";
            // get default release version
            try {
                final DefaultVersionInfo versionInfo = new DefaultVersionInfo(
                        currentVersion);
                defaultVersion = versionInfo.getReleaseVersionString();
            } catch (VersionParseException e) {
                if (getLog().isDebugEnabled()) {
                    getLog().debug(e);
                }
            }

            String version = null;
            if (settings.isInteractiveMode()) {
                try {
                    version = prompter.prompt(
                    		msg.getMessage(PromptMessages.release_branch_name_to_create_prompt, defaultVersion));
                } catch (PrompterException e) {
                    getLog().error(e);
                }
            }

            if (StringUtils.isBlank(version)) {
                version = defaultVersion;
            }

            // git checkout -b release/... develop
            gitCreateAndCheckout(gitFlowConfig.getReleaseBranchPrefix() + version,
            		gitFlowConfig.getDevelopmentBranch());

            // mvn versions:set -DnewVersion=... -DgenerateBackupPoms=false
            mvnSetVersions(version);

            // git commit -a -m updating poms for release
            gitCommit(msg.getMessage(CommitMessages.updating_pom_for_release_version, version));

            if (installProject) {
                // mvn clean install
                mvnCleanInstall();
            }
        } catch (CommandLineException e) {
            getLog().error(e);
        }
    }
}
