package com.temis.app.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextUtils {

    private TextUtils() {
        
    }

    public static List<String> splitIntoSentences(String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(text.split("\\n"))
                .map(String::trim)
                .filter(sentence -> !sentence.isEmpty())
                //.map(sentence -> sentence + ".")
                .toList();
    }
}