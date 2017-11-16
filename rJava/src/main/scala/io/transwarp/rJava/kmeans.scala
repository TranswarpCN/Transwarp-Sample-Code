package io.transwarp.rJava

import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object kmeans {
  def main(args: Array[String]): Unit = {
    val sc = init()
    /* val input = "/home/data.txt"
     * val input = "E:\\星环\\shanghai2.txt"
     */
    val input = "/home/kkkk/shanghai2.txt"
    run(sc, input, 15)
    print("test over!")
  }

  def init(): SparkContext = {
    val conf = new SparkConf().setAppName("K-means").setMaster("local")
    new SparkContext(conf)
  }

  def run(sc: SparkContext, input: String, clusters: Int): RDD[Int] = {
    val data = sc.textFile(input).map(_.split(",").map(_.toDouble)).map(Vectors.dense(_))
    val model = KMeans.train(data, clusters, 20, 1)
    model.predict(data)
  }
}