package com.jacobenget.tictactoe

import scala.util.Try

// Tic-Tac-Toe has two players, one plays X and the other plays O
// (and each player type is associate with a string for display reasons)
object Player extends Enumeration {
  type Player = Value
  val X = Value("X")
  val O = Value("O")
}
import Player._

// A Tic-Tac-Toe board has three rows
object Row extends Enumeration {
  type Row = Value
  val Top, Middle, Bottom = Value
}
import Row._

// A Tic-Tac-Toe board has three columns
object Column extends Enumeration {
  type Column = Value
  val Left, Center, Right = Value
}
import Column._

// BoardSpace represents one of the 9 spots/spaces on a Tic-Tac-Toe board
// A BoardSpace is identified by the specific row and column it lies in
case class BoardSpace(row: Row, column: Column)

/* 
Any match/game of Tic-Tac-Toe is in one of these overall states:
  - Either the match is over (it ended either in a draw, or with a winner decided)
  - Or the match is still being played and it's either X's turn or O's turn.

MatchState is a trait which represents all these general possible states the match could be in.
*/
sealed trait MatchState
sealed trait MatchIsOver extends MatchState
case object Draw extends MatchIsOver
case class Victory(winner: Player, winningSpaces: Set[BoardSpace]) extends MatchIsOver
case class StillPlaying(nextToPlay: Player) extends MatchState

// An immutable snapshot of a Tic-Tac-Toe match
//
// The state of the match is determined entirely by which board spaces are marked by which player.
case class TicTacToeMatchSnapshot(markedSpaces: Map[BoardSpace, Player]) {

  // Report the state of the match
  def matchState: MatchState = {

    // 1. First, check to see if there's a winner!
    val winningPlayers = Player.values.filter(player => hasPlayerWon(player))
    if (winningPlayers.nonEmpty) {
      val winner = winningPlayers.head  // if for some reason there happens to be more than one winner, we just select the first one listed
      Victory(winner, spacesContributingToPlayerWin(winner))
    } else {

      // 2. We found no winner, so check to see if there is a draw due to all the spaces being filled
      val atADraw = unmarkedSpaces.isEmpty
      if (atADraw) {
        Draw
      } else {

        // 3. Not all the spaces are filled, so determine which player goes next
        //
        // We're assuming that X always goes first in a match, and that the players alternate turns after that.
        // 
        // So the next player to go is exactly the one that has marked fewer spaces on the board,
        // except that X goes next in the case that both players have marked the same number of spaces
        if (spacesMarkedByPlayer(X).size > spacesMarkedByPlayer(O).size) {
          StillPlaying(nextToPlay = O)
        } else {
          StillPlaying(nextToPlay = X)
        }
      }
    }
  }

  /**
    * Mark a space on the board for the current player, and return the new Tic-Tac-Toe match snapshot that
    * is the result of that space being marked
    *
    * @param boardSpace - the board space to mark
    * @return - the match snapshot that represents the state of the match now that this board space is marked
    */
  def markSpaceForCurrentPlayer(boardSpace: BoardSpace): Try[TicTacToeMatchSnapshot] = Try {
    matchState match {
      case StillPlaying(nextToPlay) => 
        if (unmarkedSpaces.contains(boardSpace)) {
          TicTacToeMatchSnapshot(markedSpaces + (boardSpace -> nextToPlay))
        } else {
          throw new RuntimeException(s"Player ${nextToPlay} tried to mark the space with (row, column) equal to (${boardSpace.row}, ${boardSpace.column}) but this space was already marked!")
        }
      case _ => 
        throw new RuntimeException(s"An attempt to mark a board space was made in a match that has already concluded!")
    }
  }

  // Returns true if and only if the given play has marked enough spaces to secur a win
  def hasPlayerWon(player: Player): Boolean = spacesContributingToPlayerWin(player).nonEmpty

  // Return all the board spaces that are:
  //  1. Marked by this player
  //  2. Part of a set of board spaces that result in a win for this player
  def spacesContributingToPlayerWin(player: Player): Set[BoardSpace] = {
    val marked: Set[BoardSpace] = spacesMarkedByPlayer(player)
    val allMarkedWinningCombinations = winningCombinationsOfSpaces.filter(_.subsetOf(marked))
    allMarkedWinningCombinations.fold(Set.empty)(_ ++ _)
  }

  // Return all the board spaces that are currently marked by the given player
  def spacesMarkedByPlayer(player: Player): Set[BoardSpace] = {
    markedSpaces.collect({ case (space, p) if (p == player) => space }).toSet
  }

  // Return all the board spaces not yet marked in this match
  def unmarkedSpaces: Set[BoardSpace] = {
    val allSpaces: Set[BoardSpace] = for {
      row <- Row.values.unsorted
      column <- Column.values.unsorted
    } yield BoardSpace(row, column)

    allSpaces -- markedSpaces.keySet
  }
  
  // Define which sets of marked board spaces constitue a 'win' in Tic-Tac-Toe 
  val winningCombinationsOfSpaces: Set[Set[BoardSpace]] = {
    // Three across in any single row
    val threeAcrossARow = {
      for (row <- Row.values.unsorted) yield {
        for (column <- Column.values.unsorted) yield {
          BoardSpace(row, column)
        }
      }
    }

    // Three down in any single column
    val threeAcrossAColumn = {
      for (column <- Column.values.unsorted) yield {
        for (row <- Row.values.unsorted) yield {
          BoardSpace(row, column)
        }
      }
    }

    // Either of the two diagonals
    val diagonals = Set(
      Set(BoardSpace(Top, Left), BoardSpace(Middle, Center), BoardSpace(Bottom, Right)),
      Set(BoardSpace(Top, Right), BoardSpace(Middle, Center), BoardSpace(Bottom, Left)),
    )

    threeAcrossARow ++ threeAcrossAColumn ++ diagonals
  }
}

object TicTacToeMatchSnapshot {
  def newMatch: TicTacToeMatchSnapshot = TicTacToeMatchSnapshot(Map.empty)
}
