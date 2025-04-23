package com.microblog.app.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HashtagUtil {

    public static String linkifyHashtags(String text) {
        if (text == null) return "";
        Pattern pattern = Pattern.compile("#(\\w+)");
        Matcher matcher = pattern.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String tag = matcher.group(1);
            matcher.appendReplacement(result,
                    "<a href='/hashtag/" + tag + "' class='text-decoration-none'>#" + tag + "</a>");
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
