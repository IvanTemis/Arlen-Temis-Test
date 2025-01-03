package com.temis.app.exception;

public class JSONNotFoundException extends Exception{
    public JSONNotFoundException(String errorMessage){
        super(errorMessage);
    }
}
