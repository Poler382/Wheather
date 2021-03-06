import math._
import breeze.linalg._

/*
affine
ReLU
LeakyLeLU
tanh
Sigmoid
Pooling
Convolution
BacthNormalization 未完成　arrayVerを作る
AdamDM
AdamDV
object Image 
BacthNormalization3 Array.ver
*/

abstract class Layer {
  def forward(x:Array[Double]) : Array[Double]
  def forward(x:Array[Array[Double]]) : Array[Array[Double]]={
    var xs=new Array[Array[Double]](x.size)
    for(i <- 0 until x.size){
      xs(i)=forward(x(i))
    }
    xs
  }
  def backward(x:Array[Double]) : Array[Double]
  def backward(x:Array[Array[Double]]) :Array[Array[Double]]={
    var xs=new Array[Array[Double]](x.size)
    for(i <- x.size-1 to 0 by -1){
      xs(i)=backward(x(i))
    }
    xs
  }
  def update() : Unit
  def reset() : Unit
  def load(fn:String) {}
  def save(fn:String) {}
}


class Affine(val xn:Int, val yn:Int) extends Layer{
  val rand = new scala.util.Random(0)
  var W = DenseMatrix.zeros[Double](yn,xn).map(_ => rand.nextGaussian*0.01)
  for(i <- 0 until yn;j <- 0 until xn){
    W(i,j)=rand.nextGaussian*0.01
  }
  var b = DenseVector.zeros[Double](yn)
  var dW = DenseMatrix.zeros[Double](yn,xn)
  var db = DenseVector.zeros[Double](yn)
  var xs = List[Array[Double]]()
  var t=0
  def push(x:Array[Double]) = { xs ::= x; x }
  def pop() = { val x = xs.head; xs = xs.tail; x }

  def forward(x:Array[Double]) = {
    push(x)
    val xv = DenseVector(x)
    val y = W * xv + b
    y.toArray
  }

  def backward(d:Array[Double]) = {
    val x = pop()
    val dv = DenseVector(d)
    val X = DenseVector(x)
    // dW,dbを計算する ★
    dW += dv * X.t
    db += dv
    var dx = DenseVector.zeros[Double](xn)
    // dxを計算する ★
    dx = W.t * dv
    dx.toArray
  }
  var rt1=1d
  var rt2=1d
  var sW = DenseMatrix.zeros[Double](yn,xn)
  var rW = DenseMatrix.zeros[Double](yn,xn)
  var sb =  DenseVector.zeros[Double](yn)
  var rb =  DenseVector.zeros[Double](yn)

  def update() {
    // W,bを更新する ★
    val epsilon = 0.001
    val rho1=0.9
    val rho2=0.999
    val delta=0.000000001
    var d_tW =DenseMatrix.zeros[Double](yn,xn)
   
    var s_hW = DenseMatrix.zeros[Double](yn,xn)
    var r_hW = DenseMatrix.zeros[Double](yn,xn)

    var d_tb = DenseVector.zeros[Double](yn)
    var s_hb =  DenseVector.zeros[Double](yn)
    var r_hb =  DenseVector.zeros[Double](yn)

    rt1=rt1*rho1
    rt2=rt2*rho2
    t=t+1
   
    for(i <- 0 until yn){
      sb(i) = rho1*sb(i)+ (1 - rho1)*db(i)
      rb(i) = rho2*rb(i) + (1 - rho2)*db(i)*db(i)
      s_hb(i) = sb(i)/(1-rt1)
      r_hb(i) = rb(i)/(1-rt2)
      d_tb(i) = - epsilon * (s_hb(i)/(Math.sqrt(r_hb(i))+delta))
      b(i) = b(i) + d_tb(i)
      for(j <- 0 until xn){
        sW(i,j) =  rho1*sW(i,j) + (1 - rho1)*dW(i,j)
        rW(i,j) =  rho2*rW(i,j) + (1 - rho2)*dW(i,j)*dW(i,j)
        s_hW(i,j) = sW(i,j)/(1-rt1)
        r_hW(i,j) = rW(i,j)/(1-rt2)
        d_tW(i,j) = - epsilon * (s_hW(i,j) /(Math.sqrt(r_hW(i,j))+delta))
        W(i,j) = W(i,j) + d_tW(i,j)
      }
    }
       reset()
  }
  def update_sgd(){
    val lr=0.01
    W -= lr * dW
    b -= lr * db
    reset()
  }
  def reset() {
    dW = DenseMatrix.zeros[Double](yn,xn)
    db = DenseVector.zeros[Double](yn)
    xs = List[Array[Double]]()
  }

 
  override def load(fn:String) {
    println(fn)
    val f =scala.io.Source.fromFile(fn).getLines.map(_.split(",").map(_.toDouble).toArray).toArray
    for(i <- 0 until yn; j <- 0 until xn){
      W(i,j)=f(0)(i*xn+j)
    }
    for(i <- 0 until yn){
      b(i)=f(1)(i)
    }
  }
  override def save(fn:String) {
    val file=new java.io.PrintWriter(fn)
    for(i <- 0 until yn; j <- 0 until xn){
      if(i == yn-1 && j== xn-1){
        file.print(W(i,j))
      }
      else{
        file.print(W(i,j)+",")
      }
    }
    file.println()
    for(i <- 0 until yn){
      if(i == yn-1){file.print(b(i))}
      else{
        file.print(b(i)+",")
      }
    }
    file.close
  }
}


