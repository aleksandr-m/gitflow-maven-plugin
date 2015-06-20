package com.amashchenko.maven.plugin.gitflow.i18n;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;


@BaseName("com/amashchenko/maven/plugin/gitflow/i18n/PromptMessages")
@LocaleData({ @Locale("en") })
public enum PromptMessages {
	feature_branch_list_header,
	feature_branch_number_to_finish_prompt,
	feature_branch_name_to_create_prompt,

	hotfix_branch_list_header,
	hotfix_branch_number_to_finish_prompt,
	hotfix_branch_name_to_create_prompt,

	release_branch_name_to_create_prompt
}

/* EOF */
