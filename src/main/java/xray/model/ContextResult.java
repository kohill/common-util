package xray.model;

import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "name",
        "duration",
        "log",
        "status",
        "evidences"
})
public class ContextResult {

    @JsonProperty("name")
    private String name;
    @JsonProperty("duration")
    private Double duration;
    @JsonProperty("log")
    private String log;

    @JsonProperty("status")
    private ContextResult.Status status;
    @JsonProperty("evidences")
    private List<Evidence> evidences = new ArrayList<Evidence>();

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("duration")
    public Double getDuration() {
        return duration;
    }

    @JsonProperty("duration")
    public void setDuration(Double duration) {
        this.duration = duration;
    }

    @JsonProperty("log")
    public String getLog() {
        return log;
    }

    @JsonProperty("log")
    public void setLog(String log) {
        this.log = log;
    }

    @JsonProperty("status")
    public ContextResult.Status getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(ContextResult.Status status) {
        this.status = status;
    }

    @JsonProperty("evidences")
    public List<Evidence> getEvidences() {
        return evidences;
    }

    @JsonProperty("evidences")
    public void setEvidences(List<Evidence> evidences) {
        this.evidences = evidences;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ContextResult.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("duration");
        sb.append('=');
        sb.append(((this.duration == null) ? "<null>" : this.duration));
        sb.append(',');
        sb.append("log");
        sb.append('=');
        sb.append(((this.log == null) ? "<null>" : this.log));
        sb.append(',');
        sb.append("status");
        sb.append('=');
        sb.append(((this.status == null) ? "<null>" : this.status));
        sb.append(',');
        sb.append("evidences");
        sb.append('=');
        sb.append(((this.evidences == null) ? "<null>" : this.evidences));

        if (sb.charAt((sb.length() - 1)) == ',') {
            sb.setCharAt((sb.length() - 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result * 31) + ((this.duration == null) ? 0 : this.duration.hashCode()));
        result = ((result * 31) + ((this.log == null) ? 0 : this.log.hashCode()));
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.evidences == null) ? 0 : this.evidences.hashCode()));
        result = ((result * 31) + ((this.status == null) ? 0 : this.status.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ContextResult)) {
            return false;
        }
        ContextResult rhs = ((ContextResult) other);
        return ((((((((this.duration == rhs.duration) || ((this.duration != null) && this.duration.equals(rhs.duration))) && ((this.log == rhs.log) || ((this.log != null) && this.log.equals(rhs.log)))) && ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))) && ((this.evidences == rhs.evidences) || ((this.evidences != null) && this.evidences.equals(rhs.evidences))))))) && ((this.status == rhs.status) || ((this.status != null) && this.status.equals(rhs.status)));
    }

    public enum Status {

        TODO("TODO"),
        FAILED("FAILED"),
        PASSED("PASSED"),
        EXECUTING("EXECUTING");
        private final static Map<String, Status> CONSTANTS = new HashMap<String, Status>();

        static {
            for (ContextResult.Status c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        private Status(String value) {
            this.value = value;
        }

        @JsonCreator
        public static ContextResult.Status fromValue(String value) {
            ContextResult.Status constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }
    }
}
