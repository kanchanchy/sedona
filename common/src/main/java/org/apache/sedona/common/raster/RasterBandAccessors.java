/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sedona.common.raster;

import org.apache.sedona.common.utils.RasterUtils;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridCoverage2D;

import java.awt.image.Raster;

public class RasterBandAccessors {

    public static Double getBandNoDataValue(GridCoverage2D raster, int band) {
        RasterUtils.ensureBand(raster, band);
        GridSampleDimension bandSampleDimension = raster.getSampleDimension(band - 1);
        double noDataValue = RasterUtils.getNoDataValue(bandSampleDimension);
        if (Double.isNaN(noDataValue)) {
            return null;
        } else {
            return noDataValue;
        }
    }

    public static Double getBandNoDataValue(GridCoverage2D raster) {
        return getBandNoDataValue(raster, 1);
    }

    public static long getCount(GridCoverage2D raster, int band, boolean excludeNoDataValue) {
        int height = RasterAccessors.getHeight(raster), width = RasterAccessors.getWidth(raster);
        if(excludeNoDataValue) {
            RasterUtils.ensureBand(raster, band);
            long numberOfPixel = 0;
            Double bandNoDataValue = RasterBandAccessors.getBandNoDataValue(raster, band);

            for(int j = 0; j < height; j++){
                for(int i = 0; i < width; i++){

                    double[] bandPixelValues = raster.evaluate(new GridCoordinates2D(i, j), (double[]) null);
                    double bandValue = bandPixelValues[band - 1];
                    if(bandNoDataValue == null || bandValue != bandNoDataValue){
                        numberOfPixel += 1;
                    }
                }
            }
            return numberOfPixel;
        } else {
            // code for false
            return width * height;
        }
    }

    public static long getCount(GridCoverage2D raster) {
        return getCount(raster, 1, true);
    }

    public static long getCount(GridCoverage2D raster, int band) {
        return getCount(raster, band, true);
    }

//    Removed for now as it InferredExpression doesn't support function with same arity but different argument types
//    Will be added later once it is supported.
//    public static Integer getCount(GridCoverage2D raster, boolean excludeNoDataValue) {
//        return getCount(raster, 1, excludeNoDataValue);
//    }

    public static double[] getSummaryStats(GridCoverage2D rasterGeom, int band, boolean excludeNoDataValue) {
        RasterUtils.ensureBand(rasterGeom, band);
        Raster raster = rasterGeom.getRenderedImage().getData();
        int height = RasterAccessors.getHeight(rasterGeom), width = RasterAccessors.getWidth(rasterGeom);
        double[] pixels = raster.getSamples(0, 0, width, height, band - 1, (double[]) null);
        double count = 0, sum = 0, mean = 0, stddev = 0, min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
        Double noDataValue = RasterBandAccessors.getBandNoDataValue(rasterGeom, band);
        for (double pixel: pixels) {
            if(excludeNoDataValue) {
            // exclude no data values
                if (noDataValue == null || pixel != noDataValue) {
                    count++;
                    sum += pixel;
                    min = Math.min(min, pixel);
                    max = Math.max(max, pixel);
                }
            } else {
                // include no data values
                count = pixels.length;
                sum += pixel;
                min = Math.min(min, pixel);
                max = Math.max(max, pixel);
            }
        }
        if (count == 0) {
            return new double[] {0, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN};
        }
        mean = sum / count;
        for(double pixel: pixels){
            if (excludeNoDataValue){
                if (noDataValue == null || pixel != noDataValue) {
                    stddev += Math.pow(pixel - mean, 2);
                }
            } else {
                stddev += Math.pow(pixel - mean, 2);
            }
        }
        stddev = Math.sqrt(stddev/count);
        return new double[]{count, sum, mean, stddev, min, max};
    }

    public static double[] getSummaryStats(GridCoverage2D raster, int band) {
        return getSummaryStats(raster, band, true);
    }

    public static double[] getSummaryStats(GridCoverage2D raster) {
        return getSummaryStats(raster, 1, true);
    }

//  Adding the function signature when InferredExpression supports function with same arity but different argument types
//    public static double[] getSummaryStats(GridCoverage2D raster, boolean excludeNoDataValue) {
//        return getSummaryStats(raster, 1, excludeNoDataValue);
//    }

    public static String getBandType(GridCoverage2D raster, int band) {
        RasterUtils.ensureBand(raster, band);
        GridSampleDimension bandSampleDimension = raster.getSampleDimension(band - 1);
        return bandSampleDimension.getSampleDimensionType().name();
    }

    public static String getBandType(GridCoverage2D raster){
        return getBandType(raster, 1);
    }
}
