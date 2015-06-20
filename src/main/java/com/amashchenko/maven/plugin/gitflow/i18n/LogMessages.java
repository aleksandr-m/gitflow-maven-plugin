package com.amashchenko.maven.plugin.gitflow.i18n;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;


@BaseName("com/amashchenko/maven/plugin/gitflow/i18n/LogMessages")
@LocaleData({ @Locale("en") })
public enum LogMessages {
	looking_for_uncommitted_files,
	checking_out_branch,
	checking_out_new_branch,
	committing_changes,
	merging_branch,
	creating_tag,
	deleting_branch,
	updating_poms_version_to,
	removing_snapshots,
	cleaning_and_testing,
	cleaning_and_installing
}

/* EOF */
