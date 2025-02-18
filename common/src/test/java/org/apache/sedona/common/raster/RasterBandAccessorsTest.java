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

import org.geotools.coverage.grid.GridCoverage2D;
import org.junit.Test;
import org.opengis.referencing.FactoryException;

import java.io.IOException;

import static org.junit.Assert.*;

public class RasterBandAccessorsTest extends RasterTestBase {

    @Test
    public void testBandNoDataValueCustomBand() throws FactoryException {
        int width = 5, height = 10;
        GridCoverage2D emptyRaster = RasterConstructors.makeEmptyRaster(1, width, height, 53, 51, 1, 1, 0, 0, 4326);
        double[] values = new double[width * height];
        for (int i = 0; i < values.length; i++) {
            values[i] = i + 1;
        }
        emptyRaster = MapAlgebra.addBandFromArray(emptyRaster, values, 2, 1d);
        assertNotNull(RasterBandAccessors.getBandNoDataValue(emptyRaster, 2));
        assertEquals(1, RasterBandAccessors.getBandNoDataValue(emptyRaster, 2), 1e-9);
        assertNull(RasterBandAccessors.getBandNoDataValue(emptyRaster));
    }

    @Test
    public void testBandNoDataValueDefaultBand() throws FactoryException {
        int width = 5, height = 10;
        GridCoverage2D emptyRaster = RasterConstructors.makeEmptyRaster(1, width, height, 53, 51, 1, 1, 0, 0, 4326);
        double[] values = new double[width * height];
        for (int i = 0; i < values.length; i++) {
            values[i] = i + 1;
        }
        emptyRaster = MapAlgebra.addBandFromArray(emptyRaster, values, 1, 1d);
        assertNotNull(RasterBandAccessors.getBandNoDataValue(emptyRaster));
        assertEquals(1, RasterBandAccessors.getBandNoDataValue(emptyRaster), 1e-9);
    }

    @Test
    public void testBandNoDataValueDefaultNoData() throws FactoryException {
        int width = 5, height = 10;
        GridCoverage2D emptyRaster = RasterConstructors.makeEmptyRaster(1,"I", width, height, 53, 51, 1, 1, 0, 0, 0);
        double[] values = new double[width * height];
        for (int i = 0; i < values.length; i++) {
            values[i] = i + 1;
        }
        assertNull(RasterBandAccessors.getBandNoDataValue(emptyRaster, 1));
    }

