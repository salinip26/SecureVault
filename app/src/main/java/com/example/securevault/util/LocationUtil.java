package com.example.securevault.util;

import android.location.Location;

public class LocationUtil {

    // Method to calculate distance between two latitude and longitude points
    public static float calculateDistance(double currentLat, double currentLng, double targetLat, double targetLng) {
        float[] results = new float[1];
        Location.distanceBetween(currentLat, currentLng, targetLat, targetLng, results);
        return results[0];
    }

    // Method to check if current location is within a certain radius of target location
    public static boolean isWithinRadius(double currentLat, double currentLng, double targetLat, double targetLng, double radiusInMeters) {
        float distance = calculateDistance(currentLat, currentLng, targetLat, targetLng);
        return distance <= radiusInMeters;
    }
}
