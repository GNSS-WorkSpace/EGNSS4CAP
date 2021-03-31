/*
 * Copyright 2018 TFI Systems

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

package eu.foxcom.gnss_compare_core.Corrections;

import android.location.Location;

import com.galfins.gogpsextracts.Constants;
import com.galfins.gogpsextracts.Coordinates;
import com.galfins.gogpsextracts.NavigationProducer;
import com.galfins.gogpsextracts.SatellitePosition;
import com.galfins.gogpsextracts.Time;
import com.galfins.gogpsextracts.TopocentricCoordinates;


public class TropoCorrection extends Correction {

    private final static String NAME = "Tropospheric correction";

    private double correctionValue;
    @Override
    public void calculateCorrection(Time currentTime, Coordinates approximatedPose, SatellitePosition satelliteCoordinates, NavigationProducer navigationProducer, Location initialLocation) {

        // Compute also the geodetic version of the user position (latitude, longitude, height)
        approximatedPose.computeGeodetic();

        // Get the user's height
        double height = approximatedPose.getGeodeticHeight();

        // Compute the elevation and azimuth angles for each satellite
        TopocentricCoordinates topo = new TopocentricCoordinates();
        topo.computeTopocentric(approximatedPose, satelliteCoordinates);

        // Assign the elevation information to a new variable
        double elevation = topo.getElevation();

        double tropoCorr = 0;

        if (height > 5000)
            return;

        elevation = Math.toRadians(Math.abs(elevation));
        if (elevation == 0){
            elevation = elevation + 0.01;
        }

        // Numerical constants and tables for Saastamoinen algorithm
        // (troposphere correction)
        final double hr = 50.0;
        final int[] ha = {0, 500, 1000, 1500, 2000, 2500, 3000, 4000, 5000 };
        final double[] ba = { 1.156, 1.079, 1.006, 0.938, 0.874, 0.813, 0.757, 0.654, 0.563 };

        // Saastamoinen algorithm
        double P = Constants.STANDARD_PRESSURE * Math.pow((1 - 0.0000226 * height), 5.225);
        double T = Constants.STANDARD_TEMPERATURE - 0.0065 * height;
        double H = hr * Math.exp(-0.0006396 * height);

        // If height is below zero, keep the maximum correction value
        double B = ba[0];
        // Otherwise, interpolate the tables
        if (height >= 0) {
            int i = 1;
            while (height > ha[i]) {
                i++;
            }
            double m = (ba[i] - ba[i - 1]) / (ha[i] - ha[i - 1]);
            B = ba[i - 1] + m * (height - ha[i - 1]);
        }

        double e = 0.01
                * H
                * Math.exp(-37.2465 + 0.213166 * T - 0.000256908
                * Math.pow(T, 2));

        tropoCorr = ((0.002277 / Math.sin(elevation))
                * (P - (B / Math.pow(Math.tan(elevation), 2))) + (0.002277 / Math.sin(elevation))
                * (1255 / T + 0.05) * e);

        correctionValue = tropoCorr;

    }

    @Override
    public double getCorrection() {
        return correctionValue;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public static void registerClass(){
        register(NAME, TropoCorrection.class);
    }
}

/**
 * Created for the GSA in 2020-2021. Project management: SpaceTec Partners, software development: www.foxcom.eu
 */
