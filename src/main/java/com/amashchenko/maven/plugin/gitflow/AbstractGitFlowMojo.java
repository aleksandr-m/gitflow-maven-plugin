/*
 * Copyright 2014-2022 Aleksandr Mashchenko.
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.release.policy.version.VersionPolicy;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * Abstract git flow mojo.
 * 
 */
public abstract class AbstractGitFlowMojo extends AbstractMojo {
    /** Group and artifact id of the versions-maven-plugin. */
    private static final String VERSIONS_MAVEN_PLUGIN = "org.codehaus.mojo:versions-maven-plugin";
    /** The versions-maven-plugin set goal. */
    private static final String VERSIONS_MAVEN_PLUGIN_SET_GOAL = "set";
    /** The versions-maven-plugin set-property goal. */
    private static final String VERSIONS_MAVEN_PLUGIN_SET_PROPERTY_GOAL = "set-property";

    /** Group and artifact id of the tycho-versions-plugin. */
    private static final String TYCHO_VERSIONS_PLUGIN = "org.eclipse.tycho:tycho-versions-plugin";
    /** The tycho-versions-plugin set-version goal. */
    private static final String TYCHO_VERSIONS_PLUGIN_SET_GOAL = "set-version";

    /** Name of the property needed to have reproducible builds. */
    private static final String REPRODUCIBLE_BUILDS_PROPERTY = "project.build.outputTimestamp";

    /** System line separator. */
    protected static final String LS = System.getProperty("line.separator");

    /** Success exit code. */
    private static final int SUCCESS_EXIT_CODE = 0;

    /** Pattern of disallowed characters in Maven commands. */
    private static final Pattern MAVEN_DISALLOWED_PATTERN = Pattern
            .compile("[&|;]");

    /** Command line for Git executable. */
    private final Commandline cmdGit = new Commandline();
    /** Command line for Maven executable. */
    private final Commandline cmdMvn = new Commandline();

    /** Whether .gitmodules file exists in project. */
    private final boolean gitModulesExists;

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
     * Whether to fetch remote branch and compare it with the local one.
     * 
     * @since 1.3.0
     */
    @Parameter(property = "fetchRemote", defaultValue = "true")
    protected boolean fetchRemote;

    /**
     * Whether to print commands output into the console.
     * 
     * @since 1.0.7
     */
    @Parameter(property = "verbose", defaultValue = "false")
    private boolean verbose = false;

    /**
     * Command line arguments to pass to the underlying Maven commands.
     * 
     * @since 1.8.0
     */
    @Parameter(property = "argLine")
    private String argLine;

    /**
     * Whether to make a GPG-signed commit.
     * 
     * @since 1.9.0
     */
    @Parameter(property = "gpgSignCommit", defaultValue = "false")
    private boolean gpgSignCommit = false;

    /**
     * Whether to set -DgroupId='*' -DartifactId='*' when calling
     * versions-maven-plugin.
     * 
     * @since 1.10.0
     */
    @Parameter(property = "versionsForceUpdate", defaultValue = "false")
    private boolean versionsForceUpdate = false;

    /**
     * Property to set version to.
     *
     * @since 1.13.0
     */
    @Parameter(property = "versionProperty")
    private String versionProperty;

    /**
     * Whether to skip updating version. Useful with {@link #versionProperty} to be
     * able to update <code>revision</code> property without modifying version tag.
     * 
     * @since 1.13.0
     */
    @Parameter(property = "skipUpdateVersion")
    private boolean skipUpdateVersion = false;

    /**
     * Prefix that is applied to commit messages.
     * 
     * @since 1.14.0
     */
    @Parameter(property = "commitMessagePrefix")
    private String commitMessagePrefix;

    /**
     * Whether to update the <code>project.build.outputTimestamp</code> property
     * automatically or not.
     *
     * @since 1.17.0
     */
    @Parameter(property = "updateOutputTimestamp", defaultValue = "true")
    private boolean updateOutputTimestamp = true;

    /**
     * The role-hint for the
     * {@link org.apache.maven.shared.release.policy.version.VersionPolicy}
     * implementation used to calculate the project versions. If a policy is set
     * other parameters controlling the generation of version are ignored
     * (digitsOnlyDevVersion, versionDigitToIncrement).
     *
     * @since 1.18.0
     */
    @Parameter(property = "projectVersionPolicyId")
    private String projectVersionPolicyId;

    /**
     * Version of versions-maven-plugin to use.
     * 
     * @since 1.18.0
     */
    @Parameter(property = "versionsMavenPluginVersion", defaultValue = "2.8.1")
    private String versionsMavenPluginVersion = "2.8.1";

    /**
     * Version of tycho-versions-plugin to use.
     * 
     * @since 1.18.0
     */
    @Parameter(property = "tychoVersionsPluginVersion", defaultValue = "0.24.0")
    private String tychoVersionsPluginVersion = "0.24.0";

