import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.scalajs.js.JSConverters._
import scala.util.{Try, Success, Failure}

import org.scalajs.dom
import org.scalajs.dom.html
import com.jacobenget.tictactoe._
import scala.util.matching.Regex


@JSExportTopLevel("GameUIElements")
case class GameUIElements (
  board: dom.HTMLTableElement, // a table element with at least three rows, each with at least three columns
  playerXSelect: dom.HTMLSelectElement, // a combo box for selecting who (Human or AI) will play as X's
  playerOSelect: dom.HTMLSelectElement, // a combo box for selecting who (Human or AI) will play as O's
  newGameButton: dom.HTMLButtonElement, // a button element for starting a new game
  gameStatus: dom.HTMLDivElement //  a span element that'll be used to communicate the state of the current game to the user
)

sealed trait PlayerType
case object Human extends PlayerType
case object AI extends PlayerType

/* 
 * Completely represents the state of the Tic-Tac-Toe game, including both the game board and the data controlled by the associate UI components
 */
class GameState(
  var playerXType: PlayerType,
  var playerOType: PlayerType,
  var matchSnapshot: TicTacToeMatchSnapshot = TicTacToeMatchSnapshot.newMatch,
  var pendingAIResponseTimerId: Option[Int] = None
) {

  def resetGameBoard(): Unit = {
    dom.console.debug(s"Game board reset")
    matchSnapshot = TicTacToeMatchSnapshot.newMatch
  }

  // Mark the given board space for the current player
  def markSpaceForCurrentPlayer(boardSpace: BoardSpace): Try[Unit] = {
    for (resultantSnapshot <- matchSnapshot.markSpaceForCurrentPlayer(boardSpace)) yield {
      matchSnapshot = resultantSnapshot
    }
  }

  def playerThatHasATurn: Option[(PlayerType, Player.Player)] = matchSnapshot.matchState match {
    case StillPlaying(Player.X) => Some(playerXType, Player.X)
    case StillPlaying(Player.O) => Some(playerOType, Player.O)
    case _ => None
  }
}

@JSExportTopLevel("Main")
object Main {

  // Two utilities to make it easy to PlayerType to associated label and back
  val labelFromPlayerType: Map[PlayerType, String] = Map(Human -> "Human", AI -> "AI")
  val playerTypeFromLabel: Map[String, PlayerType] = labelFromPlayerType.map(_.swap)

