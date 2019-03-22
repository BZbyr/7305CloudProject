package hk.hku.spark.utils

import com.google.gson.Gson

object GsonSingleton {

  @transient
  @volatile private var instance: Gson = _

  def getInstance() = {
    if (instance == null) {
      synchronized {
        if (instance == null) {
          instance = new Gson()
        }
      }
    }
    instance
  }
}
