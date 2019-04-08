//package hk.hku.spark.utils
//
//import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
//import org.deeplearning4j.util.ModelSerializer
//
//object Dl4jModelLoader {
//
//  def loadDl4jModel(dl4jModelFileName : String):MultiLayerNetwork = {
//    // 加载 resource 里的 deep learning 模型
//    ModelSerializer.restoreMultiLayerNetwork(getClass.getResourceAsStream("/" + dl4jModelFileName))
//  }
//}
