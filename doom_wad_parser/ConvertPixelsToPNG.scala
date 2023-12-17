import scala.scalajs.js
import scala.concurrent.{Future, ExecutionContext}

import collection.JavaConverters._
import org.scalajs.dom

object ConvertPixelsToPNG {
  
  // An interface for what constitues an 'image' that will be convertable to a PNG
  trait Image {
    def width: Int
    def height: Int

    /**
      * Returns the color value in this image for a specific pixel, specified by the coordinates (x, y),
      * where (0, 0) is the top left of the image.
      * 
      * The returned value is an Int in RGBA format, there is one byte for each of the three color channels, and one byte for alpha.
      * So in Hex the related parts of the returned Int are 0xRRGGBBAA.
      * 
      * Its assume that the caller will never call this function with an x outside the range [0, width - 1] or with an y value outside of the range [0, height - 1]
      *
      * @param x
      * @param y
      * @return the color at the pixel, in RGBA format, one byte for each channel
      */
    def apply(x: Int, y: Int): Int
  }
  
  /**
    * Convert an Image into the PNG format (https://en.wikipedia.org/wiki/PNG), returning the resultant bytes
    *
    * @param image - the image to be converted
    * @return a Future that resolves to the Bytes of the image when converted to PNG
    */
  def apply(image: Image)(implicit ec: ExecutionContext): Future[Array[Byte]] = {
    // Algorithm outline: the image is written to an OffscreenCanvas, and then the OffscreenCanvas is converted to a PNG blob

    val offscreenCanvas: dom.OffscreenCanvas = new dom.OffscreenCanvas(width = image.width, height = image.height)
    val ctx = offscreenCanvas.getContext("2d")

    for {
      x <- (0 until image.width)
      y <- (0 until image.height)
    } {
      val colorAtPixel = image(x, y)
      // Note: some time was spent attempting to use ImageData (https://developer.mozilla.org/en-US/docs/Web/API/ImageData)
      // to write the per-pixel color data to the offscreen canvas, but that approach never managed to work as needed.
      //
      // So instead we're resorting to filling in one 1x1 rectangle per pixel, which works and is performant enough!
      ctx.fillStyle = f"#${colorAtPixel}%08X"
      ctx.fillRect(x, y, 1, 1)
    }

    for {
      imageBlob: dom.Blob <- offscreenCanvas.convertToBlob(new dom.ConvertToBlobOptions { `type` = "image/png" }).toFuture
      imageData <- imageBlob.arrayBuffer.toFuture
    } yield {
      new js.typedarray.Int8Array(imageData).toArray
    }  
  }
}