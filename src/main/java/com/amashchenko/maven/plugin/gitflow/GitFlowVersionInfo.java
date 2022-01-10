/*
 * Copyright 2014-2022 Aleksandr Mashchenko.
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
import org.apache.maven.shared.release.policy.PolicyException;
import org.apache.maven.shared.release.policy.version.VersionPolicy;
import org.apache.maven.shared.release.policy.version.VersionPolicyRequest;
import org.apache.maven.shared.release.versions.DefaultVersionInfo;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.codehaus.plexus.util.StringUtils;

/**
 * Git flow {@link org.apache.maven.shared.release.versions.VersionInfo}
 * implementation. Adds few convenient methods.
 * 
 */
public class GitFlowVersionInfo extends DefaultVersionInfo {
    
    private final VersionPolicy versionPolicy;

    public GitFlowVersionInfo(final String version, final VersionPolicy versionPolicy)
            throws VersionParseException {
        super(version);
        this.versionPolicy = versionPolicy;
    }

    /**
     * Returns a new GitFlowVersionInfo that holds only digits in the version.
     * 
     * @return Digits only GitFlowVersionInfo instance.
     * @throws VersionParseException
     *             If version parsing fails.
     */
    public GitFlowVersionInfo digitsVersionInfo() throws VersionParseException {
        return new GitFlowVersionInfo(joinDigitString(getDigits()), versionPolicy);
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

    @Override
    public String getReleaseVersionString() {
        if (versionPolicy != null) {
            try {
                VersionPolicyRequest request = new VersionPolicyRequest().setVersion(this.toString());
                return versionPolicy.getReleaseVersion(request).getVersion();
            } catch (PolicyException | VersionParseException ex) {
                throw new RuntimeException("Unable to get release version from policy.", ex);
            }
        }
        return super.getReleaseVersionString();
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
     * Gets next SNAPSHOT version.
     * 
     * @param index
     *            Which part of version to increment.
     * @return Next SNAPSHOT version.
     */
    public String nextSnapshotVersion(final Integer index) {
        return nextVersion(index, true);
    }

    /**
     * Gets next version. If index is <code>null</code> or not valid then it
     * delegates to {@link #getNextVersion()} method.
     * 
     * @param index
     *            Which part of version to increment.
     * @param snapshot
     *            Whether to use SNAPSHOT version.
     * @return Next version.
     */
    private String nextVersion(final Integer index, boolean snapshot) {
        if (versionPolicy != null) {
            try {
                VersionPolicyRequest request = new VersionPolicyRequest().setVersion(this.toString());
                if (snapshot) {
                    return versionPolicy.getDevelopmentVersion(request).getVersion();
                } else {
                    return versionPolicy.getReleaseVersion(request).getVersion();
                }
            } catch (PolicyException | VersionParseException ex) {
                throw new RuntimeException("Unable to get development version from policy.", ex);
            }
        }

        List<String> digits = getDigits();

        String nextVersion = null;

        if (digits != null) {
            if (index != null && index >= 0 && index < digits.size()) {
                int origDigitsLength = joinDigitString(digits).length();
                digits.set(index, incrementVersionString(digits.get(index)));
                for (int i = index + 1; i < digits.size(); i++) {
                    digits.set(i, "0");
                }
                String digitsStr = joinDigitString(digits);
                nextVersion = digitsStr + (snapshot ? getSnapshotVersionString().substring(origDigitsLength)
                        : getReleaseVersionString().substring(origDigitsLength));
            } else {
                nextVersion = snapshot ? getNextVersion().getSnapshotVersionString() : getNextVersion().getReleaseVersionString();
            }
        } else {
            nextVersion = snapshot ? getSnapshotVersionString() : getReleaseVersionString();
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
     * @param index
     *            Which part of version to increment.
     * @return Next version.
     */
    public String hotfixVersion(boolean preserveSnapshot, final Integer index) {
        return nextVersion(index, (preserveSnapshot && isSnapshot()));
    }
}
