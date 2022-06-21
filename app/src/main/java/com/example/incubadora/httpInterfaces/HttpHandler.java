package com.example.incubadora.httpInterfaces;
import android.content.Context;

import com.example.incubadora.resources.BusinessException;
import com.example.incubadora.resources.URLS;
import com.loopj.android.http.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import cz.msebera.android.httpclient.Header;


public class HttpHandler {

    private static final AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void patch(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.patch(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void delete(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.delete(getAbsoluteUrl(url), params, responseHandler);
    }

    /**
     * Method to get server's absolute URL
     * @param relativeUrl
     * @return
     */
    private static String getAbsoluteUrl(String relativeUrl) {
        return URLS.SERVER_IP + URLS.BASE_URL + relativeUrl;
    }

    /**
     * Method to make a get request for a singular object
     * @param relativeUrl
     * @param callback
     */
    public static void simpleObjectGETRequest(String relativeUrl, JSONObjectCallback callback, Context context) {

        HttpHandler.get(relativeUrl, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Object data = new JSONTokener(response.get("data").toString()).nextValue();
                    if(data == null)
                        throw new BusinessException("", context);
                    else
                        callback.onSuccess((JSONObject) data);
                } catch (Exception e) {
                    BusinessException businessException = new BusinessException("No se pudo conectar con el servidor", context);
                    businessException.showErrorMessage();
                }
            }
        });
    }

    /**
     * Method to make get request for an array of objects
     * @param relativeUrl
     * @param callback
     */
    public static void arrayObjectGETRequest(String relativeUrl, JSONArrayCallback callback, Context context) {

        HttpHandler.get(relativeUrl, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try{
                    Object data = new JSONTokener(response.get("data").toString()).nextValue();
                    if(data != null) {
                        callback.onSuccess((JSONArray) data);
                    } else {
                        throw new BusinessException("", context);
                    }
                } catch (Exception e) {
                    BusinessException businessException = new BusinessException("No se pudo conectar con el servidor", context);
                    businessException.showErrorMessage();
                }
            }
        });


    }
}
