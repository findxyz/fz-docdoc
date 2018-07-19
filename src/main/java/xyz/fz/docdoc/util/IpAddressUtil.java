package xyz.fz.docdoc.util;

import io.vertx.core.http.HttpServerRequest;
import org.apache.commons.lang3.StringUtils;

public class IpAddressUtil {
    private static final String UNKNOWN = "unknown";

    public static String getIpAddress(HttpServerRequest request) {
        String[] ipHeaders = new String[]{
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };
        for (String ipHeader : ipHeaders) {
            String ip = request.getHeader(ipHeader);
            if (ipOk(ip)) {
                return ipFormat(ip);
            }
        }
        return StringUtils.defaultIfBlank(request.remoteAddress().host(), UNKNOWN);
    }

    private static boolean ipOk(String ip) {
        return StringUtils.isNotBlank(ip) && !UNKNOWN.equalsIgnoreCase(ip);
    }

    private static String ipFormat(String okIp) {
        int index = okIp.indexOf(",");
        if (index != -1) {
            return okIp.substring(0, index);
        } else {
            return okIp;
        }
    }
}
