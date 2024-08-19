---
title: doom asset viewer
description: online doom WAD reader
project_id: doomWadReader
layout: project
associated_top_level_page: projects
---

<!-- using jquery(UI) to make Drag-n-Drop handling easier to get right -->
<script src="https://code.jquery.com/jquery-1.11.3.min.js" type="text/javascript"></script>
<script src="https://code.jquery.com/ui/1.11.4/jquery-ui.min.js" type="text/javascript"></script>

This page allows you to easily view the image assets of <a href="https://en.wikipedia.org/wiki/Doom_WAD">Doom WAD files</a>
(these are files that supply all game assets to <a href="https://en.wikipedia.org/wiki/Doom_(1993_video_game)">Doom</a> and its sequel).
By dragging a WAD file and dropping it onto the area below you'll receive a listing of the image assets that appear in the file
(after a bit of a wait, commonly around 15-30 seconds).

A couple of links to WAD files hosted by this site are provided here, which you can drag-n-drop directly onto the area below,
but you can find more <a href="http://www.doomwadstation.net/idgames/index.php?folder=ZG9vbS9tZWdhd2Fkcw==">here</a> and at other places on the web
(but, due to <a href="https://en.wikipedia.org/wiki/Cross-origin_resource_sharing">CORS</a> likely not being enabled on WAD files hosted elsewhere,
to use such a 3rd-party WAD file you'll need to download it first and then drag it from outside a browser (e.g. from your "Downloads" folder) onto the area below).

Note that this page currently only works for WAD files from Doom 1 (specifically, it doesn't work on WAD files from Doom 2), and only processes and displays the image assets in the WAD (ignoring the other types of assets: maps, sounds, demos, ...).

- <a draggable="true" href="/assets/files/doom_wads/doom1.wad.zip">Doom Shareware WAD</a>
- <a draggable="true" href="/assets/files/doom_wads/alientc1.zip">Aliens Total Conversion</a>
- <a draggable="true" href="/assets/files/doom_wads/ldsprts.zip">Lego Sprites</a>

<br/>

<script src="/assets/js/projects/doomWadReader/jszip.js"></script>
<script src="/assets/js/projects/doomWadReader/setup_ui_interactions.js" type="text/javascript"></script>
<div id="dropWadHere">
    <div class="inner">
        <div class="contents"> <!-- This div element is entirely swapped out in during the processing of a WAD file, after drag-n-drop completes -->
            <img id="dropIcon" class="icon" src="/assets/images/projects/doomWadReader/arrow-down-a.png"/>
            <div id="label">Drag and drop a link to a WAD file here</div>
        </div>
    </div>
</div>
<script type="module">
    import { Main } from "/assets/js/projects/doomWadReader/main.js";

    $(document).ready(function() {
        $( "#dropWadHere" )
        .on("dragenter", onDragEnter)
        .on("dragover", onDragOver)
        .on("dragleave", onDragLeave)
        .on("drop", onDrop("/assets/images/projects/doomWadReader/Doomguy-movinganim.gif", function (wadInbuffer) { return Main.parseDoomWadData(wadInbuffer); }));
    });
</script>

<div id="resultsGoHere">
</div>

<!-- NOTE: This template isn't actually used, it's just here to illustrate that actual HTML structure produced by this code -->
<template>
    <div id="resultsGoHere">
        <!-- IF loading images from WAD failed -->
            <p>
                An error was encountered while processing the data you submitted! Perhaps the data wasn't a WAD file intended for Doom 1?
            </p>
        <!-- ELSE -->
            <h3>doom1.wad.zip</h3>
            <hr/>
            <!-- FOR each set of images loaded -->
                <div class="sectionContainer">
                    <h4 class="iconicHeader">
                        <span class="title">sprites</span>
                        <span class="description">objects that appear inside a map</span>
                    </h4>
                    <!-- FOR each image loaded -->
                    <img src="" title="CHGGA0" class="imageFromWAD">
                </div>
                <hr/>
            <!-- OR, if there were no sets of images -->
                <p>
                    No images were found while processing the WAD you submitted.  This is unfortunate, but not unexpected because many Doom WADs only introduce new maps using all the original image/texture assets from Doom.
                </p>
                <hr/>
            <!-- end FOR -->
            <div>
        <!-- end IF -->
        <h3>Stats</h3>
        <div>
            <div>Total time seen by browser: 22.427 seconds</div>
            <!-- IF timings information was present -->
                <ul>
                    <li>
                        <div>time spent parsing file: 5.075 seconds</div>
                    </li>
                    <li>
                        <div>time spent building images: 17.133 seconds</div>
                    </li>
                </ul>
            <!-- end IF -->
        </div>
    </div>
</template>
