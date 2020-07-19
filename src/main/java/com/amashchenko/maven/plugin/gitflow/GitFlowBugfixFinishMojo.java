package com.amashchenko.maven.plugin.gitflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

/**
 * The git flow bugfix finish mojo.
 * 
 */
@Mojo(name = "bugfix-finish", aggregator = true)
public class GitFlowBugfixFinishMojo extends AbstractGitFlowMojo {

    /** Whether to keep bugfix branch after finish. */
    @Parameter(property = "keepBranch", defaultValue = "false")
    private boolean keepBranch = false;

    /**
     * Whether to skip calling Maven test goal before merging the branch.
     * 
     * @since 1.0.5
     */
    @Parameter(property = "skipTestProject", defaultValue = "false")
    private boolean skipTestProject = false;

    /**
     * Whether to squash bugfix branch commits into a single commit upon
     * merging.
     * 
     * @since 1.2.3
     */
    @Parameter(property = "bugfixSquash", defaultValue = "false")
    private boolean bugfixSquash = false;

    /**
     * Whether to push to the remote.
     * 
     * @since 1.3.0
     */
    @Parameter(property = "pushRemote", defaultValue = "true")
    private boolean pushRemote;

    /**
     * bugfix name to use in non-interactive mode.
     * 
     * @since 1.9.0
     */
    @Parameter(property = "bugfixName")
    private String bugfixName;

    /**
     * Maven goals to execute in the bugfix branch before merging into the
     * development branch.
     *
     * @since 1.13.0
     */
    @Parameter(property = "preBugfixFinishGoals")
    private String preBugfixFinishGoals;

    /**
     * Maven goals to execute in the development branch after merging a bugfix.
     *
     * @since 1.13.0
     */
    @Parameter(property = "postBugfixFinishGoals")
    private String postBugfixFinishGoals;


    /** {@inheritDoc} */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        validateConfiguration(preBugfixFinishGoals, postBugfixFinishGoals);

        try {
            // check uncommitted changes
            checkUncommittedChanges();

            String bugfixBranchName = null;
            if (settings.isInteractiveMode()) {
                bugfixBranchName = promptBranchName();
            } else if (StringUtils.isNotBlank(bugfixName)) {
                final String branch = gitFlowConfig.getBugfixBranchPrefix()
                        + bugfixName;
                if (!gitCheckBranchExists(branch)) {
                    throw new MojoFailureException("Bugfix branch with name '"
                            + branch
                            + "' doesn't exist. Cannot finish bugfix.");
                }
                bugfixBranchName = branch;
            }

            if (StringUtils.isBlank(bugfixBranchName)) {
                throw new MojoFailureException(
                        "Bugfix branch name to finish is blank.");
            }

            // fetch and check remote
            if (fetchRemote) {
                gitFetchRemoteAndCompare(bugfixBranchName);

                gitFetchRemoteAndCompare(gitFlowConfig.getDevelopmentBranch());
            }

            if (!skipTestProject) {
                // git checkout bugfix/...
                gitCheckout(bugfixBranchName);

                // mvn clean test
                mvnCleanTest();
            }

            // maven goals before merge
            if (StringUtils.isNotBlank(preBugfixFinishGoals)) {
                mvnRun(preBugfixFinishGoals);
            }

            final String currentBugfixVersion = getCurrentProjectVersion();

            final String bugName = bugfixBranchName.replaceFirst(gitFlowConfig.getBugfixBranchPrefix(), "");

            if (currentBugfixVersion.contains("-" + bugName)) {
                final String version = currentBugfixVersion.replaceFirst("-" + bugName, "");

                // mvn versions:set -DnewVersion=... -DgenerateBackupPoms=false
                mvnSetVersions(version);

                Map<String, String> properties = new HashMap<String, String>();
                properties.put("version", version);
                properties.put("bugfixName", bugName);

                // git commit -a -m updating versions for development branch
                gitCommit(commitMessages.getBugfixFinishMessage(), properties);
            }

            // git checkout develop
            gitCheckout(gitFlowConfig.getDevelopmentBranch());

            if (bugfixSquash) {
                // git merge --squash bugfix/...
                gitMergeSquash(bugfixBranchName);
                gitCommit(bugfixBranchName);
            } else {
                // git merge --no-ff bugfix/...
                gitMergeNoff(bugfixBranchName, commitMessages.getBugfixFinishDevMergeMessage(), null);
            }

            // maven goals after merge
            if (StringUtils.isNotBlank(postBugfixFinishGoals)) {
                mvnRun(postBugfixFinishGoals);
            }

            if (installProject) {
                // mvn clean install
                mvnCleanInstall();
            }

            if (keepBranch) {
                gitCheckout(bugfixBranchName);

                mvnSetVersions(currentBugfixVersion);

                Map<String, String> properties = new HashMap<String, String>();
                properties.put("version", currentBugfixVersion);
                properties.put("bugFixName", bugName);

                gitCommit(commitMessages.getUpdateBugfixBackMessage(), properties);
            }

            if (pushRemote) {
                gitPush(gitFlowConfig.getDevelopmentBranch(), false);

                if (keepBranch) {
                    gitPush(bugfixBranchName, false);
                } else {
                    gitPushDelete(bugfixBranchName);
                }
            }

            if (!keepBranch) {
                if (bugfixSquash) {
                    // git branch -D bugfix/...
                    gitBranchDeleteForce(bugfixBranchName);
                } else {
                    // git branch -d bugfix/...
                    gitBranchDelete(bugfixBranchName);
                }
            }
        } catch (Exception e) {
            throw new MojoFailureException("bugfix-finish", e);
        }
    }

    private String promptBranchName() throws MojoFailureException, CommandLineException {
        // git for-each-ref --format='%(refname:short)' refs/heads/bugfix/*
        final String bugfixBranches = gitFindBranches(gitFlowConfig.getBugfixBranchPrefix(), false);

        final String currentBranch = gitCurrentBranch();

        if (StringUtils.isBlank(bugfixBranches)) {
            throw new MojoFailureException("There are no bugfix branches.");
        }

        final String[] branches = bugfixBranches.split("\\r?\\n");

        List<String> numberedList = new ArrayList<String>();
        String defaultChoice = null;
        StringBuilder str = new StringBuilder("Bugfix branches:").append(LS);
        for (int i = 0; i < branches.length; i++) {
            str.append((i + 1) + ". " + branches[i] + LS);
            numberedList.add(String.valueOf(i + 1));
            if (branches[i].equals(currentBranch)) {
                defaultChoice = String.valueOf(i + 1);
            }
        }
        str.append("Choose bugfix branch to finish");

        String bugfixNumber = null;
        try {
            while (StringUtils.isBlank(bugfixNumber)) {
                bugfixNumber = prompter.prompt(str.toString(), numberedList, defaultChoice);
            }
        } catch (PrompterException e) {
            throw new MojoFailureException("bugfix-finish", e);
        }

        String bugfixBranchName = null;
        if (bugfixNumber != null) {
            int num = Integer.parseInt(bugfixNumber);
            bugfixBranchName = branches[num - 1];
        }

        return bugfixBranchName;
    }
}
