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

import org.apache.maven.shared.release.policy.oddeven.OddEvenVersionPolicy;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GitFlowVersionInfoTest {
    @Test
    public void testCreating() throws Exception {
        final String version = "0.9";
        GitFlowVersionInfo info = new GitFlowVersionInfo(version, null);
        Assertions.assertNotNull(info);
        Assertions.assertEquals(version, info.toString());
    }

    @Test
    public void testVersionParseException() throws Exception {
        Assertions.assertThrows(VersionParseException.class, ()-> new GitFlowVersionInfo("", null));
    }

    @Test
    public void testIsValidVersion() throws Exception {
        Assertions.assertTrue(GitFlowVersionInfo.isValidVersion("0.9"));
        Assertions.assertTrue(GitFlowVersionInfo.isValidVersion("some-SNAPSHOT"));
        Assertions.assertTrue(GitFlowVersionInfo
                .isValidVersion("0.9-RC3-feature-SNAPSHOT"));

        Assertions.assertFalse(GitFlowVersionInfo.isValidVersion("some.0.9"));
        Assertions.assertFalse(GitFlowVersionInfo.isValidVersion(null));
        Assertions.assertFalse(GitFlowVersionInfo.isValidVersion(""));
        Assertions.assertFalse(GitFlowVersionInfo.isValidVersion(" "));
        Assertions.assertFalse(GitFlowVersionInfo.isValidVersion("-1"));
    }

    @Test
    public void testHotfixVersion() throws Exception {
        GitFlowVersionInfo info = new GitFlowVersionInfo("0.9", null);
        Assertions.assertNotNull(info);
        Assertions.assertEquals("0.10", info.hotfixVersion(false, null));
    }

    @Test
    public void testHotfixVersion2() throws Exception {
        GitFlowVersionInfo info = new GitFlowVersionInfo("0.9-SNAPSHOT", null);
        Assertions.assertNotNull(info);
        Assertions.assertEquals("0.10", info.hotfixVersion(false, null));
    }

    @Test
    public void testHotfixVersion3() throws Exception {
        GitFlowVersionInfo info = new GitFlowVersionInfo("0.9", null);
        Assertions.assertNotNull(info);
        Assertions.assertEquals("0.10", info.hotfixVersion(true, null));
    }

    @Test
    public void testHotfixVersion4() throws Exception {
        GitFlowVersionInfo info = new GitFlowVersionInfo("0.9-SNAPSHOT", null);
        Assertions.assertNotNull(info);
        Assertions.assertEquals("0.10-SNAPSHOT", info.hotfixVersion(true, null));
    }

    @Test
    public void testDigitsVersionInfo() throws Exception {
        GitFlowVersionInfo info = new GitFlowVersionInfo("0.9", null);
        Assertions.assertNotNull(info);
        info = info.digitsVersionInfo();
        Assertions.assertNotNull(info);
        Assertions.assertEquals(new GitFlowVersionInfo("0.9", null), info);
    }

    @Test
    public void testDigitsVersionInfo2() throws Exception {
        GitFlowVersionInfo info = new GitFlowVersionInfo(
                "0.9-RC3-feature-SNAPSHOT", null);
        Assertions.assertNotNull(info);
        info = info.digitsVersionInfo();
        Assertions.assertNotNull(info);
        Assertions.assertEquals(new GitFlowVersionInfo("0.9", null), info);
    }

    @Test
    public void testWithoutVersionPolicy() throws Exception {
        GitFlowVersionInfo info1 = new GitFlowVersionInfo("1.0.0-SNAPSHOT", null);
        Assertions.assertEquals("1.0.0", info1.getReleaseVersionString());
        Assertions.assertEquals("1.0.1-SNAPSHOT", info1.nextSnapshotVersion());

        GitFlowVersionInfo info2 = new GitFlowVersionInfo("1.0.1-SNAPSHOT", null);
        Assertions.assertEquals("1.0.1", info2.getReleaseVersionString());
        Assertions.assertEquals("1.0.2-SNAPSHOT", info2.nextSnapshotVersion());
    }

    @Test
    public void testWithVersionPolicy() throws Exception {
        GitFlowVersionInfo info1 = new GitFlowVersionInfo("1.0.0-SNAPSHOT", new OddEvenVersionPolicy());
        Assertions.assertEquals("1.0.0", info1.getReleaseVersionString());
        Assertions.assertEquals("1.0.1-SNAPSHOT", info1.nextSnapshotVersion());


        GitFlowVersionInfo info2 = new GitFlowVersionInfo("1.0.1-SNAPSHOT", new OddEvenVersionPolicy());
        Assertions.assertEquals("1.0.2", info2.getReleaseVersionString());
        Assertions.assertEquals("1.0.3-SNAPSHOT", info2.nextSnapshotVersion());
    }

}
