package xyz.fz.test;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ATest {

    @Test
    public void restfulTest() {
        Pattern pattern = Pattern.compile("/rest/([^/]+)/ful/([^/]+)");
        Matcher matcher = pattern.matcher("/rest/zhangsan/ful/123");
        if (matcher.matches()) {
            System.out.println(matcher.group());
            System.out.println(matcher.group(1));
            System.out.println(matcher.group(2));
        }

        List<String> allFind = new ArrayList<>();
        Pattern pattern2 = Pattern.compile("(\\{[^}]+})");
        Matcher matcher2 = pattern2.matcher("/rest/{name}/ful/{id}");
        while (matcher2.find()) {
            allFind.add(matcher2.group());
        }
        System.out.println(allFind);

        System.out.println("/rest/{name}/ful/{id}".replaceAll("(\\{[^}]+})", "([^/]+)"));
        System.out.println("/rest/a/ful/b".replaceAll("(\\{[^}]+})", "([^/]+)"));

        System.out.println("/aaa/bbb?ccc=ddd".replaceAll("\\?.*", ""));
        System.out.println("/aaa/bbb/?ccc=ddd".replaceAll("\\?.*", ""));
        System.out.println("/aaa/bbb?".replaceAll("\\?.*", ""));
        System.out.println("/aaa/bbb/?".replaceAll("\\?.*", ""));

        System.out.println("/rest/{name}/ful/{id}".matches("/rest/([^/]+)/ful/([^/]+)"));
    }
}
