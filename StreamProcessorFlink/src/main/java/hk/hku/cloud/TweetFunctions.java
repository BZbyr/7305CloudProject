package hk.hku.cloud;
/**
 * Created by IntelliJ IDEA
 * Project: CloudProject
 * Author: Boyang
 * Date: 2019-04-09 11:51
 */
import twitter4j.Status;
import twitter4j.GeoLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private static final List<String> LANG_CODE = new ArrayList<>(Arrays.asList("zh","en","ja","es","ms","pt","ar","fr","ko"));


    public static String getTweetLanguage(Status tweet){
        String language = null;
        if(tweet.getLang() != null) {
            language = tweet.getLang().substring(0,2);
            if (language.equals(JP) ){
                language = "ja";
            }else if (language.equals(SPA)){
                language = "es";
            }else if (language.equals(POR)){
                language = "pt";
            }else {
                if (!LANG_CODE.contains(language)) {
                    language = "ot";
                }
            }
        }


        return language;
    }

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
            double centerLatitude = Arrays.stream(boundingBox).map(location -> location.getLatitude())
                    .collect(Collectors.averagingDouble(d -> d));
            double centerLongitude = Arrays.stream(boundingBox).map(location -> location.getLongitude())
                    .collect(Collectors.averagingDouble(d -> d));

            coordinates = new GeoLocation(centerLatitude, centerLongitude);
        }
        return coordinates;
    }
}