  /**
    * Single entrypoint of this module, which initiaizes a Tic Tac Toe board game for playing the game
    */
  @JSExport
  def initializeTicTacToeGame(gameUI: GameUIElements): Unit = {

    // Dig into the provided <table> element in order to get a table cell for each space in the Tic-Tac-Toe board,
    // while ensuring that the <table> provided has enough cells to support a game of Tic-Tac-Toe
    val tableCellFromBoardspace: Map[BoardSpace, dom.HTMLElement] = {
      
      val boardSpacesPairedWithCells = for {
        row <- Row.values.unsorted
        column <- Column.values.unsorted
        boardSpace  = BoardSpace(row, column)
      } yield {
        val associatedTableDataCell = {
          val rowIndex = boardSpace.row match {
            case Row.Top => 0
            case Row.Middle => 1
            case Row.Bottom => 2
          }

          val columnIndex = boardSpace.column match {
            case Column.Left => 0
            case Column.Center => 1
            case Column.Right => 2
          }

          Try{
            Option( // Use Option here so an error is immediately reported if the cast operation fails, which would result in a null value 
              gameUI.board
                .getElementsByTagName("tr").item(rowIndex)
                .getElementsByTagName("td").item(columnIndex)
                .asInstanceOf[dom.HTMLElement]
            ).get
          }
        }
        require(associatedTableDataCell.isSuccess, s"Game board needs to have a table data cell for (row, column) equal to (${boardSpace.row}, ${boardSpace.column})")

        (boardSpace -> associatedTableDataCell.get)
      }

      boardSpacesPairedWithCells.toMap
    }

    // Make sure the opponent-type select combo boxes list exactly the options they should
    for {
      playerTypeSelect <- List(gameUI.playerXSelect, gameUI.playerOSelect)
    } {
      // Remove all existing options from the select control
      while (playerTypeSelect.options.length > 0) {
        playerTypeSelect.remove(0)
      }
      // Add back an option for each player type
      for {playerTypeLabel <- playerTypeFromLabel.keys} {
        val newOption = dom.document.createElement("option").asInstanceOf[dom.HTMLOptionElement]
        newOption.label = playerTypeLabel
        newOption.value = playerTypeLabel
        playerTypeSelect.add(newOption)
      }
    }

    // Set up the initial game state
    val gameState: GameState = new GameState(playerXType = Human, playerOType = AI)

    // A function which is in charge of responding to changes in the game state,
    // and therefore should be called whenever there is a change in the game state.
    def respondToStateChange(): Unit = {
      // First, make sure the UI is refreshed do it accurately represents the new game state
      refreshUI(gameState, gameUI, tableCellFromBoardspace)

      // Cancel any pending AI response, because a new state change has come in before the AI could respond,
      // and likely its response will now be out of date if/when it does come in.
      gameState.pendingAIResponseTimerId.map(dom.window.clearTimeout(_))
      gameState.pendingAIResponseTimerId = None

      // If the current turn is for an AI player then schedule a callback to have the AI make their move
      gameState.playerThatHasATurn match {
        case Some((AI, _)) => 
          val chooseNextMove = () => {
            // We're in the AI reponse code, so unassign any bookkeeping we have that keeps track of this pending response
            gameState.pendingAIResponseTimerId = None

            val spaceChosenByAi = haveAiTakeATurn(gameState.matchSnapshot).get
            gameState.markSpaceForCurrentPlayer(spaceChosenByAi)

            respondToStateChange()
          }

          // delay the AI response by just a tish, so the user has a chance to notice the individaul
          // turns happening even when the AI's decision process is really quick
          val delayBeforeAiTurn_in_ms = 500
          val timerId = dom.window.setTimeout(chooseNextMove, delayBeforeAiTurn_in_ms) 
          gameState.pendingAIResponseTimerId = Some(timerId)
        case _ => ()
      }
    }

    //////////////////////////
    // Provide each UI element with their associated behavior
    //////////////////////////

    // Restart Game button
    gameUI.newGameButton.addEventListener("click", (x: dom.PointerEvent) => {
      dom.console.debug("Choosen to start a new game!")
      gameState.resetGameBoard()

      respondToStateChange()
    })

    // The two <select> controls for selecting a player type (human or ai) for each player
    for {
      (selectElement, player) <- List(
        (gameUI.playerXSelect, Player.X),
        (gameUI.playerOSelect, Player.O)
      )
    } {
      selectElement.addEventListener("change", (event: dom.Event) => {
        val selectedLabel = event.target.asInstanceOf[dom.HTMLOptionElement].value

        val newPlayerType = playerTypeFromLabel(selectedLabel)
        player match {
          case Player.X => gameState.playerXType = newPlayerType
          case Player.O => gameState.playerOType = newPlayerType
        }

        respondToStateChange()
      })
    }

    // Each of the 9 cells in the <table>
    for {
      (boardSpace, tableCell) <- tableCellFromBoardspace
    } {
      tableCell.addEventListener("click", (event: dom.PointerEvent) => {
        dom.console.debug(s"Clicked on board space ${boardSpace}")

        // If there's a game that's still being played,
        // and the current turn is for a player that's human,
        // and this board space hasn't already been marked
        // then mark this board space for this player

        val itIsAHumansTurn = gameState.playerThatHasATurn.filter(_._1 == Human).isDefined
        val boardSpaceIsOpen = gameState.matchSnapshot.unmarkedSpaces.contains(boardSpace)

        if (itIsAHumansTurn && boardSpaceIsOpen) {
          gameState.markSpaceForCurrentPlayer(boardSpace)
          respondToStateChange()
        }
      })
    }

    // And finally, perform the initial refresh of the UI
    respondToStateChange()
  }

