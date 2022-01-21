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

/**
 * Git commit messages.
 *
 */
public class CommitMessages {
    private String featureStartMessage;
    private String featureFinishMessage;

    private String hotfixStartMessage;
    private String hotfixFinishMessage;

    private String hotfixVersionUpdateMessage;

    private String releaseStartMessage;
    private String releaseFinishMessage;

    private String releaseVersionUpdateMessage;

    private String releaseFinishMergeMessage;
    private String releaseFinishDevMergeMessage;

    private String featureFinishDevMergeMessage;
    private String featureSquashMessage;

    private String hotfixFinishMergeMessage;
    private String hotfixFinishDevMergeMessage;
    private String hotfixFinishReleaseMergeMessage;
    private String hotfixFinishSupportMergeMessage;

    private String tagHotfixMessage;
    private String tagReleaseMessage;
    private String tagVersionUpdateMessage;

    private String updateDevToAvoidConflictsMessage;
    private String updateDevBackPreMergeStateMessage;

    private String updateReleaseToAvoidConflictsMessage;
    private String updateReleaseBackPreMergeStateMessage;

    private String updateFeatureBackMessage;
    private String featureFinishIncrementVersionMessage;

    private String supportStartMessage;

    private String versionUpdateMessage;

    public CommitMessages() {
        featureStartMessage = System.getProperty("commitMessages.featureStartMessage", "Update versions for feature branch");
        featureFinishMessage = System.getProperty("commitMessages.featureFinishMessage", "Update versions for development branch");

        hotfixStartMessage = System.getProperty("commitMessages.hotfixStartMessage", "Update versions for hotfix");
        hotfixFinishMessage = System.getProperty("commitMessages.hotfixFinishMessage", "Update for next development version");

        hotfixVersionUpdateMessage = System.getProperty("commitMessages.hotfixVersionUpdateMessage", "Update to hotfix version");

        releaseStartMessage = System.getProperty("commitMessages.releaseStartMessage", "Update versions for release");
        releaseFinishMessage = System.getProperty("commitMessages.releaseFinishMessage", "Update for next development version");

        releaseVersionUpdateMessage = System.getProperty("commitMessages.releaseVersionUpdateMessage",
                "Update for next development version");

        releaseFinishMergeMessage = System.getProperty("commitMessages.releaseFinishMergeMessage", "");
        releaseFinishDevMergeMessage = System.getProperty("commitMessages.releaseFinishDevMergeMessage", "");

        featureFinishDevMergeMessage = System.getProperty("commitMessages.featureFinishDevMergeMessage", "");
        featureSquashMessage = System.getProperty("commitMessages.featureSquashMessage", "");

        hotfixFinishMergeMessage = System.getProperty("commitMessages.hotfixFinishMergeMessage", "");
        hotfixFinishDevMergeMessage = System.getProperty("commitMessages.hotfixFinishDevMergeMessage", "");
        hotfixFinishReleaseMergeMessage = System.getProperty("commitMessages.hotfixFinishReleaseMergeMessage", "");
        hotfixFinishSupportMergeMessage = System.getProperty("commitMessages.hotfixFinishSupportMergeMessage", "");

        tagHotfixMessage = System.getProperty("commitMessages.tagHotfixMessage", "Tag hotfix");
        tagReleaseMessage = System.getProperty("commitMessages.tagReleaseMessage", "Tag release");
        tagVersionUpdateMessage = System.getProperty("commitMessages.tagVersionUpdateMessage", "Tag version update");

        updateDevToAvoidConflictsMessage = System.getProperty("commitMessages.updateDevToAvoidConflictsMessage",
                "Update develop to production version to avoid merge conflicts");
        updateDevBackPreMergeStateMessage = System.getProperty("commitMessages.updateDevBackPreMergeStateMessage",
                "Update develop version back to pre-merge state");

        updateReleaseToAvoidConflictsMessage = System.getProperty("commitMessages.updateReleaseToAvoidConflictsMessage",
                "Update release to hotfix version to avoid merge conflicts");
        updateReleaseBackPreMergeStateMessage = System.getProperty("commitMessages.updateReleaseBackPreMergeStateMessage",
                "Update release version back to pre-merge state");

        updateFeatureBackMessage = System.getProperty("commitMessages.updateFeatureBackMessage",
                "Update feature branch back to feature version");
        featureFinishIncrementVersionMessage = System.getProperty("commitMessages.featureFinishIncrementVersionMessage",
                "Increment feature version");

        supportStartMessage = System.getProperty("commitMessages.supportStartMessage", "Update versions for support branch");

        versionUpdateMessage = System.getProperty("commitMessages.versionUpdateMessage", "Update versions");
    }

