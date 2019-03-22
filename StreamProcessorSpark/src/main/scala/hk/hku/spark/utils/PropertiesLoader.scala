package hk.hku.spark.utils

import java.util.Properties

//import com.typesafe.config.{Config, ConfigFactory}


/**
  * Exposes all the key-value pairs as properties object using Config object of Typesafe Config project.
  */
object PropertiesLoader {
  private val conf = new Properties;
  val inputStream = PropertiesLoader.getClass.getClassLoader.getResourceAsStream("./application.conf")
  conf.load(inputStream)
  //  private val conf: Config = ConfigFactory.load("application.conf")

  //  val sentiment140TrainingFilePath = conf.getString("SENTIMENT140_TRAIN_DATA_ABSOLUTE_PATH")
  //  val sentiment140TestingFilePath = conf.getString("SENTIMENT140_TEST_DATA_ABSOLUTE_PATH")
  //  val nltkStopWords = conf.getString("NLTK_STOPWORDS_FILE_NAME ")
  //
  //  val naiveBayesModelPath = conf.getString("NAIVEBAYES_MODEL_ABSOLUTE_PATH")
  //  val modelAccuracyPath = conf.getString("NAIVEBAYES_MODEL_ACCURACY_ABSOLUTE_PATH ")
  //
  //  val tweetsRawPath = conf.getString("TWEETS_RAW_ABSOLUTE_PATH")
  //  val saveRawTweets = conf.getBoolean("SAVE_RAW_TWEETS")
  //
  //  val tweetsClassifiedPath = conf.getString("TWEETS_CLASSIFIED_ABSOLUTE_PATH")
  //
  //  val consumerKey = conf.getString("CONSUMER_KEY")
  //  val consumerSecret = conf.getString("CONSUMER_SECRET")
  //  val accessToken = conf.getString("ACCESS_TOKEN_KEY")
  //  val accessTokenSecret = conf.getString("ACCESS_TOKEN_SECRET")
  //
  //  val microBatchTimeInSeconds = conf.getInt("STREAMING_MICRO_BATCH_TIME_IN_SECONDS")
  //  val totalRunTimeInMinutes = conf.getInt("TOTAL_RUN_TIME_IN_MINUTES")

  val sentiment140TrainingFilePath = conf.getProperty("SENTIMENT140_TRAIN_DATA_ABSOLUTE_PATH")
  val sentiment140TestingFilePath = conf.getProperty("SENTIMENT140_TEST_DATA_ABSOLUTE_PATH")
  val nltkStopWords = conf.getProperty("NLTK_STOPWORDS_FILE_NAME")

  val naiveBayesModelPath = conf.getProperty("NAIVEBAYES_MODEL_ABSOLUTE_PATH")
  val modelAccuracyPath = conf.getProperty("NAIVEBAYES_MODEL_ACCURACY_ABSOLUTE_PATH")

  val tweetsRawPath = conf.getProperty("TWEETS_RAW_ABSOLUTE_PATH")
  val saveRawTweets = conf.getProperty("SAVE_RAW_TWEETS").toBoolean

  val tweetsClassifiedPath = conf.getProperty("TWEETS_CLASSIFIED_ABSOLUTE_PATH")

  val consumerKey = conf.getProperty("CONSUMER_KEY")
  val consumerSecret = conf.getProperty("CONSUMER_SECRET")
  val accessToken = conf.getProperty("ACCESS_TOKEN_KEY")
  val accessTokenSecret = conf.getProperty("ACCESS_TOKEN_SECRET")

  val microBatchTimeInSeconds = conf.getProperty("STREAMING_MICRO_BATCH_TIME_IN_SECONDS").toInt
  val totalRunTimeInMinutes = conf.getProperty("TOTAL_RUN_TIME_IN_MINUTES").toInt

  def main(args: Array[String]): Unit = {
    println(PropertiesLoader.nltkStopWords)
    println(PropertiesLoader.saveRawTweets)
  }
}