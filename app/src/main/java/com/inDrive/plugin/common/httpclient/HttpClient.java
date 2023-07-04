package com.inDrive.plugin.common.httpclient;

import android.util.Log;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpClient {
    private static class HttpClientHelper {
        private static final HttpClient INSTANCE = new HttpClient();
    };
    private OkHttpClient okHttpClient;

    private static final MediaType MEDIA_TYPE_JSON = MediaType.get("application/json; charset=utf-8");
    private static final String TAG = "HttpClient";

    private HttpClient() {
        okHttpClient = new OkHttpClient();
    }

    public static HttpClient getInstance() {
        return HttpClientHelper.INSTANCE;
    }

    public String get(String url) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Log.i(TAG, String.format("Initiating HTTP GET request to URL %s", url));
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.isSuccessful()) return response.body().string();

            Log.w(
                    TAG,
                    String.format(
                            "HTTP GET request failed with status %d and reason %s. URL: %s",
                            response.code(),
                            response.message(),
                            url
                    )
            );
        } catch (IOException ex) {
            Log.e(
                    TAG,
                    String.format("Exception while sending HTTP GET request to url: %s", url)
            );
        }

        return null;
    }

    public String post(String url, String requestBody) {
        RequestBody body = RequestBody.create(requestBody, MEDIA_TYPE_JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Log.i(TAG, String.format("Initiating HTTP POST request %s with body %s", request, request.body()));
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.isSuccessful()) return response.body().string();

            Log.w(
                    TAG,
                    String.format(
                            "HTTP POST request failed with status %d and reason %s. " +
                            "URL: %s, Request Body: %s",
                            response.code(),
                            response.message(),
                            url,
                            requestBody
                    )
            );
        } catch (IOException ex) {
            Log.e(
                    TAG,
                    String.format("Exception while sending HTTP POST request to url: %s and " +
                            "body: %s", url, requestBody)
            );
        }

        return null;
    }
}
