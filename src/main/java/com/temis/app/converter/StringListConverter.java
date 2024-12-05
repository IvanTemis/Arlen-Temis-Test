package com.temis.app.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.List;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> list) {
        if(list == null) return null;
        return String.join(",", list);
    }

    @Override
    public List<String> convertToEntityAttribute(String joined) {
        if(joined == null) return null;
        return Arrays.stream(joined.split(",")).toList();
    }

}
