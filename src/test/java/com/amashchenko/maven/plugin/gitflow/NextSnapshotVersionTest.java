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
public class NextSnapshotVersionTest {
    private final String version;
    private final Integer index;
    private final String expectedVersion;

    public NextSnapshotVersionTest(final String version, final Integer index,
            final String expectedVersion) {
        this.version = version;
        this.index = index;
        this.expectedVersion = expectedVersion;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays
                .asList(new Object[][] {
                                { "some-SNAPSHOT", null, "some-SNAPSHOT" },
                                { "some-SNAPSHOT", 0, "some-SNAPSHOT" },
                                { "0.58", null, "0.59-SNAPSHOT" },
                                { "0.58", -1, "0.59-SNAPSHOT" },
                                { "0.58", 0, "1.0-SNAPSHOT" },
                                { "0.58", 1, "0.59-SNAPSHOT" },
                                { "0.58", 100, "0.59-SNAPSHOT" },
                                { "0.9", 1, "0.10-SNAPSHOT" },
                                { "0.09", 1, "0.10-SNAPSHOT" },
                                { "0.0009", 0, "1.0-SNAPSHOT" },
                                { "0.0009", 1, "0.0010-SNAPSHOT" },
                                { "0.09-RC2", null, "0.09-RC3-SNAPSHOT" },
                                { "0.09-RC3", 0, "1.0-RC3-SNAPSHOT" },
                                { "0.09-RC3-SNAPSHOT", 0, "1.0-RC3-SNAPSHOT" },
                                { "0.9-SNAPSHOT", null, "0.10-SNAPSHOT" },
                                { "0.09-RC3-feature-SNAPSHOT", 0,
                                                "1.0-RC3-feature-SNAPSHOT" },
                                { "0.09-RC3-feature", null,
                                                "0.09-RC4-feature-SNAPSHOT" },
                                { "2.3.4", 0, "3.0.0-SNAPSHOT" },
                                { "2.3.4", 1, "2.4.0-SNAPSHOT" } });
    }

    @Test
    public void testNextSnapshotVersion() throws Exception {
        Assert.assertEquals(expectedVersion,
                new GitFlowVersionInfo(version).nextSnapshotVersion(index));
    }
}