    /**
     * @return the featureStartMessage
     */
    public String getFeatureStartMessage() {
        return featureStartMessage;
    }

    /**
     * @param featureStartMessage
     *            the featureStartMessage to set
     */
    public void setFeatureStartMessage(String featureStartMessage) {
        this.featureStartMessage = featureStartMessage;
    }

    /**
     * @return the featureFinishMessage
     */
    public String getFeatureFinishMessage() {
        return featureFinishMessage;
    }

    /**
     * @param featureFinishMessage
     *            the featureFinishMessage to set
     */
    public void setFeatureFinishMessage(String featureFinishMessage) {
        this.featureFinishMessage = featureFinishMessage;
    }

    /**
     * @return the hotfixStartMessage
     */
    public String getHotfixStartMessage() {
        return hotfixStartMessage;
    }

    /**
     * @param hotfixStartMessage
     *            the hotfixStartMessage to set
     */
    public void setHotfixStartMessage(String hotfixStartMessage) {
        this.hotfixStartMessage = hotfixStartMessage;
    }

    /**
     * @return the hotfixFinishMessage
     */
    public String getHotfixFinishMessage() {
        return hotfixFinishMessage;
    }

    /**
     * @param hotfixFinishMessage
     *            the hotfixFinishMessage to set
     */
    public void setHotfixFinishMessage(String hotfixFinishMessage) {
        this.hotfixFinishMessage = hotfixFinishMessage;
    }

    public String getHotfixVersionUpdateMessage() {
        return hotfixVersionUpdateMessage;
    }

    public void setHotfixVersionUpdateMessage(String hotfixVersionUpdateMessage) {
        this.hotfixVersionUpdateMessage = hotfixVersionUpdateMessage;
    }

    /**
     * @return the releaseStartMessage
     */
    public String getReleaseStartMessage() {
        return releaseStartMessage;
    }

    /**
     * @param releaseStartMessage
     *            the releaseStartMessage to set
     */
    public void setReleaseStartMessage(String releaseStartMessage) {
        this.releaseStartMessage = releaseStartMessage;
    }

    /**
     * @return the releaseFinishMessage
     */
    public String getReleaseFinishMessage() {
        return releaseFinishMessage;
    }

    /**
     * @param releaseFinishMessage
     *            the releaseFinishMessage to set
     */
    public void setReleaseFinishMessage(String releaseFinishMessage) {
        this.releaseFinishMessage = releaseFinishMessage;
    }

    public String getReleaseVersionUpdateMessage() {
        return releaseVersionUpdateMessage;
    }

    public void setReleaseVersionUpdateMessage(String releaseVersionUpdateMessage) {
        this.releaseVersionUpdateMessage = releaseVersionUpdateMessage;
    }

    /**
     * @return the releaseFinishMergeMessage
     */
    public String getReleaseFinishMergeMessage() {
        return releaseFinishMergeMessage;
    }

    /**
     * @param releaseFinishMergeMessage
     *            the releaseFinishMergeMessage to set
     */
    public void setReleaseFinishMergeMessage(String releaseFinishMergeMessage) {
        this.releaseFinishMergeMessage = releaseFinishMergeMessage;
    }

    /**
     * @return the releaseFinishDevMergeMessage
     */
    public String getReleaseFinishDevMergeMessage() {
        return releaseFinishDevMergeMessage;
    }

