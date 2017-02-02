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

import java.io.FileReader;
import java.util.List;

import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * Abstract git flow mojo.
 * 
 * @author Aleksandr Mashchenko
 * 
 */
public abstract class AbstractGitFlowMojo extends AbstractMojo {
    /** A full name of the versions-maven-plugin set goal. */
    private static final String VERSIONS_MAVEN_PLUGIN_SET_GOAL = "org.codehaus.mojo:versions-maven-plugin:2.1:set";
    /** Name of the tycho-versions-plugin set-version goal. */
    private static final String TYCHO_VERSIONS_PLUGIN_SET_GOAL = "org.eclipse.tycho:tycho-versions-plugin:set-version";

    /** System line separator. */
    protected static final String LS = System.getProperty("line.separator");

    /** Success exit code. */
    private static final int SUCCESS_EXIT_CODE = 0;

    /** Command line for Git executable. */
    private final Commandline cmdGit = new Commandline();
    /** Command line for Maven executable. */
    private final Commandline cmdMvn = new Commandline();

    /** Git flow configuration. */
    @Parameter(defaultValue = "${gitFlowConfig}")
    protected GitFlowConfig gitFlowConfig;

    /**
     * Git commit messages.
     * 
     * @since 1.2.1
     */
    @Parameter(defaultValue = "${commitMessages}")
    protected CommitMessages commitMessages;

    /**
     * Whether this is Tycho build.
     * 
     * @since 1.1.0
     */
    @Parameter(defaultValue = "false")
    protected boolean tychoBuild;

    /**
     * Whether to call Maven install goal during the mojo execution.
     * 
     * @since 1.0.5
     */
    @Parameter(property = "installProject", defaultValue = "false")
    protected boolean installProject = false;

    /**
     * Whether to allow SNAPSHOT versions in dependencies.
     * 
     * @since 1.2.2
     */
    @Parameter(property = "allowSnapshots", defaultValue = "false")
    protected boolean allowSnapshots = false;

    /**
     * Whether to fetch remote branch and compare it with the local one.
     * 
     * @since 1.3.0
     */
    @Parameter(property = "fetchRemote", defaultValue = "true")
    protected boolean fetchRemote;

    /**
     * Whether to push to the remote.
     * 
     * @since 1.3.0
     */
    @Parameter(property = "pushRemote", defaultValue = "true")
    protected boolean pushRemote;

    /**
     * Whether to print commands output into the console.
     * 
     * @since 1.0.7
     */
    @Parameter(property = "verbose", defaultValue = "false")
    private boolean verbose = false;

    /**
     * The path to the Maven executable. Defaults to "mvn".
     */
    @Parameter(property = "mvnExecutable")
    private String mvnExecutable;
    /**
     * The path to the Git executable. Defaults to "git".
     */
    @Parameter(property = "gitExecutable")
    private String gitExecutable;

    /** Maven session. */
    @Component
    private MavenSession mavenSession;
    /** Maven project. */
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;
    /** Default prompter. */
    @Component
    protected Prompter prompter;
    /** Maven settings. */
    @Parameter(defaultValue = "${settings}", readonly = true)
    protected Settings settings;

    /**
     * Initializes command line executables.
     * 
     */
    private void initExecutables() {
        if (StringUtils.isBlank(cmdMvn.getExecutable())) {
            if (StringUtils.isBlank(mvnExecutable)) {
                mvnExecutable = "mvn";
            }
            cmdMvn.setExecutable(mvnExecutable);
        }
        if (StringUtils.isBlank(cmdGit.getExecutable())) {
            if (StringUtils.isBlank(gitExecutable)) {
                gitExecutable = "git";
            }
            cmdGit.setExecutable(gitExecutable);
        }
    }

    /**
     * Gets current project version from pom.xml file.
     * 
     * @return Current project version.
     * @throws MojoFailureException
     */
    protected String getCurrentProjectVersion() throws MojoFailureException {
        try {
            // read pom.xml
            final MavenXpp3Reader mavenReader = new MavenXpp3Reader();
            final FileReader fileReader = new FileReader(project.getFile()
                    .getAbsoluteFile());
            try {
                final Model model = mavenReader.read(fileReader);

                if (model.getVersion() == null) {
                    throw new MojoFailureException(
                            "Cannot get current project version. This plugin should be executed from the parent project.");
                }

                return model.getVersion();
            } finally {
                if (fileReader != null) {
                    fileReader.close();
                }
            }
        } catch (Exception e) {
            throw new MojoFailureException("", e);
        }
    }

