---
title: doom.wasm
description: doom as a simple WebAssembly module
slug: doomWasm
layout: project
associated_top_level_page: projects
---

[_Doom_](https://en.wikipedia.org/wiki/Doom_(1993_video_game)) is infamous for [how easily portable it is](https://doomwiki.org/wiki/Can_it_run_Doom%3F).  But can we port _Doom_ to [WebAssembly](https://webassembly.org/)?

Of course we can! And we would not be the first to do this, _by far_ (e.g. [1](https://github.com/cloudflare/doom-wasm), [2](https://github.com/lazarv/wasm-doom), [3](https://github.com/diekmann/wasm-fizzbuzz/tree/main/doom#but-can-it-run-doom), [4](https://github.com/UstymUkhman/webDOOM), [5](https://github.com/raz0red/webprboom), [6](https://github.com/VanIseghemThomas/wasmDOOM)).

But!

What if our goal wasn't just to port _Doom_ to WebAssembly, but instead to port _Doom_ to WebAssembly in a way that empowers developers to make use of our work? What if our goal was **to produce a _Doom_ WebAssembly module that is super easy to reuse**?

This is what I had in mind with [`doom.wasm`](https://github.com/jacobenget/doom.wasm).

The output of that project isn't just an demo of running _Doom_ in the browser (although, it has that too! [here](https://jacobenget.github.io/doom.wasm/examples/browser/doom.html), and embedded at the bottom of this page).

The output of `doom.wasm` is an artifact that can be reused to easily run _Doom_ on any WebAssembly runtime, and the project includes examples of doing just that in both [C](https://github.com/jacobenget/doom.wasm/tree/master/examples/native), [Python](https://github.com/jacobenget/doom.wasm/tree/master/examples/python), and (previously mentioned) in [the browser via JavaScript](https://github.com/jacobenget/doom.wasm/tree/master/examples/browser).

This project accomplishes this by caring a lot about the WebAssembly interface exposed by `doom.wasm`. The imports and exports of `doom.wasm` have been curated to maximize usability while supporting as many features of _Doom_ as possible (e.g. supporting the loading of custom [_Doom_ WADs](https://en.wikipedia.org/wiki/Doom_modding)).

You can find the latest release of `doom.wasm` [here](https://github.com/jacobenget/doom.wasm/releases/latest). There you'll also find a quick glance at the small set of imports and exports of `doom.wasm`, along with links to documentation of these features, and general advice on how to leverage this WebAssembly module.

Also, [here](https://github.com/jacobenget/doom.wasm/tree/master?tab=readme-ov-file#why) you'll find a comparison on how `doom.wasm` measures up in usability against many other existing "_Doom_ ported to WebAssembly" projects (summary: `doom.wasm` has around 75% less imports and exports than any of them).

And here, via `doom.wasm`, you can play the first episode of _Doom_, compliments of the [_Doom_ Shareware WAD](https://doomwiki.org/wiki/DOOM1.WAD) (game controls are the same keyboard controls present in vanilla _Doom_, detailed [here](https://doom.fandom.com/wiki/Controls#Default_controls)):

<iframe
    width="640"
    height="400"
    style="display:block; margin: 0 auto"
    src="https://jacobenget.github.io/doom.wasm/examples/browser/doom.html">
    <!--
    The actual size of the webpage we're embedding in this iframe, as of version 0.1.0 of `doom.wasm`, has a
    (width, height) size of (640, 400) because the embedded webpage is the exact same size as the `canvas`
    where Doom is 'running', and `doom.wasm` is running Doom at a resolution of 640 x 400.
    So, we'll hardcode the size of this iframe to match the size of the embedded webpage.
    -->
</iframe>

I'm hoping `doom.wasm` makes _Doom_ even more accessible and portable, empowering others to do new or unusual things with _Doom_, like:
- Use snapshotting of the WebAssembly instance to support rewinding of time in _Doom_
- Run _Doom_ entirely via the terminal
   - Sure, [it has been done](https://github.com/wojciech-graj/doom-ascii), but maybe you'll do something different like [use emojis](https://github.com/joachimbbp/spritefire) instead of ASCII characters?
- Use _Doom_ to benchmark different WebAssembly runtimes
- Make it so the passage of time in _Doom_ is not constant
    - Maybe time constantly speeds up while the player has any keys pressed, and slows down otherwise?
- Control _Doom_ via unorthodox input devices
- Perform interesting post-processing on the frames of _Doom_
   - E.g. enhance differences between the frames like [here](https://www.youtube.com/watch?v=NSS6yAMZF78)
- ... and so many other things I haven't dreamed of!