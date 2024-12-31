package xray.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "data",
        "filename",
        "contentType"
})
public class Evidence {

    /**
     * (Required)
     */
    @JsonProperty("data")
    private String data;
    /**
     * (Required)
     */
    @JsonProperty("filename")
    private String filename;
    @JsonProperty("contentType")
    private String contentType;

    /**
     * (Required)
     */
    @JsonProperty("data")
    public String getData() {
        return data;
    }

    /**
     * (Required)
     */
    @JsonProperty("data")
    public void setData(String data) {
        this.data = data;
    }

    /**
     * (Required)
     */
    @JsonProperty("filename")
    public String getFilename() {
        return filename;
    }

    /**
     * (Required)
     */
    @JsonProperty("filename")
    public void setFilename(String filename) {
        this.filename = filename;
    }

    @JsonProperty("contentType")
    public String getContentType() {
        return contentType;
    }

    @JsonProperty("contentType")
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

}
