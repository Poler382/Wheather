import breeze.linalg._
import math._

object BN{
  def sub(y:Array[DenseVector[Double]],a:Array[DenseVector[Double]] )={
    var return_d = new Array[DenseVector[Double]](a.size)
    var d = Array.ofDim[Double](a(0).size)

    for(i <- 0 until a.size){
      for(j <- 0 until a(0).size){
        d(j) = y(i)(j) - a(i)(j)
      }
      return_d(i) = DenseVector(d)
    }
    return_d
  }
/*
  def test()={
    val bn   = new BatchNormalization3(2,3)
    val test = Array(
      Array(1d,2d),
      Array(3d,4d),
      Array(5d,6d)
    )

    val test2 = new Array(
      Array[Double](1d,0d),Array[Double](2d,1d),Array[Double](1d,2d)
    )

    val l = bn.forward(test)

    val ll = bn.backward(test2)
  }
 */
}
 
