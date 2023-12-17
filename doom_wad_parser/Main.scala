import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.scalajs.js.JSConverters._
import scala.util.{Try, Success, Failure}

import org.scalajs.dom
import org.scalajs.dom.html

@JSExportAll
case class ParseResult(
  wadImages: js.UndefOr[WadImages],
  timings: Timings
)

@JSExportAll
case class WadImages(
  sprites: js.Array[NamedImageInPngFormat],
  flats: js.Array[NamedImageInPngFormat],
  textures: js.Array[NamedImageInPngFormat],
  otherGraphics: js.Array[NamedImageInPngFormat]
)

@JSExportAll
case class NamedImageInPngFormat (
  name: String,
  imageAsBase64EncodedPng: String
)

@JSExportAll
case class Timings(
  timeToParseFile_in_ms: Int,
  timeToBuildImages_in_ms: Int
)

@JSExportTopLevel("Main")
object Main {

  // Use the default, possibly sub-optimal, scala.js execution context until we have a reason to do otherwise.
  // Make this use explicit to avoid the warning (detailed here: https://www.scala-js.org/news/2021/12/10/announcing-scalajs-1.8.0/)
  // that shows when instead using the more standard default "scala.concurrent.ExecutionContext.Implicits.global"
  implicit val executionContext: scala.concurrent.ExecutionContext = scala.scalajs.concurrent.JSExecutionContext.queue

  /**
    * Single entrypoint of this module, which allows the parsing of a Doom 1 WAD
    */
  @JSExport
  def parseDoomWadData(wadInBuffer: js.typedarray.ArrayBuffer): js.Promise[ParseResult] = {
    val timeAtStart = System.nanoTime()
    
    val wadBytes: Array[Byte] = new js.typedarray.Int8Array(wadInBuffer).toArray
    val rawParseResult: DoomWadParser.ParseResult[DoomWad] = DoomWadParser.parseDoomWad(new ByteReader(wadBytes))
    
    val timeAfterParsingFile = System.nanoTime()
    
    val parseResult = for {
      wadImages <- rawParseResult match {
        case DoomWadParser.Success(parsedWad, _) => extractAndFormatImages(parsedWad).map(Some(_))
        case _ => Future.successful(None)
      }
    } yield {
      val timeAfterBuildingImages = System.nanoTime()
      
      val timings = Timings(
        timeToParseFile_in_ms = ((timeAfterParsingFile - timeAtStart) / 1000000).toInt,
        timeToBuildImages_in_ms = ((timeAfterBuildingImages - timeAfterParsingFile) / 1000000).toInt
      )

      ParseResult(
        wadImages = wadImages.orUndefined,
        timings = timings
      )
    }

    parseResult.toJSPromise
  }
  
  lazy val standardDoomAssetsRequest: Future[DoomAssets] = {
    for (
      wadBytes <- getBytesFromURL("/assets/files/doom_wads/DOOM1.WAD")
     ) yield {
      val parseResult: DoomWadParser.ParseResult[DoomWad] = DoomWadParser.parseDoomWad(new ByteReader(wadBytes))
      parseResult match {
        case DoomWadParser.Success(wad: DoomIWad, _) => new DoomAssets(wad)
        case _ => new DoomAssets(new DoomIWad)
      }
    }
  }
  
  /**
    * Utility to retrieve the bytes contained inside a remote resource
    *
    * @param url
    * @return the complete Bytes of the resource that exists at the given URL, otherwise Failure if no such resource can be retrieved
    */
  def getBytesFromURL(url: String): Future[Array[Byte]] = {
    for {
      response <- dom.fetch(url).toFuture
      arrayBuffer <- response.arrayBuffer().toFuture
    } yield {
      new js.typedarray.Int8Array(arrayBuffer).toArray
    }
  }
    
  case class NamedImage(name: String, image: ConvertPixelsToPNG.Image)
    
  def imageFromGraphic(palette: DoomWad.Palette)(graphic: DoomWad.Graphic) = {
    NamedImage(graphic.name, new ConvertPixelsToPNG.Image {
      val width = graphic.width
      val height = graphic.height
      def apply(x: Int, y: Int): Int = DoomAssets.pixelToColorInt(graphic.pixels(y)(x), palette)
    })
  }
  
  def imageFromFlat(palette: DoomWad.Palette)(flat: DoomWad.Flat) = {
    NamedImage(flat.name, new ConvertPixelsToPNG.Image {
      val width = 64
      val height = 64
      def apply(x: Int, y: Int): Int = DoomAssets.pixelToColorInt(flat.pixels(y)(x), palette)
    })
  }
  
