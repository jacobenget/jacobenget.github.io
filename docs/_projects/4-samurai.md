---
title: stick-figure half-demon samurai
description: a 2D flash side-scrolling game
slug: samurai
layout: project
associated_top_level_page: projects
---

This is a flash game I created with another student as our final project for a class in 2D animation.

From the beginning, we had big ambitions.
The class focused on animation, but we wanted to create a piece that was highly interactive as well.
Our enthusiasm helped us cover the extra ground, and we felt that it all came together into something that was fun to play.

<div id="samuraiFlashContainer"></div>

<script type="text/javascript">
    window.addEventListener("load", (event) => {
        const ruffle = window.RufflePlayer.newest();
        const player = ruffle.createPlayer();
        const container = document.getElementById("samuraiFlashContainer");
        container.appendChild(player);
        player.classList.add("samuraiFlash");

        player.load({
            url: "/assets/flash/Samurai.swf",
            parameters: {
                gameURL: "/assets/flash/Game.swf",
                introURL: "/assets/flash/IntroControls.swf",
            }
        });
    });
</script>
<script src="/assets/js/ruffle/nightly-build-2023_12_07-web-selfhosted/ruffle.js"></script>

<div id="samuraiControls">
    <div>Move the samurai with &larr;, &uarr;, and &rarr;</div>
    <div>Use 'c' for a fast attack</div>
    <div>Use 'z' for a strong attack</div>
</div>

My partner and I took complementary roles on this project.  He handled most of the artistic side by designing the introduction animation and creating background art pieces for the game.  I tackled the technical aspects of the project by:

- Building a 2D game engine in ActionScript.  This engine handled:
   - Screen Scrolling: adding and removing background objects as the samurai moves throughout the stage
   - User Interaction Components: user input, the timer, and the kill count
   - Collision Detection and Response
   - Gravity
   - Building the Environment: based on data read in
- Programming the zombies so they respond to the samurai's movements and his attacks.
- Animating the samurai and the zombies and writing code to handle the animations according to the state of the game.
