package com.temis.app.exception;

public class PlaceholderNotFoundException extends Exception{
    public PlaceholderNotFoundException(String errorMessage){
        super(errorMessage);
    }
}