class ReLU() extends Layer {
  var ys = List[Array[Double]]()
  def push(y:Array[Double]) = { ys ::= y; y }
  def pop() = { val y = ys.head; ys = ys.tail; y }

  def forward(x:Array[Double]) = {
    push(x.map(a => math.max(a,0)))
  }

  def backward(d:Array[Double]) = {
    val y = pop()
    (0 until d.size).map(i => if(y(i) > 0) d(i) else 0d).toArray
  }

  def update() {reset()}
  def reset() {ys = List[Array[Double]]()}

  override def save(fn:String){}
  override def load(fn:String){}

}

class LeakyReLU() extends Layer{
  var ys = List[Array[Double]]()
  def push(y:Array[Double]) = { ys ::= y; y }
  def pop() = { val y = ys.head; ys = ys.tail; y }

  def forward(x:Array[Double]) = {
    var returny = new Array[Double](x.size)
    for(i <- 0 until x.size){
      if(x(i) > 0){ 
        returny(i) = x(i) 
      }else{
        returny(i) = x(i)*0.2
      }
    }
    push(returny)
  }

  def backward(d:Array[Double]) = {
    val y = pop()
    (0 until d.size).map(i => if(y(i) > 0) d(i) else d(i)*0.2).toArray
  }

  def update() {reset()}
  def reset() {ys = List[Array[Double]]()}

  override def save(fn:String){}
  override def load(fn:String){}
}



class Tanh() extends Layer{
  var ys = List[DenseVector[Double]]()
  def tanh(x:Double) = (math.exp(x)-math.exp(-x))/(math.exp(x)+math.exp(-x))
  def forward(xx:Array[Double]) = {
    val x = DenseVector(xx)
    ys ::= x.map(tanh)
    ys.head.toArray
  }

  def backward(d:Array[Double]) = {
    val y = ys.head
    ys = ys.tail
    val ds =DenseVector(d)
    val r = ds *:* (1d - y*y)
    r.toArray
  }

  def update() {
    reset()
  }

  def reset() {
    ys = List[DenseVector[Double]]()
  }
  override def save(fn:String){}
  override def load(fn:String){}

}

class Sigmoid() extends Layer{
  var ys = List[DenseVector[Double]]()
  def sigmoid(x:Double) = 1 / (1 + math.exp(-x))
  def forward(xx:Array[Double]) = {
    val x = DenseVector(xx)
    ys ::= x.map(sigmoid)
    ys.head.toArray
  }

  def backward(d:Array[Double]) = {
    val ds = DenseVector(d)

    val y = ys.head
    ys = ys.tail
    val r =ds *:* y *:* (1d - y)
  
    r.toArray
  }
  def update()={
    reset()
  }
  def reset()={
    ys = List[DenseVector[Double]]()
  }

 
 override def save(fn:String){}
 override def load(fn:String){}

}

class Pooling(val BW:Int, val IC:Int, val IH:Int, val IW:Int) extends Layer{
  val OH = IH / BW
  val OW = IW / BW
  val OC = IC
  var masks = List[Array[Double]]()
  def push(x:Array[Double]) = { masks ::= x; x }
  def pop() = { val mask = masks.head; masks = masks.tail; mask }

