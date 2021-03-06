package org.apache.isis.subdomains.base.applib.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;

public final class MessageUtils {

    private MessageUtils(){}
    private static final Pattern pattern = Pattern.compile(".*Reason: (.+?)[ ]*Identifier:.*");

    public static String normalize(final Exception ex) {
        if(ex == null || Strings.isNullOrEmpty(ex.getMessage())) {
            return null;
        }
        String message = ex.getMessage();
        final Matcher matcher = pattern.matcher(message);
        if(matcher.matches()) {
            return matcher.group(1);
        }
        return message;
    }
}