  /**
    * Update the state of all Tic-Tac-Toe UI elements so they match our internal state of the game.
    *
    * @param gameState - the internal state of the game
    * @param gameUI - the collection of UI elements associated with the Tic-Tac-Toe game
    * @param tableCellFromBoardspace - table cells each associated with a single Tic-Tac-Toe board space 
    */
  def refreshUI(gameState: GameState, gameUI: GameUIElements, tableCellFromBoardspace: Map[BoardSpace, dom.HTMLElement]): Unit = {
    val playerThatHasATurn = gameState.playerThatHasATurn
    val gameMatchState = gameState.matchSnapshot.matchState
    val unmarkedSpaces = gameState.matchSnapshot.unmarkedSpaces

    // 1. Refresh all the table cells
    for (
      (boardSpace, tableCell) <- tableCellFromBoardspace
    ) {
      // Before anything, reset the affected parts of these table cells (i.e. undo anything we would have done in a previous call to this function)
      // Specifically, remove all css classes that start with 'tictactoe-', and remove any content in the table cells
      val classesDynamicallyApplied = tableCell.classList.filter(_.startsWith("tictactoe-"))
      classesDynamicallyApplied.foreach(tableCell.classList.remove(_))
      tableCell.textContent = ""

      // Then, make sure the marked spaces have the players mark
      gameState.matchSnapshot.markedSpaces.get(boardSpace) match {
        case Some(player) => 
          tableCell.textContent = s"$player"
          tableCell.classList.add(s"tictactoe-markForPlayer$player")
        case _ => ()
      }

      // And, in there's a game still going on, and the current turn is a human player, make sure all unmarked spaces are told their open for this player to choose
      playerThatHasATurn match {
        case Some((Human, player)) if unmarkedSpaces.contains(boardSpace) =>
          tableCell.classList.add(s"tictactoe-openForPlayer$player")
        case _ => ()
      }

      // Finally, if the game has ended with a winner, mark all the spaces that contributed to their win
      gameMatchState match {
        case Victory(winner, winningSpaces) if winningSpaces.contains(boardSpace) =>
          tableCell.classList.add(s"tictactoe-contributedToWinOfPlayer$winner")
        case _ => ()
      }
    }
    
    // 2. Refresh the opponent types select controls
    gameUI.playerXSelect.value = labelFromPlayerType(gameState.playerXType)
    gameUI.playerOSelect.value = labelFromPlayerType(gameState.playerOType)

    // 3. Refresh the game status
    def labelForPlayer(player: Player.Player): String = s"<span class=\"tictactoe-markForPlayer${player}\">$player</span>"
    gameUI.gameStatus.innerHTML = gameMatchState match {
      case Victory(winner, _) => s"Player ${labelForPlayer(winner)} has won!"
      case Draw => "Game ended in a draw!"
      case StillPlaying(nextToPlay) =>
        val humannessLabel = playerThatHasATurn.get._1 match {
          case AI => "AI ... thinking"
          case Human => "Human"
        }
        s"Player ${labelForPlayer(nextToPlay)}'s turn ($humannessLabel)"
    }
  }

  // Implementation of GameAI.TwoPlayerGameSnapshot specific to our game of Tic-Tac-Toe
  //
  // This allows us to leverage, our game of Tic-Tac-Toe, the generic AI provided by GameAI 
  case class GameSnapshotForTicTacToe(matchSnapshot: TicTacToeMatchSnapshot, playerControlledByAi: Player.Player) extends GameAI.TwoPlayerGameSnapshot {

    case class MarkThisBoardSpace(boardSpace: BoardSpace) extends GameAI.Action

    def gameState: GameAI.GameState = matchSnapshot.matchState match {
      case Draw => GameAI.GameOver(None)
      case Victory(winner, _) =>
        val aiWon = winner == playerControlledByAi
        GameAI.GameOver(Some(if (aiWon) GameAI.Me else GameAI.Opponent))
      case StillPlaying(_) =>
        val availableActions: Set[GameAI.Action] = matchSnapshot.unmarkedSpaces.map(MarkThisBoardSpace)
        GameAI.StillPlaying(availableActions)
    }

    def performAction(action: GameAI.Action): Try[GameAI.TwoPlayerGameSnapshot] = for {
      boardSpaceToMark <- unwrapAction(action)
      resultantMatchSnapshot <- matchSnapshot.markSpaceForCurrentPlayer(boardSpaceToMark)
    } yield GameSnapshotForTicTacToe(resultantMatchSnapshot, playerControlledByAi)

    // Utility for 
    def unwrapAction(action: GameAI.Action): Try[BoardSpace] = Try {
      Option(action.asInstanceOf[MarkThisBoardSpace]).get.boardSpace
    }
  }

  /**
    * Have the AI determine what next board space should be chosen
    *
    * @param matchSnapshot - a snapshot of the Tic-Tac-Toe match going on
    * @return the board piece that the AI has decided to mark
    */
  private def haveAiTakeATurn(matchSnapshot: TicTacToeMatchSnapshot): Try[BoardSpace] = for {
    playerControlledByAi: Player.Player <- Try { matchSnapshot.matchState match {
      case StillPlaying(nextToPlay) => nextToPlay
      case _ => throw new RuntimeException("AI was asked to take a turn in a match that has already finished!")
    }}
    snapshotForGameAi = GameSnapshotForTicTacToe(matchSnapshot, playerControlledByAi)

    chosenAction <- GameAI.chooseNextAction(snapshotForGameAi)
    chosenBoardSpace <- snapshotForGameAi.unwrapAction(chosenAction)
  } yield chosenBoardSpace
}