    /**
     * @param releaseFinishDevMergeMessage
     *            the releaseFinishDevMergeMessage to set
     */
    public void setReleaseFinishDevMergeMessage(String releaseFinishDevMergeMessage) {
        this.releaseFinishDevMergeMessage = releaseFinishDevMergeMessage;
    }

    /**
     * @return the tagHotfixMessage
     */
    public String getTagHotfixMessage() {
        return tagHotfixMessage;
    }

    /**
     * @param tagHotfixMessage
     *            the tagHotfixMessage to set
     */
    public void setTagHotfixMessage(String tagHotfixMessage) {
        this.tagHotfixMessage = tagHotfixMessage;
    }

    /**
     * @return the tagReleaseMessage
     */
    public String getTagReleaseMessage() {
        return tagReleaseMessage;
    }

    /**
     * @param tagReleaseMessage
     *            the tagReleaseMessage to set
     */
    public void setTagReleaseMessage(String tagReleaseMessage) {
        this.tagReleaseMessage = tagReleaseMessage;
    }

    /**
     * @return the updateDevToAvoidConflictsMessage
     */
    public String getUpdateDevToAvoidConflictsMessage() {
        return updateDevToAvoidConflictsMessage;
    }

    /**
     * @param updateDevToAvoidConflictsMessage
     *            the updateDevToAvoidConflictsMessage to set
     */
    public void setUpdateDevToAvoidConflictsMessage(String updateDevToAvoidConflictsMessage) {
        this.updateDevToAvoidConflictsMessage = updateDevToAvoidConflictsMessage;
    }

    /**
     * @return the updateDevBackPreMergeStateMessage
     */
    public String getUpdateDevBackPreMergeStateMessage() {
        return updateDevBackPreMergeStateMessage;
    }

    /**
     * @param updateDevBackPreMergeStateMessage
     *            the updateDevBackPreMergeStateMessage to set
     */
    public void setUpdateDevBackPreMergeStateMessage(String updateDevBackPreMergeStateMessage) {
        this.updateDevBackPreMergeStateMessage = updateDevBackPreMergeStateMessage;
    }

    /**
     * @return the updateReleaseToAvoidConflictsMessage
     */
    public String getUpdateReleaseToAvoidConflictsMessage() {
        return updateReleaseToAvoidConflictsMessage;
    }

    /**
     * @param updateReleaseToAvoidConflictsMessage the updateReleaseToAvoidConflictsMessage to set
     */
    public void setUpdateReleaseToAvoidConflictsMessage(String updateReleaseToAvoidConflictsMessage) {
        this.updateReleaseToAvoidConflictsMessage = updateReleaseToAvoidConflictsMessage;
    }

    /**
     * @return the updateReleaseBackPreMergeStateMessage
     */
    public String getUpdateReleaseBackPreMergeStateMessage() {
        return updateReleaseBackPreMergeStateMessage;
    }

    /**
     * @param updateReleaseBackPreMergeStateMessage the updateReleaseBackPreMergeStateMessage to set
     */
    public void setUpdateReleaseBackPreMergeStateMessage(String updateReleaseBackPreMergeStateMessage) {
        this.updateReleaseBackPreMergeStateMessage = updateReleaseBackPreMergeStateMessage;
    }

    /**
     * @return the hotfixFinishMergeMessage
     */
    public String getHotfixFinishMergeMessage() {
        return hotfixFinishMergeMessage;
    }

    /**
     * @param hotfixFinishMergeMessage
     *            the hotfixFinishMergeMessage to set
     */
    public void setHotfixFinishMergeMessage(String hotfixFinishMergeMessage) {
        this.hotfixFinishMergeMessage = hotfixFinishMergeMessage;
    }

    /**
     * @return the hotfixFinishDevMergeMessage
     */
    public String getHotfixFinishDevMergeMessage() {
        return hotfixFinishDevMergeMessage;
    }

    /**
     * @param hotfixFinishDevMergeMessage
     *            the hotfixFinishDevMergeMessage to set
     */
    public void setHotfixFinishDevMergeMessage(String hotfixFinishDevMergeMessage) {
        this.hotfixFinishDevMergeMessage = hotfixFinishDevMergeMessage;
    }

