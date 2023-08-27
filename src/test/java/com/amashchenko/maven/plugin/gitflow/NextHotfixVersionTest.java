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
package com.amashchenko.maven.plugin.gitflow;

import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class NextHotfixVersionTest {

    public static Collection<Object[]> data() {
        return Arrays
                .asList(new Object[][] {
                                { "some-SNAPSHOT", null, "some" },
                                { "some-SNAPSHOT", 0, "some" },
                                { "0.58", null, "0.59" },
                                { "0.58", -1, "0.59" },
                                { "0.58", 0, "1.0" },
                                { "0.58", 1, "0.59" },
                                { "0.58", 100, "0.59" },
                                { "0.9", 1, "0.10" },
                                { "0.09", 1, "0.10" },
                                { "0.0009", 0, "1.0" },
                                { "0.0009", 1, "0.0010" },
                                { "0.09-RC2", null, "0.09-RC3" },
                                { "0.09-RC3", 0, "1.0-RC3" },
                                { "0.09-RC3-SNAPSHOT", 0, "1.0-RC3" },
                                { "0.9-SNAPSHOT", null, "0.10" },
                                { "0.09-RC3-feature-SNAPSHOT", 0, "1.0-RC3-feature" },
                                { "0.09-RC3-feature", null, "0.09-RC4-feature" },
                                { "2.3.4", 0, "3.0.0" },
                                { "2.3.4", 1, "2.4.0" } });
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testNextSnapshotVersion(final String version, final Integer index, final String expectedVersion) throws Exception {
        Assertions.assertEquals(expectedVersion, new GitFlowVersionInfo(version, null).hotfixVersion(false, index));
    }
}
