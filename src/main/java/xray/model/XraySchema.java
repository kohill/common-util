package xray.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "testExecutionKey",
        "info",
        "tests"
})
public class XraySchema {

    @JsonProperty("testExecutionKey")
    private String testExecutionKey;

    @JsonProperty("tests")
    private List<XrayTest> tests = new ArrayList<XrayTest>();

    @JsonProperty("testExecutionKey")
    public String getTestExecutionKey() {
        return testExecutionKey;
    }

    @JsonProperty("testExecutionKey")
    public void setTestExecutionKey(String testExecutionKey) {
        this.testExecutionKey = testExecutionKey;
    }

    @JsonProperty("tests")
    public List<XrayTest> getTests() {
        return tests;
    }

    @JsonProperty("tests")
    public void setTests(List<XrayTest> tests) {
        this.tests = tests;
    }

}
