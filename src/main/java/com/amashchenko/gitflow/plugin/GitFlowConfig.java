/*
 * Copyright 2014 Aleksandr Mashchenko.
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
package com.amashchenko.gitflow.plugin;

public class GitFlowConfig {
    private String productionBranch;
    private String developmentBranch;
    private String featureBranchPrefix;
    private String releaseBranchPrefix;
    private String hotfixBranchPrefix;
    private String versionTagPrefix;

    public GitFlowConfig() {
        this.productionBranch = "master";
        this.developmentBranch = "develop";
        this.featureBranchPrefix = "feature/";
        this.releaseBranchPrefix = "release/";
        this.hotfixBranchPrefix = "hotfix/";
        this.versionTagPrefix = "";
    }

    /**
     * @return the productionBranch
     */
    public String getProductionBranch() {
        return productionBranch;
    }

    /**
     * @param productionBranch
     *            the productionBranch to set
     */
    public void setProductionBranch(String productionBranch) {
        this.productionBranch = productionBranch;
    }

    /**
     * @return the developmentBranch
     */
    public String getDevelopmentBranch() {
        return developmentBranch;
    }

    /**
     * @param developmentBranch
     *            the developmentBranch to set
     */
    public void setDevelopmentBranch(String developmentBranch) {
        this.developmentBranch = developmentBranch;
    }

    /**
     * @return the featureBranchPrefix
     */
    public String getFeatureBranchPrefix() {
        return featureBranchPrefix;
    }

    /**
     * @param featureBranchPrefix
     *            the featureBranchPrefix to set
     */
    public void setFeatureBranchPrefix(String featureBranchPrefix) {
        this.featureBranchPrefix = featureBranchPrefix;
    }

    /**
     * @return the releaseBranchPrefix
     */
    public String getReleaseBranchPrefix() {
        return releaseBranchPrefix;
    }

    /**
     * @param releaseBranchPrefix
     *            the releaseBranchPrefix to set
     */
    public void setReleaseBranchPrefix(String releaseBranchPrefix) {
        this.releaseBranchPrefix = releaseBranchPrefix;
    }

    /**
     * @return the hotfixBranchPrefix
     */
    public String getHotfixBranchPrefix() {
        return hotfixBranchPrefix;
    }

    /**
     * @param hotfixBranchPrefix
     *            the hotfixBranchPrefix to set
     */
    public void setHotfixBranchPrefix(String hotfixBranchPrefix) {
        this.hotfixBranchPrefix = hotfixBranchPrefix;
    }

    /**
     * @return the versionTagPrefix
     */
    public String getVersionTagPrefix() {
        return versionTagPrefix;
    }

    /**
     * @param versionTagPrefix
     *            the versionTagPrefix to set
     */
    public void setVersionTagPrefix(String versionTagPrefix) {
        this.versionTagPrefix = versionTagPrefix;
    }
}
