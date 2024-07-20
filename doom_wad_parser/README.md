# Doom WAD parser
Here lies the source code for a tool, written in Scala, for parsing [WAD asset files](https://en.wikipedia.org/wiki/Doom_modding) intended for the [original Doom game](https://en.wikipedia.org/wiki/Doom_(1993_video_game)).

## Building
This tool leverages [Scala.js](https://www.scala-js.org/) and [sbt](https://www.scala-sbt.org/) to generate a single `main.js` file, orginized as an ECMAScript module, which is then copied over to the `docs` folder where the main contents of the related website live, which is where the script is served up from.

To generate, from scratch, and then apporpraitely copy this `main.js` artifact you'll need to have `sbt` installed.

Then you can either run

```
sbt fastLinkJSAndCopy
```

to quickly generate and copy a non-optimized *developement* version of `main.js`.

Or you could run

```
sbt fullLinkJSAndCopy
```

to invest time in generating and then copying an optimized *production* version of `main.js`.
