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

import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.release.versions.DefaultVersionInfo;
import org.apache.maven.shared.release.versions.VersionInfo;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.codehaus.plexus.util.StringUtils;

/**
 * Git flow {@link VersionInfo} implementation. Adds few convenient methods.
 * 
 */
public class GitFlowVersionInfo extends DefaultVersionInfo {

    public GitFlowVersionInfo(final String version)
            throws VersionParseException {
        super(version);
    }

    /**
     * Returns a new GitFlowVersionInfo that holds only digits in the version.
     * 
     * @return Digits only GitFlowVersionInfo instance.
     * @throws VersionParseException
     */
    public GitFlowVersionInfo digitsVersionInfo() throws VersionParseException {
        return new GitFlowVersionInfo(joinDigitString(getDigits()));
    }

    /**
     * Validates version.
     * 
     * @param version
     *            Version to validate.
     * @return <code>true</code> when version is valid, <code>false</code>
     *         otherwise.
     */
    public static boolean isValidVersion(final String version) {
        return StringUtils.isNotBlank(version)
                && (ALTERNATE_PATTERN.matcher(version).matches() || STANDARD_PATTERN
                        .matcher(version).matches());
    }

    /**
     * Gets next SNAPSHOT version.
     * 
     * @return Next SNAPSHOT version.
     */
    public String nextSnapshotVersion() {
        return nextSnapshotVersion(null);
    }

    /**
     * Gets next SNAPSHOT version. If index is <code>null</code> or not valid
     * then it delegates to {@link #getNextVersion()} method.
     * 
     * @param index
     *            Which part of version to increment.
     * @return Next SNAPSHOT version.
     */
    public String nextSnapshotVersion(final Integer index) {
        List<String> digits = getDigits();

        String nextVersion = null;

        if (digits != null) {
            if (index != null && index >= 0 && index < digits.size()) {
                int origDigitsLength = joinDigitString(digits).length();
                digits.set(index,
                        incrementVersionString((String) digits.get(index)));
                for (int i = index + 1; i < digits.size(); i++) {
                    digits.set(i, "0");
                }
                String digitsStr = joinDigitString(digits);
                nextVersion = digitsStr
                        + getSnapshotVersionString()
                                .substring(origDigitsLength);
            } else {
                nextVersion = getNextVersion().getSnapshotVersionString();
            }
        } else {
            nextVersion = getSnapshotVersionString();
        }
        return nextVersion;
    }

    /**
     * Gets version with appended feature name.
     * 
     * @param featureName
     *            Feature name to append.
     * @return Version with appended feature name.
     */
    public String featureVersion(final String featureName) {
        String version = toString();
        if (featureName != null) {
            version = getReleaseVersionString() + "-" + featureName
                    + (isSnapshot() ? "-" + Artifact.SNAPSHOT_VERSION : "");
        }
        return version;
    }

    /**
     * Gets next hotfix version.
     * 
     * @param preserveSnapshot
     *            Whether to preserve SNAPSHOT in the version.
     * @return Next version.
     */
    public String hotfixVersion(boolean preserveSnapshot) {
        return (preserveSnapshot && isSnapshot()) ? getNextVersion()
                .getSnapshotVersionString() : getNextVersion()
                .getReleaseVersionString();
    }
}
