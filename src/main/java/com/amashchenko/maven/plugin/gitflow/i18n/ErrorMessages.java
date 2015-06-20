package com.amashchenko.maven.plugin.gitflow.i18n;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;


@BaseName("com/amashchenko/maven/plugin/gitflow/i18n/ErrorMessages")
@LocaleData({ @Locale("en") })
public enum ErrorMessages {
	unexpected_error,
	not_in_a_maven_folder,
	uncommitted_files_detected,
	next_snapshot_version_empty,

	no_feature_branch_found,
	feature_branch_name_empty,
	feature_branch_name_duplicate,

	no_hotfix_branch_found,
	hotfix_branch_name_empty,
	hotfix_branch_name_duplicate,

	no_release_branch_found,
	release_branch_name_empty,
	release_branch_not_unique,
	release_branch_already_exists
}

/* EOF */