    /**
     * Compares the production branch name with the development branch name.
     * 
     * @return <code>true</code> if the production branch name is different from
     *         the development branch name, <code>false</code> otherwise.
     */
    protected boolean notSameProdDevName() {
        return !gitFlowConfig.getProductionBranch().equals(
                gitFlowConfig.getDevelopmentBranch());
    }

    /**
     * Checks uncommitted changes.
     * 
     * @throws MojoFailureException
     * @throws CommandLineException
     */
    protected void checkUncommittedChanges() throws MojoFailureException,
            CommandLineException {
        getLog().info("Checking for uncommitted changes.");
        if (executeGitHasUncommitted()) {
            throw new MojoFailureException(
                    "You have some uncommitted files. Commit or discard local changes in order to proceed.");
        }
    }

    protected void checkSnapshotDependencies() throws MojoFailureException {
        getLog().info("Checking for SNAPSHOT versions in dependencies.");
        List<MavenProject> projects = mavenSession.getProjects();
        for (MavenProject project : projects) {
            List<Dependency> dependencies = project.getDependencies();
            for (Dependency d : dependencies) {
                if (ArtifactUtils.isSnapshot(d.getVersion())) {
                    throw new MojoFailureException(
                            "There is some SNAPSHOT dependencies in the project. Change them or ignore with `allowSnapshots` property.");
                }
            }
        }
    }

    /**
     * Checks if branch name is acceptable.
     * 
     * @param branchName
     *            Branch name to check.
     * @return <code>true</code> when name is valid, <code>false</code>
     *         otherwise.
     * @throws MojoFailureException
     * @throws CommandLineException
     */
    protected boolean validBranchName(final String branchName)
            throws MojoFailureException, CommandLineException {
        CommandResult r = executeGitCommandExitCode("check-ref-format",
                "--allow-onelevel", branchName);
        return r.getExitCode() == SUCCESS_EXIT_CODE;
    }

    /**
     * Executes git commands to check for uncommitted changes.
     * 
     * @return <code>true</code> when there are uncommitted changes,
     *         <code>false</code> otherwise.
     * @throws CommandLineException
     * @throws MojoFailureException
     */
    private boolean executeGitHasUncommitted() throws MojoFailureException,
            CommandLineException {
        boolean uncommited = false;

        // 1 if there were differences and 0 means no differences

        // git diff --no-ext-diff --ignore-submodules --quiet --exit-code
        final CommandResult diffCommandResult = executeGitCommandExitCode(
                "diff", "--no-ext-diff", "--ignore-submodules", "--quiet",
                "--exit-code");

        String error = null;

        if (diffCommandResult.getExitCode() == SUCCESS_EXIT_CODE) {
            // git diff-index --cached --quiet --ignore-submodules HEAD --
            final CommandResult diffIndexCommandResult = executeGitCommandExitCode(
                    "diff-index", "--cached", "--quiet", "--ignore-submodules",
                    "HEAD", "--");
            if (diffIndexCommandResult.getExitCode() != SUCCESS_EXIT_CODE) {
                error = diffIndexCommandResult.getError();
                uncommited = true;
            }
        } else {
            error = diffCommandResult.getError();
            uncommited = true;
        }

        if (StringUtils.isNotBlank(error)) {
            throw new MojoFailureException(error);
        }

        return uncommited;
    }

    /**
     * Executes git config commands to set Git Flow configuration.
     * 
     * @throws MojoFailureException
     * @throws CommandLineException
     */
    protected void initGitFlowConfig() throws MojoFailureException,
            CommandLineException {
        gitSetConfig("gitflow.branch.master",
                gitFlowConfig.getProductionBranch());
        gitSetConfig("gitflow.branch.develop",
                gitFlowConfig.getDevelopmentBranch());

        gitSetConfig("gitflow.prefix.feature",
                gitFlowConfig.getFeatureBranchPrefix());
        gitSetConfig("gitflow.prefix.release",
                gitFlowConfig.getReleaseBranchPrefix());
        gitSetConfig("gitflow.prefix.hotfix",
                gitFlowConfig.getHotfixBranchPrefix());
        gitSetConfig("gitflow.prefix.support",
                gitFlowConfig.getSupportBranchPrefix());
        gitSetConfig("gitflow.prefix.versiontag",
                gitFlowConfig.getVersionTagPrefix());

        gitSetConfig("gitflow.origin", gitFlowConfig.getOrigin());
    }

    /**
     * Executes git config command.
     * 
     * @param name
     *            Option name.
     * @param value
     *            Option value.
     * @throws MojoFailureException
     * @throws CommandLineException
     */
    private void gitSetConfig(final String name, String value)
            throws MojoFailureException, CommandLineException {
        if (value == null || value.isEmpty()) {
            value = "\"\"";
        }

        // ignore error exit codes
        executeGitCommandExitCode("config", name, value);
    }

