---
title: tic-tac-toe
description: a classic game of simple strategy
slug: ticTacToe
layout: project
associated_top_level_page: projects
---

<script src="https://code.jquery.com/jquery-1.11.3.min.js" type="text/javascript"></script>
<link rel="stylesheet" media="screen" href="/assets/css/projects/ticTacToe.css">

<div id="ticTacToeGame">
    <table class="board">
        <tbody>
            <tr>
                <td></td>
                <td></td>
                <td></td>
            </tr>
            <tr>
                <td></td>
                <td></td>
                <td></td>
            </tr>
            <tr>
                <td></td>
                <td></td>
                <td></td>
            </tr>
        </tbody>
    </table>
    <div class="status"></div>
    <div class="playerTypeSelect"><span class="tictactoe-markForPlayerX">X</span>'s played by <select class="playerXSelect"></select></div>
    <div class="playerTypeSelect"><span class="tictactoe-markForPlayerO">O</span>'s played by <select class="playerOSelect"></select></div>
    <button class="newGame" type="button">Restart Game</button>
</div>

This page allows you to play the game tic-tac-toe, against either an AI opponent or a human opponent that is sitting next to you. To simplify things, X's always go first in the game. 

I mainly[^1] built this because I wanted to test out how well a very generic AI algorithm would work on a game like tic-tac-toe, which has a reasonably small set of possible states
(only around 6000...this number [could be a lot less](https://en.wikipedia.org/wiki/Tic-tac-toe#Combinatorics) if you're aware of how some game states are similar due to reflection or rotational symmetries, but my AI isn't that smart and doesn't need to be).

My idea was to implement an AI algorithm that was completely blind to the mechanics of the game it was playing, in this case tic-tac-toe.
More specifically, I was thinking of this approach:

> During its turn the AI would only be given a list of numbered actions it could take for that turn.
>
> It would know *nothing* concrete about those actions (i.e. it wouldn't be able to tell that an action meant something like "Place an X in the center square").
> Instead it would only know enough to be able to tell the actions apart from each other.
>
> Upon taking an action the AI's turn would be over and the result of its action/turn would be revealed to the AI:
> either the game has now ended (with a win, a loss, or a draw) or the game will continue with the opponent's turn, and the opponent has these opaque actions available to them.

Also, the AI would assume a simple model of its opponent's behavior: its opponent will seek an immediate win if that's possible this turn, otherwise its opponent will act randomly.

With this perspective, and simulation of an opponent, the AI can play this game over and over again on its own.
When first playing the game the AI will act blindly because it knows nothing about the mechanics of the game it's playing.
But just due to random choice the AI will win some games and lose others, and the AI will remember which exact sequences of actions were more likely to lead to a win or a loss.
Armed with that knowledge the AI can then, at each turn, choose the action that maximizes its chances that the game will eventually end up in a 'win' state for the AI. 

That's the idea!

Also, the process of the AI "playing the game over and over again in order to learn how the game works" actually happens here in my implementation all at once, on the AI's first turn,
as it simulates all possible further states of the game and calculates its remaining chances of winning at each state.

Feel free to play against this AI above to see what you think.  It's definitely not perfect (e.g. it doesn't see a difference between a 'loss' and a 'draw',
they're both just 'not winning' to it, so sometimes it'll appear to let you win when really it just acted randomly because <em>it</em> couldn't win) but I think it demonstrates that with a large working memory and great
processing speed some straightforward logic can manifest itself as "intelligence."

Also, you can browse the source code for this game [here](https://github.com/jacobenget/jacobenget.github.io/tree/main/tic_tac_toe).
Specifically, you can see the implementation of the AI algorithm in the file [GameAI.scala](https://github.com/jacobenget/jacobenget.github.io/blob/main/tic_tac_toe/src/main/scala/GameAI.scala).

[^1]: I also built this as part of an application to the [Recurse Center](https://www.recurse.com/) in fall 2023. (I got accepted!)

<script type="module">
    import { Main, GameUIElements } from "/assets/js/projects/ticTacToe/main.js";

    $(document).ready(function() {
        var game = document.getElementById("ticTacToeGame")
        var board = game.querySelector("table.board");
        var newGameButton = game.querySelector("button.newGame");
        var playerXSelect = game.querySelector("select.playerXSelect");
        var playerOSelect = game.querySelector("select.playerOSelect");
        var gameStatus = game.querySelector("div.status");

        var gameElements = new GameUIElements(
            board,
            playerXSelect,
            playerOSelect,
            newGameButton,
            gameStatus
        );
        Main.initializeTicTacToeGame(gameElements);
    });
</script>
