package hk.hku.spark.mllib

import hk.hku.spark.utils.{LogUtils, PropertiesLoader, SQLContextSingleton, StopWordsLoader}
import org.apache.hadoop.io.compress.GzipCodec
import org.apache.log4j.{Level, LogManager}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.mllib.classification.{NaiveBayes, NaiveBayesModel}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.serializer.KryoSerializer
import org.apache.spark.sql._
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Creates a Model of the training dataset using Spark MLlib's Naive Bayes classifier.
  */
// spark-submit --class "org.p7h.spark.sentiment.mllib.SparkNaiveBayesModelCreator" --master spark://spark:7077 spark-streaming-corenp-mllib-tweet-sentiment-assembly-0.1.jar
object SparkNaiveBayesModelCreator {
  val log = LogManager.getRootLogger

  def main(args: Array[String]) {
    //    val tweet = "hello world , i have a boy? https://www.baidu.com. and you?"
    //    val stopWordsList = StopWordsLoader.loadStopWords(PropertiesLoader.nltkStopWords)
    //    val tweetInWords: Seq[String] = MLlibSentimentAnalyzer.getBarebonesTweetText(tweet, stopWordsList)
    //    println(tweetInWords)

    val sc = createSparkContext()

    log.setLevel(Level.INFO)
    log.warn("SparkNaiveBayesModelCreator start ")
//    LogUtils.setLogLevels(sc)

    val stopWordsList = sc.broadcast(StopWordsLoader.loadStopWords(PropertiesLoader.nltkStopWords))
    createAndSaveNBModel(sc, stopWordsList)
    validateAccuracyOfNBModel(sc, stopWordsList)
  }

  /**
    * Remove new line characters.
    *
    * @param tweetText -- Complete text of a tweet.
    * @return String with new lines removed.
    */
  def replaceNewLines(tweetText: String): String = {
    tweetText.replaceAll("\n", "")
  }

  /**
    * Create SparkContext.
    * Future extension: enable checkpointing to HDFS [is it really reqd??].
    *
    * @return SparkContext
    */
  def createSparkContext(): SparkContext = {
    val conf = new SparkConf()
      .setAppName(this.getClass.getSimpleName)
      .set("spark.serializer", classOf[KryoSerializer].getCanonicalName)
    val sc = SparkContext.getOrCreate(conf)
    sc
  }

  /**
    * Creates a Naive Bayes Model of Tweet and its Sentiment from the Sentiment140 file.
    * 构建Bayes模型
    *
    * @param sc            -- Spark Context.
    * @param stopWordsList -- Broadcast variable for list of stop words to be removed from the tweets.
    */
  def createAndSaveNBModel(sc: SparkContext, stopWordsList: Broadcast[List[String]]): Unit = {
//    使用spark sql 加载文本数据
    val tweetsDF: DataFrame = loadSentiment140File(sc, PropertiesLoader.sentiment140TrainingFilePath)

    log.info("createAndSaveNBModel loadSentiment140File")

    val labeledRDD = tweetsDF.select("polarity", "status").rdd.map {
      case Row(polarity: Int, tweet: String) =>
        val tweetInWords: Seq[String] = MLlibSentimentAnalyzer.getBarebonesTweetText(tweet, stopWordsList.value)
        // 将tweet 过滤后的文本String 序列，打上polarity 标签值
        LabeledPoint(polarity, MLlibSentimentAnalyzer.transformFeatures(tweetInWords))

    }
    labeledRDD.cache()

    val naiveBayesModel: NaiveBayesModel = NaiveBayes.train(labeledRDD, lambda = 1.0, modelType = "multinomial")
    naiveBayesModel.save(sc, PropertiesLoader.naiveBayesModelPath)
  }

  /**
    * Validates and check the accuracy of the model by comparing the polarity of a tweet from the dataset and compares it with the MLlib predicted polarity.
    * 测试模型准确性
    *
    * @param sc            -- Spark Context.
    * @param stopWordsList -- Broadcast variable for list of stop words to be removed from the tweets.
    */
  def validateAccuracyOfNBModel(sc: SparkContext, stopWordsList: Broadcast[List[String]]): Unit = {
    val naiveBayesModel: NaiveBayesModel = NaiveBayesModel.load(sc, PropertiesLoader.naiveBayesModelPath)

    val tweetsDF: DataFrame = loadSentiment140File(sc, PropertiesLoader.sentiment140TestingFilePath)
    val actualVsPredictionRDD = tweetsDF.select("polarity", "status").rdd.map {
      case Row(polarity: Int, tweet: String) =>
        val tweetText = replaceNewLines(tweet)
        val tweetInWords: Seq[String] = MLlibSentimentAnalyzer.getBarebonesTweetText(tweetText, stopWordsList.value)
        (polarity.toDouble,
          naiveBayesModel.predict(MLlibSentimentAnalyzer.transformFeatures(tweetInWords)),
          tweetText)
    }
    val accuracy = 100.0 * actualVsPredictionRDD.filter(x => x._1 == x._2).count() / tweetsDF.count()
    /*actualVsPredictionRDD.cache()
    val predictedCorrect = actualVsPredictionRDD.filter(x => x._1 == x._2).count()
    val predictedInCorrect = actualVsPredictionRDD.filter(x => x._1 != x._2).count()
    val accuracy = 100.0 * predictedCorrect.toDouble / (predictedCorrect + predictedInCorrect).toDouble*/
    println(f"""\n\t<==******** Prediction accuracy compared to actual: $accuracy%.2f%% ********==>\n""")
    saveAccuracy(sc, actualVsPredictionRDD)
  }

  /**
    * Loads the Sentiment140 file from the specified path using SparkContext.
    *
    * @param sc                   -- Spark Context.
    * @param sentiment140FilePath -- Absolute file path of Sentiment140.
    * @return -- Spark DataFrame of the Sentiment file with the tweet text and its polarity.
    */
  def loadSentiment140File(sc: SparkContext, sentiment140FilePath: String): DataFrame = {
    val sqlContext = SQLContextSingleton.getInstance(sc)
    // training data 格式:polarity(情绪标签),id,date,query,user,status(tweet元数据)
    val tweetsDF = sqlContext.read
      .format("com.databricks.spark.csv")
      .option("header", "false")
      .option("inferSchema", "true")
      .load(sentiment140FilePath)
      .toDF("polarity", "id", "date", "query", "user", "status")

    // Drop the columns we are not interested in.
    tweetsDF.drop("id").drop("date").drop("query").drop("user")
  }

  /**
    * Saves the accuracy computation of the ML library.
    * The columns are actual polarity as per the dataset, computed polarity with MLlib and the tweet text.
    *
    * @param sc                    -- Spark Context.
    * @param actualVsPredictionRDD -- RDD of polarity of a tweet in dataset and MLlib computed polarity.
    */
  def saveAccuracy(sc: SparkContext, actualVsPredictionRDD: RDD[(Double, Double, String)]): Unit = {
    val sqlContext = SQLContextSingleton.getInstance(sc)
    import sqlContext.implicits._
    val actualVsPredictionDF = actualVsPredictionRDD.toDF("Actual", "Predicted", "Text")
    actualVsPredictionDF.coalesce(1).write
      .format("com.databricks.spark.csv")
      .option("header", "true")
      .option("delimiter", "\t")
      // Compression codec to compress while saving to file.
      .option("codec", classOf[GzipCodec].getCanonicalName)
      .mode(SaveMode.Append)
      .save(PropertiesLoader.modelAccuracyPath)
  }
}