    /**
     * Executes git for-each-ref with <code>refname:short</code> format.
     * 
     * @param branchName
     *            Branch name to find.
     * @param firstMatch
     *            Return first match.
     * @return Branch names which matches <code>refs/heads/{branchName}*</code>.
     * @throws MojoFailureException
     * @throws CommandLineException
     */
    protected String gitFindBranches(final String branchName,
            final boolean firstMatch) throws MojoFailureException,
            CommandLineException {
        String branches;
        if (firstMatch) {
            branches = executeGitCommandReturn("for-each-ref", "--count=1",
                    "--format=\"%(refname:short)\"", "refs/heads/" + branchName
                            + "*");
        } else {
            branches = executeGitCommandReturn("for-each-ref",
                    "--format=\"%(refname:short)\"", "refs/heads/" + branchName
                            + "*");
        }

        // on *nix systems return values from git for-each-ref are wrapped in
        // quotes
        // https://github.com/aleksandr-m/gitflow-maven-plugin/issues/3
        if (branches != null && !branches.isEmpty()) {
            branches = branches.replaceAll("\"", "");
        }

        return branches;
    }

    /**
     * Checks if local branch with given name exists.
     * 
     * @param branchName
     *            Name of the branch to check.
     * @return <code>true</code> if local branch exists, <code>false</code>
     *         otherwise.
     * @throws MojoFailureException
     * @throws CommandLineException
     */
    protected boolean gitCheckBranchExists(final String branchName)
            throws MojoFailureException, CommandLineException {
        CommandResult commandResult = executeGitCommandExitCode("show-ref",
                "--verify", "--quiet", "refs/heads/" + branchName);
        return commandResult.getExitCode() == SUCCESS_EXIT_CODE;
    }

    /**
     * Executes git checkout.
     * 
     * @param branchName
     *            Branch name to checkout.
     * @throws MojoFailureException
     * @throws CommandLineException
     */
    protected void gitCheckout(final String branchName)
            throws MojoFailureException, CommandLineException {
        getLog().info("Checking out '" + branchName + "' branch.");

        executeGitCommand("checkout", branchName);
    }

    /**
     * Executes git checkout -b.
     * 
     * @param newBranchName
     *            Create branch with this name.
     * @param fromBranchName
     *            Create branch from this branch.
     * @throws MojoFailureException
     * @throws CommandLineException
     */
    protected void gitCreateAndCheckout(final String newBranchName,
            final String fromBranchName) throws MojoFailureException,
            CommandLineException {
        getLog().info(
                "Creating a new branch '" + newBranchName + "' from '"
                        + fromBranchName + "' and checking it out.");

        executeGitCommand("checkout", "-b", newBranchName, fromBranchName);
    }

    /**
     * Executes git commit -a -m.
     * 
     * @param message
     *            Commit message.
     * @throws MojoFailureException
     * @throws CommandLineException
     */
    protected void gitCommit(final String message) throws MojoFailureException,
            CommandLineException {
        getLog().info("Committing changes.");

        executeGitCommand("commit", "-a", "-m", message);
    }

    /**
     * Executes git rebase or git merge --no-ff or git merge.
     * 
     * @param branchName
     *            Branch name to merge.
     * @param rebase
     *            Do rebase.
     * @param noff
     *            Merge with --no-ff.
     * @param ffonly
     *            Merge with --ff-only.
     * @throws MojoFailureException
     * @throws CommandLineException
     */
    protected void gitMerge(final String branchName, boolean rebase,
            boolean noff, boolean ffonly) throws MojoFailureException,
            CommandLineException {
        if (rebase) {
            getLog().info("Rebasing '" + branchName + "' branch.");
            executeGitCommand("rebase", branchName);
        } else if (ffonly) {
            getLog().info("Merging (--ff-only) '" + branchName + "' branch.");
            executeGitCommand("merge", "--ff-only", branchName);
        } else if (noff) {
            getLog().info("Merging (--no-ff) '" + branchName + "' branch.");
            executeGitCommand("merge", "--no-ff", branchName);
        } else {
            getLog().info("Merging '" + branchName + "' branch.");
            executeGitCommand("merge", branchName);
        }
    }

    /**
     * Executes git merge --no-ff.
     * 
     * @param branchName
     *            Branch name to merge.
     * @throws MojoFailureException
     * @throws CommandLineException
     */
    protected void gitMergeNoff(final String branchName)
            throws MojoFailureException, CommandLineException {
        gitMerge(branchName, false, true, false);
    }

