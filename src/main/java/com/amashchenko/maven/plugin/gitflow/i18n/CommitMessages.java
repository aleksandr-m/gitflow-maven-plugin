package com.amashchenko.maven.plugin.gitflow.i18n;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;


@BaseName("com/amashchenko/maven/plugin/gitflow/i18n/CommitMessages")
@LocaleData({ @Locale("en") })
public enum CommitMessages {
	updating_pom_for_develop_branch,
	updating_pom_for_feature_branch,
	updating_pom_for_develop_version,
	updating_pom_for_hotfix_version,
	updating_pom_for_release_version,
	tagging_hotfix,
	tagging_release
}

/* EOF */
