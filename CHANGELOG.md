# Changelog

## v1.14.0 (2019-12-06)

* Fixed snapshot dependencies check and improved version resolution - [#204](https://github.com/aleksandr-m/gitflow-maven-plugin/pull/204)
* Added fetching of the remote release branch in `release-finish` goal - [#196](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/196)
* Added ability to add prefix to commit messages - [#188](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/188)
* Added `branchName` parameter in the `release-start` goal - [#70](https://github.com/aleksandr-m/gitflow-maven-plugin/pull/70)

## v1.13.0 (2019-09-09)

* Added ability to change git merge messages for feature and hotfix goals - [#185](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/185)
* Added support for running custom Maven goals in `feature-finish` - [#177](https://github.com/aleksandr-m/gitflow-maven-plugin/pull/177)
* Added ability to change release finish development merge commit message - [#175](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/175)
* Added ability to skip updating `version`
* Added support for updating property in pom (e.g. `revision`) - [#151](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/151)

## v1.12.0 (2019-04-09)

* Fixed wrong versions in production branch when using `useSnapshotInHotfix` parameter
* Fixed wrong versions in production branch when using `useSnapshotInRelease` parameter - [#158](https://github.com/aleksandr-m/gitflow-maven-plugin/pull/158)
* Fixed merge conflict in pom during `hotfix-finish` into existing release branch - [#160](https://github.com/aleksandr-m/gitflow-maven-plugin/pull/160)
* Added support for using properties in `releaseFinishMergeMessage` - [#163](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/163)
* Added support to push the release branch on `hotfix-finish` goal - [#166](https://github.com/aleksandr-m/gitflow-maven-plugin/pull/166)
* Improved check of snapshot dependencies in the current pom - [#169](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/169)
* Added support to skip merging into the development branch in `hotfit-finish` goal - [#149]https://github.com/aleksandr-m/gitflow-maven-plugin/pull/149)
* Added support to skip merging into the production branch in `hotfix-finish` goal - [#164](https://github.com/aleksandr-m/gitflow-maven-plugin/pull/164)
* Improved usage and content of commit messages

## v1.11.0 (2018-12-11)

* Fixed `versionsForceUpdate` parameter doesn't work on windows - [#134](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/134)
* Fixed merge pom's conflict on release finish using `useSnapshotInRelease` - [#123](https://github.com/aleksandr-m/gitflow-maven-plugin/pull/123)
* Fixed merge conflict during release finish when using `commitDevelopmentVersionAtStart` and `useSnapshotInRelease` - [#122](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/122)
* Added ability to change release finish merge commit message - [#135](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/135)
* Capitalized default messages - [#131](https://github.com/aleksandr-m/gitflow-maven-plugin/pull/131)
* Added configurable commit message for hotfix version update - [#128](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/128)
* Added configurable commit message for release start with `commitDevelopmentVersionAtStart` - [#120](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/120)
* Added ability to use `useSnapshotInHotfix` and `useSnapshotInRelease` from the command line with a warning message in logs

## v1.10.0 (2018-08-02)

* Improved branch removing. Deleting remote first then local branch to avoid failing a build - [#114](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/114)
* Added ability to use different versions-maven-plugin version - [#116](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/116)
* Added ability to replace `@{version}` with updated version in tag messages - [#113](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/113)
* Added ability to force update versions of modules with different groupId / artifactId - [#48](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/48)
* Added ability to use SNAPSHOT versions in release - [#98](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/98)
* Added ability to use SNAPSHOT versions in hotfix - [#81](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/81)

## v1.9.0 (2018-01-27)

* Fixed not failing the whole build on goals errors
* Added ability to make a GPG-signed tags and commits - [#73](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/73)
* Added ability to execute `support-start` goal in non-interactive mode - [#75](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/75)
* Added ability to execute feature and hotfix goals in non-interactive mode - [#71](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/71)
* Added ability to start hotfix from support branch in non-interactive mode - [#88](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/88)
* Added ability to fetch and checkout from the remote if local branch doesn't exist in `hotfix-finish` goal - [#87](https://github.com/aleksandr-m/gitflow-maven-plugin/pull/87)
* Improved finding of hotfix support branches in `hotfix-finish` goal - [#68](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/68)

## v1.8.0 (2017-11-04)

* Added ability to run custom Maven goals before and after release and hotfix - [#13](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/13), [#29](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/29), [#54](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/54)
* Added ability to allow to pass arguments to the underlying Maven commands - [#53](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/53)

## v1.7.0 (2017-08-30)

* Fixed [#19](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/19) - Avoid merge conflict in `hotfix-finish`
* Fixed [#60](https://github.com/aleksandr-m/gitflow-maven-plugin/pull/60) - The `releaseMergeFFOnly` is used when merging release to develop
* Added option to update versions on the develop branch when starting a release - [#61](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/61)
* Added option to set the starting point on `release-start` goal - [#56](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/56)
* Added ability to replace `@{featureName}` in commit messages - [#62](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/62)
* Added ability to replace `@{version}` with updated version in commit messages - [#41](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/41)

## v1.6.0 (2017-07-03)

* Fixed [#47](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/47) - Wrong development version after `release` goal
* Added option to push to the remote in start goals - [#32](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/32)
* Added ability to delete remote branch in finish goals - [#44](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/44)
* Added ability to remove qualifiers from next development version during the release - [#42](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/42)
* Added ability to set next development version from property in non interactive mode - [#42](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/42)
* Added ability to increment other digits in next development version - [#42](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/42)
* Added upstream (tracking) reference for the branch when pushing to remote - [#52](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/52)
* Various improvements

## v1.5.0 (2017-04-12)

* Added ability to fetch and checkout from the remote if local branch doesn't exist - closes [#40](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/40)
* Added support for the `support` branches - see [#23](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/23)
* Added ability to enforce naming of the feature branches - closes [#38](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/38)
* Added validation for version received via prompt
* Improved getting project versions
* Other small fixes and code improvements

## v1.4.1 (2017-03-05)

* Fixed [#39](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/39) - Snapshot dependency check should not check internal dependencies of a multi module project
* Improved error reporting of snapshot dependency check

## v1.4.0 (2017-02-12)

* Added support for `--ff-only` option for release merge ([#30](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/30))
* Added validation for branch names
* Fixed [#28](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/28) - Release-start and release-finish doesn't check for SNAPSHOT dependencies on submodules

## v1.3.1 (2016-11-04)

* Added ability to set version in non interactive mode (see [#25](https://github.com/aleksandr-m/gitflow-maven-plugin/pull/25))
* Don't merge/tag/fetch/push to the same branch when using single branch model like GitHub Flow (see [#22](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/22))

## v1.3.0 (2016-09-30)

* Remote interaction. Added `fetchRemote` and `pushRemote` parameters.
* Updated default executable names (see [#20](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/20))

## v1.2.3 (2016-06-19)

* Added `featureSquash` parameter which allows to squash feature commits (see [#17](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/17))
* Added ability to rebase or merge w/o no-ff option in release goals (see [#14](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/14))
* Fixed [#18](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/18) - Executing commands with verbose parameter

## v1.2.2 (2016-03-24)

* Added `allowSnapshots` parameter to allow SNAPSHOT dependencies on releasing (see [#10](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/10))
* Dependencies versions updated

## v1.2.1 (2015-12-22)

* Added `commitMessages` configuration which allows to customize commit messages (see [#8](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/8))
* Added maven site

## v1.2.0 (2015-11-01)

* Added `release` goal for creating releases w/o separate release branch
* Added `sameBranchName` parameter which allows to use the same name for the release branch (see [#5](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/5))
* Fixed [#7](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/7) - Correctly set empty value to Git configuration
* Improved commands execution

## v1.1.0 (2015-10-07)

* Added support for Eclipse plugins build with Tycho
* Skipped checking out unnecessary branch when `skipTestProject` is set to true
* Improved error log

## v1.0.8 (2015-02-10)

* Fixed issue [#3](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/3) - on *nix systems return values from `git for-each-ref` are wrapped in quotes
* Added null check on getting current project version
* Small code improvements

## v1.0.7 (2015-01-18)

* Reduced spam to console
* Added `verbose` parameter
* Small code improvements

## v1.0.6 (2014-11-06)

* Setting git flow configuration into project git configuration
* Nicer prompt

## v1.0.5 (2014-08-28)

* Added `installProject` parameter
* Added `skipTestProject` parameter
* Added `skipFeatureVersion` parameter
* Added auto generated `help` goal

## v1.0.1-alpha4 (2014-08-09)

* Added support for releasing in non-interactive (batch) mode
* Small code improvements

## v1.0.1-alpha3 (2014-07-11)

* Added `keepBranch` parameter
* Fixed issue [#1](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/1) - using commands to check for uncommitted changes

## v1.0.1-alpha2 (2014-05-12)

* Added `skipTag` parameter

## v1.0.1-alpha1 (2014-04-29)

* Initial version