  def iindex(i:Int, j:Int, k:Int) = i * IH * IW + j * IW + k
  def oindex(i:Int, j:Int, k:Int) = i * OH * OW + j * OW + k
  
  def forward(X:Array[Double]) = {
    val mask = push(Array.ofDim[Double](IC * IH * IW))
    val Z = Array.ofDim[Double](OC * OH * OW)
    for(i <- 0 until OC; j <- 0 until OH; k <- 0 until OW) {
      var v = Double.NegativeInfinity
      var row_max = -1
      var col_max = -1
      for(m <- 0 until BW; n <- 0 until BW if v < X(iindex(i,j*BW+m,k*BW+n))) {
        row_max = j*BW+m
        col_max = k*BW+n
        v = X(iindex(i,j*BW+m,k*BW+n))
      }
      mask(iindex(i,row_max,col_max)) = 1
      Z(oindex(i,j,k)) = v
    }
    Z
  }

  def backward(d:Array[Double]) = {
    val mask = pop()
    val D = Array.ofDim[Double](mask.size)
    for(i <- 0 until OC; j <- 0 until OH; k <- 0 until OW) {
      for(m <- 0 until BW; n <- 0 until BW if mask(iindex(i,j*BW+m,k*BW+n)) == 1) {
        D(iindex(i,j*BW+m,k*BW+n)) = d(oindex(i,j,k))
      }
    }
    D
  }

  def update() {
    reset()
  }

  def reset() {
    masks = List[Array[Double]]()
  }

  override def save(fn:String){}
  override def load(fn:String){}
}


