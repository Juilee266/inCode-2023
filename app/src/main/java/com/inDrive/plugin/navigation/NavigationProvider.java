package com.inDrive.plugin.navigation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.Task;
import com.inDrive.plugin.navigation.graphhopper.GraphhopperClient;
import com.inDrive.plugin.navigation.graphhopper.response.direction.DirectionResponse;
import com.inDrive.plugin.navigation.graphhopper.response.geocode.GeocodeResponse;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class NavigationProvider {
    private static final String TAG = "NavigationProvider";

    private FusedLocationProviderClient fusedLocationProviderClient;

    private GraphhopperClient graphhopperClient = GraphhopperClient.getInstance();

    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    public NavigationProvider(Context context) {
        fusedLocationProviderClient = new FusedLocationProviderClient(context);
    }

    @SuppressLint("MissingPermission")
    public Optional<Location> getLastLocation() {
        try {
            Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
            locationTask.wait();

            if (!locationTask.isSuccessful()) {
                Log.e(TAG, "Get last location task unsuccessful.");
                return Optional.empty();
            }

            return Optional.of(locationTask.getResult());
        } catch (InterruptedException ex) {
            Log.e(TAG, "Get last location task interrupted.", ex);
        }

        return Optional.empty();
    }

    public Optional<Location> getLocation(String place) {
        Optional<GeocodeResponse> geocodeResponseOptional = graphhopperClient.getGeocode(place);
        if (!geocodeResponseOptional.isPresent())
            return Optional.empty();

        GeocodeResponse geocodeResponse = geocodeResponseOptional.get();

        Location location = new Location("");
        location.setLatitude(geocodeResponse.getHits().get(0).getPoint().getLatitude());
        location.setLongitude(geocodeResponse.getHits().get(0).getPoint().getLongitude());

        return Optional.of(location);
    }

    public Optional<DirectionResponse> getDirections(String from, String to) {
        try {
            Future<Optional<Location>> fromFuture = executorService.submit(() -> getLocation(from));
            Future<Optional<Location>> toFuture = executorService.submit(() -> getLocation(to));

            Optional<Location> fromLocationOptional = fromFuture.get();
            Optional<Location> toLocationOptional = toFuture.get();

            if (!fromLocationOptional.isPresent() || !toLocationOptional.isPresent())
                return Optional.empty();

            Location fromLocation = fromLocationOptional.get();
            Location toLocation = toLocationOptional.get();

            List<List<Double>> locations = List.of(
                    List.of(fromLocation.getLongitude(), fromLocation.getLatitude()),
                    List.of(toLocation.getLongitude(), toLocation.getLatitude())
            );

            return graphhopperClient.getDirections(locations);
        } catch (Exception ex) {
            Log.d(
                    TAG,
                    String.format("Error fetching directions from %s to %s", from, to),
                    ex
            );
        }

        return Optional.empty();
    }

    public void checkNavigation() {
        // TODO: Handle navigation
        //fusedLocationProviderClient.get
    }
}
