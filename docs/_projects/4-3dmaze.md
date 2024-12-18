---
title: 3D maze creator
description: software to design + explore a 3D maze
slug: 3DMaze
layout: project
associated_top_level_page: projects
---

In 2012, after fighting with Win32/MFC for far too long while working on tools at id Software, I was really excited about <a class="boldMe" href="https://en.wikipedia.org/wiki/Qt_(software)">Qt</a>, the cross-platform application framework, and decided to revisit an old project of mine.

The project stemmed from a computer graphics assignment where the goal was to create two C++/OpenGL programs from scratch: one that could display a model of a 3D maze, and another that would let the user walk around in the same 3D model.
I also added the ability to author the maze in the app visually, collision detection and reaction, and the ability to jump and adjust the field of view while in first-person view.

Porting the GUI code to Qt made it quite easy to combine all the tools into one application and simplify the maze creation process overall.
Now the user can see the results in real-time while editing the maze and quickly get a first-person preview.

At the <a class="boldMe" href="#links">bottom</a> of this page I've included links to the source code for this project.
Here is a screenshot of the application after the Qt conversion:

<div class="main3dMazeImage">
    <img src="/assets/images/projects/3DMaze/3DMaze_afterQt.jpg"/>
</div>

To use this application to design and explore a 3D maze, you would first draw the maze in its flat 2D representation in the left pane as I've demonstrated here:

<div class="imageRow">
    <!--
        NOTE: the <div> elements wrapping the <img> elements here (and in many instances further below)
        were only needed because without them Safari (version 16.6, on Mac OS X 11.7) was not properly
        scaling the images down to fit into the containing flex layout.

        This JSFiddle allowed for easy troubleshooting of this issue: https://jsfiddle.net/0scrgd9j/
        -->
    <div><img src="/assets/images/projects/3DMaze/2Dmaze0.gif"/></div>
    <div><img src="/assets/images/projects/3DMaze/2Dmaze1.gif"/></div>
    <div><img src="/assets/images/projects/3DMaze/2Dmaze2.gif"/></div>
    <div><img src="/assets/images/projects/3DMaze/2Dmaze3.gif"/></div>
</div>

As the maze is edited a 3D version of the maze is shown in the right pane.
The width and height of the walls of the 3D maze can be adjusted using the sliders at the top of the application.
Through menu options the textures used for the floor and walls of the 3D maze can be changed:

<div class="imageRow">
    <div><img src="/assets/images/projects/3DMaze/viewRegular.jpg"/></div>
    <div><img src="/assets/images/projects/3DMaze/viewAlienWalls.jpg"/></div>
    <div><img src="/assets/images/projects/3DMaze/viewGrassWalls.jpg"/></div>
    <div><img src="/assets/images/projects/3DMaze/viewStainWalls.jpg"/></div>
</div>

The maze in the 3D view can be rotated, translated, and scaled and the camera angle can be distorted:

<div class="imageRow">
    <div><img src="/assets/images/projects/3DMaze/viewCloseUp.jpg"/></div>
    <div><img src="/assets/images/projects/3DMaze/viewTwisted.jpg"/></div>
    <div><img src="/assets/images/projects/3DMaze/viewUpsideDown.jpg"/></div>
    <div><img src="/assets/images/projects/3DMaze/viewVerySmall.jpg"/></div>
</div>

Selecting the menu option 'View\|Explore Maze In First Person' will open a dialog that lets you step inside the maze in a first person fashion.  The custom textures are applied in this mode as well:

<div class="imageRow">
    <div><img src="/assets/images/projects/3DMaze/enterRegular.jpg"/></div>
    <div><img src="/assets/images/projects/3DMaze/enterAlienWalls.jpg"/></div>
    <div><img src="/assets/images/projects/3DMaze/enterGrassWalls.jpg"/></div>
    <div><img src="/assets/images/projects/3DMaze/enterStainWalls.jpg"/></div>
</div>

And while in first person view, you can move around the maze (within the boundaries of the walls), jump, and distort your field of view:

<div class="imageRow">
    <div><img src="/assets/images/projects/3DMaze/exploreFisheye1.jpg"/></div>
    <div><img src="/assets/images/projects/3DMaze/exploreJump.jpg"/></div>
    <div><img src="/assets/images/projects/3DMaze/exploreFisheye2.jpg"/></div>
    <div><img src="/assets/images/projects/3DMaze/exploreFisheye3.jpg"/></div>
</div>

<a name="links"></a>
The source code for this project is available on GitHub <a class="boldMe" href="https://github.com/jacobenget/3d-maze-creator">here</a>.  On that site you can browse the source code and <a class="boldMe" href="https://github.com/jacobenget/3d-maze-creator?tab=readme-ov-file#building-the-3dmaze-application">view documentation</a> on how to download the source code and build this application yourself.

