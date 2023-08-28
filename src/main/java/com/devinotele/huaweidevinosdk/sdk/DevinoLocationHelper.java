package com.devinotele.huaweidevinosdk.sdk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.huawei.hmf.tasks.Task;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;

class DevinoLocationHelper {

    private final FusedLocationProviderClient fusedLocationClient;
    private final Context context;
    volatile List<Location> locations = new ArrayList<>();
    private final LocationManager locationManager;
    private LocationListener locationListener;

    DevinoLocationHelper(Context ctx) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx);
        locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        this.context = ctx;
    }

    @SuppressLint("MissingPermission")
    Single<Location> getNewLocation() {
        fusedLocationClient.getLocationAvailability().addOnSuccessListener(loc -> {
            Log.d("DevinoPush", "DevinoLocationHelper isLocationAvailable=" + loc.isLocationAvailable());
        });

        return Single.create(e -> {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            ) {
                Task<Location> loc = fusedLocationClient.getLastLocation();
                loc.addOnSuccessListener(location -> {
                            if (location != null) {
                                Log.d("DevinoPush", "getNewLocation location=" + location);
                                e.onSuccess(location);
                            } else {
                                e.onError(new Throwable("Your location settings is turned off"));
                                Log.d("DevinoPush", "Your location settings is turned off");
                            }
                        }
                );
                loc.addOnFailureListener(exception -> {
                    Log.d("DevinoPush", "getLastLocation() exception: " + exception.getLocalizedMessage());
                    e.onError(new Throwable("getLastLocation() exception: " + exception.getLocalizedMessage()));
                });
            } else {
                e.onError(new Throwable("You haven't given the permissions"));
            }
        });
    }

    private void startUpdates() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        ) {
            return;
        }
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0,
                getLocationListener()
        );
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0,
                0,
                getLocationListener()
        );
    }

    private LocationListener getLocationListener() {
        if (locationListener == null) {
            return new LocationListener() {
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        locations.add(location);
                    }
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                public void onProviderEnabled(String provider) {
                }

                public void onProviderDisabled(String provider) {
                }
            };
        }
        else return locationListener;
    }

    private Location pickBestLocation(List<Location> locations) {

        if (locations.size() == 0) {
            return null;
        }

        Location bestLocation = null;

        for (Location location : locations) {
            if (bestLocation == null || location.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = location;
            }
        }
        return bestLocation;
    }
}