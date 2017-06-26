/*
 * Copyright 2014-2017 Aleksandr Mashchenko.
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
import org.junit.Assert;
import org.junit.Test;

public class GitFlowVersionInfoTest {
    @Test
    public void testCreating() throws Exception {
        final String version = "0.9";
        GitFlowVersionInfo info = new GitFlowVersionInfo(version);
        Assert.assertNotNull(info);
        Assert.assertEquals(version, info.toString());
    }

    @Test(expected = VersionParseException.class)
    public void testVersionParseException() throws Exception {
        new GitFlowVersionInfo("");
    }

    @Test
    public void testIsValidVersion() throws Exception {
        Assert.assertTrue(GitFlowVersionInfo.isValidVersion("0.9"));
        Assert.assertTrue(GitFlowVersionInfo.isValidVersion("some-SNAPSHOT"));
        Assert.assertTrue(GitFlowVersionInfo
                .isValidVersion("0.9-RC3-feature-SNAPSHOT"));

        Assert.assertFalse(GitFlowVersionInfo.isValidVersion("some.0.9"));
        Assert.assertFalse(GitFlowVersionInfo.isValidVersion(null));
        Assert.assertFalse(GitFlowVersionInfo.isValidVersion(""));
        Assert.assertFalse(GitFlowVersionInfo.isValidVersion(" "));
        Assert.assertFalse(GitFlowVersionInfo.isValidVersion("-1"));
    }

    @Test
    public void testHotfixVersion() throws Exception {
        GitFlowVersionInfo info = new GitFlowVersionInfo("0.9");
        Assert.assertNotNull(info);
        Assert.assertEquals("0.10", info.hotfixVersion(false));
    }

    @Test
    public void testHotfixVersion2() throws Exception {
        GitFlowVersionInfo info = new GitFlowVersionInfo("0.9-SNAPSHOT");
        Assert.assertNotNull(info);
        Assert.assertEquals("0.10", info.hotfixVersion(false));
    }

    @Test
    public void testHotfixVersion3() throws Exception {
        GitFlowVersionInfo info = new GitFlowVersionInfo("0.9");
        Assert.assertNotNull(info);
        Assert.assertEquals("0.10", info.hotfixVersion(true));
    }

    @Test
    public void testHotfixVersion4() throws Exception {
        GitFlowVersionInfo info = new GitFlowVersionInfo("0.9-SNAPSHOT");
        Assert.assertNotNull(info);
        Assert.assertEquals("0.10-SNAPSHOT", info.hotfixVersion(true));
    }

    @Test
    public void testDigitsVersionInfo() throws Exception {
        GitFlowVersionInfo info = new GitFlowVersionInfo("0.9");
        Assert.assertNotNull(info);
        info = info.digitsVersionInfo();
        Assert.assertNotNull(info);
        Assert.assertEquals(new GitFlowVersionInfo("0.9"), info);
    }

    @Test
    public void testDigitsVersionInfo2() throws Exception {
        GitFlowVersionInfo info = new GitFlowVersionInfo(
                "0.9-RC3-feature-SNAPSHOT");
        Assert.assertNotNull(info);
        info = info.digitsVersionInfo();
        Assert.assertNotNull(info);
        Assert.assertEquals(new GitFlowVersionInfo("0.9"), info);
    }
}
