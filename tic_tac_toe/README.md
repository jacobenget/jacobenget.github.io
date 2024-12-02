# Tic Tac Toe
This project implements an HTML-based version of the classic game Tic-Tac-Toe.  The user is able to play against either an AI opponent or a human opponent that is sitting next to them. To simplify things, X's always go first in the game.

You can see and play this game [here](https://jacobenget.com/projects/ticTacToe.html). 

I mainly<sup>*</sup> built this because I wanted to test out how well a very generic AI algorithm would work on a game like tic-tac-toe, which has a reasonably small set of possible states (around 6000, assuming that you're oblivious to rotations and symmetries of the game board, which my AI is).

My idea was to implement an AI algorithm that was completely blind to the mechanics of the game it was playing, in this case tic-tac-toe. All I wanted the AI to know during its turn was (1) what distinct actions it could take, and (2) what the result would be when it took those actions. I wanted it to know nothing concrete about the actions it could take, and when the AI took an action it would learn nothing more than "game ends (with a win, loss, or draw)" or "opponents turn, and they have these actions available to them".

Also, the AI would assume a simple model of it opponent's behavior. It would assume that its opponent would seek an immediate win if that was possible, otherwise the opponent would act randomly.

With this perspective the AI algorithm can, on its turn, simulate all possible further states of the game and calculate its remaining chances of winning at each state. Armed with that information the AI then chooses, for its turn, the action that maximizes its remaining chances of winning. And that's it!

Feel free to play against this AI [here](https://jacobenget.com/projects/ticTacToe.html) to see what you think. It's definitely not perfect (e.g. it doesn't see a difference between a 'loss' and a 'draw', they're both just 'not winning' to it, so it'll sometimes make surprising choices) but I think it demonstrates that with a large working memory and great processing speed some simplish logic can manifest itself as "intelligence."

Also, you can browse the source code for this game in these three files:
- [TicTacToe.scala](src/main/scala/TicTacToe.scala) - Generic components of the Tic-Tac-Toe game
- [Main.scala](src/main/scala/Main.scala) - UI logic of the Tic-Tac-Toe game
- [GameAI.scala](src/main/scala/GameAI.scala) - Implementation of the generic AI descripted above

<sup>*</sup> - I also built this as part of an application to the [Recurse Center](https://www.recurse.com/) in fall 2023. (I got accepted!)

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