    /**
     * Executes git merge --squash.
     * 
     * @param branchName
     *            Branch name to merge.
     * @throws MojoFailureException
     * @throws CommandLineException
     */
    protected void gitMergeSquash(final String branchName)
            throws MojoFailureException, CommandLineException {
        getLog().info("Squashing '" + branchName + "' branch.");
        executeGitCommand("merge", "--squash", branchName);
    }

    /**
     * Executes git tag -a -m.
     * 
     * @param tagName
     *            Name of the tag.
     * @param message
     *            Tag message.
     * @throws MojoFailureException
     * @throws CommandLineException
     */
    protected void gitTag(final String tagName, final String message)
            throws MojoFailureException, CommandLineException {
        getLog().info("Creating '" + tagName + "' tag.");

        executeGitCommand("tag", "-a", tagName, "-m", message);
    }

    /**
     * Executes git branch -d.
     * 
     * @param branchName
     *            Branch name to delete.
     * @throws MojoFailureException
     * @throws CommandLineException
     */
    protected void gitBranchDelete(final String branchName)
            throws MojoFailureException, CommandLineException {
        getLog().info("Deleting '" + branchName + "' branch.");

        executeGitCommand("branch", "-d", branchName);
    }

    /**
     * Executes git branch -D.
     * 
     * @param branchName
     *            Branch name to delete.
     * @throws MojoFailureException
     * @throws CommandLineException
     */
    protected void gitBranchDeleteForce(final String branchName)
            throws MojoFailureException, CommandLineException {
        getLog().info("Deleting (-D) '" + branchName + "' branch.");

        executeGitCommand("branch", "-D", branchName);
    }

    /**
     * Executes git fetch and compares local branch with the remote.
     * 
     * @param branchName
     *            Branch name to fetch and compare.
     * @throws MojoFailureException
     * @throws CommandLineException
     */
    protected void gitFetchRemoteAndCompare(final String branchName)
            throws MojoFailureException, CommandLineException {
        getLog().info(
                "Fetching remote branch '" + gitFlowConfig.getOrigin() + " "
                        + branchName + "'.");

        CommandResult result = executeGitCommandExitCode("fetch", "--quiet",
                gitFlowConfig.getOrigin(), branchName);

        if (result.getExitCode() == SUCCESS_EXIT_CODE) {
            getLog().info(
                    "Comparing local branch '" + branchName + "' with remote '"
                            + gitFlowConfig.getOrigin() + "/" + branchName
                            + "'.");
            String revlistout = executeGitCommandReturn("rev-list",
                    "--left-right", "--count", branchName + "..."
                            + gitFlowConfig.getOrigin() + "/" + branchName);

            String[] counts = org.apache.commons.lang3.StringUtils.split(
                    revlistout, '\t');
            if (counts != null && counts.length > 1) {
                if (!"0".equals(org.apache.commons.lang3.StringUtils
                        .deleteWhitespace(counts[1]))) {
                    throw new MojoFailureException(
                            "Remote branch is ahead of the local branch. Execute git pull.");
                }
            }
        } else {
            getLog().warn(
                    "There were some problems fetching remote branch '"
                            + gitFlowConfig.getOrigin()
                            + " "
                            + branchName
                            + "'. You can turn off remote branch fetching by setting the 'fetchRemote' parameter to false.");
        }
    }

    /**
     * Executes git push, optionally with the <code>--follow-tags</code>
     * argument.
     * 
     * @param branchName
     *            Branch name to push.
     * @param pushTags
     *            If <code>true</code> adds <code>--follow-tags</code> argument
     *            to the git <code>push</code> command.
     * @throws MojoFailureException
     * @throws CommandLineException
     */
    protected void gitPush(final String branchName, boolean pushTags)
            throws MojoFailureException, CommandLineException {
        getLog().info(
                "Pushing '" + branchName + "' branch" + " to '"
                        + gitFlowConfig.getOrigin() + "'.");

        if (pushTags) {
            executeGitCommand("push", "--quiet", "--follow-tags",
                    gitFlowConfig.getOrigin(), branchName);
        } else {
            executeGitCommand("push", "--quiet", gitFlowConfig.getOrigin(),
                    branchName);
        }
    }

