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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FeatureVersionTest {

    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                        { "0.9-SNAPSHOT", "feature", "0.9-feature-SNAPSHOT" },
                        { "0.9-RC3-SNAPSHOT", "feature",
                                        "0.9-RC3-feature-SNAPSHOT" },
                        { "0.9", "feature", "0.9-feature" },
                        { "0.9-RC3", "feature", "0.9-RC3-feature" },
                        { "0.9-RC3", null, "0.9-RC3" } });
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testFeatureVersion(final String version, final String featureName,
                                   final String expectedVersion) throws Exception {
        assertEquals(expectedVersion,
                new GitFlowVersionInfo(version, null).featureVersion(featureName));
    }
}
