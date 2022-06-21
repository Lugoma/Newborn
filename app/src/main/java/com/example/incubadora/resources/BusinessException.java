package com.example.incubadora.resources;

import android.content.Context;
import android.widget.Toast;

public class BusinessException extends Exception {

    private final String message;
    private Context context;

    public BusinessException(String message, Context context) {
        this.message = message;
        this.context = context;
    }

    public BusinessException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void showErrorMessage() {

        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public boolean emptyContext() {
        return context == null;
    }
}