class Convolution(
  val I:Int, //入力チャネル数
  val H:Int, //入力の高さ
  val W:Int, //入力の幅
  val O:Int, //出力チャネル数
  val kw:Int //カーネルの幅
) extends Layer {
  val rand = new scala.util.Random(0)
  var K=Array.ofDim[Double](O,I,kw*kw).map(_.map(_.map(a => rand.nextGaussian*0.01)))
  var t=0
  var V2=Array[Double]()
  val w_d=W-kw+1
  val h_d=H-kw+1
  var d_k=Array.ofDim[Double](O,I,kw*kw)

  def vind(i:Int,j:Int,k:Int)=i*H*W+j*W+k
  def zind(i:Int,j:Int,k:Int)=i*(H-kw+1)*(W-kw+1)+j*(W-kw+1)+k

   override def save(fn:String){
    val save = new java.io.PrintWriter("biasdata/"+fn+"-"+I.toString+H.toString+O.toString+W.toString+kw.toString+"Conv.txt")
    for(i<-0 until O ; j<-0 until I;k <- 0 until kw*kw){
      save.println(K(i)(j)(k))
    }
    save.close
  }

   override def load(fn:String){
    val f = scala.io.Source.fromFile("biasdata"+fn+"-"+I.toString+H.toString+O.toString+W.toString+kw.toString+"Conv.txt").getLines.toArray
    for(i<-0 until O ; j<-0 until I ; k<-0 until kw* kw){
      K(i)(j)(k) = f(i*I*kw*kw + j*kw*kw + k*kw ).toDouble
    }
  }



  def forward(V:Array[Double])={
    V2=V
    val Z=Array.ofDim[Double](O*h_d*w_d)
    for(i <- 0 until O ; j <- 0 until h_d; k <- 0 until w_d){
      var s=0d
      for(l <- 0 until I ; m <- 0 until kw ; n <- 0 until  kw){
        s += V(vind(l,j+m,k+n))*K(i)(l)(m*kw+n)
      }
      Z(zind(i,j,k))=s
    }
    Z
  }

  def backward(G:Array[Double])={
    var d_v=Array.ofDim[Double](I*H*W)
    for(i <- 0 until O ;j <- 0 until I ;k <- 0 until kw;l <- 0 until kw){
      var s_k=0d
      for(m <- 0 until h_d; n <- 0 until w_d){
        s_k += G(zind(i,m,n))*V2(vind(j,m+k,n+l))
      }
      d_k(i)(j)(k * kw + l)=s_k
    }
    for(i <- 0 until I;j <- 0 until H;k <- 0 until W){
      var s_v=0d
      for(l <- 0 until h_d;m <- 0 until kw if l+m == j){
        for(n <- 0 until w_d;p <- 0 until kw if n+p ==k){
          for(q <- 0 until O){
            s_v += K(q)(i)(m*kw+p)*G(zind(q,l,n))
          }
        }
        d_v(vind(i,j,k))=s_v
      }
    }
    d_v
  }
  var rt1=1d
  var rt2=1d
  var s=Array.ofDim[Double](O,I,kw*kw)
  var r=Array.ofDim[Double](O,I,kw*kw)
  
  def update()={
    val epsilon = 0.001
    val rho1=0.9
    val rho2=0.999
    val delta=0.000000001
    var d_t=Array.ofDim[Double](O,I,kw*kw)
    var s_h=Array.ofDim[Double](O,I,kw*kw)
    var r_h=Array.ofDim[Double](O,I,kw*kw)
    rt1=rt1*rho1
    rt2=rt2*rho2
    t=t+1
    for(i <- 0 until O; j <- 0 until I; k <- 0 until kw*kw){
      s(i)(j)(k) = rho1*s(i)(j)(k) + (1 - rho1)*d_k(i)(j)(k)
      r(i)(j)(k) = rho2*r(i)(j)(k) + (1 - rho2)*d_k(i)(j)(k)*d_k(i)(j)(k)
      s_h(i)(j)(k) = s(i)(j)(k)/(1-rt1)
      r_h(i)(j)(k) = r(i)(j)(k)/(1-rt2)
      d_t(i)(j)(k) = - epsilon * (s_h(i)(j)(k)/(math.sqrt(r_h(i)(j)(k))+delta))
      K(i)(j)(k) = K(i)(j)(k) + d_t(i)(j)(k)
    }
    reset()
  }

  def reset() {
    d_k=Array.ofDim[Double](O,I,kw*kw)
  }
}
/*
//D:各データの個数　データ数n
class BatchNormalization(val D:Int,val n: Int) extends Layer{
  var xn = D
  var gamma   = DenseVector.ones[Double](D)
  var beta    = DenseVector.zeros[Double](D)
  var d_beta  = DenseVector.zeros[Double](D)
  var d_gamma = DenseVector.zeros[Double](D)
  var mu      = DenseVector.zeros[Double](D)
  var eps     = 0.00000001
  var sigma   = DenseVector.zeros[Double](D)
  var x_h     = new Array[DenseVector[Double]](D)
  var x_m     = new Array[DenseVector[Double]](D)
  var xmu     = new Array[DenseVector[Double]](D)
  var dg      = DenseVector.zeros[Double](xn)
  var db      = DenseVector.zeros[Double](xn)
  var count   = 0

  def forward(x:Array[Double] )={x}

  def backward(x:Array[Double] )={x}


  def forward2(xs:Array[DenseVector[Double]] )={

    
    var y = new Array[DenseVector[Double]](xs.size)

    for(i <- 0 until xs.size){
      for (j <- 0 until xn){
        mu(j)+=xs(i)(j)/xs.size
      }
    }

    x_m = new Array[DenseVector[Double]](xs.size)
    
    for(i <- 0 until xs.size){
      x_m(i)=DenseVector.zeros[Double](xn)
      for(j <- 0 until xn){
        x_m(i)(j) = xs(i)(j) - mu(j)
  
        sigma(j) += x_m(i)(j)* x_m(i)(j)/xs.size
      }
    }

    x_h = new Array[DenseVector[Double]](xs.size)
    for(i <- 0 until xs.size){
      y(i)=DenseVector.zeros[Double](xn)
      x_h(i)=DenseVector.zeros[Double](xn)
      for (j <- 0 until xn){
        x_h(i)(j)= (x_m(i)(j))/math.sqrt(sigma(j)+eps)
        y(i)(j)=gamma(j)*x_h(i)(j)+beta(j)
      }
    }
    y
  }



//たてよこの行列を潰して各データのsumに変える
  def sumMatrix(in:Array[DenseVector[Double]])={
    var m  = DenseVector.zeros[Double](D)
    for(j <- 0 until D){
      for(i <- 0 until n){
        m(j) += in(i)(j)
      }
    }
    m
  }

  def backward2(d:Array[DenseVector[Double]])={

   

    var d_beta = sumMatrix(d)
    var dx = new Array[DenseVector[Double]](n)
  
    for(i <- 0 until n){
      dx(i) = DenseVector.zeros[Double](D)
    }

    for(j <- 0 until D){
      for(i <- 0 until n){
        d_gamma(j) += d(i)(j) * x_h(i)(j)
      }
    }

    //各次元ごとに計算
    for(j <- 0 until D ){
      var d2 = 0d
      var d1 = DenseVector.zeros[Double](n)
      for(i <- 0 until n ){
        d1(i) = gamma(j) * d(i)(j)
        d2 += x_m(i)(j) *d1(i)
      }
      
      var d3 = - d2 / (sigma(j)+eps) 
      var d4 = d3 / (2*sqrt(sigma(j)+eps))

      var d8 = 0d
      var d6 = DenseVector.zeros[Double](n)
      var d7 = DenseVector.zeros[Double](n)
     
      for(i <- 0 until n){
        var d5 = 1 / n.toDouble *d4
        d6(i) = 2 * x_m(i)(j) * d5
        d7(i) = d1(i) * 1/sqrt(sigma(j)+eps)
        d8 -= d6(i) + d7(i)
      }
      for(i <- 0 until n){
        var d9 = 1 / n.toDouble * d8
        var d10 = d6(i)+d7(i)
        dx(i)(j) = d9 + d10
      }

    }
    dx
  }
  var adam_b = new Adam_DV(xn)
  var adam_g = new Adam_DV(xn)
  def update(){
    adam_b.update(beta,d_beta,n)
    adam_g.update(gamma,d_gamma,n)
    reset()
  }
  def reset(){
    db = DenseVector.zeros[Double](xn)
    dg = DenseVector.zeros[Double](xn)
    count=0
  }

  override def save(fn:String){}
  override def load(fn:String){}

}

 */

