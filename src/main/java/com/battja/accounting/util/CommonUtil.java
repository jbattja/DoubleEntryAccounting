package com.battja.accounting.util;

import org.springframework.lang.NonNull;

public class CommonUtil {

    public static String enumNameToString(@NonNull String enumValue) {
        char first = enumValue.charAt(0);
        enumValue = enumValue.toLowerCase().replaceAll("_"," ");
        return first + enumValue.substring(1);
    }

}
