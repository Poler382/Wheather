import breeze.linalg._
import math._

object whether{
  val path = "Wheater/"
  val py_path = "py_program"




}

object makedata{
  val filepath = "/home/share/chijou/"
  val path = "Wheather/"
  val py_path = "py_program/"
  val textpath = "Wheather/imgtext/"

  def main(args:Array[String]){
    val line = scala.sys.process.Process("ls /home/share/chijou" ).lineStream.toArray
    var c = 0;
    for (file <- line){

      if(file.contains("jp_c")){
        convert(file,file+".txt")
        c+=1
      }

      
    }
    println(c)

  }


  def convert(fn:String,out:String){
    val p =scala.sys.process.Process(
      s"ipython py_program/convert_c.py "+filepath+fn+" "+textpath+out
    ).run

    while(p.isAlive()){
      Thread.sleep(500)
      print(".")
    }

    println("finish convert "+fn+" -> "+out)
    
  }


}


object maketeachdata{

  val filepath = "/home/share/chijou/"
  val path = "Wheather/"
  val py_path = "py_program/"
  val textpath = "Wheather/imgtext/"
  val testpath = path + "testtxet/"

  def f(line:String)=line.split(",").toArray

  def main(args:Array[String]){
    val line = scala.sys.process.Process("ls "+testpath ).lineStream.toArray

    val pathName = testpath + "test-d.txt"
    val writer1 =  new java.io.PrintWriter(pathName)
    
    var c = 1;
    for (l <- line){
      print(c +" -> ")
      var start_a = System.currentTimeMillis 
      val m = scala.io.Source.fromFile(testpath+l).getLines.map(f).toArray
      for(i <- 0 until m.size){
        var ys = ""
        for(j <- 0 until m(0).size){
          ys += m(i)(j)
          if(i == m.size-1 && j == m(0).size-1){
            ys += "\n"
          }else{
            ys+= ","
          }
        }
        writer1.write(ys)
      }

      writer1.close()
     
      println(" file"+c+" finish time:"+(System.currentTimeMillis - start_a)/1000d)
      c+=1
    }
  }
}

object pickR{
  val filepath = "/home/share/chijou/"
  val path = "Wheather/"
  val py_path = "py_program/"
  val textpath = "Wheather/imgtext/"
  val testpath = path + "testtxet/"

  

  def f(line:String)=line.split(",").toArray
  def main(args:Array[String]){
    val line = scala.sys.process.Process("ls "+testpath ).lineStream.toArray
    val ls = line(16)

    val m = scala.io.Source.fromFile(testpath+ls).getLines.map(f).toArray
    var colerfile =  Array.ofDim[Double](m.size,600,3)
    var rfile = Array.ofDim[Int](m.size,600,3)

    for(i <- 0 until m.size;j <- 0 until m(0).size/3; k <- 0 until 3){
      colerfile(i)(j)(k) = m(i)(j*3+k).toDouble
    }

    for(i <- 0 until m.size;j <- 0 until m(0).size/3; k <- 0 until 3){
      if(colerfile(i)(j)(0) > 250 && colerfile(i)(j)(1) < 50 && colerfile(i)(j)(2) < 50&& k == 0 ){
        rfile(i)(j)(k)= colerfile(i)(j)(k).toInt
      }
      else {
        rfile(i)(j)(k) = 0
      }
    }


    Image.write(m+".png",rfile)


  }


}

