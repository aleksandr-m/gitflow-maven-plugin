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

import org.apache.maven.shared.release.versions.VersionParseException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GitFlowVersionInfoTest {

    @Test
    public void testCreating() throws Exception {
        final String version = "0.9";
        GitFlowVersionInfo info = new GitFlowVersionInfo(version);
        assertNotNull(info);
        assertEquals(version, info.toString());
    }

    @Test
    public void testVersionParseException() throws Exception {
        assertThrows(VersionParseException.class, () -> new GitFlowVersionInfo(""));
    }

    @Test
    public void testIsValidVersion() throws Exception {
        assertTrue(GitFlowVersionInfo.isValidVersion("0.9"));
        assertTrue(GitFlowVersionInfo.isValidVersion("some-SNAPSHOT"));
        assertTrue(GitFlowVersionInfo
                .isValidVersion("0.9-RC3-feature-SNAPSHOT"));

        assertFalse(GitFlowVersionInfo.isValidVersion("some.0.9"));
        assertFalse(GitFlowVersionInfo.isValidVersion(null));
        assertFalse(GitFlowVersionInfo.isValidVersion(""));
        assertFalse(GitFlowVersionInfo.isValidVersion(" "));
        assertFalse(GitFlowVersionInfo.isValidVersion("-1"));
    }

    @Test
    public void testHotfixVersion() throws Exception {
        GitFlowVersionInfo info = new GitFlowVersionInfo("0.9");
        assertNotNull(info);
        assertEquals("0.10", info.hotfixVersion(false));
    }

    @Test
    public void testHotfixVersion2() throws Exception {
        GitFlowVersionInfo info = new GitFlowVersionInfo("0.9-SNAPSHOT");
        assertNotNull(info);
        assertEquals("0.10", info.hotfixVersion(false));
    }

    @Test
    public void testHotfixVersion3() throws Exception {
        GitFlowVersionInfo info = new GitFlowVersionInfo("0.9");
        assertNotNull(info);
        assertEquals("0.10", info.hotfixVersion(true));
    }

    @Test
    public void testHotfixVersion4() throws Exception {
        GitFlowVersionInfo info = new GitFlowVersionInfo("0.9-SNAPSHOT");
        assertNotNull(info);
        assertEquals("0.10-SNAPSHOT", info.hotfixVersion(true));
    }

    @Test
    public void testDigitsVersionInfo() throws Exception {
        GitFlowVersionInfo info = new GitFlowVersionInfo("0.9");
        assertNotNull(info);
        info = info.digitsVersionInfo();
        assertNotNull(info);
        assertEquals(new GitFlowVersionInfo("0.9"), info);
    }

    @Test
    public void testDigitsVersionInfo2() throws Exception {
        GitFlowVersionInfo info = new GitFlowVersionInfo(
                "0.9-RC3-feature-SNAPSHOT");
        assertNotNull(info);
        info = info.digitsVersionInfo();
        assertNotNull(info);
        assertEquals(new GitFlowVersionInfo("0.9"), info);
    }
}
