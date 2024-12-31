package xray;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.*;
import xray.model.ContextResult;
import xray.model.Xray;

import java.util.Objects;

public class XrayListener implements IInvokedMethodListener, ITestListener {

    private static final Logger log = LoggerFactory.getLogger(XrayListener.class);

    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method.isTestMethod() && annotationPresent(method)) {
            testResult.setAttribute("requirement",
                    method.getTestMethod().getConstructorOrMethod().getMethod().getAnnotation(
                            Xray.class).requirement());
            testResult.setAttribute("test",
                    method.getTestMethod().getConstructorOrMethod().getMethod().getAnnotation(
                            Xray.class).id());
            testResult.setAttribute("labels",
                    method.getTestMethod().getConstructorOrMethod().getMethod().getAnnotation(
                            Xray.class).labels());
        }
    }

    private boolean annotationPresent(IInvokedMethod method) {
        return
                method.getTestMethod().getConstructorOrMethod().getMethod()
                        .isAnnotationPresent(Xray.class);
    }

    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        // Do nothing
    }

    public void onTestStart(ITestResult result) {
        uploadRealTimeStatus(result, ContextResult.Status.EXECUTING, "onTestStart");
    }

    public void onTestSuccess(ITestResult result) {
        uploadRealTimeStatus(result, ContextResult.Status.PASSED, "onTestSuccess");
    }

    public void onTestFailure(ITestResult result) {
        uploadRealTimeStatus(result, ContextResult.Status.FAILED, "onTestFailure");
    }

    public void onTestSkipped(ITestResult result) {
        uploadRealTimeStatus(result, ContextResult.Status.TODO, "onTestSkipped");
    }

    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        uploadRealTimeStatus(result, ContextResult.Status.FAILED, "onTestFailedButWithinSuccessPercentage");
    }

    public void onStart(ITestContext context) {
    }

    public void onFinish(ITestContext context) {
        try {
            if (requiresUploadButNotInRealTime()) {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                XrayJsonService.uploadFinalTestSuiteStatusToJira(context);
                stopWatch.stop();
            }

        } catch (Throwable ignoreThrowable) {
            log.debug("Exception in XrayListner onFinish", ignoreThrowable);
        }
    }

    private boolean requiresUpload() {
        XrayConfig xRayConfig = XrayConfigManager.getInstance();
        return Objects.nonNull(xRayConfig) && xRayConfig.upload();
    }

    private boolean requiresUploadButNotInRealTime() {
        XrayConfig xRayConfig = XrayConfigManager.getInstance();
        return Objects.nonNull(xRayConfig) && xRayConfig.upload() && !xRayConfig.uploadInRealTime();
    }

    private boolean requiresUploadInRealTime() {
        try {
            XrayConfig xRayConfig = XrayConfigManager.getInstance();
            return Objects.nonNull(xRayConfig) && xRayConfig.upload() && xRayConfig
                    .uploadInRealTime();
        } catch (Throwable ignoreThrowable) {
            log.debug("Exception in Xray Service requiresUploadInRealTime", ignoreThrowable);
            return false;
        }
    }

    private void uploadRealTimeStatus(ITestResult result, ContextResult.Status status,
                                      String event) {
        try {
            XrayConfig xRayConfig = XrayConfigManager.getInstance();
            if (requiresUploadInRealTime()) {
                boolean upload = true;
                switch (status) {
                    case TODO:
                        upload = xRayConfig.uploadTodoStatus();
                        break;
                    case EXECUTING:
                        upload = xRayConfig.uploadExecutingStatus();
                        break;
                    case PASSED:
                    case FAILED:
                    default:
                        // using already assigned value of true for upload
                        break;
                }
                if (upload) {
                    StopWatch stopWatch = new StopWatch();
                    stopWatch.start();
                    XrayJsonService.uploadTestStatusToJira(result, status);
                    stopWatch.stop();
                }
            }
        } catch (Throwable ignoreThrowable) {
            log.debug("Exception in XrayListner {}", event, ignoreThrowable);
        }
    }
}