class Adam_DM(val rows:Int, val cols:Int) {
  val eps = 0.0002
  val delta = 1e-8
  val rho1 = 0.5
  val rho2 = 0.999
  var rho1t = 1d
  var rho2t = 1d
  var s = DenseMatrix.zeros[Double](rows,cols)
  var r = DenseMatrix.zeros[Double](rows,cols)

  def update(K:DenseMatrix[Double], dK:DenseMatrix[Double],count:Int) = {
    rho1t *= rho1
    rho2t *= rho2
    val rho1tr = 1 / (1 - rho1t)
    val rho2tr = 1 / (1 - rho2t)
    for(i <- 0 until K.rows; j <- 0 until K.cols) {
      s(i,j) = rho1 * s(i,j) + (1 - rho1) * dK(i,j)
      r(i,j) = rho2 * r(i,j) + (1 - rho2) * dK(i,j) * dK(i,j)
      val d = (s(i,j) * rho1tr) / (math.sqrt(r(i,j) * rho2tr) + delta)
      K(i,j) = K(i,j) - eps/count * d
    }
  }
}
class Adam_DV(val n:Int) {
  val eps = 0.0002
  val delta = 1e-8
  val rho1 = 0.5
  val rho2 = 0.999
  var rho1t = 1d
  var rho2t = 1d
  var s = DenseVector.zeros[Double](n)
  var r = DenseVector.zeros[Double](n)

  def update(K:DenseVector[Double], dK:DenseVector[Double],count:Int) = {
    rho1t *= rho1
    rho2t *= rho2
    val rho1tr = 1 / (1 - rho1t)
    val rho2tr = 1 / (1 - rho2t)
    for(i <- 0 until K.size) {
      s(i) = rho1 * s(i) + (1 - rho1) * dK(i)
      r(i) = rho2 * r(i) + (1 - rho2) * dK(i) * dK(i)
      val d = (s(i) * rho1tr) / (math.sqrt(r(i) * rho2tr) + delta)
      K(i) = K(i) - eps/count * d
    }
  }
}


 

object Image {
  def rgb(im : java.awt.image.BufferedImage, i:Int, j:Int) = {
    val c = im.getRGB(i,j)
    Array(c >> 16 & 0xff, c >> 8 & 0xff, c & 0xff)
  }

  def pixel(r:Int, g:Int, b:Int) = {
    val a = 0xff
    ((a & 0xff) << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff)
  }

  def read(fn:String) = {
    val im = javax.imageio.ImageIO.read(new java.io.File(fn))
    (for(i <- 0 until im.getHeight; j <- 0 until im.getWidth)
    yield rgb(im, j, i)).toArray.grouped(im.getWidth).toArray
  }

