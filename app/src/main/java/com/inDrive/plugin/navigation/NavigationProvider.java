package com.inDrive.plugin.navigation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.inDrive.plugin.entities.Location;
import com.inDrive.plugin.entities.LocationCoordinate;
import com.inDrive.plugin.navigation.graphhopper.GraphhopperClient;
import com.inDrive.plugin.navigation.graphhopper.response.direction.DirectionResponse;
import com.inDrive.plugin.navigation.graphhopper.response.geocode.Geocode;
import com.inDrive.plugin.navigation.graphhopper.response.geocode.GeocodeResponse;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import opennlp.tools.util.StringUtil;

public class NavigationProvider {
    private static final String TAG = "NavigationProvider";

    private FusedLocationProviderClient fusedLocationProviderClient;

    private GraphhopperClient graphhopperClient = GraphhopperClient.getInstance();

    private ExecutorService executorService = Executors.newFixedThreadPool(5);

    public NavigationProvider(Context context) {
        fusedLocationProviderClient = new FusedLocationProviderClient(context);
    }

    @SuppressLint("MissingPermission")
    public Optional<Location> getCurrentLocation() {
        try {
            Task<android.location.Location> locationTask = fusedLocationProviderClient.getLastLocation();

            Future<android.location.Location> fusedLocationFuture = executorService.submit(() -> Tasks.await(locationTask));
            android.location.Location fusedLocation = fusedLocationFuture.get();

            return getLocation(fusedLocation.getLatitude(), fusedLocation.getLongitude());
        } catch (Exception ex) {
            Log.e(TAG, "Get last location task interrupted.", ex);
        }

        return Optional.empty();
    }

    public Optional<Location> getLocation(String place) {
        Optional<GeocodeResponse> geocodeResponseOptional = graphhopperClient.getGeocode(place);
        if (!geocodeResponseOptional.isPresent())
            return Optional.empty();

        GeocodeResponse geocodeResponse = geocodeResponseOptional.get();

        Geocode geoCode = geocodeResponse.getHits().get(0);
        Location location =  new Location();
        if (!StringUtil.isEmpty(geoCode.getName()))
            location.setLocationName(geoCode.getName());
        else
            location.setLocationName(geoCode.getStreet());
        location.setLocationCoordinates(new LocationCoordinate(geoCode.getPoint().getLatitude(), geoCode.getPoint().getLongitude()));

        return Optional.of(location);
    }

    public Optional<Location> getLocation(double latitude, double longitude) {
        Optional<GeocodeResponse> geocodeResponseOptional = graphhopperClient.getGeocode(latitude, longitude);
        if (!geocodeResponseOptional.isPresent())
            return Optional.empty();

        GeocodeResponse geocodeResponse = geocodeResponseOptional.get();
        if(geocodeResponse.getHits().isEmpty()) {
            return null;
        }
        Geocode geoCode = geocodeResponse.getHits().get(0);
        Location location =  new Location();
        if (!StringUtil.isEmpty(geoCode.getName()))
            location.setLocationName(geoCode.getName());
        else
            location.setLocationName(geoCode.getStreet());
        location.setLocationCoordinates(new LocationCoordinate(geoCode.getPoint().getLatitude(), geoCode.getPoint().getLongitude()));

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
                    List.of(fromLocation.getLocationCoordinates().getLongitude(), fromLocation.getLocationCoordinates().getLatitude()),
                    List.of(toLocation.getLocationCoordinates().getLongitude(), toLocation.getLocationCoordinates().getLatitude())
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
