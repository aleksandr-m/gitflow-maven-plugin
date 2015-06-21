# Changelog

## v1.0.10
* Take full ownership of this fork
* Change the groupId to `<groupId>com.zartc.maven.plugin</groupId>`
* Change the main packages name to `com.zartc.maven.plugin.gitflow`
* Reformat the source code to 160 columns.

## v1.0.9

* Forked from `https://github.com/aleksandr-m/gitflow-maven-plugin`
* Fully externalize all strings used in the user interface or the commit message.
* Small code improvements

## v1.0.8

* Fixed issue #3 - on *nix systems return values from `git for-each-ref` are wrapped in quotes
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
* Fixed issue #1 - using commands to check for uncommitted changes

## v1.0.1-alpha2

* Added `skipTag` parameter

## v1.0.1-alpha1

* Initial version
