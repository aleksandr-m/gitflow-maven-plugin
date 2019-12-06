# Git-Flow Maven Plugin

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.amashchenko.maven.plugin/gitflow-maven-plugin/badge.svg?subject=Maven%20Central)](https://maven-badges.herokuapp.com/maven-central/com.amashchenko.maven.plugin/gitflow-maven-plugin/)
[![License](https://img.shields.io/badge/License-Apache%20License%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

The Maven plugin that supports various Git workflows, including Vincent Driessen's [successful Git branching model](https://nvie.com/posts/a-successful-git-branching-model/) and [GitHub Flow](https://guides.github.com/introduction/flow/).

Currently a Java implementation of Git version control system [JGit](https://github.com/eclipse/jgit) doesn't support [`.gitattributes`](https://git-scm.com/book/en/Customizing-Git-Git-Attributes).

This plugin runs Git and Maven commands from the command line ensuring that all Git features work properly.

## Changelog

See what's changed - [CHANGELOG](CHANGELOG.md)

# Installation

The plugin is available from Maven Central.
    
    <build>
        <plugins>
            <plugin>
                <groupId>com.amashchenko.maven.plugin</groupId>
                <artifactId>gitflow-maven-plugin</artifactId>
                <version>1.14.0</version>
                <configuration>
                    <!-- optional configuration -->
                </configuration>
            </plugin>
        </plugins>
    </build>


# Goals Overview

- `gitflow:release-start` - Starts a release branch and updates version(s) to release version.
- `gitflow:release-finish` - Merges a release branch and updates version(s) to next development version.
- `gitflow:release` - Releases project w/o creating a release branch.
- `gitflow:feature-start` - Starts a feature branch and optionally updates version(s).
- `gitflow:feature-finish` - Merges a feature branch.
- `gitflow:hotfix-start` - Starts a hotfix branch and updates version(s) to hotfix version.
- `gitflow:hotfix-finish` - Merges a hotfix branch.
- `gitflow:support-start` - Starts a support branch from the production tag.
- `gitflow:help` - Displays help information.


# Git Workflows

The gitflow-maven-plugin is very versatile. It can be easily configured to use different Git workflows.

## GitHub Flow

The GitHub Flow is a lightweight, branch-based workflow that supports teams and projects where deployments are made regularly.

To configure this plugin to use single branch model, such as GitHub Flow, just set the `developmentBranch` parameter to the same value as the `productionBranch` in your pom.xml file.

    <gitFlowConfig>
        <developmentBranch>master</developmentBranch>
    </gitFlowConfig>

That's it!


# Eclipse Plugins build with Tycho

Since version `1.1.0` this plugin supports Eclipse plugin projects which are build with [Tycho](https://eclipse.org/tycho/).
To enable this feature put `<tychoBuild>true</tychoBuild>` into `<configuration>` section of this plugin in your pom.xml file.

### Features of `tychoBuild` 

The [`tycho-versions-plugin`](https://eclipse.org/tycho/sitedocs/tycho-release/tycho-versions-plugin/plugin-info.html) Maven plugin will be used to set versions instead of [`versions-maven-plugin`](https://www.mojohaus.org/versions-maven-plugin/).

Feature name will not be appended to project version on `gitflow:feature-start` goal even if the `skipFeatureVersion` is set to `false`.

If version has qualifier then it will not be removed in the release or hotfix goals.


# Signing Your Work

To sign tags and/or commits you need to configure GPG and install personal key. Read more [Git Tools - Signing Your Work](https://git-scm.com/book/en/v2/Git-Tools-Signing-Your-Work).

Next you need to configure Git to use your personal key.

```
git config --global user.signingkey GPG_key_id
```

Sometimes you need to tell Git where the GPG program is. Use `gpg.program` option for that.

```
git config --global gpg.program "path_to_gpg"
```

### Signing Tags

The `gitflow:release`, `gitflow:release-finish` and `gitflow:hotfix-finish` goals have `gpgSignTag` parameter. Set it to `true` to sign tags with configured personal key. The default value is `false`.

### Signing Commits

All goals have `gpgSignCommit` parameter. Set it to `true` to sign commits with configured personal key. The default value is `false`.


# Plugin Common Parameters

All parameters are optional. The `gitFlowConfig` parameters defaults are the same as in the example below.
Maven and Git executables are assumed to be in the PATH, if executables are not available in the PATH or you want to use different version use `mvnExecutable` and `gitExecutable` parameters.
The `installProject` parameter controls whether the Maven `install` goal will be called during the mojo execution. The default value for this parameter is `false` (i.e. the project will NOT be installed).
Since `1.0.7` version of this plugin the output of the executed commands will NOT be printed into the console. This can be changed by setting `verbose` parameter to `true`.

    <configuration>
        <mvnExecutable>path_to_maven_executable</mvnExecutable>
        <gitExecutable>path_to_git_executable</gitExecutable>

        <installProject>false</installProject>
        <verbose>false</verbose>

        <gitFlowConfig>
            <productionBranch>master</productionBranch>
            <developmentBranch>develop</developmentBranch>
            <featureBranchPrefix>feature/</featureBranchPrefix>
            <releaseBranchPrefix>release/</releaseBranchPrefix>
            <hotfixBranchPrefix>hotfix/</hotfixBranchPrefix>
            <supportBranchPrefix>support/</supportBranchPrefix>
            <versionTagPrefix></versionTagPrefix>
            <origin>origin</origin>
        </gitFlowConfig>

        <commitMessages>
            <!-- since 1.2.1, see Customizing commit messages -->
        </commitMessages>
    </configuration>

## Customizing commit messages

Since `1.2.1` commit messages can be changed in plugin's configuration section in pom.xml. Commit messages defaults are seen below.

    <configuration>
        <commitMessages>
            <featureStartMessage>Update versions for feature branch</featureStartMessage>
            <featureFinishMessage>Update versions for development branch</featureFinishMessage>

            <hotfixStartMessage>Update versions for hotfix</hotfixStartMessage>
            <hotfixFinishMessage>Update for next development version</hotfixFinishMessage>

            <hotfixVersionUpdateMessage>Update to hotfix version</hotfixVersionUpdateMessage>

            <releaseStartMessage>Update versions for release</releaseStartMessage>
            <releaseFinishMessage>Update for next development version</releaseFinishMessage>

            <releaseVersionUpdateMessage>Update for next development version</releaseVersionUpdateMessage>

            <!-- git merge messages -->
            <!-- Default git merge commit message will be used if left empty or undefined. -->

            <releaseFinishMergeMessage></releaseFinishMergeMessage>
            <releaseFinishDevMergeMessage></releaseFinishDevMergeMessage>

            <featureFinishDevMergeMessage></featureFinishDevMergeMessage>

            <hotfixFinishMergeMessage></hotfixFinishMergeMessage>
            <hotfixFinishDevMergeMessage></hotfixFinishDevMergeMessage>
            <hotfixFinishReleaseMergeMessage></hotfixFinishReleaseMergeMessage>
            <hotfixFinishSupportMergeMessage></hotfixFinishSupportMergeMessage>

            <!-- / git merge messages -->

            <tagHotfixMessage>Tag hotfix</tagHotfixMessage>
            <tagReleaseMessage>Tag release</tagReleaseMessage>

            <!-- Migration Note: This was called <updateDevToAvoidConflitsMessage> in version 1.11.0, but has been deprecated in favour of the correctly spelt one below. -->
            <updateDevToAvoidConflictsMessage>Update develop to production version to avoid merge conflicts</updateDevToAvoidConflictsMessage>
            <updateDevBackPreMergeStateMessage>Update develop version back to pre-merge state</updateDevBackPreMergeStateMessage>
            
            <updateReleaseToAvoidConflictsMessage>Update release to hotfix version to avoid merge conflicts</updateReleaseToAvoidConflictsMessage>
            <updateReleaseBackPreMergeStateMessage>Update release version back to pre-merge state</updateReleaseBackPreMergeStateMessage>
        </commitMessages>
    </configuration>

Maven properties can be used in commit messages. For example `<featureStartMessage>updating ${project.artifactId} project for feature branch</featureStartMessage>` will produce message where 
`${project.artifactId}` will be substituted for projects `<artifactId>`.

Note that although `${project.version}` can be used, any changes to version introduced by this goal won't be reflected in a commit message for this goal (see Custom properties).

Commit messages can be prefixed by using `commitMessagePrefix` parameter.

### Custom properties in commit messages

`@{version}` will be replaced with the updated version.

`@{featureName}` will be replaced in `feature-` goals with the name of the current feature.

## Maven arguments

The `argLine` parameter can be used to pass command line arguments to the underlying Maven commands. For example, `-DcreateChecksum` in `mvn gitflow:release-start -DargLine=-DcreateChecksum` 
will be passed to all underlying Maven commands.

## Maven CI friendly versions

Maven property can be updated with the new version by setting the `versionProperty` parameter with the property you want to update.
For example, `-DversionProperty=revision` will update the `<revision>` property defined in the project pom.xml.

The `skipUpdateVersion` parameter can be used to skip updating `<version>` in the pom.xml. The default value is `false` (i.e. the version will be updated).

To support [CI friendly versioning](https://maven.apache.org/maven-ci-friendly.html) in projects which use `<version>${revision}</version>` (e.g. [spring-boot](https://github.com/spring-projects/spring-boot/blob/master/pom.xml))
set `versionProperty` to `revision` and `skipUpdateVersion` to `true`.

## Additional goal parameters

The `gitflow:release-finish`, `gitflow:release` and `gitflow:hotfix-finish` goals have `skipTag` parameter. This parameter controls whether the release/hotfix will be tagged in Git.
The default value is `false` (i.e. the release/hotfix will be tagged).

The `gitflow:feature-start` goal has `skipFeatureVersion` parameter which controls whether the feature name will be appended to the project version or not.
The default value is `false` (e.g. if the project version is `1.0.0-SNAPSHOT` and feature name is `feature_name` then after the execution it will be `1.0.0-feature_name-SNAPSHOT`).

The `gitflow:feature-start` goal has `featureNamePattern` parameter which allows to enforce naming of the feature branches with a regular expression. Doesn't have effect if it isn't set or left blank.
By default it isn't set.

All `-finish` goals have `keepBranch` parameter which controls whether created support branch will be kept in Git after the goal finishes.
The default value is `false` (i.e. the supporting branch will be deleted). If the `pushRemote` parameter is set to `true` and `keepBranch` is `false` remote branch will be deleted as well.

All `-finish` goals and `gitflow:release` have `skipTestProject` parameter which controls whether Maven `test` goal will be called before merging branches.
The default value is `false` (i.e. the project will be tested before merging branches).

All `release` goals have `allowSnapshots` parameter which controls whether SNAPSHOT dependencies are allowed. The default value is `false` (i.e. build fails if there SNAPSHOT dependency in project).

The `gitflow:release-finish` and `gitflow:release` goals have `digitsOnlyDevVersion` parameter which will remove qualifiers from the next development version if set to `true`.
For example, if the release version is `1.0.0-Final` then development version will be `1.0.1-SNAPSHOT`.
The default value is `false` (i.e. qualifiers will be preserved in next development version).

The `gitflow:release-finish` and `gitflow:release` goals have `versionDigitToIncrement` parameter which controls which digit to increment in the next development version. Starts from zero.
For example, if the release version is `1.2.3.4` and `versionDigitToIncrement` is set to `1` then the next development version will be `1.3.0.0-SNAPSHOT`.
If not set or set to not valid value defaults to increment last digit in the version.

The `gitflow:release-start` and `gitflow:release-finish` have `commitDevelopmentVersionAtStart` parameter which controls whether the next development version is set and committed at start or after finish.
By default the value is `false` which means that the next development version is set on the development branch after the release branch has been merged onto the development branch when finishing the release.
This has the benefit of being able to easily cancel the release process simply by deleting the release branch.
If the value is `true` then versioning happens on `gitflow:release-start`. First the project version is set to the release version on the development branch and the release branch is created.
Then the development branch is set to the next development version.
This allows the development branch to continue immediately with a new version and helps avoid any future merge conflicts related to project versioning.
Has effect only when there are separate development and production branches.

The `gitflow:release-start` goal has `sameBranchName` parameter which can be used to use the same name for the release branch. The default value is `false`.
By itself the default `releaseBranchPrefix` is not a valid branch name. You must change it when setting `sameBranchName` to `true`.
Will have no effect if the `branchName` parameter is set.

The `gitflow:release-start` goal has `branchName` parameter which controls how the release branch will be named.

The `gitflow:release-start` goal has `fromCommit` parameter which allows to start the release from the specific commit (SHA).

The `gitflow:release-start` and `gitflow:release-finish` goals have `useSnapshotInRelease` parameter which allows to start the release with SNAPSHOT version and finish it without this value in project version. By default the value is `false`.
For example, if the release version  is `1.0.2` and `useSnapshotInRelease` is set to `true` and using `gitflow:release-start` goal then the release version will be `1.0.2-SNAPSHOT` and when finishing the release with `gitflow:release-finish` goal, the release version will be `1.0.2`

The `gitflow:hotfix-start` and `gitflow:hotfix-finish` goals have `useSnapshotInHotfix` parameter which allows to start the hotfix with SNAPSHOT version and finish it without this value in the version. By default the value is `false`.
For example, if the hotfix version  is `1.0.2.1` and `useSnapshotInHotfix` is set to `true` and using `gitflow:hotfix-start` goal then the hotfix version will be `1.0.2.1-SNAPSHOT` and when finishing the release with `gitflow:hotfix-finish` goal, the release version will be `1.0.2.1`

The `gitflow:hotfix-finish` goal also supports the parameter `skipMergeDevBranch` which prevents merging the hotfix branch into the development branch. 

The `gitflow:hotfix-finish` goal also supports the parameter `skipMergeProdBranch` which prevents merging the hotfix branch into the production branch and deletes the hotfix branch leaving only the tagged commit. Useful, along with `skipMergeDevBranch`, to allow hotfixes to very old code that are not applicable to current development.

Version update of all modules ignoring groupId and artifactId can be forced by setting `versionsForceUpdate` parameter to `true`. The default value is `false`.

### Remote interaction

At the start of the each goal remote branch(es) will be fetched and compared with the local branch(es). If the local branch doesn't exist it will be checked out from the remote.
Both of these options can be turned off by setting `fetchRemote` parameter to `false`.

At the end of the `-finish` goals development or production and development branches will be pushed to remote. This can be turned off by setting `pushRemote` parameter to `false`.

At the end of the `-start` goals newly created branch (release / feature / hotfix) can be pushed to the remote. This can be achieved by setting `pushRemote` parameter to `true`.

The default remote name is `origin`. It can be customized with `<gitFlowConfig><origin>custom_origin</origin></gitFlowConfig>` configuration in pom.xml.

### Rebase, Merge, Fast Forward, Squash

Release branch can be rebased instead of merged by setting `releaseRebase` parameter to `true`. The default value is `false` (i.e. merge will be performed).

Release branch can be merged without `--no-ff` option by setting `releaseMergeNoFF` parameter to `false`. The default value is `true` (i.e. `merge --no-ff` will be performed).
The `releaseMergeNoFF` parameter has no effect when `releaseRebase` parameter is set to `true`.

Release branch can be merged with `--ff-only` option by setting `releaseMergeFFOnly` parameter to `true`. The default value is `false` (i.e. The `--ff-only` option won't be used).

Feature branch can be squashed before merging by setting `featureSquash` parameter to `true`. The default value is `false` (i.e. merge w/o squash will be performed).

### Running custom Maven goals

The `preFeatureFinishGoals` parameter can be used in `gitflow:feature-finish` goal to run defined Maven goals before the finishing and merging a feature.
E.g. `mvn gitflow:feature-finish -DpreFeatureFinishGoals=test` will run `mvn test` goal in the feature branch before merging into the development branch.

The `postFeatureFinishGoals` parameter can be used in `gitflow:feature-finish` goal to run defined Maven goals after merging a feature.
E.g. `mvn gitflow:feature-finish -postFeatureFinishGoals=test` will run `mvn test` goal in the development branch after merging a feature.

The `preReleaseGoals` parameter can be used in `gitflow:release-finish` and `gitflow:release` goals to run defined Maven goals before the release.
E.g. `mvn gitflow:release-finish -DpreReleaseGoals=test` will run `mvn test` goal in the release branch before merging into the production branch.

The `postReleaseGoals` parameter can be used in `gitflow:release-finish` and `gitflow:release` goals to run defined Maven goals after the release.
E.g. `mvn gitflow:release-finish -DpostReleaseGoals=deploy` will run `mvn deploy` goal in the production branch after the release.

The `gitflow:hotfix-finish` goal has `preHotfixGoals` and `postHotfixGoals` parameters which can be used to run defined Maven goals before and after the hotfix respectively.

# Non-interactive Mode

Maven can be run in non-interactive (batch) mode. By using non-interactive mode goals can be run in continuous integration environment.
To put Maven in the batch mode use `-B` or `--batch-mode` option.

## Non-interactive Release

Releases could be performed without prompting for the release version during `gitflow:release-start` or `gitflow:release` goals by telling Maven to run in non-interactive (batch) mode.
The `releaseVersion` parameter can be used to set the release version in non-interactive mode. If `releaseVersion` parameter is not set then the default release version will be used.

    mvn -B gitflow:release-start gitflow:release-finish
    
To release w/o creating separate release branch use `gitflow:release` goal.

    mvn -B gitflow:release

This gives the ability to perform releases in non-interactive mode (e.g. in CI server).

The `gitflow:release-finish` and `gitflow:release` goals have `developmentVersion` parameter which can be used to set the next development version in non-interactive mode.

## Non-interactive Feature

The `gitflow:feature-start` and `gitflow:feature-finish` goals have `featureName` parameter which can be used to set a name of the feature in non-interactive mode.

## Non-interactive Hotfix

The `gitflow:hotfix-start` goal has `fromBranch` parameter which can be used to set starting branch of the hotfix. It can be set to production branch or one of the support branches.
If it is left blank then hotfix will be started from the production branch.

The `gitflow:hotfix-start` and `gitflow:hotfix-finish` goals have `hotfixVersion` parameter which can be used to set version of the hotfix.
If it is left blank in `gitflow:hotfix-start` goal then the default version will be used.

## Non-interactive Support

The `gitflow:support-start` goal can be run in non-interactive mode. Use `tagName` parameter to set tag from which supporting branch will be started.
If `tagName` is not set but the goal is running in non-interactive mode then the last tag will be used.
