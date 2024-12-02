---
title: extras
description: small projects of interest
slug: extras
layout: project
associated_top_level_page: projects
---

### synthetic snow

This is a flash animation I completed for an assignment in a 2D animation class.
The task was to create an animation that demonstrated the concept of <em>weight and flow</em>.

I had a lot of fun working on this project, especially when writing ActionScript to create effects and control the animation.

The last half of the animation (beginning about a minute or so in) loops indefinitely with little bits of randomization.
It feels a bit like a fancy screen saver, I still think it's effective at demonstrating how much fun you can have with just a few lines of code.
The music is "Over The Pond" by The Album Leaf.

<div id="syntheticSnowFlashContainer"></div>

<script type="text/javascript">
    window.addEventListener("load", (event) => {
        const ruffle = window.RufflePlayer.newest();
        const player = ruffle.createPlayer();
        const container = document.getElementById("syntheticSnowFlashContainer");
        container.appendChild(player);
        player.classList.add("syntheticSnowFlash");

        player.load({
            url: "/assets/flash/synSnow.swf",
            parameters: {
                swfURL: "/assets/flash/SyntheticSnow.swf",
                musicURL: "/assets/flash/AlbumLeafOverThePond64.mp3",
            }
        });
    });
</script>
<script src="/assets/js/ruffle/nightly-build-2023_12_07-web-selfhosted/ruffle.js"></script>

---

### anagram press

In the Summer of 2008 I developed a portfolio website for my good friend, illustrator and designer, Chandler O'Leary. 

I was given a set of design documents detailing how the site was to look. 
And from there I sliced up the mockups, coded the html and css, and set up hosting for the site. 
My biggest accomplishment was engineering the site in a modular and intuitive fashion so that it could be easily and confidently updated by the owner, who had no web development experience.
It was great to hear Chandler say "Oh, that's all?!" when I explained to her how to exchange image detail pages in the portfolio.

This website was originally up at <a class="boldMe" href="http://www.anagram-press.com">www.anagram-press.com</a>
but was completely redesigned in 2014 to be mobile friendly, and I wasn't involved with the newer version of the site you see now. 
Still, I keep this mention of Anagram Press, and my involvement, here in memory of Chandler, who sadly <a class="boldMe" href="https://www.seattletimes.com/entertainment/books/chandler-oleary-tacoma-illustrator-author-dies-at-41/">passed away in 2023</a>.
