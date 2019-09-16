/*
 * Copyright 2014-2019 Aleksandr Mashchenko.
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

    /**
     * Tag name to use in non-interactive mode.
     *
     * @since 1.9.0
     */
    @Parameter(property = "tagName")
    private String tagName;

    /** {@inheritDoc} */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        validateConfiguration();

        try {
            // set git flow configuration
            initGitFlowConfig();

            // check uncommitted changes
            checkUncommittedChanges();

            String tag = null;
            if (settings.isInteractiveMode()) {
                // get tags
                String tagsStr = gitFindTags();

                if (StringUtils.isBlank(tagsStr)) {
                    throw new MojoFailureException("There are no tags.");
                }

                try {
                    tag = prompter.prompt("Choose tag to start support branch",
                            Arrays.asList(tagsStr.split("\\r?\\n")));
                } catch (PrompterException e) {
                    throw new MojoFailureException("support-start", e);
                }
            } else if (StringUtils.isNotBlank(tagName)) {
                if (gitCheckTagExists(tagName)) {
                    tag = tagName;
                } else {
                    throw new MojoFailureException("The tag '" + tagName + "' doesn't exist.");
                }
            } else {
                getLog().info("The tagName is blank. Using the last tag.");
                tag = gitFindLastTag();
            }

            if (StringUtils.isBlank(tag)) {
                throw new MojoFailureException("Tag is blank.");
            }

            // git for-each-ref refs/heads/support/...
            final boolean supportBranchExists = gitCheckBranchExists(gitFlowConfig
                    .getSupportBranchPrefix() + tag);

            if (supportBranchExists) {
                throw new MojoFailureException(
                        "Support branch with that name already exists.");
            }

            // git checkout -b ... tag
            gitCreateAndCheckout(gitFlowConfig.getSupportBranchPrefix() + tag, tag);

            if (installProject) {
                // mvn clean install
                mvnCleanInstall();
            }

            if (pushRemote) {
                gitPush(gitFlowConfig.getSupportBranchPrefix() + tag, false);
            }
        } catch (CommandLineException e) {
            throw new MojoFailureException("support-start", e);
        }
    }
}
