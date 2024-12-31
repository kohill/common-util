package xray;

import org.aeonbits.owner.Config;


public interface XrayConfig extends Config {

    @Key("xray.upload")
    boolean upload();

    @Key("xray.upload.realtime")
    boolean uploadInRealTime();

    @Key("xray.upload.todo")

    boolean uploadTodoStatus();

    @Key("xray.upload.executing")

    boolean uploadExecutingStatus();

    @Key("xray.project.key")
    String projectKey();

    @Key("xray.testExecution.create")
    boolean createTestExecution();

    @Key("xray.testExecution.key")
    String testExecutionKey();

    @Key("xray.testPlan.key")
    String testPlanKey();

    @Key("xray.url.authenticate")
    @DefaultValue("https://xray.cloud.xpand-it.com/api/v1/authenticate")
    String authenticateUrl();

    @Key("xray.url.import.results.testNg")
    @DefaultValue("https://xray.cloud.xpand-it.com/api/v1/import/execution/testng")
    String testNgImportResultsUrl();

    @Key("xray.url.import.results.xrayJson")
    @DefaultValue("https://xray.cloud.getxray.app/api/v1/import/execution")
    String xRayJsonImportResultsUrl();

    @Key("xray.authenticate.client.id")
    String clientId();

    @Key("xray.authenticate.client.secret")
    String clientSecret();

    @Key("xray.comment")
    String executionComment();
}
