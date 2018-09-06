import sys.process._

object  wether {


  def  main(args: Array[String]): Unit = {
    val (d,t) = loadfile("learndate/may_")
    val epoch = 5
    val net = select("test")
    val L = new ML()
    for(i <- 0 until epoch){
      val raw = d(i)
      val cr = t(i)
      val d = L.forwards(net,raw)
      L.backwards(net,d.zip(cr).map{case(a,b) => (a-b)*(a-b)})

      println(raw.size)
      println(cr.size)
      val mse = d.zip(cr).map{case(a,b) => (a-b)*(a-b)}.sum / raw.size
     Image.write("y"+i.toString+".png",toRGBArray(d,145,150))
     Image.write("s"+i.toString+".png",toRGBArray(cr,145,150))
     println(i,mse)
     if(i%10==0){
       L.updates(net)
     }

    }
  }

  def loadfile(fn:String) = {
    def fd(line:String) = line.split(",").map(_.toDouble).toArray
    def ft(line:String) = line.split(",").map(_.toDouble).toArray
    var d = scala.io.Source.fromFile(fn+"raw.txt").getLines.map(fd).toArray
    var t = scala.io.Source.fromFile(fn+"crect.txt").getLines.map(ft).toArray

    (d,t)
  }



  def toRGBArray(im: Array[Double],h:Int,w:Int)={

    var rim = Array.ofDim[Int](h,w,3)
    var c=0
    for(i<- 0 until h; j<- 0 until w; r<- 0 until 3){
      rim(i)(j)(r)=im(c).toInt
  //  println(i,j,r,c)
      c+=1
    }
    rim
  }

  def select(mode:String)={
    val d = mode match {
      case "test"=>{
        val a= new Affine(65250,100)
        val b = new ReLU()
        val c= new Affine(100,65250)
        List(a,b,c)
      }
    }
    d
  }

}
