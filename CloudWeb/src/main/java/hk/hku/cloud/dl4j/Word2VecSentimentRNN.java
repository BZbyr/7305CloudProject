//package hk.hku.spark.dl;
//
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.io.FilenameUtils;
//import org.deeplearning4j.eval.Evaluation;
//import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
//import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
//import org.deeplearning4j.nn.conf.GradientNormalization;
//import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
//import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
//import org.deeplearning4j.nn.conf.layers.LSTM;
//import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
//import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
//import org.deeplearning4j.nn.weights.WeightInit;
//import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
//import org.deeplearning4j.util.ModelSerializer;
//import org.nd4j.linalg.activations.Activation;
//import org.nd4j.linalg.api.ndarray.INDArray;
//import org.nd4j.linalg.factory.Nd4j;
//import org.nd4j.linalg.indexing.NDArrayIndex;
//import org.nd4j.linalg.learning.config.Adam;
//import org.nd4j.linalg.lossfunctions.LossFunctions;
//
//import java.io.File;
//
///**Example: Given a movie review (raw text), classify that movie review as either positive or negative based on the words it contains.
// * This is done by combining Word2Vec vectors and a recurrent neural network model. Each word in a review is vectorized
// * (using the Word2Vec model) and fed into a recurrent neural network.
// * Training data is the "Large Movie Review Dataset" from http://ai.stanford.edu/~amaas/data/sentiment/
// * This data set contains 25,000 training reviews + 25,000 testing reviews
// *
// * Process:
// * 1. Automatic on first run of example: Download data (movie reviews) + extract
// * 2. Load existing Word2Vec model (for example: Google News word vectors. You will have to download this MANUALLY)
// * 3. Load each each review. Convert words to vectors + reviews to sequences of vectors
// * 4. Train network
// *
// * With the current configuration, gives approx. 83% accuracy after 1 epoch. Better performance may be possible with
// * additional tuning.
// *
// * NOTE / INSTRUCTIONS:
// * You will have to download the Google News word vector model manually. ~1.5GB
// * The Google News vector model available here: https://code.google.com/p/word2vec/
// * Download the GoogleNews-vectors-negative300.bin.gz file
// * Then: set the WORD_VECTORS_PATH field to point to this location.
// *
// * @author Alex Black
// *
// * 这段代码包含2部分:
// * 前提: 词向量 是提前训练好的一个文件.
// * 1、训练，需要加载词向量文件 + 训练数据，计算得到 模型.
// * 2、测试，需要加载 模型+词向量，在对测试数据 进行计算得到分析结果。
// * 注: 测试、训练，可以直接在本地，也可以在集群上进行。
// * 对于实际使用中，需要加载 模型+词向量 就可进行实时分析。（默认接受生成模型的准确性）
// *
// */
//public class Word2VecSentimentRNN {
//
//    /** Data URL for downloading */
//    public static final String DATA_URL = "http://ai.stanford.edu/~amaas/data/sentiment/aclImdb_v1.tar.gz";
//    /** Location to save and extract the training/testing data */
//    public static final String DATA_PATH = FilenameUtils.concat(System.getProperty("java.io.tmpdir"), "dl4j_w2vSentiment/");
//    /** Location (local file system) for the Google News vectors. Set this manually. */
//    public static final String WORD_VECTORS_PATH = "/Users/woora/Downloads/GoogleNews-vectors-negative300.bin.gz";
//
//
//    public static void main(String[] args) throws Exception {
//        if(WORD_VECTORS_PATH.startsWith("/PATH/TO/YOUR/VECTORS/")){
//            throw new RuntimeException("Please set the WORD_VECTORS_PATH before running this example");
//        }
//
//        //Download and extract data
//        downloadData();
//
//        int batchSize = 1;     //Number of examples in each minibatch
//        int vectorSize = 300;   //Size of the word vectors. 300 in the Google News model
//        int nEpochs = 1;        //Number of epochs (full passes of training data) to train on
//        int truncateReviewsToLength = 256;  //Truncate reviews with length (# words) greater than this
//        final int seed = 0;     //Seed for reproducibility
//
//        Nd4j.getMemoryManager().setAutoGcWindow(10000);  //https://deeplearning4j.org/workspaces
//
//        //Set up network configuration
//        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
//            .seed(seed)
//            .updater(new Adam(5e-3))
//            .l2(1e-5)
//            .weightInit(WeightInit.XAVIER)
//            .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue).gradientNormalizationThreshold(1.0)
//            .list()
//            .layer(0, new LSTM.Builder().nIn(vectorSize).nOut(256)
//                .activation(Activation.TANH).build())
//            .layer(1, new RnnOutputLayer.Builder().activation(Activation.SOFTMAX)
//                .lossFunction(LossFunctions.LossFunction.MCXENT).nIn(256).nOut(2).build())
//            .build();
//
//        MultiLayerNetwork net = new MultiLayerNetwork(conf);
//        net.init();
//        net.setListeners(new ScoreIterationListener(1));
//
//        //DataSetIterators for training and testing respectively
//        WordVectors wordVectors = WordVectorSerializer.loadStaticModel(new File(WORD_VECTORS_PATH));
//        SentimentExampleIterator train = new SentimentExampleIterator(DATA_PATH, wordVectors, batchSize, truncateReviewsToLength, true);
//        SentimentExampleIterator test = new SentimentExampleIterator(DATA_PATH, wordVectors, batchSize, truncateReviewsToLength, false);
//
//
//
//
//        System.out.println("Starting training");
//        for (int i = 0; i < nEpochs; i++) {
//            net.fit(train);
//            train.reset();
//            System.out.println("Epoch " + i + " complete. Starting evaluation:");
//
//            //Run evaluation. This is on 25k reviews, so can take some time
//            Evaluation evaluation = net.evaluate(test);
//            System.out.println(evaluation.stats());
//        }
//
//        File locationToSave = new File("DumpedModel.zip");      //Where to save the network. Note: the file is in .zip format - can be opened externally
//        boolean saveUpdater = true;                                             //Updater: i.e., the state for Momentum, RMSProp, Adagrad etc. Save this if you want to train your network more in the future
//        ModelSerializer.writeModel(net, locationToSave, saveUpdater);
//
//
//        //Load the model
//
//        MultiLayerNetwork restored = ModelSerializer.restoreMultiLayerNetwork(locationToSave);
//        Evaluation evaluation_restored = restored.evaluate(test);
//        System.out.println("evaluate result of resotred model: "+evaluation_restored.stats());
//
//
//        //After training: load a single example and generate predictions
//        File firstPositiveReviewFile = new File(FilenameUtils.concat(DATA_PATH, "aclImdb/test/pos/0_10.txt"));
//        String firstPositiveReview = FileUtils.readFileToString(firstPositiveReviewFile);
//
//        INDArray features = test.loadFeaturesFromString(firstPositiveReview, truncateReviewsToLength);
//        INDArray networkOutput = net.output(features);
//        INDArray networkOutput_restored = restored.output(features);
//
//        long timeSeriesLength = networkOutput.size(2);
//        long timeSeriesLength_restored = networkOutput_restored.size(2);
//
//        INDArray probabilitiesAtLastWord = networkOutput.get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(timeSeriesLength - 1));
//        INDArray probabilitiesAtLastWord_restored = networkOutput_restored.get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(timeSeriesLength_restored - 1));
//
//        System.out.println("\n\n-------------------------------");
//        System.out.println("First positive review: \n" + firstPositiveReview);
//        System.out.println("\n\nProbabilities at last time step:");
//        System.out.println("p(positive): " + probabilitiesAtLastWord.getDouble(0));
//        System.out.println("p(negative): " + probabilitiesAtLastWord.getDouble(1));
//        System.out.println("\n\n--------------resotred model result-----------------");
//
//        System.out.println("p(positive): " + probabilitiesAtLastWord_restored.getDouble(0));
//        System.out.println("p(negative): " + probabilitiesAtLastWord_restored.getDouble(1));
//
//        System.out.println("----- Example complete -----");
//    }
//
//    public static void downloadData() throws Exception {
////        //Create directory if required
////        File directory = new File(DATA_PATH);
////        if(!directory.exists()) directory.mkdir();
////
////        //Download file:
////        String archizePath = DATA_PATH + "aclImdb_v1.tar.gz";
////        File archiveFile = new File(archizePath);
////        String extractedPath = DATA_PATH + "aclImdb";
////        File extractedFile = new File(extractedPath);
////
////        if( !archiveFile.exists() ){
////            System.out.println("Starting data download (80MB)...");
////            FileUtils.copyURLToFile(new URL(DATA_URL), archiveFile);
////            System.out.println("Data (.tar.gz file) downloaded to " + archiveFile.getAbsolutePath());
////            //Extract tar.gz file to output directory
////            DataUtilities.extractTarGz(archizePath, DATA_PATH);
////        } else {
////            //Assume if archive (.tar.gz) exists, then data has already been extracted
////            System.out.println("Data (.tar.gz file) already exists at " + archiveFile.getAbsolutePath());
////            if( !extractedFile.exists()){
////            	//Extract tar.gz file to output directory
////            	DataUtilities.extractTarGz(archizePath, DATA_PATH);
////            } else {
////            	System.out.println("Data (extracted) already exists at " + extractedFile.getAbsolutePath());
////            }
////        }
//    }
//
//
//}