    @Test
    public void testBandNoDataValueIllegalBand() throws FactoryException, IOException {
        GridCoverage2D raster = rasterFromGeoTiff(resourceFolder + "raster/raster_with_no_data/test5.tiff");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> RasterBandAccessors.getBandNoDataValue(raster, 2));
        assertEquals("Provided band index 2 is not present in the raster", exception.getMessage());
    }

    @Test
    public void testSummaryStatsWithAllNoData() throws FactoryException {
        GridCoverage2D emptyRaster = RasterConstructors.makeEmptyRaster(1, 5, 5, 0, 0, 1, -1, 0, 0, 0);
        double[] values = new double[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        emptyRaster = MapAlgebra.addBandFromArray(emptyRaster, values, 1, 0d);
        double count = 0.0;
        double sum = Double.NaN;
        double mean = Double.NaN;
        double stddev = Double.NaN;
        double min = Double.NaN;
        double max = Double.NaN;
        double[] result = RasterBandAccessors.getSummaryStats(emptyRaster);
        assertEquals(count, result[0], 0.1d);
        assertEquals(sum, result[1], 0.1d);
        assertEquals(mean, result[2], 0.1d);
        assertEquals(stddev, result[3], 0.1d);
        assertEquals(min, result[4], 0.1d);
        assertEquals(max, result[5], 0.1d);
    }

    @Test
    public void testSummaryStatsWithEmptyRaster() throws FactoryException {
        GridCoverage2D emptyRaster = RasterConstructors.makeEmptyRaster(2, 5, 5, 0, 0, 1, -1, 0, 0, 0);
        double[] values1 = new double[] {1,2,0,0,0,0,7,8,0,10,11,0,0,0,0,16,17,0,19,20,21,0,23,24,25};
        double[] values2 = new double[] {0,0,28,29,0,0,0,33,34,35,36,37,38,0,0,0,0,43,44,45,46,47,48,49,50};
        emptyRaster = MapAlgebra.addBandFromArray(emptyRaster, values1, 1, 0d);
        emptyRaster = MapAlgebra.addBandFromArray(emptyRaster, values2, 2, 0d);
        double count = 25.0;
        double sum = 204.0;
        double mean = 8.16;
        double stddev = 9.27655108324209;
        double min = 0.0;
        double max = 25.0;
        double[] result = RasterBandAccessors.getSummaryStats(emptyRaster, 1, false);
        assertEquals(count, result[0], 0.1d);
        assertEquals(sum, result[1], 0.1d);
        assertEquals(mean, result[2], 1e-2d);
        assertEquals(stddev, result[3], 1e-6);
        assertEquals(min, result[4], 0.1d);
        assertEquals(max, result[5], 0.1d);

        count = 16.0;
        sum = 642.0;
        mean = 40.125;
        stddev = 6.9988838395847095;
        min = 28.0;
        max = 50.0;
        result = RasterBandAccessors.getSummaryStats(emptyRaster, 2);
        assertEquals(count, result[0], 0.1d);
        assertEquals(sum, result[1], 0.1d);
        assertEquals(mean, result[2], 1e-3d);
        assertEquals(stddev, result[3], 1e-6d);
        assertEquals(min, result[4], 0.1d);
        assertEquals(max, result[5], 0.1d);

        count = 14.0;
        sum = 204.0;
        mean = 14.571428571428571;
        stddev = 7.761758689832072;
        min = 1.0;
        max = 25.0;
        result = RasterBandAccessors.getSummaryStats(emptyRaster);
        assertEquals(count, result[0], 0.1d);
        assertEquals(sum, result[1], 0.1d);
        assertEquals(mean, result[2], 1e-6d);
        assertEquals(stddev, result[3], 1e-6d);
        assertEquals(min, result[4], 0.1d);
        assertEquals(max, result[5], 0.1d);
    }

    @Test
    public void testSummaryStatsWithRaster() throws IOException {
        GridCoverage2D raster = rasterFromGeoTiff(resourceFolder + "raster/raster_with_no_data/test5.tiff");
        double count = 1036800.0;
        double sum = 2.06233487E8;
        double mean = 198.91347125771605;
        double stddev = 95.09054096106192;
        double min = 0.0;
        double max = 255.0;
        double[] result = RasterBandAccessors.getSummaryStats(raster, 1, false);
        assertEquals(count, result[0], 0.1d);
        assertEquals(sum, result[1], 0.1d);
        assertEquals(mean, result[2], 1e-6d);
        assertEquals(stddev, result[3], 1e-6d);
        assertEquals(min, result[4], 0.1d);
        assertEquals(max, result[5], 0.1d);

        count = 928192.0;
        sum = 2.06233487E8;
        mean = 222.18839097945252;
        stddev = 70.20559521132097;
        min = 1.0;
        max = 255.0;
        result = RasterBandAccessors.getSummaryStats(raster, 1);
        assertEquals(count, result[0], 0.1d);
        assertEquals(sum, result[1], 0.1d);
        assertEquals(mean, result[2], 1e-6d);
        assertEquals(stddev, result[3], 1e-6d);
        assertEquals(min, result[4], 0.1d);
        assertEquals(max, result[5], 0.1d);

        result = RasterBandAccessors.getSummaryStats(raster);
        assertEquals(count, result[0], 0.1d);
        assertEquals(sum, result[1], 0.1d);
        assertEquals(mean, result[2], 1e-6d);
        assertEquals(stddev, result[3], 1e-6d);
        assertEquals(min, result[4], 0.1d);
        assertEquals(max, result[5], 0.1d);
    }

    @Test
    public void testCountWithEmptyRaster() throws FactoryException {
        // With each parameter and excludeNoDataValue as true
        GridCoverage2D emptyRaster = RasterConstructors.makeEmptyRaster(2, 5, 5, 0, 0, 1, -1, 0, 0, 0);
        double[] values1 = new double[] {0, 0, 0, 5, 0, 0, 1, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0};
        double[] values2 = new double[] {0, 0, 0, 6, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0};
        emptyRaster = MapAlgebra.addBandFromArray(emptyRaster, values1, 1, 0d);
        emptyRaster = MapAlgebra.addBandFromArray(emptyRaster, values2, 2, 0d);
        long actual = RasterBandAccessors.getCount(emptyRaster, 1, false);
        long expected = 25;
        assertEquals(expected,actual);

        // with just band parameter
        actual = RasterBandAccessors.getCount(emptyRaster, 2);
        expected = 4;
        assertEquals(expected, actual);

        // with no parameters except raster
        actual = RasterBandAccessors.getCount(emptyRaster);
        expected = 6;
        assertEquals(expected, actual);
    }

    @Test
    public void testCountWithEmptySkewedRaster() throws FactoryException {
        GridCoverage2D emptyRaster = RasterConstructors.makeEmptyRaster( 2, 5, 5, 23, -25, 1, -1, 2, 2, 0);
        double[] values1 = new double[] {0, 0, 0, 3, 4, 6, 0, 3, 2, 0, 0, 0, 0, 3, 4, 5, 0, 0, 0, 0, 0, 2, 2, 0, 0};
        double[] values2 = new double[] {0, 0, 0, 0, 3, 2, 5, 6, 0, 0, 3, 2, 0, 0, 2, 3, 0, 0, 0, 0, 0, 3, 4, 4, 3};
        emptyRaster = MapAlgebra.addBandFromArray(emptyRaster, values1, 1, 0d);
        emptyRaster = MapAlgebra.addBandFromArray(emptyRaster, values2, 2, 0d);
        long actual = RasterBandAccessors.getCount(emptyRaster, 2, false);
        long expected = 25;
        assertEquals(expected, actual);

        // without excludeNoDataValue flag
        actual = RasterBandAccessors.getCount(emptyRaster, 1);
        expected = 10;
        assertEquals(expected, actual);

        // just with raster
        actual = RasterBandAccessors.getCount(emptyRaster);
        expected = 10;
        assertEquals(expected, actual);
    }

    @Test
    public void testCountWithRaster() throws IOException {
        GridCoverage2D raster = rasterFromGeoTiff(resourceFolder + "raster/raster_with_no_data/test5.tiff");
        long actual = RasterBandAccessors.getCount(raster, 1, false);
        long expected = 1036800;
        assertEquals(expected,actual);

        actual = RasterBandAccessors.getCount(raster, 1);
        expected = 928192;
        assertEquals(expected,actual);

        actual = RasterBandAccessors.getCount(raster);
        expected = 928192;
        assertEquals(expected, actual);

    }

    @Test
    public void testBandPixelType() throws FactoryException {
        double[] values = new double[]{1.2, 1.1, 32.2, 43.2};

        //create double raster
        GridCoverage2D emptyRaster = RasterConstructors.makeEmptyRaster(2, "D", 2, 2, 53, 51, 1, 1, 0, 0, 0);
        emptyRaster = MapAlgebra.addBandFromArray(emptyRaster, values, 1, 0.0);
        assertEquals("REAL_64BITS", RasterBandAccessors.getBandType(emptyRaster));
        assertEquals("REAL_64BITS", RasterBandAccessors.getBandType(emptyRaster, 2));
        double[] bandValues = MapAlgebra.bandAsArray(emptyRaster, 1);
        double[] expectedBandValuesD = new double[]{1.2, 1.1, 32.2, 43.2};
        for (int i = 0; i < bandValues.length; i++) {
            assertEquals(expectedBandValuesD[i], bandValues[i], 1e-9);
        }
        //create float raster
        emptyRaster = RasterConstructors.makeEmptyRaster(2, "F", 2, 2, 53, 51, 1, 1, 0, 0, 0);
        emptyRaster = MapAlgebra.addBandFromArray(emptyRaster, values, 1, 0.0);
        assertEquals("REAL_32BITS", RasterBandAccessors.getBandType(emptyRaster));
        assertEquals("REAL_32BITS", RasterBandAccessors.getBandType(emptyRaster, 2));
        bandValues = MapAlgebra.bandAsArray(emptyRaster, 1);
        float[] expectedBandValuesF = new float[]{1.2f, 1.1f, 32.2f, 43.2f};
        for (int i = 0; i < bandValues.length; i++) {
            assertEquals(expectedBandValuesF[i], bandValues[i], 1e-9);
        }

        //create integer raster
        emptyRaster = RasterConstructors.makeEmptyRaster(2, "I", 2, 2, 53, 51, 1, 1, 0, 0, 0);
        emptyRaster = MapAlgebra.addBandFromArray(emptyRaster, values, 1, 0.0);
        assertEquals("SIGNED_32BITS", RasterBandAccessors.getBandType(emptyRaster));
        assertEquals("SIGNED_32BITS", RasterBandAccessors.getBandType(emptyRaster, 2));
        bandValues = MapAlgebra.bandAsArray(emptyRaster, 1);
        int[] expectedBandValuesI = new int[]{1, 1, 32, 43};
        for (int i = 0; i < bandValues.length; i++) {
            assertEquals(expectedBandValuesI[i], bandValues[i], 1e-9);
        }

        //create byte raster
        emptyRaster = RasterConstructors.makeEmptyRaster(2, "B", 2, 2, 53, 51, 1, 1, 0, 0, 0);
        emptyRaster = MapAlgebra.addBandFromArray(emptyRaster, values, 1, 0.0);
        bandValues = MapAlgebra.bandAsArray(emptyRaster, 1);
        assertEquals("UNSIGNED_8BITS", RasterBandAccessors.getBandType(emptyRaster));
        assertEquals("UNSIGNED_8BITS", RasterBandAccessors.getBandType(emptyRaster, 2));
        byte[] expectedBandValuesB = new byte[]{1, 1, 32, 43};
        for (int i = 0; i < bandValues.length; i++) {
            assertEquals(expectedBandValuesB[i], bandValues[i], 1e-9);
        }

        //create short raster
        emptyRaster = RasterConstructors.makeEmptyRaster(2, "S", 2, 2, 53, 51, 1, 1, 0, 0, 0);
        emptyRaster = MapAlgebra.addBandFromArray(emptyRaster, values, 1, 0.0);
        assertEquals("SIGNED_16BITS", RasterBandAccessors.getBandType(emptyRaster));
        assertEquals("SIGNED_16BITS", RasterBandAccessors.getBandType(emptyRaster, 2));
        bandValues = MapAlgebra.bandAsArray(emptyRaster, 1);
        short[] expectedBandValuesS = new short[]{1, 1, 32, 43};
        for (int i = 0; i < bandValues.length; i++) {
            assertEquals(expectedBandValuesS[i], bandValues[i], 1e-9);
        }

        //create unsigned short raster
        values = new double[]{-1.2, 1.1, -32.2, 43.2};
        emptyRaster = RasterConstructors.makeEmptyRaster(2, "US", 2, 2, 53, 51, 1, 1, 0, 0, 0);
        emptyRaster = MapAlgebra.addBandFromArray(emptyRaster, values, 1, 0.0);
        assertEquals("UNSIGNED_16BITS", RasterBandAccessors.getBandType(emptyRaster));
        assertEquals("UNSIGNED_16BITS", RasterBandAccessors.getBandType(emptyRaster, 2));
        bandValues = MapAlgebra.bandAsArray(emptyRaster, 1);

        short[] expectedBandValuesUS = new short[]{-1, 1, -32, 43};
        for (int i = 0; i < bandValues.length; i++) {
            assertEquals(Short.toUnsignedInt(expectedBandValuesUS[i]), Short.toUnsignedInt((short) bandValues[i]), 1e-9);
        }
    }

    @Test
    public void testBandPixelTypeIllegalBand() throws FactoryException {
        GridCoverage2D emptyRaster = RasterConstructors.makeEmptyRaster(2, "US", 2, 2, 53, 51, 1, 1, 0, 0, 0);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> RasterBandAccessors.getBandType(emptyRaster, 5));
        assertEquals("Provided band index 5 is not present in the raster", exception.getMessage());
    }



}
