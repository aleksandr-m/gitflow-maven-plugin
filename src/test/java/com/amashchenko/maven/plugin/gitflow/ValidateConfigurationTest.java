/*
 * Copyright 2014-2020 Aleksandr Mashchenko.
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

import java.util.Arrays;
import java.util.Collection;

import org.apache.maven.plugin.MojoFailureException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ValidateConfigurationTest {
    private final String argLine;
    private final boolean expected;

    public ValidateConfigurationTest(String argLine, boolean expected) {
        this.argLine = argLine;
        this.expected = expected;
    }

    @Parameters(name = "{0}->{1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { "-X -e", true },
                        { "-DsomeArg1=true", true }, { null, true },
                        { "", true }, { "-DsomeArg & clean", false },
                        { "-DsomeArg  && clean", false },
                        { "-DsomeArg  | clean", false },
                        { "-DsomeArg  || clean", false },
                        { "-DsomeArg  ; clean", false } });
    }

    @Test
    public void testValidateConfiguration() throws Exception {
        GitFlowReleaseStartMojo mojo = new GitFlowReleaseStartMojo();
        mojo.setArgLine(argLine);

        try {
            mojo.validateConfiguration();
        } catch (MojoFailureException e) {
            if (expected) {
                Assert.fail();
            }
        }
    }
}
