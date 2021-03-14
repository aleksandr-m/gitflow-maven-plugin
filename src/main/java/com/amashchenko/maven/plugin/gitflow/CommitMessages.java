/*
 * Copyright 2014-2021 Aleksandr Mashchenko.
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

    private static final String PROPERTY_PREFIX = "commitMessages.";

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

    private String hotfixFinishMergeMessage;
    private String hotfixFinishDevMergeMessage;
    private String hotfixFinishReleaseMergeMessage;
    private String hotfixFinishSupportMergeMessage;

    private String tagHotfixMessage;
    private String tagReleaseMessage;

    private String updateDevToAvoidConflictsMessage;
    private String updateDevBackPreMergeStateMessage;

    private String updateReleaseToAvoidConflictsMessage;
    private String updateReleaseBackPreMergeStateMessage;

    private String updateFeatureBackMessage;
    private String featureFinishIncrementVersionMessage;

    private String supportStartMessage;

    public CommitMessages() {
        featureStartMessage = "Update versions for feature branch";
        featureFinishMessage = "Update versions for development branch";

        hotfixStartMessage = "Update versions for hotfix";
        hotfixFinishMessage = "Update for next development version";

        hotfixVersionUpdateMessage = "Update to hotfix version";

        releaseStartMessage = "Update versions for release";
        releaseFinishMessage = "Update for next development version";

        releaseVersionUpdateMessage = "Update for next development version";

        releaseFinishMergeMessage = "";
        releaseFinishDevMergeMessage = "";

        tagHotfixMessage = "Tag hotfix";
        tagReleaseMessage = "Tag release";

        updateDevToAvoidConflictsMessage = "Update develop to production version to avoid merge conflicts";
        updateDevBackPreMergeStateMessage = "Update develop version back to pre-merge state";

        updateReleaseToAvoidConflictsMessage = "Update release to hotfix version to avoid merge conflicts";
        updateReleaseBackPreMergeStateMessage = "Update release version back to pre-merge state";

        updateFeatureBackMessage = "Update feature branch back to feature version";
        featureFinishIncrementVersionMessage = "Increment feature version";

        supportStartMessage = "Update versions for support branch";
    }

    /**
     * @return the featureStartMessage
     */
    public String getFeatureStartMessage() {
        return System.getProperty(PROPERTY_PREFIX + "featureStartMessage", featureStartMessage);
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
        return System.getProperty(PROPERTY_PREFIX + "featureFinishMessage", featureFinishMessage);
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
        return System.getProperty(PROPERTY_PREFIX + "hotfixStartMessage", hotfixStartMessage);
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
        return System.getProperty(PROPERTY_PREFIX + "hotfixFinishMessage", hotfixFinishMessage);
    }

    /**
     * @param hotfixFinishMessage
     *            the hotfixFinishMessage to set
     */
    public void setHotfixFinishMessage(String hotfixFinishMessage) {
        this.hotfixFinishMessage = hotfixFinishMessage;
    }

    public String getHotfixVersionUpdateMessage() {
        return System.getProperty(PROPERTY_PREFIX + "hotfixVersionUpdateMessage", hotfixVersionUpdateMessage);
    }

    public void setHotfixVersionUpdateMessage(String hotfixVersionUpdateMessage) {
        this.hotfixVersionUpdateMessage = hotfixVersionUpdateMessage;
    }

    /**
     * @return the releaseStartMessage
     */
    public String getReleaseStartMessage() {
        return System.getProperty(PROPERTY_PREFIX + "releaseStartMessage", releaseStartMessage);
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
        return System.getProperty(PROPERTY_PREFIX + "releaseFinishMessage", releaseFinishMessage);
    }

    /**
     * @param releaseFinishMessage
     *            the releaseFinishMessage to set
     */
    public void setReleaseFinishMessage(String releaseFinishMessage) {
        this.releaseFinishMessage = releaseFinishMessage;
    }

    public String getReleaseVersionUpdateMessage() {
        return System.getProperty(PROPERTY_PREFIX + "releaseVersionUpdateMessage", releaseVersionUpdateMessage);
    }

    public void setReleaseVersionUpdateMessage(String releaseVersionUpdateMessage) {
        this.releaseVersionUpdateMessage = releaseVersionUpdateMessage;
    }

    /**
     * @return the releaseFinishMergeMessage
     */
    public String getReleaseFinishMergeMessage() {
        return System.getProperty(PROPERTY_PREFIX + "releaseFinishMergeMessage", releaseFinishMergeMessage);
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
        return System.getProperty(PROPERTY_PREFIX + "releaseFinishDevMergeMessage", releaseFinishDevMergeMessage);
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
        return System.getProperty(PROPERTY_PREFIX + "tagHotfixMessage", tagHotfixMessage);
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
        return System.getProperty(PROPERTY_PREFIX + "tagReleaseMessage", tagReleaseMessage);
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
        return System.getProperty(PROPERTY_PREFIX + "updateDevToAvoidConflictsMessage", updateDevToAvoidConflictsMessage);
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
        return System.getProperty(PROPERTY_PREFIX + "updateDevBackPreMergeStateMessage", updateDevBackPreMergeStateMessage);
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
        return System.getProperty(PROPERTY_PREFIX + "updateReleaseToAvoidConflictsMessage", updateReleaseToAvoidConflictsMessage);
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
        return System.getProperty(PROPERTY_PREFIX + "updateReleaseBackPreMergeStateMessage", updateReleaseBackPreMergeStateMessage);
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
        return System.getProperty(PROPERTY_PREFIX + "hotfixFinishMergeMessage", hotfixFinishMergeMessage);
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
        return System.getProperty(PROPERTY_PREFIX + "hotfixFinishDevMergeMessage", hotfixFinishDevMergeMessage);
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
        return System.getProperty(PROPERTY_PREFIX + "hotfixFinishReleaseMergeMessage", hotfixFinishReleaseMergeMessage);
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
        return System.getProperty(PROPERTY_PREFIX + "hotfixFinishSupportMergeMessage", hotfixFinishSupportMergeMessage);
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
        return System.getProperty(PROPERTY_PREFIX + "featureFinishDevMergeMessage", featureFinishDevMergeMessage);
    }

    /**
     * @param featureFinishDevMergeMessage
     *            the featureFinishDevMergeMessage to set
     */
    public void setFeatureFinishDevMergeMessage(String featureFinishDevMergeMessage) {
        this.featureFinishDevMergeMessage = featureFinishDevMergeMessage;
    }

    /**
     * @return the updateFeatureBackMessage
     */
    public String getUpdateFeatureBackMessage() {
        return System.getProperty(PROPERTY_PREFIX + "updateFeatureBackMessage", updateFeatureBackMessage);
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
        return System.getProperty(PROPERTY_PREFIX + "featureFinishIncrementVersionMessage", featureFinishIncrementVersionMessage);
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
        return System.getProperty(PROPERTY_PREFIX + "supportStartMessage", supportStartMessage);
    }

    /**
     * @param supportStartMessage
     *            the supportStartMessage to set
     */
    public void setSupportStartMessage(String supportStartMessage) {
        this.supportStartMessage = supportStartMessage;
    }
}
