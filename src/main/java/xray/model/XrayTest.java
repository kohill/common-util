package xray.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "testKey",
        "start",
        "finish",
        "comment",
        "executedBy",
        "assignee",
        "status",
        "results",
        "steps",
        "defects",
        "evidences"
})
public class XrayTest {

    /**
     * (Required)
     */
    @JsonProperty("testKey")
    private String testKey;
    @JsonProperty("start")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private Date start;
    @JsonProperty("finish")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private Date finish;
    @JsonProperty("comment")
    private String comment;
    @JsonProperty("executedBy")
    private String executedBy;
    @JsonProperty("assignee")
    private String assignee;
    /**
     * (Required)
     */
    @JsonProperty("status")
    private String status;

    @JsonProperty("examples1")
    private List<String> examples1 = new ArrayList<String>();
    @JsonProperty("defects")
    private List<String> defects = new ArrayList<String>();
    @JsonProperty("evidences")
    private List<Evidence> evidences = new ArrayList<Evidence>();

    /**
     * (Required)
     */
    @JsonProperty("testKey")
    public String getTestKey() {
        return testKey;
    }

    /**
     * (Required)
     */
    @JsonProperty("testKey")
    public void setTestKey(String testKey) {
        this.testKey = testKey;
    }

    @JsonProperty("start")
    public Date getStart() {
        return start;
    }

    @JsonProperty("start")
    public void setStart(Date start) {
        this.start = start;
    }

    @JsonProperty("finish")
    public Date getFinish() {
        return finish;
    }

    @JsonProperty("finish")
    public void setFinish(Date finish) {
        this.finish = finish;
    }

    @JsonProperty("comment")
    public String getComment() {
        return comment;
    }

    @JsonProperty("comment")
    public void setComment(String comment) {
        this.comment = comment;
    }

    @JsonProperty("executedBy")
    public String getExecutedBy() {
        return executedBy;
    }

    @JsonProperty("executedBy")
    public void setExecutedBy(String executedBy) {
        this.executedBy = executedBy;
    }

    @JsonProperty("assignee")
    public String getAssignee() {
        return assignee;
    }

    @JsonProperty("assignee")
    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    /**
     * (Required)
     */
    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    /**
     * (Required)
     */
    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("examples1")
    public List<String> getExamples1() {
        return examples1;
    }

    @JsonProperty("examples1")
    public void setExamples1(List<String> examples1) {
        this.examples1 = examples1;
    }

    @JsonProperty("defects")
    public List<String> getDefects() {
        return defects;
    }

    @JsonProperty("defects")
    public void setDefects(List<String> defects) {
        this.defects = defects;
    }

    @JsonProperty("evidences")
    public List<Evidence> getEvidences() {
        return evidences;
    }

    @JsonProperty("evidences")
    public void setEvidences(List<Evidence> evidences) {
        this.evidences = evidences;
    }

}