  // TODO: merge pNames and patches, so pName index produces a Patch with only one level of indirection
  def imageFromTexture(pNames: Vector[DoomWad.PName], patches: List[DoomWad.Graphic], palette: DoomWad.Palette)(texture: DoomWad.Texture) = {
    case class ReferencedPatch(patch: DoomWad.Graphic, xOffset: Int, yOffset: Int)
    
    val referencedPatches: List[ReferencedPatch] = for {
      patchReference <- texture.patchReferences
      patch <- {
        val namesOfReferencedPatch = pNames(patchReference.indexIntoPNames).name.toLowerCase
        patches find(_.name.toLowerCase == namesOfReferencedPatch)  // some patch names don't match exactly by case
      }          
    } yield ReferencedPatch(patch, patchReference.xOffset, patchReference.yOffset)
    
    if (referencedPatches.isEmpty) None  // avoid rendering textures that will just be transparent
    // (some Wad seem to have bad textures like this in them (http://www.doomworld.com/idgames/levels/doom/Ports/v-z/vengnce.zip, Texture)
    // ...little cost to keeping these textures in the WAD (probably tens of bytes), so probably left over stuff from development)
    else {
      Some(NamedImage(texture.name, new ConvertPixelsToPNG.Image {
        val width = texture.width
        val height = texture.height
        def apply(x: Int, y: Int): Int = {
          // search all referenced patches to the 'top-most' pixel that's not transparent
          val finalPixel = referencedPatches.foldLeft(DoomWad.Transparent().asInstanceOf[DoomWad.Pixel])((pixel, referencedPatch) => {
            val ReferencedPatch(patch, xOffset, yOffset) = referencedPatch
            val xInPatch = x - xOffset
            val yInPatch = y - yOffset
            if ((xInPatch >= 0 && xInPatch < patch.width) && (yInPatch >= 0 && yInPatch < patch.height)) {
              val patchPixel = patch.pixels(yInPatch)(xInPatch)
              if (patchPixel == DoomWad.Transparent()) pixel else patchPixel
            }
            else pixel // let the current pixel pass through
          })
          DoomAssets.pixelToColorInt(finalPixel, palette)
        }
      }))
    }
  }

  /**
    * Organize all images in a DoomWad and format them according the the PNG standard (for easy display on a webpage)
    *
    * @param doomWad - the DoomWad that we wish to extract images from
    * @return the result of extracting and formating the images contained in the DoomWad, Failure if an issue was encountered
    */
  def extractAndFormatImages(doomWad: DoomWad): Future[WadImages] = {
    def convertImageToPngFormat(namedImage: NamedImage): Future[NamedImageInPngFormat] = {
      for {
        pngBytes <- ConvertPixelsToPNG(namedImage.image)
      } yield {
        val pngInBase64 = java.util.Base64.getEncoder.encodeToString(pngBytes)
        
        NamedImageInPngFormat(
          name = namedImage.name,
          imageAsBase64EncodedPng = pngInBase64
        )
      }
    }
    
    for {
      standardDoomAssets <- standardDoomAssetsRequest
      (doomAssets, differenceToShow) = (doomWad match {
        case wad: DoomIWad => (new DoomAssets(wad), new DoomAssets.Difference(wad.textures, wad.flats, wad.sprites, wad.otherGraphics))
        case wad: DoomPWad => {
          val patchedAssets = standardDoomAssets applyPatch wad
          (patchedAssets, patchedAssets - standardDoomAssets)
        }
      })
      flatProcessor = (imageFromFlat(doomAssets.palettes.head) _)
      graphicsProcessor = (imageFromGraphic(doomAssets.palettes.head) _)
      textureProcessor = (imageFromTexture(doomAssets.pNames, doomAssets.patches, doomAssets.palettes.head) _)
      textures <- Future.sequence(
        for {
          texture <- differenceToShow.textures
          image <- textureProcessor(texture)
        } yield convertImageToPngFormat(image)
      )
      sprites <- Future.sequence(differenceToShow.sprites map {sprite => convertImageToPngFormat(graphicsProcessor(sprite))})
      flats <- Future.sequence(differenceToShow.flats map {flat => convertImageToPngFormat(flatProcessor(flat))})
      otherGraphics <- Future.sequence(differenceToShow.otherGraphics map {otherGraphic => convertImageToPngFormat(graphicsProcessor(otherGraphic))})
    } yield {

      WadImages(
        textures = js.Array(textures:_*),
        sprites = js.Array(sprites:_*),
        flats = js.Array(flats:_*),
        otherGraphics = js.Array(otherGraphics:_*)
      )
    }
  }
}

