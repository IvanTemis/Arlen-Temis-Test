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
        return Arrays.stream(text.split("\\?LL\\?"))
                .map(String::trim)
                .filter(sentence -> !sentence.isEmpty())
                .map(s -> forceSplitIntoSentences(s, 0))
                .flatMap(List::stream)
                //.map(sentence -> sentence + ".")
                .toList();
    }

    private static List<String> forceSplitIntoSentences(String text, int depth){
        if(text.length() < 1500){
            return List.of(text);
        }

        switch (depth){
            case 0:{
                var split = text.split("(?<!.)\\n");

                return Arrays.stream(split).map(string -> forceSplitIntoSentences(string, 1)).flatMap(List::stream).toList();
            }
            case 1:{
                var split = text.split("\\n");
                return Arrays.stream(split).map(string -> forceSplitIntoSentences(string, 2)).flatMap(List::stream).toList();
            }
            case 2:{
                var split = text.split("\\.");

                int half = (int) Math.ceil(split.length / 2.0);

                var first = Arrays.copyOfRange(split, 0, half);
                var second = Arrays.copyOfRange(split, half, split.length);

                List<String> splitJoined = List.of(String.join(".", first) + ".", String.join(".", second) + ".");

                return splitJoined.stream().map(string -> forceSplitIntoSentences(string, 2)).flatMap(List::stream).toList();
            }
            default:
        }

        var split = text.split(" ");
        int half = (int) Math.ceil(split.length / 2.0);

        var first = Arrays.copyOfRange(split, 0, half);
        var second = Arrays.copyOfRange(split, half, split.length);

        List<String> splitJoined = List.of(String.join(" ", first), String.join(" ", second));

        return splitJoined.stream().map(string -> forceSplitIntoSentences(string, 3)).flatMap(List::stream).toList();
    }
}