  def write(fn:String, b:Array[Array[Array[Int]]]) = {
    val w = b(0).size
    val h = b.size
    val im = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_RGB);
    for(i <- 0 until im.getHeight; j <- 0 until im.getWidth) {
      im.setRGB(j,i,pixel(b(i)(j)(0), b(i)(j)(1), b(i)(j)(2)));
    }
    javax.imageio.ImageIO.write(im, "png", new java.io.File(fn))
  }

  def make_image2(ys:Array[Array[Double]], NW:Int, NH:Int, H:Int, W:Int) = {
    val im = Array.ofDim[Int](NH * H, NW * W, 3)
    val ymax = ys.flatten.max
    val ymin = ys.flatten.min
    def f(a:Double) = ((a - ymin) / (ymax - ymin) * 255).toInt
    for(i <- 0 until NH; j <- 0 until NW) {
      for(p <- 0 until H; q <- 0 until W; k <- 0 until 3) {//k * H * W +
        im(i * H + p)(j * W + q)(k) = f(ys(i * NW + j)( p * W + q))
      }
    }
    im
  }


  def make_image3(ys:Array[Array[Double]], NW:Int, NH:Int, H:Int, W:Int) = {
    val im = Array.ofDim[Int](NH * H, NW * W, 3)
    /*  val ymax = ys.flatten.max
     val ymin = ys.flatten.min*/
    def f(a:Double) = (a*255).toInt

    for(i <- 0 until NH; j <- 0 until NW) {
      for(p <- 0 until H; q <- 0 until W; k <- 0 until 3) {
        im(i * H + p)(j * W + q)(k) = f(ys(i * NW + j)( p * W + q))
      }
    }
    im
  }

  //三色用

  def make_image(ys:Array[Array[Double]], NW:Int, NH:Int, H:Int, W:Int) = {
    val im = Array.ofDim[Int](NH * H, NW * W, 3)
    val ymax = ys.flatten.max
    val ymin = ys.flatten.min
    def f(a:Double) = ((a - ymin) / (ymax - ymin) * 255).toInt
    for(i <- 0 until NH; j <- 0 until NW) {
      for(p <- 0 until H; q <- 0 until W; k <- 0 until 3) {
        im(i * H + p)(j * W + q)(k) = f(ys(i * NW + j)(k * H * W + p * W + q))
      }
    }
    im
  }

  def to3DArrayOfColor(image:Array[Double],h:Int,w:Int) = {
    val input = image.map(_*256)
    var output = List[Array[Array[Double]]]()
    for(i <- 0 until h) {
      var row = List[Array[Double]]()
      for(j <- 0 until w) {
        val red = input(i*w+j)
        val green = input(i*w+j+h*w)
        val blue = input(i*w+j+h*w*2)
        row ::= Array(red,green,blue)
      }
      output ::= row.reverse.toArray
    }
    output.reverse.toArray.map(_.map(_.map(_.toInt)))
  }



}

//Array version
//n個データがきたら更新する
//D:各データの個数　データ数n
class BatchNormalization3(val D:Int,val n: Int) extends Layer{
  var xn = D
  var gamma   = new Array[Double](D).map(_ => 1d)
  var beta    = new Array[Double](D).map(_ => 0d)
  var d_beta  = new Array[Double](D).map(_ => 0d)
  var d_gamma = new Array[Double](D).map(_ => 0d)
  var mu      = new Array[Double](D).map(_ => 0d)
  var eps     = 0.00000001
  var sigma   = new Array[Double](D).map(_ => 0d)
  var x_h     = new Array[Array[Double]](D).map(_.map(_ => 0d))
  var x_m     = new Array[Array[Double]](D).map(_.map(_ => 0d))
  var xmu     = new Array[Array[Double]](D).map(_.map(_ => 0d))
  var dg      = new Array[Double](xn)
  var db      = new Array[Double](xn)
  var count   = 0

  def forward(xs:Array[Double])=xs
  def backward(xs:Array[Double])=xs

