package xray;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Objects;

import static io.restassured.RestAssured.given;

class XrayAuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(XrayAuthenticationService.class);
    private static String authenticationToken = null;

    private XrayAuthenticationService() {
    }

    static synchronized String getInstance() {
        if (Objects.isNull(authenticationToken)) {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            authenticationToken = getAuthenticationToken();
            stopWatch.stop();
            log.debug("Generated authentication token in {} seconds", stopWatch.getTime() / 1000);
            return authenticationToken;
        }
        return authenticationToken;
    }

    private static String getAuthenticationToken() {
        try {
            XrayConfig xrayConfig = XrayConfigManager.getInstance();
            HashMap<String, String> authRequest = new HashMap<>();
            authRequest.put("client_id", xrayConfig.clientId());
            authRequest.put("client_secret", xrayConfig.clientSecret());

            String token = "";
            token =
                    //@formatter:off
                    given()
                            .log().ifValidationFails()
                            .contentType("application/json")
                            .request().body(authRequest)
                            .when()
                            .post(xrayConfig.authenticateUrl())
                            .then()
                            .statusCode(200)
                            .extract().asString();
            //@formatter:on
            return token.replaceAll("^\"|\"$", "");
        } catch (Exception e) {
            e.printStackTrace();
            log.debug("Error fetching authentication details");
            throw e;
        }
    }
}
