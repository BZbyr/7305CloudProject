package hk.hku.spark.dl

import java.io.File

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.util.ModelSerializer
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.indexing.NDArrayIndex

/**
  * 去掉了 deep learning 训练部分的依赖包，只能进行正向预测.
  * 其他依赖包参见Git 或者pom.xml 注释部分
  */
class Word2VecSentimentRNNTest {

  def main(args: Array[String]): Unit = {
    //对于大于256 词数, 只取前256 词
    val truncateReviewsToLength: Int = 256

    // 加载词向量文件
    val wordVectors: WordVectors = WordVectorSerializer.loadStaticModel(new File("/Users/woora/Downloads/GoogleNews-vectors-negative300.bin.gz"))

    // test 迭代器
    val test: SentimentExampleIterator = new SentimentExampleIterator(wordVectors)

    // 要分析的twitter text
    val firstPositiveReview: String = "I went and saw this movie last night after being coaxed to by a few friends of mine. I'll admit that I was reluctant to see it because from what I knew of Ashton Kutcher he was only able to do comedy. I was wrong. Kutcher played the character of Jake Fischer very well, and Kevin Costner played Ben Randall with such professionalism. The sign of a good movie is that it can toy with our emotions. This one did exactly that. The entire theater (which was sold out) was overcome by laughter during the first half of the movie, and were moved to tears during the second half. While exiting the theater I not only saw many women in tears, but many full grown men as well, trying desperately not to let anyone see them crying. This movie was great, and I suggest that you go see it before you judge."

    // 加载train 好的模型文件
    val restored: MultiLayerNetwork = ModelSerializer.restoreMultiLayerNetwork("./DumpedModel.zip")

    val features: INDArray = test.loadFeaturesFromString(firstPositiveReview, truncateReviewsToLength)

    // 结果格式：
    val networkOutput_restored: INDArray = restored.output(features)

    val timeSeriesLength_restored: Long = networkOutput_restored.size(2)

    val probabilitiesAtLastWord_restored: INDArray = networkOutput_restored.get(NDArrayIndex.point(0), NDArrayIndex.all, NDArrayIndex.point(timeSeriesLength_restored - 1))

    val zero: Long = 0
    // 分析结果为positive 的概率
    val positive: Double = probabilitiesAtLastWord_restored.getDouble(zero)
    //    System.out.println("p(positive): " + probabilitiesAtLastWord_restored.getDouble(0))
    //    System.out.println("p(negative): " + probabilitiesAtLastWord_restored.getDouble(1))

  }
}
