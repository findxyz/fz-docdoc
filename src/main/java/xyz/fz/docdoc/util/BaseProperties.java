package xyz.fz.docdoc.util;

import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

public class BaseProperties {
    private static Properties properties;

    static {
        try {
            properties = PropertiesLoaderUtils.loadAllProperties("application.properties");
        } catch (IOException ignore) {
            // ignore
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }
}
