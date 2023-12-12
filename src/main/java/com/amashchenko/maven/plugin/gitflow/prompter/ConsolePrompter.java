/*
 * Copyright 2014-2023 Aleksandr Mashchenko.
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
package com.amashchenko.maven.plugin.gitflow.prompter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

/**
 * Default prompter which uses console for input.
 *
 */
@Named
@Singleton
public class ConsolePrompter implements GitFlowPrompter {
    private static final String LS = System.getProperty("line.separator");

    private final Prompter prompter;

    @Inject
    public ConsolePrompter(Prompter prompter) {
        this.prompter = prompter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String prompt(String[] choices, final String defaultChoice, String preMessage, String postMessage) throws MojoFailureException {
        List<String> numberedList = new ArrayList<>();
        String defChoice = null;
        StringBuilder str = new StringBuilder(preMessage).append(LS);
        for (int i = 0; i < choices.length; i++) {
            str.append(i + 1).append(". ").append(choices[i]).append(LS);
            numberedList.add(String.valueOf(i + 1));
            if (choices[i].equals(defaultChoice)) {
                defChoice = String.valueOf(i + 1);
            }
        }
        str.append(postMessage);

        String response = null;
        try {
            while (StringUtils.isBlank(response)) {
                if (defaultChoice == null || defChoice == null) {
                    response = prompter.prompt(str.toString(), numberedList);
                } else {
                    response = prompter.prompt(str.toString(), numberedList, defChoice);
                }
            }
        } catch (PrompterException e) {
            throw new MojoFailureException("prompter error", e);
        }

        String result = null;
        if (response != null) {
            int num = Integer.parseInt(response);
            result = choices[num - 1];
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String prompt(String message, PromptValidation<String> validation) throws MojoFailureException, CommandLineException {
        String response = null;
        try {
            while (response == null) {
                response = prompter.prompt(message);

                if (!validation.valid(response)) {
                    response = null;
                }
            }
        } catch (PrompterException e) {
            throw new MojoFailureException("prompter error", e);
        }
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String prompt(String message, List<String> choices) throws MojoFailureException {
        String response = null;
        try {
            response = prompter.prompt(message, choices);
        } catch (PrompterException e) {
            throw new MojoFailureException("prompter error", e);
        }
        return response;
    }
}
