package hk.hku.spark.utils

import java.io.{BufferedOutputStream, File, FileOutputStream}

object FileUtils {

  def writeBytes(data: Array[Byte], file: File) = {
    val target = new BufferedOutputStream(new FileOutputStream(file));
    try data.foreach(target.write(_)) finally target.close;
  }
}
