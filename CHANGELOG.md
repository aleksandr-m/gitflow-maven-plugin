# Changelog

## v1.6.0

* Fixed [#47](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/47) - Wrong development version after `release` goal
* Added option to push to the remote in start goals - [#32](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/32)
* Added ability to delete remote branch in finish goals - [#44](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/44)
* Added ability to remove qualifiers from next development version during the release - [#42](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/42)
* Added ability to set next development version from property in non interactive mode - [#42](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/42)
* Added ability to increment other digits in next development version - [#42](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/42)
* Added upstream (tracking) reference for the branch when pushing to remote - [#52](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/52)
* Various improvements

## v1.5.0

* Added ability to fetch and checkout from the remote if local branch doesn't exist - closes [#40](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/40)
* Added support for the `support` branches - see [#23](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/23)
* Added ability to enforce naming of the feature branches - closes [#38](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/38)
* Added validation for version received via prompt
* Improved getting project versions
* Other small fixes and code improvements

## v1.4.1

* Fixed [#39](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/39) - Snapshot dependency check should not check internal dependencies of a multi module project
* Improved error reporting of snapshot dependency check

## v1.4.0

* Added support for `--ff-only` option for release merge ([#30](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/30))
* Added validation for branch names
* Fixed [#28](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/28) - Release-start and release-finish doesn't check for SNAPSHOT dependencies on submodules

## v1.3.1

* Added ability to set version in non interactive mode (see [#25](https://github.com/aleksandr-m/gitflow-maven-plugin/pull/25))
* Don't merge/tag/fetch/push to the same branch when using single branch model like GitHub Flow (see [#22](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/22))

## v1.3.0

* Remote interaction. Added `fetchRemote` and `pushRemote` parameters.
* Updated default executable names (see [#20](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/20))

## v1.2.3

* Added `featureSquash` parameter which allows to squash feature commits (see [#17](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/17))
* Added ability to rebase or merge w/o no-ff option in release goals (see [#14](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/14))
* Fixed [#18](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/18) - Executing commands with verbose parameter

## v1.2.2

* Added `allowSnapshots` parameter to allow SNAPSHOT dependencies on releasing (see [#10](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/10))
* Dependencies versions updated

## v1.2.1

* Added `commitMessages` configuration which allows to customize commit messages (see [#8](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/8))
* Added maven site

## v1.2.0

* Added `release` goal for creating releases w/o separate release branch
* Added `sameBranchName` parameter which allows to use the same name for the release branch (see [#5](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/5))
* Fixed [#7](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/7) - Correctly set empty value to Git configuration
* Improved commands execution

## v1.1.0

* Added support for Eclipse plugins build with Tycho
* Skipped checking out unnecessary branch when `skipTestProject` is set to true
* Improved error log

## v1.0.8

* Fixed issue [#3](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/3) - on *nix systems return values from `git for-each-ref` are wrapped in quotes
* Added null check on getting current project version
* Small code improvements

## v1.0.7

* Reduced spam to console
* Added `verbose` parameter
* Small code improvements

## v1.0.6

* Setting git flow configuration into project git configuration
* Nicer prompt

## v1.0.5

* Added `installProject` parameter
* Added `skipTestProject` parameter
* Added `skipFeatureVersion` parameter
* Added auto generated `help` goal

## v1.0.1-alpha4

* Added support for releasing in non-interactive (batch) mode
* Small code improvements

## v1.0.1-alpha3

* Added `keepBranch` parameter
* Fixed issue [#1](https://github.com/aleksandr-m/gitflow-maven-plugin/issues/1) - using commands to check for uncommitted changes

## v1.0.1-alpha2

* Added `skipTag` parameter

## v1.0.1-alpha1

* Initial version
