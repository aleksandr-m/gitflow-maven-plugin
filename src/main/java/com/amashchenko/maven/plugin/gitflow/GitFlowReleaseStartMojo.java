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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

/**
 * The git flow release start mojo.
 * 
 */
@Mojo(name = "release-start", aggregator = true)
public class GitFlowReleaseStartMojo extends AbstractGitFlowMojo {

    /**
     * Whether to use the same name of the release branch for every release.
     * Default is <code>false</code>, i.e. project version will be added to
     * release branch prefix. <br/>
     * Will have no effect if the <code>branchName</code> parameter is set.
     * <br/>
     *
     * Note: By itself the default releaseBranchPrefix is not a valid branch
     * name. You must change it when setting sameBranchName to <code>true</code>
     * .
     * 
     * @since 1.2.0
     */
    @Parameter(property = "sameBranchName", defaultValue = "false")
    private boolean sameBranchName = false;

    /**
     * Whether to allow SNAPSHOT versions in dependencies.
     * 
     * @since 1.2.2
     */
    @Parameter(property = "allowSnapshots", defaultValue = "false")
    private boolean allowSnapshots = false;

    /**
     * Release version to use instead of the default next release version in non
     * interactive mode.
     * 
     * @since 1.3.1
     */
    @Parameter(property = "releaseVersion", defaultValue = "")
    private String releaseVersion = "";

    /**
     * Whether to push to the remote.
     *
     * @since 1.6.0
     */
    @Parameter(property = "pushRemote", defaultValue = "false")
    private boolean pushRemote;

    /**
     * Whether to commit development version when starting the release (vs when
     * finishing the release which is the default). Has effect only when there
     * are separate development and production branches.
     * 
     * @since 1.7.0
     */
    @Parameter(property = "commitDevelopmentVersionAtStart", defaultValue = "false")
    private boolean commitDevelopmentVersionAtStart;

    /**
     * Whether to remove qualifiers from the next development version.
     *
     * @since 1.7.0
     */
    @Parameter(property = "digitsOnlyDevVersion", defaultValue = "false")
    private boolean digitsOnlyDevVersion = false;

    /**
     * Development version to use instead of the default next development
     * version in non interactive mode.
     *
     * @since 1.7.0
     */
    @Parameter(property = "developmentVersion", defaultValue = "")
    private String developmentVersion = "";

    /**
     * Which digit to increment in the next development version. Starts from
     * zero.
     *
     * @since 1.7.0
     */
    @Parameter(property = "versionDigitToIncrement")
    private Integer versionDigitToIncrement;

    /**
     * Start a release branch from this commit (SHA).
     * 
     * @since 1.7.0
     */
    @Parameter(property = "fromCommit")
    private String fromCommit;

    /**
     * Whether to use snapshot in release.
     * 
     * @since 1.10.0
     */
    @Parameter(property = "useSnapshotInRelease", defaultValue = "false")
    private boolean useSnapshotInRelease;

    /**
     * Name of the created release branch.<br>
     * The effective branch name will be a composite of this branch name and the
     * <code>releaseBranchPrefix</code>.
     * 
     * @since 1.14.0
     */
    @Parameter(property = "branchName")
    private String branchName;

    /** {@inheritDoc} */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        validateConfiguration();

