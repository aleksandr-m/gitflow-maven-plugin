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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class NextSnapshotVersionTest {

    public static Collection<Arguments> data() {
        return Arrays.asList(
                arguments("some-SNAPSHOT", null, "some-SNAPSHOT"),
                arguments("some-SNAPSHOT", 0, "some-SNAPSHOT"),
                arguments("0.58", null, "0.59-SNAPSHOT"),
                arguments("0.58", -1, "0.59-SNAPSHOT"),
                arguments("0.58", 0, "1.0-SNAPSHOT"),
                arguments("0.58", 1, "0.59-SNAPSHOT"),
                arguments("0.58", 100, "0.59-SNAPSHOT"),
                arguments("0.9", 1, "0.10-SNAPSHOT"),
                arguments("0.09", 1, "0.10-SNAPSHOT"),
                arguments("0.0009", 0, "1.0-SNAPSHOT"),
                arguments("0.0009", 1, "0.0010-SNAPSHOT"),
                arguments("0.09-RC2", null, "0.09-RC3-SNAPSHOT"),
                arguments("0.09-RC3", 0, "1.0-RC3-SNAPSHOT"),
                arguments("0.09-RC3-SNAPSHOT", 0, "1.0-RC3-SNAPSHOT"),
                arguments("0.9-SNAPSHOT", null, "0.10-SNAPSHOT"),
                arguments("0.09-RC3-feature-SNAPSHOT", 0, "1.0-RC3-feature-SNAPSHOT"),
                arguments("0.09-RC3-feature", null, "0.09-RC4-feature-SNAPSHOT"),
                arguments("2.3.4", 0, "3.0.0-SNAPSHOT"),
                arguments("2.3.4", 1, "2.4.0-SNAPSHOT"));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testNextSnapshotVersion(String version, Integer index, String expectedVersion) throws Exception {
        assertEquals(expectedVersion, new GitFlowVersionInfo(version).nextSnapshotVersion(index));
    }

}
