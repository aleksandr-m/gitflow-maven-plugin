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

import java.util.Arrays;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

/**
 * The git flow support start mojo.
 * 
 * @since 1.5.0
 */
@Mojo(name = "support-start", aggregator = true)
public class GitFlowSupportStartMojo extends AbstractGitFlowMojo {

    /**
     * Whether to push to the remote.
     * 
     * @since 1.6.0
     */
    @Parameter(property = "pushRemote", defaultValue = "true")
    private boolean pushRemote;

    /** {@inheritDoc} */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            // set git flow configuration
            initGitFlowConfig();

            // check uncommitted changes
            checkUncommittedChanges();

            // get tags
            String tagsStr = gitFindTags();

            if (StringUtils.isBlank(tagsStr)) {
                throw new MojoFailureException("There are no tags.");
            }

            String tagName = null;
            try {
                tagName = prompter.prompt("Choose tag to start support branch",
                        Arrays.asList(tagsStr.split("\\r?\\n")));
            } catch (PrompterException e) {
                getLog().error(e);
            }

            // git for-each-ref refs/heads/support/...
            final boolean supportBranchExists = gitCheckBranchExists(gitFlowConfig
                    .getSupportBranchPrefix() + tagName);

            if (supportBranchExists) {
                throw new MojoFailureException(
                        "Support branch with that name already exists.");
            }

            // git checkout -b ... tag
            gitCreateAndCheckout(gitFlowConfig.getSupportBranchPrefix()
                    + tagName, tagName);

            if (installProject) {
                // mvn clean install
                mvnCleanInstall();
            }

            if (pushRemote) {
                gitPush(gitFlowConfig.getSupportBranchPrefix() + tagName, false);
            }
        } catch (CommandLineException e) {
            getLog().error(e);
        }
    }
}