        try {
            // set git flow configuration
            initGitFlowConfig();

            // check uncommitted changes
            checkUncommittedChanges();

            // git for-each-ref --count=1 refs/heads/release/*
            final String releaseBranch = gitFindBranches(
                    gitFlowConfig.getReleaseBranchPrefix(), true);

            if (StringUtils.isNotBlank(releaseBranch)) {
                throw new MojoFailureException(
                        "Release branch already exists. Cannot start release.");
            }

            if (fetchRemote) {
                // checkout from remote if doesn't exist
                gitFetchRemoteAndCreate(gitFlowConfig.getDevelopmentBranch());

                // fetch and check remote
                gitFetchRemoteAndCompare(gitFlowConfig.getDevelopmentBranch());
            }

            final String startPoint;
            if (StringUtils.isNotBlank(fromCommit) && notSameProdDevName()) {
                startPoint = fromCommit;
            } else {
                startPoint = gitFlowConfig.getDevelopmentBranch();
            }

            // need to be in develop to check snapshots and to get
            // correct project version
            gitCheckout(startPoint);

            // check snapshots dependencies
            if (!allowSnapshots) {
                checkSnapshotDependencies();
            }

            if (commitDevelopmentVersionAtStart && !notSameProdDevName()) {
                getLog().warn(
                        "The commitDevelopmentVersionAtStart will not have effect. It can be enabled only when there are separate branches for development and production.");
                commitDevelopmentVersionAtStart = false;
            }

            // get release version
            final String releaseVersion = getReleaseVersion();

            // get release branch
            String fullBranchName = gitFlowConfig.getReleaseBranchPrefix();
            if (StringUtils.isNotBlank(branchName)) {
                fullBranchName += branchName;
            } else if (!sameBranchName) {
                fullBranchName += releaseVersion;
            }

            String projectVersion = releaseVersion;
            if (useSnapshotInRelease && !ArtifactUtils.isSnapshot(projectVersion)) {
                projectVersion = projectVersion + "-" + Artifact.SNAPSHOT_VERSION;
            }

            if (useSnapshotInRelease && mavenSession.getUserProperties().get("useSnapshotInRelease") != null) {
                getLog().warn(
                        "The useSnapshotInRelease parameter is set from the command line. Don't forget to use it in the finish goal as well."
                                + " It is better to define it in the project's pom file.");
            }

            if (commitDevelopmentVersionAtStart) {
                // mvn versions:set ...
                // git commit -a -m ...
                commitProjectVersion(projectVersion,
                        commitMessages.getReleaseStartMessage()); 

                // git branch release/... develop
                gitCreateBranch(fullBranchName, startPoint);

                final String nextSnapshotVersion =
                        getNextSnapshotVersion(releaseVersion);

                // mvn versions:set ...
                // git commit -a -m ...
                commitProjectVersion(nextSnapshotVersion, commitMessages.getReleaseVersionUpdateMessage());

                // git checkout release/...
                gitCheckout(fullBranchName);
            } else {
                // git checkout -b release/... develop
                gitCreateAndCheckout(fullBranchName, startPoint);

                // mvn versions:set ...
                // git commit -a -m ...
                commitProjectVersion(projectVersion,
                        commitMessages.getReleaseStartMessage());
            }

            if (installProject) {
                // mvn clean install
                mvnCleanInstall();
            }

            if (pushRemote) {
                if (commitDevelopmentVersionAtStart) {
                    gitPush(gitFlowConfig.getDevelopmentBranch(), false);
                }

                gitPush(fullBranchName, false);
            }
        } catch (CommandLineException e) {
            throw new MojoFailureException("release-start", e);
        } catch (VersionParseException e) {
            throw new MojoFailureException("release-start", e);
        }
    }

    private String getNextSnapshotVersion(String currentVersion) throws MojoFailureException, VersionParseException {
        // get next snapshot version
        final String nextSnapshotVersion;
        if (!settings.isInteractiveMode()
                && StringUtils.isNotBlank(developmentVersion)) {
            nextSnapshotVersion = developmentVersion;
        } else {
            GitFlowVersionInfo versionInfo = new GitFlowVersionInfo(
                    currentVersion);
            if (digitsOnlyDevVersion) {
                versionInfo = versionInfo.digitsVersionInfo();
            }

            nextSnapshotVersion = versionInfo
                    .nextSnapshotVersion(versionDigitToIncrement);
        }

        if (StringUtils.isBlank(nextSnapshotVersion)) {
            throw new MojoFailureException(
                    "Next snapshot version is blank.");
        }
        return nextSnapshotVersion;
    }

    private String getReleaseVersion() throws MojoFailureException, VersionParseException, CommandLineException {
        // get current project version from pom
        final String currentVersion = getCurrentProjectVersion();

        String defaultVersion = null;
        if (tychoBuild) {
            defaultVersion = currentVersion;
        } else {
            // get default release version
            defaultVersion = new GitFlowVersionInfo(currentVersion)
                    .getReleaseVersionString();
        }

        if (defaultVersion == null) {
            throw new MojoFailureException(
                    "Cannot get default project version.");
        }

        String version = null;
        if (settings.isInteractiveMode()) {
            try {
                while (version == null) {
                    version = prompter.prompt("What is release version? ["
                            + defaultVersion + "]");

                    if (!"".equals(version)
                            && (!GitFlowVersionInfo.isValidVersion(version) || !validBranchName(version))) {
                        getLog().info("The version is not valid.");
                        version = null;
                    }
                }
            } catch (PrompterException e) {
                throw new MojoFailureException("release-start", e);
            }
        } else {
            version = releaseVersion;
        }

        if (StringUtils.isBlank(version)) {
            getLog().info("Version is blank. Using default version.");
            version = defaultVersion;
        }

        return version;
    }

    private void commitProjectVersion(String version, String commitMessage)
            throws CommandLineException, MojoFailureException {
        // execute if version changed
        String currentVersion = getCurrentProjectVersion();
        if (!version.equals(currentVersion)) {
            // mvn versions:set -DnewVersion=... -DgenerateBackupPoms=false
            mvnSetVersions(version);

            Map<String, String> properties = new HashMap<String, String>();
            properties.put("version", version);

            // git commit -a -m commitMessage
            gitCommit(commitMessage, properties);
        }
    }
}
