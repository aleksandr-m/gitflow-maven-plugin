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

import java.util.List;

import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.cli.CommandLineException;

/**
 * Git flow prompter interface.
 *
 */
public interface GitFlowPrompter {
    /**
     * Prompts with predefined choices.
     *
     * @param choices
     *            Predefined list of choices.
     * @param defaultChoice
     *            The default choice to use.
     * @param preMessage
     *            Text to display before prompt.
     * @param postMessage
     *            Text to display after prompt.
     * @return Response obtained from prompting.
     * @throws MojoFailureException
     *             If error happens during prompting.
     */
    String prompt(String[] choices, String defaultChoice, String preMessage, String postMessage) throws MojoFailureException;

    /**
     * Prompts and validates the response.
     *
     * @param message
     *            Text to display on prompt.
     * @param validation
     *            Validation function to validate prompt response.
     * @return Response obtained from prompting.
     * @throws MojoFailureException
     *             If error happens during prompting.
     * @throws CommandLineException
     *             If error happens during validation.
     */
    String prompt(String message, PromptValidation<String> validation) throws MojoFailureException, CommandLineException;

    /**
     * Prompts with predefined choices.
     *
     * @param message
     *            Text to display on prompt.
     * @param choices
     *            Predefined list of choices.
     * @return Response obtained from prompting.
     * @throws MojoFailureException
     *             If error happens during prompting.
     */
    String prompt(String message, List<String> choices) throws MojoFailureException;
}
