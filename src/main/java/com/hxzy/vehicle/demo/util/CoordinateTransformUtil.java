package com.hxzy.vehicle.demo.util;

/**
 * Tool for conversion between Baidu coordinates (BD09), National Surveying Office coordinates (Mars coordinates, GCJ02), and WGS84 coordinate systems
 *
 * @see Refer to https://github.com/wandergis/coordtransform Java version of the implementation
 * @author geosmart
 */
public class CoordinateTransformUtil {


    static double x_pi = 3.14159265358979324 * 3000.0 / 180.0;
    // π
    static double pi = 3.1415926535897932384626;
    // Major semi-axis
    static double a = 6378245.0;
    // flattening
    static double ee = 0.00669342162296594323;

    /**
     * Baidu coordinate system (BD-09) to WGS coordinate
     *
     * @param lng Latitude of baidu coordinates
     * @param lat Baidu coordinates longitude
     * @return WGS84 coordinate array
     */
    public static double[] bd09towgs84(double lng, double lat) {


        double[] gcj = bd09togcj02(lng, lat);
        double[] wgs84 = gcj02towgs84(gcj[0], gcj[1]);
        return wgs84;
    }

    /**
     * WGS coordinate to Baidu coordinate system (BD-09)
     *
     * @param lng Longitude of the WGS84 coordinate system
     * @param lat Latitude of the WGS84 coordinate system
     * @return Baidu coordinate array
     */
    public static double[] wgs84tobd09(double lng, double lat) {


        double[] gcj = wgs84togcj02(lng, lat);
        double[] bd09 = gcj02tobd09(gcj[0], gcj[1]);
        return bd09;
    }

    /**
     * Mars coordinate system (GCJ-02) to Baidu Coordinate system (BD-09)
     *
     * @see Google, Autonavi -- &gt; Baidu
     * @param lng Longitude in Martian coordinates
     * @param lat Martian coordinate latitude
     * @return Baidu coordinate array
     */
    public static double[] gcj02tobd09(double lng, double lat) {


        double z = Math.sqrt(lng * lng + lat * lat) + 0.00002 * Math.sin(lat * x_pi);
        double theta = Math.atan2(lat, lng) + 0.000003 * Math.cos(lng * x_pi);
        double bd_lng = z * Math.cos(theta) + 0.0065;
        double bd_lat = z * Math.sin(theta) + 0.006;
        return new double[] {

                bd_lng, bd_lat };
    }

    /**
     * Baidu coordinate system (BD-09) to Mars coordinate system (GCJ-02)
     *
     * @see Baidu -- &gt; Google, Autonavi
     * @param lng Latitude of baidu coordinates
     * @param lat Baidu coordinates longitude
     * @return Mars coordinate array
     */
    public static double[] bd09togcj02(double bd_lon, double bd_lat) {


        double x = bd_lon - 0.0065;
        double y = bd_lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
        double gg_lng = z * Math.cos(theta);
        double gg_lat = z * Math.sin(theta);
        return new double[] {

                gg_lng, gg_lat };
    }

    /**
     * WGS84 to GCJ02(Mars coordinate system)
     *
     * @param lng Longitude of the WGS84 coordinate system
     * @param lat Latitude of the WGS84 coordinate system
     * @return Mars coordinate array
     */
    public static double[] wgs84togcj02(double lng, double lat) {


        if (out_of_china(lng, lat)) {


            return new double[] {

                    lng, lat };
        }
        double dlat = transformlat(lng - 105.0, lat - 35.0);
        double dlng = transformlng(lng - 105.0, lat - 35.0);
        double radlat = lat / 180.0 * pi;
        double magic = Math.sin(radlat);
        magic = 1 - ee * magic * magic;
        double sqrtmagic = Math.sqrt(magic);
        dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * pi);
        dlng = (dlng * 180.0) / (a / sqrtmagic * Math.cos(radlat) * pi);
        double mglat = lat + dlat;
        double mglng = lng + dlng;
        return new double[] {

                mglng, mglat };
    }

    /**
     * GCJ02(Mars coordinate system) to GPS84
     *
     * @param lng Longitude of the Martian coordinate system
     * @param lat Latitude of Mars coordinate system
     * @return WGS84 coordinate array
     */
    public static double[] gcj02towgs84(double lng, double lat) {


        if (out_of_china(lng, lat)) {


            return new double[] {

                    lng, lat };
        }
        double dlat = transformlat(lng - 105.0, lat - 35.0);
        double dlng = transformlng(lng - 105.0, lat - 35.0);
        double radlat = lat / 180.0 * pi;
        double magic = Math.sin(radlat);
        magic = 1 - ee * magic * magic;
        double sqrtmagic = Math.sqrt(magic);
        dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * pi);
        dlng = (dlng * 180.0) / (a / sqrtmagic * Math.cos(radlat) * pi);
        double mglat = lat + dlat;
        double mglng = lng + dlng;
        return new double[] {

                lng * 2 - mglng, lat * 2 - mglat };
    }

    /**
     * Conversion of latitude
     *
     * @param lng
     * @param lat
     * @return
     */
    public static double transformlat(double lng, double lat) {


        double ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * pi) + 20.0 * Math.sin(2.0 * lng * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lat * pi) + 40.0 * Math.sin(lat / 3.0 * pi)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(lat / 12.0 * pi) + 320 * Math.sin(lat * pi / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * Longitude conversion
     *
     * @param lng
     * @param lat
     * @return
     */
    public static double transformlng(double lng, double lat) {


        double ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * pi) + 20.0 * Math.sin(2.0 * lng * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lng * pi) + 40.0 * Math.sin(lng / 3.0 * pi)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(lng / 12.0 * pi) + 300.0 * Math.sin(lng / 30.0 * pi)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * Judge whether it is in the country, not in the country do not do migration
     *
     * @param lng
     * @param lat
     * @return
     */
    public static boolean out_of_china(double lng, double lat) {


        if (lng < 72.004 || lng > 137.8347) {


            return true;
        } else if (lat < 0.8293 || lat > 55.8271) {


            return true;
        }
        return false;
    }
}
