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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class FeatureVersionTest {
    private final String version;
    private final String featureName;
    private final String expectedVersion;

    public FeatureVersionTest(final String version, final String featureName,
            final String expectedVersion) {
        this.version = version;
        this.featureName = featureName;
        this.expectedVersion = expectedVersion;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                        { "0.9-SNAPSHOT", "feature", "0.9-feature-SNAPSHOT" },
                        { "0.9-RC3-SNAPSHOT", "feature",
                                        "0.9-RC3-feature-SNAPSHOT" },
                        { "0.9", "feature", "0.9-feature" },
                        { "0.9-RC3", "feature", "0.9-RC3-feature" },
                        { "0.9-RC3", null, "0.9-RC3" } });
    }

    @Test
    public void testFeatureVersion() throws Exception {
        Assert.assertEquals(expectedVersion,
                new GitFlowVersionInfo(version).featureVersion(featureName));
    }
}
