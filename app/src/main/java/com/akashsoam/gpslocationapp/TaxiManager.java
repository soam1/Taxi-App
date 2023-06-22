package com.akashsoam.gpslocationapp;

import android.location.Location;

public class TaxiManager {
    private Location destinationLocation;

    public void setDestinationLocation(Location destinationLocation) {
        this.destinationLocation = destinationLocation;
    }

    public float returnTheDistanceToDestinationLocationInMetres(Location currLocation) {
        if (currLocation != null && destinationLocation != null) {
            return currLocation.distanceTo(destinationLocation);
        }
        return -100.0f;

    }

    public String returnTheMilesBetweenCurrLocationAndDestLocation(Location currLocation, int metresPerMile) {
        int miles = (int) returnTheDistanceToDestinationLocationInMetres(currLocation) / metresPerMile;
        if (miles == 1) return "1 Mile";
        else if (miles > 1) {
            return miles + " miles";
        } else {
            return "no miles";
        }
    }

    public String returnTheTimeLeftToGetDestinationLocation(Location currLocation, float milesPerHour, int metresPerMile) {
        float distanceInMetres = returnTheDistanceToDestinationLocationInMetres(currLocation);
        float timeLeft = distanceInMetres / (milesPerHour * metresPerMile);// in hours
        String timeResult = "";
        int timeLeftInHours = (int) timeLeft;
        if (timeLeftInHours == 1) {
            timeResult = timeResult + "1 Hour";
        } else if (timeLeftInHours > 1) {
            timeResult = timeResult + timeLeftInHours + " hours";
        }
        int minutesLeft = (int) ((timeLeft - timeLeftInHours) * 60);
        if (minutesLeft == 1) {
            timeResult = timeResult + " 1 minute";
        } else if (minutesLeft > 1) {
            timeResult += minutesLeft + " minutes";
        }
        if (timeLeftInHours <= 0 && minutesLeft < 1) {
            timeResult = "less than a minute left to get to the Destination location";
        }
        return timeResult;

    }
}
