import scala.util.Try

import org.scalajs.dom

/* 
  The utilities contained here implement a generic AI that does a reasonable job at playing any two-player
  turn-taking game where the set of all possible states is very small (i.e. in the 1000's, an example would
  be Tic-Tac-Toe).

  The logic here is both very generic and not that complicated, demonstrating that with a large working memory
  and great processing speed some simplish logic can manifest itself as "intelligence."

  How is this logic very generic?
    - It only works on these assumptions about the game:
      - At any point the game has a finite set of actions which either player can take
        - Which actions are avilalbe for a player may be different at each of the player's turn
      - The AI and its opponent take turns, with each turn ending after a single action is made

  How is this logic not that complicated?
    - It starts with this simple model of its opponent:
      - opponent will always pick an action if that action results in the immediate win of the opponent, otherwise the opponent is acting randomly
    - And then it exhaustively simulates every possible resultant game in order to calculate the chance for any specific action to lead to the AI winning 
 */
object GameAI {

  // There are two types of players in a two-player game
  sealed trait Player
  case object Me extends Player
  case object Opponent extends Player

  // Represents a distinct and complete thing a player can do during their turn in a two-player game
  trait Action

  // A two-player game is always in exactly one of these game states
  //
  // Note that the detail of whose turn it currently is is not represented here.
  // This detail isn't important because we make the assumption that players swap taking turns in this,
  // and that a player's turn ends exactly after the perform one available action 
  sealed trait GameState
  case class GameOver(winner: Option[Player]) extends GameState
  case class StillPlaying(availableActions: Set[Action]) extends GameState

  // A snapshot of the two-player game, which reports the state of the game (which includes what actions are
  // available to the player's whose turn it is), and provides the resultant snapshot when a specific action is taken
  trait TwoPlayerGameSnapshot {
    def gameState: GameState
    def performAction(action: Action): Try[TwoPlayerGameSnapshot]
  }

  /**
    * Make a choice about what action to take, given that it's our turn and we're faced with the game in the state represented by the given snapshot.
    * 
    * Returns Failure in the case that the game snapshot reports there are no actions one can actually take.
    * 
    * Note: this function is defined as a 'val' instead of a 'def' in order to make use of a memoization utility, which is needed in order to make use of this funciton performant
    *
    * @param gameSnapshot - a snapshot of the state of the game
    * @return the action we're prefer to take, in case there is at least one action that can be taken, otherwise Failure
    */
  val chooseNextAction: TwoPlayerGameSnapshot => Try[Action] = memoize { (gameSnapshot: TwoPlayerGameSnapshot) => Try {
    // Sort the available actions based on how likely we are to win if take that action
    // and select the action with the highest chance of winning
    gameSnapshot.gameState match {
      case StillPlaying(availableActions) =>
        // Associate each available action with the chance that we'll win if we take that action
        val actionsAndChances: Set[(Action, Float)] = for {
          action <- availableActions
          chanceOfWinning = chanceOfWinningIfActionMade(gameSnapshot, action).get
        } yield (action, chanceOfWinning)

        // Choose the action with the highest chance of a winning (sortBy puts the list items in ascending order, so 'highest' is last)
        actionsAndChances.toList.sortBy(x => x._2).last._1
      case _ => throw new RuntimeException("Game is already over, no next action for AI to take!")
    }
  }}

  /**
    * Computes the chances that we'll eventually win, given
    *   1. An opponent that chooses obviously winning actions when available, and chooses their actions randomly otherwise
    *   2. We'll continue to act "optimally" in our subsequent actions
    * 
    * Note: this function is defined as a 'val' instead of a 'def' in order to make use of a memoization utility, which is needed in order to make use of this funciton performant
    *
    * @param gameSnapshot - the state of the game before we take the action
    * @param actionToTake - the action which may or may not lead us to victory, we want to know
    * @return A float between 0 and 1, inclusive, that represents the estimated chance that we'll win if we take the given action
    */
  private val chanceOfWinningIfActionMade: (TwoPlayerGameSnapshot, Action) => Try[Float] = memoizeTwoArg { (gameSnapshot: TwoPlayerGameSnapshot, actionToTake: Action) => Try {
    // Simulate taking the given action in order to see what happens
    val snapshotForOppenentsTurn = gameSnapshot.performAction(actionToTake).get
    snapshotForOppenentsTurn.gameState match {
      case GameOver(winner) if winner == Some(Me) => 1  // If the game now ends with us winning then we have a 100% chance of winning when we take this action
      case GameOver(_) => 0 // If the game now ends with us losing then we have a 0% chance of winning when we take this action
      case StillPlaying(actionsAvailableToOpponent) =>

        // If the game will not end when we take that action, then it will now be our opponent's turn, and we will continue calcuating our chance of winning
        // by assuming that our opponent will choose an obviously winning action if they have such an action available to them, and if not then they'll choose
        // randomly amongst the options available to them

        val opponentWinningActions = actionsAvailableToOpponent.filter(snapshotForOppenentsTurn.performAction(_).get.gameState == GameOver(Some(Opponent)))
        if (opponentWinningActions.nonEmpty) {
          0 // If on their turn the opponent will have an action that will immediately end the game in their favor then we have 0% chance of winning
        } else {
          // Our opponent doesn't have an action that allows them to immediately win, so instead we'll assume they randomly choose an action 
          val aggreateChances: Set[Float] = for {
            actionAvailableToOpponent <- actionsAvailableToOpponent
          } yield {

            // Simulate the opponent take this specific action, to see if the game ends and what possible actions will be available to use when it then becomes our turn again 
            val snapshotForMyNextTurn = snapshotForOppenentsTurn.performAction(actionAvailableToOpponent).get
            snapshotForMyNextTurn.gameState match {
              case GameOver(winner) if winner == Some(Me) => 1
              case GameOver(_) => 0
              case StillPlaying(_) => 

                // If, after the opponent has taken their action, the game still continues, then it will be our turn.
                // We'll continue the calcuation of our chance of winning by taking the best action left available to us 
                val bestNextTurnAction = chooseNextAction(snapshotForMyNextTurn).get
                val chanceOfWinningIfOpponentTakesThisAction = chanceOfWinningIfActionMade(snapshotForMyNextTurn, bestNextTurnAction).get
                chanceOfWinningIfOpponentTakesThisAction
            }
          }

          aggreateChances.sum / actionsAvailableToOpponent.size
        }
    }
  }}

  // A utility for wrapping a pure one-arg function in order to leverage memoization, with the result
  // of trading some memory space for a performance improvement, see https://en.wikipedia.org/wiki/Memoization
  def memoize[I, O](f: I => O): I  => O = {
    val cache = new scala.collection.mutable.HashMap[I, O]()
    (arg: I) => cache.getOrElseUpdate(arg, f(arg))
  }

  // A utility for wrapping a pure two-arg function in order to leverage memoization, with the result
  // of trading some memory space for a performance improvement, see https://en.wikipedia.org/wiki/Memoization
  def memoizeTwoArg[I1, I2, O](f: (I1, I2) => O): (I1, I2) => O = {
    val cache = new scala.collection.mutable.HashMap[(I1, I2), O]()
    (arg1: I1, arg2: I2) => cache.getOrElseUpdate((arg1, arg2), f(arg1, arg2))
  }
}
