package xyz.fz.docdoc.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class BaseUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return "{\"success\": false, \"message\": \"json serial error\"}";
        }
    }

    public static <T> T parseJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            return null;
        }
    }

    public static String getExceptionStackTrace(Throwable e) {

        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw, true);
            e.printStackTrace(pw);
            return sw.toString();
        } finally {
            try {
                if (sw != null) {
                    sw.close();
                }
                if (pw != null) {
                    pw.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static String urlLoading(String url) {
        String html = "<!DOCTYPE html>" +
                "<html lang='en'>" +
                "<head>" +
                "<meta charset='UTF-8'/>" +
                "<title>loading</title>" +
                "</head>" +
                "<body>" +
                "<script type='text/javascript'>" +
                "window.location = '#{url}';" +
                "</script>" +
                "</body>" +
                "</html>";
        return html.replace("#{url}", url);
    }
}