    /**
     * @return the hotfixFinishReleaseMergeMessage
     */
    public String getHotfixFinishReleaseMergeMessage() {
        return hotfixFinishReleaseMergeMessage;
    }

    /**
     * @param hotfixFinishReleaseMergeMessage
     *            the hotfixFinishReleaseMergeMessage to set
     */
    public void setHotfixFinishReleaseMergeMessage(String hotfixFinishReleaseMergeMessage) {
        this.hotfixFinishReleaseMergeMessage = hotfixFinishReleaseMergeMessage;
    }

    /**
     * @return the hotfixFinishSupportMergeMessage
     */
    public String getHotfixFinishSupportMergeMessage() {
        return hotfixFinishSupportMergeMessage;
    }

    /**
     * @param hotfixFinishSupportMergeMessage
     *            the hotfixFinishSupportMergeMessage to set
     */
    public void setHotfixFinishSupportMergeMessage(String hotfixFinishSupportMergeMessage) {
        this.hotfixFinishSupportMergeMessage = hotfixFinishSupportMergeMessage;
    }

    /**
     * @return the featureFinishDevMergeMessage
     */
    public String getFeatureFinishDevMergeMessage() {
        return featureFinishDevMergeMessage;
    }

    /**
     * @param featureFinishDevMergeMessage
     *            the featureFinishDevMergeMessage to set
     */
    public void setFeatureFinishDevMergeMessage(String featureFinishDevMergeMessage) {
        this.featureFinishDevMergeMessage = featureFinishDevMergeMessage;
    }

    /**
     * @return the featureSquashMessage
     */
    public String getFeatureSquashMessage() {
        return featureSquashMessage;
    }

    /**
     * @param featureSquashMessage
     *            the featureSquashMessage to set
     */
    public void setFeatureSquashMessage(String featureSquashMessage) {
        this.featureSquashMessage = featureSquashMessage;
    }

    /**
     * @return the updateFeatureBackMessage
     */
    public String getUpdateFeatureBackMessage() {
        return updateFeatureBackMessage;
    }

    /**
     * @param updateFeatureBackMessage
     *            the updateFeatureBackMessage to set
     */
    public void setUpdateFeatureBackMessage(String updateFeatureBackMessage) {
        this.updateFeatureBackMessage = updateFeatureBackMessage;
    }

    /**
     * @return the featureFinishIncrementVersionMessage
     */
    public String getFeatureFinishIncrementVersionMessage() {
        return featureFinishIncrementVersionMessage;
    }

    /**
     * @param featureFinishIncrementVersionMessage
     *            the featureFinishIncrementVersionMessage to set
     */
    public void setFeatureFinishIncrementVersionMessage(String featureFinishIncrementVersionMessage) {
        this.featureFinishIncrementVersionMessage = featureFinishIncrementVersionMessage;
    }

    /**
     * @return the supportStartMessage
     */
    public String getSupportStartMessage() {
        return supportStartMessage;
    }

    /**
     * @param supportStartMessage
     *            the supportStartMessage to set
     */
    public void setSupportStartMessage(String supportStartMessage) {
        this.supportStartMessage = supportStartMessage;
    }

    /**
     * @return the tagVersionUpdateMessage
     */
    public String getTagVersionUpdateMessage() {
        return tagVersionUpdateMessage;
    }

    /**
     * @param tagVersionUpdateMessage the tagVersionUpdateMessage to set
     */
    public void setTagVersionUpdateMessage(String tagVersionUpdateMessage) {
        this.tagVersionUpdateMessage = tagVersionUpdateMessage;
    }

    /**
     * @return the versionUpdateMessage
     */
    public String getVersionUpdateMessage() {
        return versionUpdateMessage;
    }

    /**
     * @param versionUpdateMessage the versionUpdateMessage to set
     */
    public void setVersionUpdateMessage(String versionUpdateMessage) {
        this.versionUpdateMessage = versionUpdateMessage;
    }
}
