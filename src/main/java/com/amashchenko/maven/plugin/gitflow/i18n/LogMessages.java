package com.amashchenko.maven.plugin.gitflow.i18n;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;


@BaseName("LogMessages")
@LocaleData({ @Locale("en") })
public enum LogMessages {
	checking_for_uncommited_files,
	checking_out_branch,
	creating_new_branch_and_checking_it_out,
	commiting_changes,
	merging_branch,
	creating_tag,
	deleting_branch,
	updating_poms_version_to,
	cleaning_and_testing,
	cleaning_and_installing

}

/* EOF */
