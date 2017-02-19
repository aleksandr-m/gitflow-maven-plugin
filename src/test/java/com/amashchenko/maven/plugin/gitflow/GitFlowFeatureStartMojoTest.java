package com.amashchenko.maven.plugin.gitflow;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by krs on 2/18/17.
 */
public class GitFlowFeatureStartMojoTest {
    private static final String CUSTOM_REGEX = "^JIRA-\\d{3,5}$";
    private GitFlowFeatureStartMojo sut;

    @BeforeMethod
    public void beforeEachTest(){
        sut = new GitFlowFeatureStartMojo();
    }

    @Test(dataProvider = "validCustomNameProvider")
    public void testCustomValidRegex(String branchName){
        sut.featureBranchRegex = CUSTOM_REGEX;
        assertTrue(sut.isBranchNameRegexCompliant(branchName), "Should be valid: " + branchName);
    }

    @DataProvider
    public Object[][] validCustomNameProvider() {
        return new Object[][] {
                { "JIRA-123" },
                { "JIRA-12345" }
        };
    }

    @Test(dataProvider = "inValidCustomNameProvider")
    public void testCustomInvalidRegex(String branchName){
        sut.featureBranchRegex = CUSTOM_REGEX;
        assertFalse(sut.isBranchNameRegexCompliant(branchName), "Should be invalid: " + branchName);
    }

    @DataProvider
    public Object[][] inValidCustomNameProvider() {
        return new Object[][]{
                {""},
                {"data"},
                {"data123"},
                {"data_123"},
                {"_123data_123"},
                {":_123data_123"},
                {"123"},
                {"JIRA-1234567890"},
                {"jira-1234"},
                {"JIRA-12"},
                {"JIRA"},
                {"data two"}
        };
    }

    @Test(dataProvider = "validDefaultNameProvider", description = "Test that default allows all input")
    public void testDefaultRegexAcceptsAllInput(String branchName){
        assertTrue(sut.isBranchNameRegexCompliant(branchName), "Should be valid: " + branchName);
    }

    @DataProvider
    public Object[][] validDefaultNameProvider() {
        return new Object[][] {
                { "" },
                { "data" },
                { "data123" },
                { "data_123" },
                { "_123data_123" },
                { ":_123data_123" },
                { "123" },
                { "data two" }
        };
    }
}