    /**
     * Executes 'set' goal of versions-maven-plugin or 'set-version' of
     * tycho-versions-plugin in case it is tycho build.
     * 
     * @param version
     *            New version to set.
     * @throws MojoFailureException
     * @throws CommandLineException
     */
    protected void mvnSetVersions(final String version)
            throws MojoFailureException, CommandLineException {
        getLog().info("Updating version(s) to '" + version + "'.");

        if (tychoBuild) {
            executeMvnCommand(TYCHO_VERSIONS_PLUGIN_SET_GOAL, "-DnewVersion="
                    + version, "-Dtycho.mode=maven");
        } else {
            executeMvnCommand(VERSIONS_MAVEN_PLUGIN_SET_GOAL, "-DnewVersion="
                    + version, "-DgenerateBackupPoms=false");
        }
    }

    /**
     * Executes mvn clean test.
     * 
     * @throws MojoFailureException
     * @throws CommandLineException
     */
    protected void mvnCleanTest() throws MojoFailureException,
            CommandLineException {
        getLog().info("Cleaning and testing the project.");
        if (tychoBuild) {
            executeMvnCommand("clean", "verify");
        } else {
            executeMvnCommand("clean", "test");
        }
    }

    /**
     * Executes mvn clean install.
     * 
     * @throws MojoFailureException
     * @throws CommandLineException
     */
    protected void mvnCleanInstall() throws MojoFailureException,
            CommandLineException {
        getLog().info("Cleaning and installing the project.");

        executeMvnCommand("clean", "install");
    }

    /**
     * Executes Git command and returns output.
     * 
     * @param args
     *            Git command line arguments.
     * @return Command output.
     * @throws CommandLineException
     * @throws MojoFailureException
     */
    private String executeGitCommandReturn(final String... args)
            throws CommandLineException, MojoFailureException {
        return executeCommand(cmdGit, true, args).getOut();
    }

    /**
     * Executes Git command without failing on non successful exit code.
     * 
     * @param args
     *            Git command line arguments.
     * @return Command result.
     * @throws CommandLineException
     * @throws MojoFailureException
     */
    private CommandResult executeGitCommandExitCode(final String... args)
            throws CommandLineException, MojoFailureException {
        return executeCommand(cmdGit, false, args);
    }

    /**
     * Executes Git command.
     * 
     * @param args
     *            Git command line arguments.
     * @throws CommandLineException
     * @throws MojoFailureException
     */
    private void executeGitCommand(final String... args)
            throws CommandLineException, MojoFailureException {
        executeCommand(cmdGit, true, args);
    }

    /**
     * Executes Maven command.
     * 
     * @param args
     *            Maven command line arguments.
     * @throws CommandLineException
     * @throws MojoFailureException
     */
    private void executeMvnCommand(final String... args)
            throws CommandLineException, MojoFailureException {
        executeCommand(cmdMvn, true, args);
    }

    /**
     * Executes command line.
     * 
     * @param cmd
     *            Command line.
     * @param failOnError
     *            Whether to throw exception on NOT success exit code.
     * @param args
     *            Command line arguments.
     * @return {@link CommandResult} instance holding command exit code, output
     *         and error if any.
     * @throws CommandLineException
     * @throws MojoFailureException
     *             If <code>failOnError</code> is <code>true</code> and command
     *             exit code is NOT equals to 0.
     */
    private CommandResult executeCommand(final Commandline cmd,
            final boolean failOnError, final String... args)
            throws CommandLineException, MojoFailureException {
        // initialize executables
        initExecutables();

        if (getLog().isDebugEnabled()) {
            getLog().debug(
                    cmd.getExecutable() + " " + StringUtils.join(args, " "));
        }

        cmd.clearArgs();
        cmd.addArguments(args);

        final StringBufferStreamConsumer out = new StringBufferStreamConsumer(
                verbose);

        final CommandLineUtils.StringStreamConsumer err = new CommandLineUtils.StringStreamConsumer();

        // execute
        final int exitCode = CommandLineUtils.executeCommandLine(cmd, out, err);

        String errorStr = err.getOutput();
        String outStr = out.getOutput();

        if (failOnError && exitCode != SUCCESS_EXIT_CODE) {
            // not all commands print errors to error stream
            if (StringUtils.isBlank(errorStr) && StringUtils.isNotBlank(outStr)) {
                errorStr = outStr;
            }

            throw new MojoFailureException(errorStr);
        }

        return new CommandResult(exitCode, outStr, errorStr);
    }

    private static class CommandResult {
        private final int exitCode;
        private final String out;
        private final String error;

        private CommandResult(final int exitCode, final String out,
                final String error) {
            this.exitCode = exitCode;
            this.out = out;
            this.error = error;
        }

        /**
         * @return the exitCode
         */
        public int getExitCode() {
            return exitCode;
        }

        /**
         * @return the out
         */
        public String getOut() {
            return out;
        }

        /**
         * @return the error
         */
        public String getError() {
            return error;
        }
    }
}