    /**
     * Options to pass to Git push command using <code>--push-option</code>.
     * Multiple options can be added separated with a space e.g.
     * <code>-DgitPushOptions="merge_request.create merge_request.target=develop
     * merge_request.label='Super feature'"</code>
     * 
     * @since 1.18.0
     */
    @Parameter(property = "gitPushOptions")
    private String gitPushOptions;

    /**
     * Explicitly enable or disable executing Git submodule update before commit. By
     * default plugin tries to automatically determine if update of the Git
     * submodules is needed.
     * 
     * @since 1.19.0
     */
    @Parameter(property = "updateGitSubmodules")
    private Boolean updateGitSubmodules;

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
    @Parameter(defaultValue = "${session}", readonly = true)
    protected MavenSession mavenSession;

    @Component
    protected ProjectBuilder projectBuilder;
    
    /** Default prompter. */
    @Component
    protected Prompter prompter;
    /** Maven settings. */
    @Parameter(defaultValue = "${settings}", readonly = true)
    protected Settings settings;

    @Component
    protected Map<String, VersionPolicy> versionPolicies;

    public AbstractGitFlowMojo() {
        gitModulesExists = FileUtils.fileExists(".gitmodules");
    }

    /**
     * Initializes command line executables.
     * 
     */
    private void initExecutables() {
        if (StringUtils.isBlank(cmdMvn.getExecutable())) {
            if (StringUtils.isBlank(mvnExecutable)) {
                final String javaCommand = mavenSession.getSystemProperties().getProperty("sun.java.command", "");
                final boolean wrapper = javaCommand.startsWith("org.apache.maven.wrapper.MavenWrapperMain");

                if (wrapper) {
                    mvnExecutable = "." + SystemUtils.FILE_SEPARATOR + "mvnw";
                } else {
                    mvnExecutable = "mvn";
                }
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
     * Validates plugin configuration. Throws exception if configuration is not
     * valid.
     * 
     * @param params
     *            Configuration parameters to validate.
     * @throws MojoFailureException
     *             If configuration is not valid.
     */
    protected void validateConfiguration(String... params)
            throws MojoFailureException {
        if (StringUtils.isNotBlank(argLine)
                && MAVEN_DISALLOWED_PATTERN.matcher(argLine).find()) {
            throw new MojoFailureException(
                    "The argLine doesn't match allowed pattern.");
        }
        if (params != null && params.length > 0) {
            for (String p : params) {
                if (StringUtils.isNotBlank(p)
                        && MAVEN_DISALLOWED_PATTERN.matcher(p).find()) {
                    throw new MojoFailureException("The '" + p
                            + "' value doesn't match allowed pattern.");
                }
            }
        }
    }

    /**
     * Gets current project version from pom.xml file.
     * 
     * @return Current project version.
     * @throws MojoFailureException
     *          If current project version cannot be obtained.
     */
    protected String getCurrentProjectVersion() throws MojoFailureException {
        final MavenProject reloadedProject = reloadProject(mavenSession.getCurrentProject());
        if (reloadedProject.getVersion() == null) {
            throw new MojoFailureException(
                    "Cannot get current project version. This plugin should be executed from the parent project.");
        }
        return reloadedProject.getVersion();
    }

    /**
     * Gets current project {@link #REPRODUCIBLE_BUILDS_PROPERTY} property value
     * from pom.xml file.
     * 
     * @return Value of {@link #REPRODUCIBLE_BUILDS_PROPERTY} property.
     * @throws MojoFailureException
     *             If project loading fails.
     */
    private String getCurrentProjectOutputTimestamp() throws MojoFailureException {
        final MavenProject reloadedProject = reloadProject(mavenSession.getCurrentProject());
        return reloadedProject.getProperties().getProperty(REPRODUCIBLE_BUILDS_PROPERTY);
    }

    /**
     * Reloads projects info from file.
     * 
     * @param project
     * @return Reloaded Maven projects.
     * @throws MojoFailureException
     *             If project loading fails.
     */
    private List<MavenProject> reloadProjects(final MavenProject project) throws MojoFailureException {
        try {
            List<ProjectBuildingResult> result = projectBuilder.build(
                    Collections.singletonList(project.getFile()),
                    true,
                    mavenSession.getProjectBuildingRequest());

            List<MavenProject> projects = new ArrayList<>();
            for (ProjectBuildingResult projectBuildingResult : result) {
                projects.add(projectBuildingResult.getProject());
            }
            return projects;
        } catch (Exception e) {
            throw new MojoFailureException("Error re-loading project info", e);
        }
    }

    /**
     * Reloads project info from file.
     * 
     * @param project
     * @return Maven project which is the execution root.
     * @throws MojoFailureException
     *             If project loading fails.
     */
    private MavenProject reloadProject(final MavenProject project) throws MojoFailureException {
        List<MavenProject> projects = reloadProjects(project);
        for (MavenProject resultProject : projects) {
            if (resultProject.isExecutionRoot()) {
                return resultProject;
            }
        }
        throw new NoSuchElementException(
                "No reloaded project appears to be the execution root (" + project.getGroupId() + ":" + project.getArtifactId() + ")");
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
     *             If there is some uncommitted files.
     * @throws CommandLineException
     *             If command line execution fails.
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

        List<String> snapshots = new ArrayList<>();
        Set<String> builtArtifacts = new HashSet<>();

        List<MavenProject> projects = reloadProjects(mavenSession.getCurrentProject());
        for (MavenProject project : projects) {
            builtArtifacts.add(project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion());
        }

        for (MavenProject project : projects) {
            List<Dependency> dependencies = project.getDependencies();
            for (Dependency d : dependencies) {
                String id = d.getGroupId() + ":" + d.getArtifactId() + ":" + d.getVersion();
                if (!builtArtifacts.contains(id) && ArtifactUtils.isSnapshot(d.getVersion())) {
                    snapshots.add(project + " -> " + d);
                }
            }
            MavenProject parent = project.getParent();
            if (parent != null) {
                String id = parent.getGroupId() + ":" + parent.getArtifactId() + ":" + parent.getVersion();
                if (!builtArtifacts.contains(id) && ArtifactUtils.isSnapshot(parent.getVersion())) {
                    snapshots.add(project + " -> " + parent);
                }
            }
        }

        if (!snapshots.isEmpty()) {
            for (String s : snapshots) {
                getLog().warn(s);
            }
            throw new MojoFailureException(
                    "There is some SNAPSHOT dependencies in the project, see warnings above."
                    + " Change them or ignore with `allowSnapshots` property.");
        }
    }

    /**
     * Checks if branch name is acceptable.
     * 
     * @param branchName
     *            Branch name to check.
     * @return <code>true</code> when name is valid, <code>false</code> otherwise.
     * @throws MojoFailureException
     *             Shouldn't happen, actually.
     * @throws CommandLineException
     *             If command line execution fails.
     */
    protected boolean validBranchName(final String branchName)
            throws MojoFailureException, CommandLineException {
        CommandResult res = executeGitCommandExitCode("check-ref-format", "--allow-onelevel", branchName);
        return res.getExitCode() == SUCCESS_EXIT_CODE;
    }

    /**
     * Executes git commands to check for uncommitted changes.
     * 
     * @return <code>true</code> when there are uncommitted changes,
     *         <code>false</code> otherwise.
     * @throws CommandLineException
     *             If command line execution fails.
     * @throws MojoFailureException
     *             If command line execution returns false code.
     */
    private boolean executeGitHasUncommitted() throws MojoFailureException,
            CommandLineException {
        boolean uncommited = false;

        // 1 if there were differences and 0 means no differences

        // git diff --no-ext-diff --ignore-submodules --quiet --exit-code
        final CommandResult diffCommandResult = executeGitCommandExitCode(
                "diff", "--no-ext-diff", "--ignore-submodules", "--quiet", "--exit-code");

        String error = null;

        if (diffCommandResult.getExitCode() == SUCCESS_EXIT_CODE) {
            // git diff-index --cached --quiet --ignore-submodules HEAD --
            final CommandResult diffIndexCommandResult = executeGitCommandExitCode(
                    "diff-index", "--cached", "--quiet", "--ignore-submodules", "HEAD", "--");
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
     *             Shouldn't happen, actually.
     * @throws CommandLineException
     *             If command line execution fails.
     */
    protected void initGitFlowConfig() throws MojoFailureException,
            CommandLineException {
        gitSetConfig("gitflow.branch.master", gitFlowConfig.getProductionBranch());
        gitSetConfig("gitflow.branch.develop", gitFlowConfig.getDevelopmentBranch());

        gitSetConfig("gitflow.prefix.feature", gitFlowConfig.getFeatureBranchPrefix());
        gitSetConfig("gitflow.prefix.release", gitFlowConfig.getReleaseBranchPrefix());
        gitSetConfig("gitflow.prefix.hotfix", gitFlowConfig.getHotfixBranchPrefix());
        gitSetConfig("gitflow.prefix.support", gitFlowConfig.getSupportBranchPrefix());
        gitSetConfig("gitflow.prefix.versiontag", gitFlowConfig.getVersionTagPrefix());

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
     *             Shouldn't happen, actually.
     * @throws CommandLineException
     *             If command line execution fails.
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
     *             If command line execution returns false code.
     * @throws CommandLineException
     *             If command line execution fails.
     */
    protected String gitFindBranches(final String branchName, final boolean firstMatch)
            throws MojoFailureException, CommandLineException {
        return gitFindBranches("refs/heads/", branchName, firstMatch);
    }

    /**
     * Executes git for-each-ref with <code>refname:short</code> format.
     * 
     * @param refs
     *            Refs to search.
     * @param branchName
     *            Branch name to find.
     * @param firstMatch
     *            Return first match.
     * @return Branch names which matches <code>{refs}{branchName}*</code>.
     * @throws MojoFailureException
     *             If command line execution returns false code.
     * @throws CommandLineException
     *             If command line execution fails.
     */
    private String gitFindBranches(final String refs, final String branchName,
            final boolean firstMatch) throws MojoFailureException,
            CommandLineException {
        String wildcard = "*";
        if (branchName.endsWith("/")) {
            wildcard = "**";
        }

        String branches;
        if (firstMatch) {
            branches = executeGitCommandReturn("for-each-ref", "--count=1",
                    "--format=\"%(refname:short)\"", refs + branchName + wildcard);
        } else {
            branches = executeGitCommandReturn("for-each-ref",
                    "--format=\"%(refname:short)\"", refs + branchName + wildcard);
        }

        // on *nix systems return values from git for-each-ref are wrapped in
        // quotes
        // https://github.com/aleksandr-m/gitflow-maven-plugin/issues/3
        branches = removeQuotes(branches);
        branches = StringUtils.strip(branches);

        return branches;
    }

    /**
     * Executes git for-each-ref to get all tags.
     *
     * @return Git tags.
     * @throws MojoFailureException
     *             If command line execution returns false code.
     * @throws CommandLineException
     *             If command line execution fails.
     */
    protected String gitFindTags() throws MojoFailureException, CommandLineException {
        String tags = executeGitCommandReturn("for-each-ref", "--sort=*authordate", "--format=\"%(refname:short)\"",
                "refs/tags/");
        // https://github.com/aleksandr-m/gitflow-maven-plugin/issues/3
        tags = removeQuotes(tags);
        return tags;
    }

    /**
     * Executes git for-each-ref to get the last tag.
     *
     * @return Last tag.
     * @throws MojoFailureException
     *             If command line execution returns false code.
     * @throws CommandLineException
     *             If command line execution fails.
     */
    protected String gitFindLastTag() throws MojoFailureException, CommandLineException {
        String tag = executeGitCommandReturn("for-each-ref", "--sort=\"-version:refname\"", "--sort=-taggerdate",
                "--count=1", "--format=\"%(refname:short)\"", "refs/tags/");
        // https://github.com/aleksandr-m/gitflow-maven-plugin/issues/3
        tag = removeQuotes(tag);
        tag = tag.replaceAll("\\r?\\n", "");
        return tag;
    }

    /**
     * Removes double quotes from the string.
     * 
     * @param str
     *            String to remove quotes from.
     * @return String without quotes.
     */
    private String removeQuotes(String str) {
        return StringUtils.replace(str, "\"", "");
    }

    /**
     * Gets the current branch name.
     * 
     * @return Current branch name.
     * @throws MojoFailureException
     *             If command line execution returns false code.
     * @throws CommandLineException
     *             If command line execution fails.
     */
    protected String gitCurrentBranch() throws MojoFailureException, CommandLineException {
        String name = executeGitCommandReturn("symbolic-ref", "-q", "--short", "HEAD");
        name = StringUtils.strip(name);
        return name;
    }

    /**
     * Checks if local branch with given name exists.
     *
     * @param branchName
     *            Name of the branch to check.
     * @return <code>true</code> if local branch exists, <code>false</code>
     *         otherwise.
     * @throws MojoFailureException
     *             Shouldn't happen, actually.
     * @throws CommandLineException
     *             If command line execution fails.
     */
    protected boolean gitCheckBranchExists(final String branchName)
            throws MojoFailureException, CommandLineException {
        CommandResult commandResult = executeGitCommandExitCode("show-ref", "--verify", "--quiet", "refs/heads/" + branchName);
        return commandResult.getExitCode() == SUCCESS_EXIT_CODE;
    }

    /**
     * Checks if local tag with given name exists.
     *
     * @param tagName
     *            Name of the tag to check.
     * @return <code>true</code> if local tag exists, <code>false</code> otherwise.
     * @throws MojoFailureException
     *             Shouldn't happen, actually.
     * @throws CommandLineException
     *             If command line execution fails.
     */
    protected boolean gitCheckTagExists(final String tagName) throws MojoFailureException, CommandLineException {
        CommandResult commandResult = executeGitCommandExitCode("show-ref", "--verify", "--quiet", "refs/tags/" + tagName);
        return commandResult.getExitCode() == SUCCESS_EXIT_CODE;
    }

    /**
     * Executes git checkout.
     *
     * @param branchName
     *            Branch name to checkout.
     * @throws MojoFailureException
     *             If command line execution returns false code.
     * @throws CommandLineException
     *             If command line execution fails.
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
     *             If command line execution returns false code.
     * @throws CommandLineException
     *             If command line execution fails.
     */
    protected void gitCreateAndCheckout(final String newBranchName,
            final String fromBranchName) throws MojoFailureException,
            CommandLineException {
        getLog().info(
                "Creating a new branch '" + newBranchName + "' from '" + fromBranchName + "' and checking it out.");

        executeGitCommand("checkout", "-b", newBranchName, fromBranchName);
    }

    /**
     * Executes git branch.
     *
     * @param newBranchName
     *            Create branch with this name.
     * @param fromBranchName
     *            Create branch from this branch.
     * @throws MojoFailureException
     *             If command line execution returns false code.
     * @throws CommandLineException
     *             If command line execution fails.
     */
    protected void gitCreateBranch(final String newBranchName, final String fromBranchName)
            throws MojoFailureException, CommandLineException {
        getLog().info(
                "Creating a new branch '" + newBranchName + "' from '" + fromBranchName + "'.");

        executeGitCommand("branch", newBranchName, fromBranchName);
    }

    /**
     * Replaces properties in message.
     * 
     * @param message
     * @param map
     *            Key is a string to replace wrapped in <code>@{...}</code>. Value
     *            is a string to replace with.
     * @return Message with replaced properties.
     */
    private String replaceProperties(String message, Map<String, String> map) {
        if (map != null) {
            for (Entry<String, String> entr : map.entrySet()) {
                message = StringUtils.replace(message, "@{" + entr.getKey() + "}", entr.getValue());
            }
        }
        return message;
    }

    /**
     * Executes git commit -a -m.
     * 
     * @param message
     *            Commit message.
     * @throws MojoFailureException
     *             If command line execution returns false code.
     * @throws CommandLineException
     *             If command line execution fails.
     */
    protected void gitCommit(final String message) throws MojoFailureException, CommandLineException {
        gitCommit(message, null);
    }

    /**
     * Executes git commit -a -m, replacing <code>@{map.key}</code> with
     * <code>map.value</code>.
     * 
     * @param message
     *            Commit message.
     * @param messageProperties
     *            Properties to replace in message.
     * @throws MojoFailureException
     *             If command line execution returns false code.
     * @throws CommandLineException
     *             If command line execution fails.
     */
    protected void gitCommit(String message, Map<String, String> messageProperties)
            throws MojoFailureException, CommandLineException {
        if ((gitModulesExists && updateGitSubmodules == null) || Boolean.TRUE.equals(updateGitSubmodules)) {
            getLog().info("Updating git submodules before commit.");
            executeGitCommand("submodule", "update");
        }

        if (StringUtils.isNotBlank(commitMessagePrefix)) {
            message = commitMessagePrefix + message;
        }

        message = replaceProperties(message, messageProperties);

        if (gpgSignCommit) {
            getLog().info("Committing changes. GPG-signed.");

            executeGitCommand("commit", "-a", "-S", "-m", message);
        } else {
            getLog().info("Committing changes.");

            executeGitCommand("commit", "-a", "-m", message);
        }
    }

    /**
     * Executes git rebase or git merge --ff-only or git merge --no-ff or git merge.
     * 
     * @param branchName
     *            Branch name to merge.
     * @param rebase
     *            Do rebase.
     * @param noff
     *            Merge with --no-ff.
     * @param ffonly
     *            Merge with --ff-only.
     * @param message
     *            Merge commit message.
     * @param messageProperties
     *            Properties to replace in message.
     * @throws MojoFailureException
     *             If command line execution returns false code.
     * @throws CommandLineException
     *             If command line execution fails.
     */
    protected void gitMerge(final String branchName, boolean rebase, boolean noff, boolean ffonly, String message,
            Map<String, String> messageProperties)
            throws MojoFailureException, CommandLineException {
        String sign = "";
        if (gpgSignCommit) {
            sign = "-S";
        }
        String msgParam = "";
        String msg = "";
        if (StringUtils.isNotBlank(message)) {
            if (StringUtils.isNotBlank(commitMessagePrefix)) {
                message = commitMessagePrefix + message;
            }

            msgParam = "-m";
            msg = replaceProperties(message, messageProperties);
        }
        if (rebase) {
            getLog().info("Rebasing '" + branchName + "' branch.");
            executeGitCommand("rebase", sign, branchName);
        } else if (ffonly) {
            getLog().info("Merging (--ff-only) '" + branchName + "' branch.");
            executeGitCommand("merge", "--ff-only", sign, branchName);
        } else if (noff) {
            getLog().info("Merging (--no-ff) '" + branchName + "' branch.");
            executeGitCommand("merge", "--no-ff", sign, branchName, msgParam, msg);
        } else {
            getLog().info("Merging '" + branchName + "' branch.");
            executeGitCommand("merge", sign, branchName, msgParam, msg);
        }
    }

    /**
     * Executes git merge --no-ff.
     * 
     * @param branchName
     *            Branch name to merge.
     * @param message
     *            Merge commit message.
     * @param messageProperties
     *            Properties to replace in message.
     * @throws MojoFailureException
     *             If command line execution returns false code.
     * @throws CommandLineException
     *             If command line execution fails.
     */
    protected void gitMergeNoff(final String branchName, final String message,
            final Map<String, String> messageProperties)
            throws MojoFailureException, CommandLineException {
        gitMerge(branchName, false, true, false, message, messageProperties);
    }

    /**
     * Executes git merge --squash.
     * 
     * @param branchName
     *            Branch name to merge.
     * @throws MojoFailureException
     *             If command line execution returns false code.
     * @throws CommandLineException
     *             If command line execution fails.
     */
    protected void gitMergeSquash(final String branchName)
            throws MojoFailureException, CommandLineException {
        getLog().info("Squashing '" + branchName + "' branch.");
        executeGitCommand("merge", "--squash", branchName);
    }

    /**
     * Executes git tag -a [-s] -m.
     * 
     * @param tagName
     *            Name of the tag.
     * @param message
     *            Tag message.
     * @param gpgSignTag
     *            Make a GPG-signed tag.
     * @param messageProperties
     *            Properties to replace in message.
     * @throws MojoFailureException
     *             If command line execution returns false code.
     * @throws CommandLineException
     *             If command line execution fails.
     */
    protected void gitTag(final String tagName, String message, boolean gpgSignTag, Map<String, String> messageProperties)
            throws MojoFailureException, CommandLineException {
        message = replaceProperties(message, messageProperties);

        if (gpgSignTag) {
            getLog().info("Creating GPG-signed '" + tagName + "' tag.");

            executeGitCommand("tag", "-a", "-s", tagName, "-m", message);
        } else {
            getLog().info("Creating '" + tagName + "' tag.");

            executeGitCommand("tag", "-a", tagName, "-m", message);
        }
    }

    /**
     * Executes git branch -d.
     * 
     * @param branchName
     *            Branch name to delete.
     * @throws MojoFailureException
     *             If command line execution returns false code.
     * @throws CommandLineException
     *             If command line execution fails.
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
     *             If command line execution returns false code.
     * @throws CommandLineException
     *             If command line execution fails.
     */
    protected void gitBranchDeleteForce(final String branchName)
            throws MojoFailureException, CommandLineException {
        getLog().info("Deleting (-D) '" + branchName + "' branch.");

        executeGitCommand("branch", "-D", branchName);
    }

    /**
     * Executes git fetch and checks if local branch exists. If local branch is
     * present then compares it with the remote, if not then branch is checked out.
     * 
     * @param branchName
     *            Branch name to check.
     * @throws MojoFailureException
     *             If command line execution returns false code or remote branch is
     *             ahead of the local branch.
     * @throws CommandLineException
     *             If command line execution fails.
     */
    protected void gitFetchRemoteAndCompareCreate(final String branchName) throws MojoFailureException, CommandLineException {
        final boolean remoteBranchExists = StringUtils.isNotBlank(gitFetchAndFindRemoteBranches(branchName, true));
        if (gitCheckBranchExists(branchName)) {
            if (remoteBranchExists) {
                getLog().info(
                        "Comparing local branch '" + branchName + "' with remote '" + gitFlowConfig.getOrigin() + "/" + branchName + "'.");
                String revlistout = executeGitCommandReturn("rev-list", "--left-right", "--count",
                        branchName + "..." + gitFlowConfig.getOrigin() + "/" + branchName);

                String[] counts = org.apache.commons.lang3.StringUtils.split(revlistout, '\t');
                if (counts != null && counts.length > 1 && !"0".equals(org.apache.commons.lang3.StringUtils.deleteWhitespace(counts[1]))) {
                    throw new MojoFailureException("Remote branch '" + gitFlowConfig.getOrigin() + "/" + branchName
                            + "' is ahead of the local branch '" + branchName + "'. Execute git pull.");
                }
            }
        } else {
            getLog().info("Local branch '" + branchName + "' doesn't exist. Trying check it out from '" + gitFlowConfig.getOrigin() + "'.");
            gitCreateAndCheckout(branchName, gitFlowConfig.getOrigin() + "/" + branchName);
        }
    }

    /**
     * Executes git fetch and git for-each-ref with <code>refname:short</code>
     * format. Searches <code>refs/remotes/{gitFlowConfig#origin}/</code>.
     * 
     * @param branchName
     *            Branch name to find.
     * @param firstMatch
     *            Return first match.
     * @return Branch names which matches
     *         <code>refs/remotes/{gitFlowConfig#origin}/{branchName}*</code>.
     * @throws MojoFailureException
     *             If command line execution returns false code.
     * @throws CommandLineException
     *             If command line execution fails.
     */
    protected String gitFetchAndFindRemoteBranches(final String branchName, final boolean firstMatch)
            throws MojoFailureException, CommandLineException {
        gitFetchRemote();
        return gitFindBranches("refs/remotes/" + gitFlowConfig.getOrigin() + "/", branchName, firstMatch);
    }

    /**
     * Executes git fetch.
     * 
     * @return <code>true</code> if git fetch returned success exit code,
     *         <code>false</code> otherwise.
     * @throws MojoFailureException
     *             Shouldn't happen, actually.
     * @throws CommandLineException
     *             If command line execution fails.
     */
    private boolean gitFetchRemote() throws MojoFailureException, CommandLineException {
        getLog().info("Fetching remote from '" + gitFlowConfig.getOrigin() + "'.");

        CommandResult result = executeGitCommandExitCode("fetch", "--quiet", gitFlowConfig.getOrigin());

        boolean success = result.getExitCode() == SUCCESS_EXIT_CODE;
        if (!success) {
            getLog().warn(
                    "There were some problems fetching from '"
                            + gitFlowConfig.getOrigin()
                            + "'. You can turn off remote fetching by setting the 'fetchRemote' parameter to false.");
        }

        return success;
    }

    /**
     * Executes git push, optionally with the <code>--follow-tags</code> argument.
     * 
     * @param branchName
     *            Branch name to push.
     * @param pushTags
     *            If <code>true</code> adds <code>--follow-tags</code> argument to
     *            the git <code>push</code> command.
     * @throws MojoFailureException
     *             If command line execution returns false code.
     * @throws CommandLineException
     *             If command line execution fails.
     */
    protected void gitPush(final String branchName, boolean pushTags) throws MojoFailureException, CommandLineException {
        getLog().info("Pushing '" + branchName + "' branch to '" + gitFlowConfig.getOrigin() + "'.");

        List<String> args = new ArrayList<>();
        args.add("push");
        args.add("--quiet");
        args.add("-u");

        if (pushTags) {
            args.add("--follow-tags");
        }

        if (StringUtils.isNotBlank(gitPushOptions)) {
            try {
                String[] opts = CommandLineUtils.translateCommandline(gitPushOptions);
                for (String opt : opts) {
                    args.add("--push-option=" + opt);
                }
            } catch (Exception e) {
                throw new CommandLineException(e.getMessage(), e);
            }
        }

        args.add(gitFlowConfig.getOrigin());
        args.add(branchName);

        executeGitCommand(args.toArray(new String[0]));
    }

    protected void gitPushDelete(final String branchName)
            throws MojoFailureException, CommandLineException {
        getLog().info(
                "Deleting remote branch '" + branchName + "' from '" + gitFlowConfig.getOrigin() + "'.");

        CommandResult result = executeGitCommandExitCode("push", "--delete", gitFlowConfig.getOrigin(), branchName);

        if (result.getExitCode() != SUCCESS_EXIT_CODE) {
            getLog().warn(
                    "There were some problems deleting remote branch '"
                            + branchName + "' from '" + gitFlowConfig.getOrigin() + "'.");
        }
    }

    /**
     * Executes 'set' goal of versions-maven-plugin or 'set-version' of
     * tycho-versions-plugin in case it is tycho build.
     * 
     * @param version
     *            New version to set.
     * @throws MojoFailureException
     *             If command line execution returns false code.
     * @throws CommandLineException
     *             If command line execution fails.
     */
    protected void mvnSetVersions(final String version) throws MojoFailureException, CommandLineException {
        getLog().info("Updating version(s) to '" + version + "'.");

        String newVersion = "-DnewVersion=" + version;
        String grp = "";
        String art = "";
        if (versionsForceUpdate) {
            grp = "-DgroupId=";
            art = "-DartifactId=";
        }

        if (tychoBuild) {
            String prop = "";
            if (StringUtils.isNotBlank(versionProperty)) {
                prop = "-Dproperties=" + versionProperty;
                getLog().info("Updating property '" + versionProperty + "' to '" + version + "'.");
            }

            executeMvnCommand(TYCHO_VERSIONS_PLUGIN + ":" + tychoVersionsPluginVersion + ":" + TYCHO_VERSIONS_PLUGIN_SET_GOAL, prop,
                    newVersion, "-Dtycho.mode=maven");
        } else {
            boolean runCommand = false;
            List<String> args = new ArrayList<>();
            args.add("-DgenerateBackupPoms=false");
            args.add(newVersion);
            if (!skipUpdateVersion) {
                runCommand = true;
                args.add(VERSIONS_MAVEN_PLUGIN + ":" + versionsMavenPluginVersion + ":" + VERSIONS_MAVEN_PLUGIN_SET_GOAL);
                args.add(grp);
                args.add(art);
            }

            if (StringUtils.isNotBlank(versionProperty)) {
                runCommand = true;
                getLog().info("Updating property '" + versionProperty + "' to '" + version + "'.");

                args.add(VERSIONS_MAVEN_PLUGIN + ":" + versionsMavenPluginVersion + ":" + VERSIONS_MAVEN_PLUGIN_SET_PROPERTY_GOAL);
                args.add("-Dproperty=" + versionProperty);
            }
            if (runCommand) {
                executeMvnCommand(args.toArray(new String[0]));

                if (updateOutputTimestamp) {
                    String timestamp = getCurrentProjectOutputTimestamp();
                    if (timestamp != null && timestamp.length() > 1) {
                        if (StringUtils.isNumeric(timestamp)) {
                            // int representing seconds since the epoch
                            timestamp = String.valueOf(System.currentTimeMillis() / 1000l);
                        } else {
                            // ISO-8601
                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                            df.setTimeZone(TimeZone.getTimeZone("UTC"));
                            timestamp = df.format(new Date());
                        }

                        getLog().info("Updating property '" + REPRODUCIBLE_BUILDS_PROPERTY + "' to '" + timestamp + "'.");

                        executeMvnCommand(
                                VERSIONS_MAVEN_PLUGIN + ":" + versionsMavenPluginVersion + ":" + VERSIONS_MAVEN_PLUGIN_SET_PROPERTY_GOAL,
                                "-DgenerateBackupPoms=false",
                                "-Dproperty=" + REPRODUCIBLE_BUILDS_PROPERTY, "-DnewVersion=" + timestamp);
                    }
                }
            }
        }
    }

    /**
     * Executes mvn clean test.
     * 
     * @throws MojoFailureException
     *             If command line execution returns false code.
     * @throws CommandLineException
     *             If command line execution fails.
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
     *             If command line execution returns false code.
     * @throws CommandLineException
     *             If command line execution fails.
     */
    protected void mvnCleanInstall() throws MojoFailureException,
            CommandLineException {
        getLog().info("Cleaning and installing the project.");

        executeMvnCommand("clean", "install");
    }

    /**
     * Executes Maven goals.
     * 
     * @param goals
     *            The goals to execute.
     * @throws Exception
     *             If command line parsing or execution fails.
     */
    protected void mvnRun(final String goals) throws Exception {
        getLog().info("Running Maven goals: " + goals);

        executeMvnCommand(CommandLineUtils.translateCommandline(goals));
    }

    /**
     * Executes Git command and returns output.
     * 
     * @param args
     *            Git command line arguments.
     * @return Command output.
     * @throws CommandLineException
     *             If command line execution fails.
     * @throws MojoFailureException
     *             If command line execution returns false code.
     */
    private String executeGitCommandReturn(final String... args)
            throws CommandLineException, MojoFailureException {
        return executeCommand(cmdGit, true, null, args).getOut();
    }

    /**
     * Executes Git command without failing on non successful exit code.
     * 
     * @param args
     *            Git command line arguments.
     * @return Command result.
     * @throws CommandLineException
     *             If command line execution fails.
     * @throws MojoFailureException
     *             Shouldn't happen, actually.
     */
    private CommandResult executeGitCommandExitCode(final String... args)
            throws CommandLineException, MojoFailureException {
        return executeCommand(cmdGit, false, null, args);
    }

    /**
     * Executes Git command.
     * 
     * @param args
     *            Git command line arguments.
     * @throws CommandLineException
     *             If command line execution fails.
     * @throws MojoFailureException
     *             If command line execution returns false code.
     */
    private void executeGitCommand(final String... args)
            throws CommandLineException, MojoFailureException {
        executeCommand(cmdGit, true, null, args);
    }

    /**
     * Executes Maven command.
     * 
     * @param args
     *            Maven command line arguments.
     * @throws CommandLineException
     *             If command line execution fails.
     * @throws MojoFailureException
     *             If command line execution returns false code.
     */
    private void executeMvnCommand(final String... args)
            throws CommandLineException, MojoFailureException {
        executeCommand(cmdMvn, true, argLine, args);
    }

    /**
     * Executes command line.
     * 
     * @param cmd
     *            Command line.
     * @param failOnError
     *            Whether to throw exception on NOT success exit code.
     * @param argStr
     *            Command line arguments as a string.
     * @param args
     *            Command line arguments.
     * @return {@link CommandResult} instance holding command exit code, output and
     *         error if any.
     * @throws CommandLineException
     *             If command line execution fails.
     * @throws MojoFailureException
     *             If <code>failOnError</code> is <code>true</code> and command exit
     *             code is NOT equals to 0.
     */
    private CommandResult executeCommand(final Commandline cmd,
            final boolean failOnError, final String argStr,
            final String... args) throws CommandLineException,
            MojoFailureException {
        // initialize executables
        initExecutables();

        if (getLog().isDebugEnabled()) {
            getLog().debug(
                    cmd.getExecutable() + " " + StringUtils.join(args, " ")
                            + (argStr == null ? "" : " " + argStr));
        }

        cmd.clearArgs();
        cmd.addArguments(args);

        if (StringUtils.isNotBlank(argStr)) {
            cmd.createArg().setLine(argStr);
        }

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

        if (verbose && StringUtils.isNotBlank(errorStr)) {
            getLog().warn(errorStr);
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

    public void setArgLine(String argLine) {
        this.argLine = argLine;
    }

    protected VersionPolicy getVersionPolicy() {
        if (StringUtils.isNotBlank(projectVersionPolicyId)) {
            VersionPolicy versionPolicy = versionPolicies.get(projectVersionPolicyId);
            if (versionPolicy == null) {
                throw new IllegalArgumentException("No implementation found for projectVersionPolicyId: " + projectVersionPolicyId);
            }
            return versionPolicy;
        }
        return null;
    }
}
