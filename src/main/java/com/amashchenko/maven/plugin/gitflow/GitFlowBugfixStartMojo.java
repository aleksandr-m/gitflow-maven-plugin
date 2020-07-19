package com.amashchenko.maven.plugin.gitflow;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

/**
 * The git flow bugfix start mojo.
 * 
 */
@Mojo(name = "bugfix-start", aggregator = true)
public class GitFlowBugfixStartMojo extends AbstractGitFlowMojo {

  
    /**
     * Regex pattern to enforce naming of the bugfix branches. Doesn't have
     * effect if not set or blank.
     * 
     * @since 1.5.0
     */
    @Parameter
    private String bugfixNamePattern;

    /**
     * Whether to push to the remote.
     * 
     * @since 1.6.0
     */
    @Parameter(property = "pushRemote", defaultValue = "false")
    private boolean pushRemote;

    /**
     * bugfix name to use in non-interactive mode.
     * 
     * @since 1.9.0
     */
    @Parameter(property = "bugfixName")
    private String bugfixName;

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

            String bugfixBranchName = null;
            if (settings.isInteractiveMode()) {
                try {
                    while (StringUtils.isBlank(bugfixBranchName)) {
                        bugfixBranchName = prompter
                                .prompt("What is a name of bugfix branch? "
                                        + gitFlowConfig
                                                .getBugfixBranchPrefix());

                        if (!validateBranchName(bugfixBranchName,
                                bugfixNamePattern)) {
                            bugfixBranchName = null;
                        }
                    }
                } catch (PrompterException e) {
                    throw new MojoFailureException("bugfix-start", e);
                }
            } else if (validateBranchName(bugfixName, bugfixNamePattern)) {
                bugfixBranchName = bugfixName;
            }

            if (StringUtils.isBlank(bugfixBranchName)) {
                throw new MojoFailureException("Branch name is blank.");
            }

            bugfixBranchName = StringUtils.deleteWhitespace(bugfixBranchName);

            // git for-each-ref refs/heads/bugfix/...
            final boolean bugfixBranchExists = gitCheckBranchExists(
                    gitFlowConfig.getBugfixBranchPrefix() + bugfixBranchName);

            if (bugfixBranchExists) {
                throw new MojoFailureException(
                        "Bugfix branch with that name already exists. Cannot start bugfix.");
            }

            // git checkout -b ... develop
            gitCreateAndCheckout(
                    gitFlowConfig.getBugfixBranchPrefix() + bugfixBranchName,
                    gitFlowConfig.getDevelopmentBranch());

         

            if (installProject) {
                // mvn clean install
                mvnCleanInstall();
            }

            if (pushRemote) {
                gitPush(gitFlowConfig.getBugfixBranchPrefix()
                        + bugfixBranchName, false);
            }
        } catch (CommandLineException e) {
            throw new MojoFailureException("bugfix-start", e);
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
