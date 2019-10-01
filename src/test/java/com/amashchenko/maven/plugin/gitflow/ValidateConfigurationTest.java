/*
 * Copyright 2014-2019 Aleksandr Mashchenko.
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
package com.amashchenko.maven.plugin.gitflow;

import org.apache.maven.plugin.MojoFailureException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.params.provider.Arguments.arguments;

public class ValidateConfigurationTest {

    public static Collection<Arguments> data() {
        return Arrays.asList(
                arguments("-X -e", true),
                arguments("-DsomeArg1=true", true),
                arguments(null, true),
                arguments("", true),
                arguments("-DsomeArg & clean", false),
                arguments("-DsomeArg  && clean", false),
                arguments("-DsomeArg  | clean", false),
                arguments("-DsomeArg  || clean", false),
                arguments("-DsomeArg  ; clean", false));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testValidateConfiguration(String argLine, boolean expected) throws Exception {
        GitFlowReleaseStartMojo mojo = new GitFlowReleaseStartMojo();
        mojo.setArgLine(argLine);

        try {
            mojo.validateConfiguration();
        } catch (MojoFailureException e) {
            if (expected) {
                Assertions.fail();
            }
        }
    }
}
