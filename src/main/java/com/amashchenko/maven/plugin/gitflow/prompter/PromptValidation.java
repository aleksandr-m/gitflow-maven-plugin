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

import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.cli.CommandLineException;

/**
 * Functional interface to validate prompt response.
 *
 * @param <T>
 *            Type of the response to validate.
 */
@FunctionalInterface
public interface PromptValidation<T> {
    /**
     * Validates prompt response.
     * 
     * @param t
     *            Response to validate.
     * @return <code>true</code> when response is valid, <code>false</code>
     *         otherwise.
     * @throws MojoFailureException
     *             If validation fails.
     * @throws CommandLineException
     *             If validation fails.
     */
    boolean valid(T t) throws MojoFailureException, CommandLineException;
}
