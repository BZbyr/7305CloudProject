package hk.hku.cloud.dl4j;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.indexing.NDArrayIndex;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * @author: LexKaing
 * @create: 2019-04-11 03:11
 * @description:
 **/
public class Word2VecSentimentRNNTest {

    public static void main(String[] args) throws Exception {
        // 从本地加载模型
        MultiLayerNetwork restored = null;
        try {
            restored = ModelSerializer.restoreMultiLayerNetwork(new File("/Users/Kai/Downloads/DumpedModel.zip"));
            System.out.println("load model successful");
        } catch (IOException e) {
            System.out.println("computeDL4JSentiment load model exception");
        }

        // 词向量本地路径
        String WORD_VECTORS_PATH = "/Users/Kai/Downloads/GoogleNews-vectors-negative300.bin.gz";
        File wordVectorsFile = new File(WORD_VECTORS_PATH);
        // 加载词向量
        WordVectors wordVectors = WordVectorSerializer.loadStaticModel(wordVectorsFile);
        System.out.println("load static model successful");

        SentimentExampleIterator iterator = new SentimentExampleIterator(wordVectors,1,256);
        System.out.println("init SentimentExampleIterator");

        String text = "When all we have anymore is pretty much reality TV shows with people making fools of themselves for whatever reason be it too fat or can't sing or cook worth a damn than I know Hollywood has run out of original ideas. I can not recall a time when anything original or intelligent came out on TV in the last 15 years. What is our obsession with watching bums make fools of themselves? I would have thought these types of programs would have run full circle but every year they come up with something new that is more strange then the one before. OK so people in this one need to lose weight...most Americans need to lose weight. I just think we all to some degree enjoy watching people humiliated. Maybe it makes us feel better when we see someone else looking like a jerk. I don't know but I just wish something intelligent would come out that did not insult your intelligence.% ";

        // 计算文本的情绪值
        INDArray features = iterator.loadFeaturesFromString(text, 256);
        INDArray networkOutput_restored = restored.output(features);
        long timeSeriesLength_restored = networkOutput_restored.size(2);
        INDArray probabilitiesAtLastWord_restored = networkOutput_restored
                .get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(timeSeriesLength_restored - 1));

        // 吐出分析结果
        Double positive = probabilitiesAtLastWord_restored.getDouble(0);
        Double negative = probabilitiesAtLastWord_restored.getDouble(1);
        System.out.println("dl4j positive : " + positive);
        System.out.println("dl4j negative : " + negative);

        while (true){
            System.out.println("inupt : ");

            Scanner sc = new Scanner(System.in);
            String input = sc.next();

            // 计算文本的情绪值
            features = iterator.loadFeaturesFromString(input, 256);
            networkOutput_restored = restored.output(features);
            timeSeriesLength_restored = networkOutput_restored.size(2);
            probabilitiesAtLastWord_restored = networkOutput_restored
                    .get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(timeSeriesLength_restored - 1));

            // 吐出分析结果
            positive = probabilitiesAtLastWord_restored.getDouble(0);
            negative = probabilitiesAtLastWord_restored.getDouble(1);
            System.out.println("dl4j positive : " + positive);
            System.out.println("dl4j negative : " + negative);

        }
    }

}