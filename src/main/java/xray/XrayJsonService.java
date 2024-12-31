package xray;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.testng.IResultMap;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import xray.model.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_EMPTY_JSON_ARRAYS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_NULL_MAP_VALUES;
import static io.restassured.RestAssured.given;

public class XrayJsonService {

    private static XrayConfig xrayConfig = XrayConfigManager.getInstance();

    private XrayJsonService() {
    }

    static synchronized void uploadInitialTestSuiteStatusToJira(ITestContext context)
            throws JsonProcessingException {
        String results = getInitialTestSuiteStatusRequestBody(context);
        uploadResultsInXRayJsonFormat(results);
    }

    private static synchronized String getInitialTestSuiteStatusRequestBody(
            ITestContext testContext)
            throws JsonProcessingException {
        XraySchema xraySchema = getXraySchema();
        ITestNGMethod[] allTestMethods = testContext.getAllTestMethods();
        List<XrayTest> xrayTests =
                Arrays
                        .stream(allTestMethods)
                        .map(testMethod -> {
                            Xray annotation = testMethod.getConstructorOrMethod().getMethod()
                                    .getAnnotation(Xray.class);
                            XrayTest xrayTest = null;
                            if (Objects.nonNull(annotation)) {
                                xrayTest = new XrayTest();
                                xrayTest.setStatus(String.valueOf(ContextResult.Status.TODO));
                                xrayTest.setTestKey(annotation.id());
                            }
                            return xrayTest;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
        xraySchema.setTests(xrayTests);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper
                .writerWithDefaultPrettyPrinter()
                .without(WRITE_EMPTY_JSON_ARRAYS)
                .without(WRITE_NULL_MAP_VALUES)
                .writeValueAsString(xraySchema);
    }

    static synchronized void uploadFinalTestSuiteStatusToJira(ITestContext context)
            throws JsonProcessingException {
        String results = getFinalTestSuiteStatusRequestBody(context);
        uploadResultsInXRayJsonFormat(results);
    }

    private static synchronized String getFinalTestSuiteStatusRequestBody(ITestContext testContext)
            throws JsonProcessingException {
        XraySchema xraySchema = getXraySchema();
        List<XrayTest> tests = new ArrayList<>();
        IResultMap passedTests = testContext.getPassedTests();
        tests.addAll(getXrayTestsFromTestResults(passedTests));
        IResultMap failedTests = testContext.getFailedTests();
        tests.addAll(getXrayTestsFromTestResults(failedTests));
        xraySchema.setTests(tests);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper
                .writerWithDefaultPrettyPrinter()
                .without(WRITE_EMPTY_JSON_ARRAYS)
                .without(WRITE_NULL_MAP_VALUES)
                .writeValueAsString(xraySchema);
    }

    private static synchronized List<XrayTest> getXrayTestsFromTestResults(
            IResultMap testNgResults) {
        return
                testNgResults
                        .getAllResults()
                        .stream()
                        .map(XrayJsonService::getXrayTestFromTestResult)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
    }

    private static synchronized XrayTest getXrayTestFromTestResult(ITestResult testResult) {
        Xray annotation = testResult.getMethod().getConstructorOrMethod().getMethod()
                .getAnnotation(Xray.class);
        XrayTest test = null;
        ArrayList<XrayTest> tests = new ArrayList<>();
        if (Objects.nonNull(annotation)) {
            test = new XrayTest();
            String xrayStatus = getXrayStatus(testResult.getStatus());
            boolean completeDetails;
            switch (ContextResult.Status.fromValue(xrayStatus)) {
                case PASSED:
                case FAILED:
                    completeDetails = true;
                    break;
                case EXECUTING:
                case TODO:
                default:
                    completeDetails = false;
            }
            test.setStatus(xrayStatus);
            test.setTestKey(annotation.id());
            if (completeDetails) {
                test.setStart(new Date(testResult.getStartMillis()));
                test.setFinish(new Date(testResult.getEndMillis()));
                if (xrayStatus.equalsIgnoreCase("FAILED")) {
                    test.setComment(
                            "Error Message :".concat("\n\n").concat(
                                            ExceptionUtils.getStackTrace(testResult.getThrowable())));
                } else {
                    test.setComment(xrayConfig.executionComment());
                }
            }
        }
        return test;
    }

    static synchronized void uploadTestStatusToJira(ITestResult result, ContextResult.Status status)
            throws JsonProcessingException {
        String results = getTestStatusRequestBody(result, status);
        uploadResultsInXRayJsonFormat(results);
    }

    private static synchronized String getTestStatusRequestBody(ITestResult result, ContextResult.Status status)
            throws JsonProcessingException {
        XraySchema xraySchema = getXraySchema();
        List<XrayTest> tests = new ArrayList<>();
        XrayTest xrayTest = getXrayTestFromTestResult(result);
        if (Objects.nonNull(xrayTest)) {
            xrayTest.setStatus(String.valueOf(status));
            tests.add(xrayTest);
        }
        xraySchema.setTests(tests);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper
                .writerWithDefaultPrettyPrinter()
                .without(WRITE_EMPTY_JSON_ARRAYS)
                .without(WRITE_NULL_MAP_VALUES)
                .writeValueAsString(xraySchema);
    }

    private static synchronized XraySchema getXraySchema() {
        XraySchema xraySchema = new XraySchema();
        xraySchema.setTestExecutionKey(xrayConfig.testExecutionKey());
        return xraySchema;
    }

    private static synchronized void uploadResultsInXRayJsonFormat(String results)
            throws JsonProcessingException {
        try{
            String authorization = "Bearer " + XrayAuthenticationService.getInstance();
            //@formatter:off
            given()
                    .log().ifValidationFails()
                    .body(results)
                    .header("Authorization", authorization)
                    .contentType(ContentType.JSON)
                    .when()
                    .post(xrayConfig.xRayJsonImportResultsUrl())
                    .then()
                    .log().ifError()
                    .statusCode(200);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private static synchronized String getXrayStatus(int status) {
        switch (status) {
            case ITestResult.CREATED:
            case ITestResult.STARTED:
                return "EXECUTING";
            case ITestResult.SUCCESS:
                return "PASSED";
            case ITestResult.FAILURE:
            case ITestResult.SUCCESS_PERCENTAGE_FAILURE:
                return "FAILED";
            case ITestResult.SKIP:
            default:
                return "TODO";
        }
    }
}
