import breeze.linalg._
import math._

object makedata{
  val filepath = "/home/share/chijou/"
  val path = "Wheather/"
  val py_path = "py_program/"
  val textpath = "Wheather/imgtext/"
  val trainpath = "Wheather/image/train/"
  val testpath  = "Wheather/image/test/"

  def main(args:Array[String]){
    val line = scala.sys.process.Process("ls /home/share/chijou" ).lineStream.toArray
    var c = 0
    for (file <- line){
      //画像からテキストを作るところ
      if(file.contains("jp_c")){
        print(c+" ->")
        //return 作った先のファイルのパス
        val txtimg = convert(filepath+file,textpath+file+".txt")
        //テキストをリサイズ　学習データ作成
        subsampling(txtimg,file+"_2.txt")

        //正解データを作成 290*300*3
        //return 正解データの絶対パス
        val testfile = fix(txtimg,file)
        convert(testfile,trainpath+"ans/"+file+"_ans.txt")//正解のテキストはimage/train/ansに保存
        c+=1
      }
    }
  }


  def convert(fn:String,out:String)={
    val p = scala.sys.process.Process(
      s"ipython py_program/convert_c.py "+fn+" "+out
    ).run

    while(p.isAlive()){
      Thread.sleep(1000)
      print(".")
    }

    println("convert "+fn+" -> "+out)
    
    out
  }

  def f(line:String)=line.split(",").toArray 

  def subsampling(fn:String,out:String)={
    val pathname = "Wheather/image/train/"+out
    val m = scala.io.Source.fromFile(fn).getLines.map(f).toArray
    var rfile = Array.ofDim[Int](580/2,600/2,3)

    val writer1 =  new java.io.PrintWriter(pathname)
    var ys = ""
    for(i <- 0 until 580;j <- 0 until 600; k <- 0 until 3){
      if(i % 2 == 0 && j %2 == 0){
        ys+= m(i)(j*3+k).toString+","
      }
    }
    writer1.write(ys)
    println("wrote -> "+out)
    writer1.close()
  }

  def fix(fn:String,file:String)={

    val m = scala.io.Source.fromFile(fn).getLines.map(f).toArray
    var colerfile =  Array.ofDim[Double](m.size,600,3)
    var rfile = Array.ofDim[Int](580/2,600/2,3)

    for(i <- 0 until m.size;j <- 0 until 600; k <- 0 until 3){
      colerfile(i)(j)(k) = m(i)(j*3+k).toDouble
    }

    for(i <- 0 until 580;j <- 0 until 600; k <- 0 until 3){
      if(i % 2==0 && j % 2 == 0){
        if(colerfile(i)(j)(0) > 250 && colerfile(i)(j)(1) < 50
          && colerfile(i)(j)(2) < 50 && k == 0 ){
          rfile(i/2)(j/2)(k)= colerfile(i)(j)(k).toInt
          rfile(i/2+1)(j/2)(k)= colerfile(i)(j)(k).toInt
          rfile(i/2+2)(j/2)(k)= colerfile(i)(j)(k).toInt
          rfile(i/2+3)(j/2)(k)= colerfile(i)(j)(k).toInt
          rfile(i/2)(j/2+1)(k)= colerfile(i)(j)(k).toInt
          rfile(i/2)(j/2+2)(k)= colerfile(i)(j)(k).toInt
          rfile(i/2)(j/2+3)(k)= colerfile(i)(j)(k).toInt
        }
        else {
          rfile(i/2)(j/2)(k) = 0
        }
      }
    }

    Image.write(path+"png/"+file+".png",rfile)

    path+"png/"+file+".png"
  }
}

object pickR{
  val filepath = "/home/share/chijou/"
  val path = "Wheather/"
  val py_path = "py_program/"
  val textpath = "Wheather/imgtext/"
  val testpath = path + "testtxet/"


  def f(line:String)=line.split(",").toArray

  def main2(fn:Array[String]){

    val line = scala.sys.process.Process("ls "+testpath ).lineStream.toArray


    val m = scala.io.Source.fromFile(testpath+line(16)).getLines.map(f).toArray
    var colerfile =  Array.ofDim[Double](m.size,600,3)
    var rfile = Array.ofDim[Int](560/2,600/2,3)

    for(i <- 0 until m.size;j <- 0 until m(0).size/3; k <- 0 until 3){
      colerfile(i)(j)(k) = m(i)(j*3+k).toDouble
    }

    for(i <- 0 until 580/2;j <- 0 until 600; k <- 0 until 3){
      if(i % 2==0 && j % 2 == 0){
        if(colerfile(i)(j)(0) > 250 && colerfile(i)(j)(1) < 50 && colerfile(i)(j)(2) < 50&& k == 0 ){
          rfile(i/2)(j/2)(k)= colerfile(i)(j)(k).toInt
          rfile(i/2+1)(j/2)(k)= colerfile(i)(j)(k).toInt
          rfile(i/2+2)(j/2)(k)= colerfile(i)(j)(k).toInt
          rfile(i/2+3)(j/2)(k)= colerfile(i)(j)(k).toInt
          rfile(i/2)(j/2+1)(k)= colerfile(i)(j)(k).toInt
          rfile(i/2)(j/2+2)(k)= colerfile(i)(j)(k).toInt
          rfile(i/2)(j/2+3)(k)= colerfile(i)(j)(k).toInt
          
        }
        else {
          rfile(i/2)(j/2)(k) = 0
        }
      }


    }
    Image.write("m.png",rfile)
  }
}

