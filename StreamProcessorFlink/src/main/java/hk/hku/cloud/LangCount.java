package hk.hku.cloud;
/**
 * Created by IntelliJ IDEA
 * Project: CloudProject
 * Author: Boyang
 * Date: 2019-04-09 19:19
 */
import java.io.*;
import java.util.ArrayList;
import java.util.List;
/**
 *
 *
 * @package: hk.hku.cloud
 * @class: LangCount
 * @author: Boyang
 * @date: 2019-04-09 19:19
 */
public class LangCount {
    private List<String> langCode639_1 = new ArrayList<>();
    private List<String> langCode639_3 = new ArrayList<>();
    private List<String> zhCode639_1_3166 = new ArrayList<>();
    private List<String> enCode639_1_3166 = new ArrayList<>();

    public LangCount(String langCode6391Path, String langCode6393Path,String zhCodePath, String enCodePath) throws IOException {
        BufferedReader langCode6391Reader;
        BufferedReader langCode6393Reader;
        BufferedReader zhCodeReader;
        BufferedReader enCodeReader;

        langCode6391Reader = new BufferedReader(new FileReader(new File(langCode6391Path)));
        langCode6393Reader = new BufferedReader(new FileReader(new File(langCode6393Path)));
        zhCodeReader = new BufferedReader(new FileReader(new File(zhCodePath)));
        enCodeReader = new BufferedReader(new FileReader(new File(enCodePath)));

        String code; // tmp
        // build lists
        while ((code = langCode6391Reader.readLine())!=null){
            langCode639_1.add(code);
        }
        while ((code = langCode6393Reader.readLine())!=null){
            langCode639_3.add(code);
        }
        while ((code = zhCodeReader.readLine())!=null){
            zhCode639_1_3166.add(code);
        }
        while ((code= enCodeReader.readLine())!=null){
            enCode639_1_3166.add(code);
        }

        langCode6391Reader.close();
        langCode6393Reader.close();
        zhCodeReader.close();
        enCodeReader.close();
    }


    public ArrayList<Integer> getLangCountList(String )
    // get the sentiment score of the tweet (negative, neutral, positive) in a range [-1;1]
    // the higher, the more positive the sentiment
    public double getSentimentScore(String text) {
        int score = 0;
        int negCounter = 0;
        int posCounter = 0;
        text = text.toLowerCase().trim().replaceAll("[^a-zA-Z0-9\\s]", "");
        String[] words = text.split(" ");

        // check if the current word appears in our reference lists...
        for (int i = 0; i < words.length; i++) {
            if (positiveWords.contains(words[i])) {
                posCounter++;
            }
            if (negativeWords.contains(words[i])) {
                negCounter++;
            }
        }

        // compute total result
        score = posCounter - negCounter;

        return Math.tanh(score);
    }

    public String getSentimentLabel(String text) {
        double score = this.getSentimentScore(text);
        if(score > 0.3) {
            return "Positive";
        }
        else if(score < 0.3) {
            return "Negative";
        }
        else {
            return "Neutral";
        }
    }
}
