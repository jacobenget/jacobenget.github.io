import java.io.File
import java.nio.file.{FileSystems, Files, Path, Paths, StandardCopyOption}
import scala.collection.JavaConverters._
import sbt.Keys.streams

// A utility for copying all files from one directory to another, overwritting existing files in the case of name conflicts.
// This utility assumes both the source and destination diretories already exists, and will fail if that's not the case.
def copyAllFilesInDirectory(sourceDir: Path, destDir: Path, log: sbt.util.Logger): Unit = {
  log.info(s"Copying all files in")
  log.info(s"  ${sourceDir.normalize}")
  log.info(s"to")
  log.info(s"  ${destDir.normalize}")

  require(Files.exists(sourceDir), s"Source directory must already exist - ${sourceDir.normalize}")
  require(Files.exists(destDir), s"Destintation directory must already exist - ${destDir.normalize}")
  require(Files.isDirectory(sourceDir), s"Source directory must actually be a directory - ${sourceDir.normalize}")
  require(Files.isDirectory(destDir), s"Destintation directory must actually be a directory - ${destDir.normalize}")

  for (sourceFile <- Files.list(sourceDir).iterator().asScala) {
    val fileName = sourceFile.getFileName
    log.info(s"... now copying $fileName")
    val destFile = destDir.resolve(fileName)
    Files.copy(sourceFile, destFile, StandardCopyOption.REPLACE_EXISTING)
  }
}

val websiteTargetDirectory: Path = Paths.get("../docs/assets/js/projects/doomWadReader")

val fastLinkJSAndCopy = taskKey[Unit](s"Compile, fast link, and then copy the output to the website's target directory: $websiteTargetDirectory")
val fullLinkJSAndCopy = taskKey[Unit](s"Compile, full link, and then copy the output to the website's target directory: $websiteTargetDirectory")

lazy val root = project
  .in(file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %%% "scala-parser-combinators" % "2.3.0",
      "org.scala-js" %%% "scalajs-dom" % "2.8.0"
    ),
    inThisBuild(List(
      organization := "com.jacobenget",
      scalaVersion := "2.13.12"
    )),
    name := "doom-wad-parser",

    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },

    // Create two new sbt tasks which run fastLinkJS or fullLinkJS, respectively,
    // and then copy the output Javascript-related files to a directory in the
    // sibling Jekyll project where they can then be deployed and loaded in webpages
    fastLinkJSAndCopy := {
      val outputDir = (Compile / fastLinkJSOutput).value
      val outputPath = outputDir.toPath
      val targetPath = baseDirectory.value.toPath.resolve(websiteTargetDirectory)
      copyAllFilesInDirectory(outputPath, targetPath, streams.value.log)
    },
    fullLinkJSAndCopy := {
      val outputDir = (Compile / fullLinkJSOutput).value
      val outputPath = outputDir.toPath
      val targetPath = baseDirectory.value.toPath.resolve(websiteTargetDirectory)
      copyAllFilesInDirectory(outputPath, targetPath, streams.value.log)
    }
  )
