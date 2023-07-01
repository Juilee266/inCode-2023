package com.inDrive.plugin.navigation.graphhopper;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inDrive.plugin.common.httpclient.HttpClient;
import com.inDrive.plugin.navigation.graphhopper.request.DirectionRequest;
import com.inDrive.plugin.navigation.graphhopper.response.direction.DirectionResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GraphhopperClient {
    private HttpClient httpClient;
    private ExecutorService executorService;
    private ObjectMapper objectMapper;

    private static final String API_KEY = "4cc2efbf-7228-4a43-b199-d6618f9cae7f";
    private static final String BASE_URL = "https://graphhopper.com/api/1";
    private static final String TAG = "GraphhopperClient";

    public GraphhopperClient() {
        httpClient = HttpClient.getInstance();
        executorService = Executors.newSingleThreadExecutor();
        objectMapper = new ObjectMapper();
    }

    public void GetDirections() {
        String url = this.getUrl("route");
        DirectionRequest request = getDirectionRequest(List.of(List.of(73.8265, 18.553), List.of(73.8561, 18.5164)));
        try {
            String body = objectMapper.writeValueAsString(request);
            Future<DirectionResponse> resultFuture = executorService.submit(new Callable<DirectionResponse>() {
                @Override
                public DirectionResponse call() throws Exception {
                    String responseJson = httpClient.post(url, body);
                    return objectMapper.readValue(responseJson, DirectionResponse.class);
                }
            });
            DirectionResponse result = resultFuture.get();
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }

    private String getUrl(String api) {
        return String.format("%s/%s?key=%s", BASE_URL, api, API_KEY);
    }

    private DirectionRequest getDirectionRequest(List<List<Double>> locations) {
        DirectionRequest request = new DirectionRequest();
        request.setPoints(locations);
        request.setSnapPreventions(List.of("motorway", "ferry", "tunnel"));
        request.setDetails(List.of("road_class", "surface"));
        request.setVehicle("bike");
        request.setLocale("en");
        request.setInstructions(true);
        request.setCalcPoints(true);
        request.setPointsEncoded(false);
        request.setAlgorithm("alternative_route");
        return request;
    }
}
