package hk.hku.cloud;
/**
 * Created by IntelliJ IDEA
 * Project: CloudProject
 * Author: Boyang
 * Date: 2019-04-09 11:51
 */
import twitter4j.Status;
import twitter4j.GeoLocation;

import java.util.*;
import java.util.stream.Collectors;
/**
 *
 *
 * @package: hk.hku.cloud
 * @class: TweetFunction
 * @author: Boyang
 * @date: 2019-04-09 11:51
 */
public class TweetFunctions {

    private static final String JP = "jp";
    private static final String SPA = "sp";
    private static final String POR = "po";
    private static final List<String> LANG_CODE = new ArrayList<>(Arrays.asList("zh","en","ja","es","pt","ar","fr","ko"));


    /**
     * get the num of fans of the tweet poster
     *
     * @param tweet the tweet
     * @return  java.lang.Long
     */
    public static String getUsrFollowerNumLevel(Status tweet){
        String followerNumLevel = null;
        int followNum = 0;
        if (tweet.getUser()!=null){
            followNum = tweet.getUser().getFollowersCount();
            if (followNum<=200){
                followerNumLevel = "200";
            }
            else if (followNum<=800){
                followerNumLevel = "800";
            }
            else if (followNum<=2000){
                followerNumLevel = "2k";
            }
            else if (followNum<=5000){
                followerNumLevel = "5k";
            }else if (followNum <= 20000){
                followerNumLevel = "20k";
            }else if (followNum <= 100000){
                followerNumLevel = "100k";
            }else if (followNum <= 1000000){
                followerNumLevel = "1kk";
            }else {
                followerNumLevel = "1kk+";
            }
        }
        return followerNumLevel;
    }

    /**
     * get the tweet language
     *
     * @param tweet the tweet
     * @return  java.lang.String
     */
    public static String getTweetLanguage(Status tweet){
        String language = null;
        if(tweet.getLang() != null) {
            language = tweet.getLang().substring(0,2);
            switch (language) {
                case JP:
                    language = "ja";
                    break;
                case SPA:
                    language = "es";
                    break;
                case POR:
                    language = "pt";
                    break;
                default:
                    if (!LANG_CODE.contains(language)) {
                        language = "ot";
                    }
                    break;
            }
        }


        return language;
    }

    /**
     * get the tweet latitude and longitude
     *
     * @param tweet the tweet
     * @return  twitter4j.GeoLocation
     */
    public static GeoLocation getTweetGPSCoordinates(Status tweet) {
        GeoLocation coordinates = null;

        /*
         favorite metric
         fallback to place "associated" with tweet and extract center GPS coordinates from bounding box
         */
        if(tweet.getGeoLocation() != null) {
            coordinates = tweet.getGeoLocation();
        }

        else if(tweet.getPlace() != null && tweet.getPlace().getBoundingBoxCoordinates() != null) {
            // get bounding box
            GeoLocation[] boundingBox =  tweet.getPlace().getBoundingBoxCoordinates()[0];
            // compute center of bounding box
            double centerLatitude = Arrays.stream(boundingBox).map(GeoLocation::getLatitude)
                    .collect(Collectors.averagingDouble(d -> d));
            double centerLongitude = Arrays.stream(boundingBox).map(GeoLocation::getLongitude)
                    .collect(Collectors.averagingDouble(d -> d));

            coordinates = new GeoLocation(centerLatitude, centerLongitude);
        }
        return coordinates;
    }
}