  override def forward(xs:Array[Array[Double]] )={
    var y = new Array[Array[Double]](xs.size)

    for(i <- 0 until xs.size){
      for (j <- 0 until xn){
        mu(j)+=xs(i)(j)/xs.size
      }
    }

    x_m = new Array[Array[Double]](xs.size)
    
    for(i <- 0 until xs.size){
      x_m(i)=new Array[Double](xn).map(_ => 0d)
      for(j <- 0 until xn){
        x_m(i)(j) = xs(i)(j) - mu(j)
  
        sigma(j) += x_m(i)(j)* x_m(i)(j)/xs.size
      }
    }

    x_h = new Array[Array[Double]](xs.size).map(_.map(_ => 0d))
    for(i <- 0 until xs.size){
      y(i)   = new Array[Double](xn).map(_ => 0d)
      x_h(i) = new Array[Double](xn).map(_ => 0d)
      for (j <- 0 until xn){
        x_h(i)(j)= (x_m(i)(j))/math.sqrt(sigma(j)+eps)
        y(i)(j)=gamma(j)*x_h(i)(j)+beta(j)
      }
    }
    y
  }



  //たてよこの行列を潰して各データのsumに変える
  def sumMatrix(in:Array[Array[Double]])={
    var m  = new Array[Double](D)
    for(j <- 0 until D){
      for(i <- 0 until n){
        m(j) += in(i)(j)
      }
    }
    m
  }

  override def backward(d:Array[Array[Double]])={

    var d_beta = sumMatrix(d)
    var dx = new Array[Array[Double]](n)
  
    for(i <- 0 until n){
      dx(i) = new Array[Double](D).map(_ => 0d)
    }

    for(j <- 0 until D){
      for(i <- 0 until n){
        d_gamma(j) += d(i)(j) * x_h(i)(j)
      }
    }

    //各次元ごとに計算
    for(j <- 0 until D ){
      var d2 = 0d
      var d1 = new Array[Double](n)
      for(i <- 0 until n ){
        d1(i) = gamma(j) * d(i)(j)
        d2 += x_m(i)(j) *d1(i)
      }
      
      var d3 = - d2 / (sigma(j)+eps) 
      var d4 = d3 / (2*sqrt(sigma(j)+eps))

      var d8 = 0d
      var d6 = new Array[Double](n)
      var d7 = new Array[Double](n)
     
      for(i <- 0 until n){
        var d5 = 1 / n.toDouble *d4
        d6(i) = 2 * x_m(i)(j) * d5
        d7(i) = d1(i) * 1/sqrt(sigma(j)+eps)
        d8 -= d6(i) + d7(i)
      }
      for(i <- 0 until n){
        var d9 = 1 / n.toDouble * d8
        var d10 = d6(i)+d7(i)
        dx(i)(j) = d9 + d10
      }

    }
    dx
  }
  var adam_b = new Adam_D(xn)
  var adam_g = new Adam_D(xn)
  def update(){
    adam_b.update(beta,d_beta,n)
    adam_g.update(gamma,d_gamma,n)
    reset()
  }
  def reset(){
    db = new Array[Double](xn)
    dg = new Array[Double](xn)
    count=0
  }

  override def save(fn:String){}
  override def load(fn:String){}

}

class BNsaki(val xn:Int) extends Layer {
  val epsilon=10E-8
  var beta = Array.ofDim[Double](xn)
  var gamma = Array.ofDim[Double](xn)
  for(i <- 0 until xn){gamma(i)=1d}
  var mu = Array.ofDim[Double](xn)
  var sigma = Array.ofDim[Double](xn)
  var x_h = new Array[Array[Double]](1)
  var x_m = new Array[Array[Double]](1)
  var dg =  Array.ofDim[Double](xn)
  var db =  Array.ofDim[Double](xn)
  var count=0
  override def forward(xs:Array[Array[Double]])={
    var y = new Array[Array[Double]](xs.size)
 
    for(i <- 0 until xs.size){
      for (j <- 0 until xn){
        mu(j)+=xs(i)(j)/xs.size
      }
    }
    x_m=new Array[Array[Double]](xs.size)
    for(i <- 0 until xs.size){
      x_m(i)=Array.ofDim[Double](xn)
      for(j <- 0 until xn){
        x_m(i)(j) = xs(i)(j)-mu(j)
        sigma(j) += x_m(i)(j)* x_m(i)(j)/xs.size
      }
    }
    x_h=new Array[Array[Double]](xs.size)
    for(i <- 0 until xs.size){
      y(i)=Array.ofDim[Double](xn)
      x_h(i)=Array.ofDim[Double](xn)
      for (j <- 0 until xn){
        x_h(i)(j)= (x_m(i)(j))/math.sqrt(sigma(j)+epsilon)
        y(i)(j)=gamma(j)*x_h(i)(j)+beta(j)
      }
    }
    y
  }

