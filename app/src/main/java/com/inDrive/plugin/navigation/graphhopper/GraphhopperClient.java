package com.inDrive.plugin.navigation.graphhopper;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inDrive.plugin.common.httpclient.HttpClient;
import com.inDrive.plugin.navigation.graphhopper.request.DirectionRequest;
import com.inDrive.plugin.navigation.graphhopper.request.GeocodeRequest;
import com.inDrive.plugin.navigation.graphhopper.response.direction.DirectionResponse;
import com.inDrive.plugin.navigation.graphhopper.response.geocode.GeocodeResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GraphhopperClient {
    private static final class GraphhopperClientHelper {
        private static final GraphhopperClient INSTANCE = new GraphhopperClient();
    }

    private HttpClient httpClient;
    private ExecutorService executorService;
    private ObjectMapper objectMapper;

    private static final String API_KEY = "4cc2efbf-7228-4a43-b199-d6618f9cae7f";
    private static final String BASE_URL = "https://graphhopper.com/api/1";
    private static final String TAG = "GraphhopperClient";

    private GraphhopperClient() {
        httpClient = HttpClient.getInstance();
        executorService = Executors.newSingleThreadExecutor();
        objectMapper = new ObjectMapper();
    }

    public static GraphhopperClient getInstance() {
        return GraphhopperClientHelper.INSTANCE;
    }

    public Optional<DirectionResponse> getDirections(List<List<Double>> locations) {
        String url = this.getUrl("route");
        DirectionRequest request = getDirectionRequest(locations);
        try {
            String body = objectMapper.writeValueAsString(request);
            Future<DirectionResponse> resultFuture = executorService.submit(new Callable<DirectionResponse>() {
                @Override
                public DirectionResponse call() throws Exception {
                    String responseJson = httpClient.post(url, body);
                    return objectMapper.readValue(responseJson, DirectionResponse.class);
                }
            });
            return Optional.of(resultFuture.get());
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }

        return Optional.empty();
    }

    public Optional<GeocodeResponse> getGeocode(String place) {
        String url = String.format(
                "%s&%s",
                this.getUrl("geocode"),
                this.getGeocodeRequest(place)
        );

        try {
            Future<GeocodeResponse> resultFuture = executorService.submit(new Callable<GeocodeResponse>() {
                @Override
                public GeocodeResponse call() throws Exception {
                    String responseJson = httpClient.get(url);
                    return objectMapper.readValue(responseJson, GeocodeResponse.class);
                }
            });
            GeocodeResponse result = resultFuture.get();
            Log.d(TAG, "Geocode Result: " + result);

            return Optional.of(result);
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }

        return Optional.empty();
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
        request.setInstructions(false);
        request.setCalcPoints(true);
        request.setPointsEncoded(false);
        request.setAlgorithm("alternative_route");
        return request;
    }

    private GeocodeRequest getGeocodeRequest(String place) {
        GeocodeRequest request = new GeocodeRequest();
        request.setLimit(1);
        request.setLocale("en");
        request.setProvider("default");
        request.setQuery(place);
        return request;
    }
}
