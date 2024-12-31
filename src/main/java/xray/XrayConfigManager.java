package xray;

import org.aeonbits.owner.ConfigFactory;

import java.io.FileInputStream;
import java.util.Properties;

public class XrayConfigManager {

    private static final String USER_DIR = System.getProperty("user.dir");
    private static final String X_RAY_PROPERTIES_PATH =
            USER_DIR + "/src/test/resources/xRay.properties";
    private static XrayConfig xRayConfig;

    private XrayConfigManager() {
    }

    public static synchronized XrayConfig getInstance() {
        if (xRayConfig == null) {
            try {
                Properties xRayProps = new Properties();
                xRayProps.load(new FileInputStream(X_RAY_PROPERTIES_PATH));
                xRayConfig = ConfigFactory.create(XrayConfig.class,
                        System.getProperties(),
                        xRayProps
                );
            } catch (Exception e) {
                // ignore exception
            }
        }
        return xRayConfig;
    }
}