  def forward(xs:Array[Double])=xs
  def backward(xs:Array[Double])=xs
  override def backward(ds:Array[Array[Double]])={
    var dx = new Array[Array[Double]](ds.size)
    var d1 = new Array[Array[Double]](ds.size)
    var d2 = Array.ofDim[Double](xn)
    var d6 = new Array[Array[Double]](ds.size)
    var d7 = new Array[Array[Double]](ds.size)
    var d8 = Array.ofDim[Double](xn)
    var d10 = new Array[Array[Double]](ds.size)

  for(i <- 0 until ds.size){
    d6(i)=Array.ofDim[Double](xn)
    d7(i)=Array.ofDim[Double](xn)
    d1(i)=Array.ofDim[Double](xn)
      for(j <- 0 until xn){
        d1(i)(j)=ds(i)(j)*gamma(j)
        d2(j) += d1(i)(j)*x_m(i)(j)
      }
    }
   for(j <- 0 until xn){
     var d3=d2(j)*(-1d)/(sigma(j)+epsilon)
     var d4=(1d/(2*(math.sqrt(sigma(j)+epsilon))))*d3
     for(i <- 0 until ds.size){
      
       d10(i)=Array.ofDim[Double](xn)
       dx(i)=Array.ofDim[Double](xn)
       var d5=d4/ds.size
       d6(i)(j)=2*x_m(i)(j)*d5
       d7(i)(j)=d1(i)(j)/(math.sqrt(sigma(j)+epsilon))
       d8(j) += -(d6(i)(j)+d7(i)(j))
     }
   }
   for(i <- 0 until ds.size;j <- 0 until xn){
     var d9 = d8(j)/ds.size
     d10(i)(j) = d6(i)(j)+d7(i)(j)
     dx(i)(j) = d9+d10(i)(j)
     dg(j) += ds(i)(j) * x_h(i)(j)
     db(j) += ds(i)(j)
   }
   count=ds.size
   dx
  }

  var adam_b = new Adam_D(xn)
  var adam_g = new Adam_D(xn)
  def update(){
    adam_b.update(beta,db,count)
    adam_g.update(gamma,dg,count)
    reset()
  }
  def reset(){
    db = Array.ofDim[Double](xn)
    dg = Array.ofDim[Double](xn)
    count=0
  }
}

class Adam_DA(val rows:Int, val cols:Int) {
  val eps = 0.0002
  val delta = 1e-8
  val rho1 = 0.5
  val rho2 = 0.999
  var rho1t = 1d
  var rho2t = 1d
  var s = Array.ofDim[Double](rows,cols)
  var r = Array.ofDim[Double](rows,cols)

  def update(K:Array[Array[Double]], dK:Array[Array[Double]],count:Int) = {
    rho1t *= rho1
    rho2t *= rho2
    val rho1tr = 1 / (1 - rho1t)
    val rho2tr = 1 / (1 - rho2t)
    for(i <- 0 until K.size; j <- 0 until K(0).size) {
      s(i)(j) = rho1 * s(i)(j) + (1 - rho1) * dK(i)(j)
      r(i)(j) = rho2 * r(i)(j) + (1 - rho2) * dK(i)(j) * dK(i)(j)
      val d = (s(i)(j) * rho1tr) / (math.sqrt(r(i)(j) * rho2tr) + delta)
      K(i)(j) = K(i)(j) - eps/count * d
    }
  }
}
class Adam_D(val n:Int) {
  val eps = 0.0002
  val delta = 1e-8
  val rho1 = 0.5
  val rho2 = 0.999
  var rho1t = 1d
  var rho2t = 1d
  var s = new Array[Double](n)
  var r = new Array[Double](n)

  def update(K:Array[Double], dK:Array[Double],count:Int) = {
    rho1t *= rho1
    rho2t *= rho2
    val rho1tr = 1 / (1 - rho1t)
    val rho2tr = 1 / (1 - rho2t)
    for(i <- 0 until K.size) {
      s(i) = rho1 * s(i) + (1 - rho1) * dK(i)
      r(i) = rho2 * r(i) + (1 - rho2) * dK(i) * dK(i)
      val d = (s(i) * rho1tr) / (math.sqrt(r(i) * rho2tr) + delta)
      K(i) = K(i) - eps/count * d
    }
  }
}

