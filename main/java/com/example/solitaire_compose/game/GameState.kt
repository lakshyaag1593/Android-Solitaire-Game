// game/GameState.kt
package com.example.solitaire_compose.game

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import kotlin.compareTo

class GameState {
    val tableau: List<SnapshotStateList<Card>> = List(7) { mutableStateListOf() }
    val foundations: List<SnapshotStateList<Card>> = List(4) { mutableStateListOf() }
    val stock: SnapshotStateList<Card> = mutableStateListOf()
    val waste: SnapshotStateList<Card> = mutableStateListOf()
    val foundationSuits = listOf(Suit.HEARTS, Suit.DIAMONDS, Suit.CLUBS, Suit.SPADES)

    // ── Move Counter ──────────────────────────────────────────────────────────
    var moveCount by mutableIntStateOf(0)
        private set

    // ── Undo Stack ────────────────────────────────────────────────────────────
    private data class GameSnapshot(
        val tableau: List<List<Pair<Card, Boolean>>>,   // card + isFaceUp
        val foundations: List<List<Pair<Card, Boolean>>>,
        val stock: List<Pair<Card, Boolean>>,
        val waste: List<Pair<Card, Boolean>>,
        val moveCount: Int
    )

    private val undoStack = ArrayDeque<GameSnapshot>()
    val canUndo: Boolean get() = undoStack.isNotEmpty()

    private fun snapshot() {
        undoStack.addLast(
            GameSnapshot(
                tableau    = tableau.map    { col  -> col.map  { Pair(it, it.isFaceUp) } },
                foundations= foundations.map{ pile -> pile.map { Pair(it, it.isFaceUp) } },
                stock      = stock.map  { Pair(it, it.isFaceUp) },
                waste      = waste.map  { Pair(it, it.isFaceUp) },
                moveCount  = moveCount
            )
        )
        // Keep undo stack from growing too large
        if (undoStack.size > 50) undoStack.removeFirst()
    }

    fun undo() {
        val last = undoStack.removeLastOrNull() ?: return
        tableau.forEachIndexed { i, col ->
            col.clear()
            last.tableau[i].forEach { (card, faceUp) ->
                card.isFaceUp = faceUp
                col.add(card)
            }
        }
        foundations.forEachIndexed { i, pile ->
            pile.clear()
            last.foundations[i].forEach { (card, faceUp) ->
                card.isFaceUp = faceUp
                pile.add(card)
            }
        }
        stock.clear()
        last.stock.forEach { (card, faceUp) ->
            card.isFaceUp = faceUp
            stock.add(card)
        }
        waste.clear()
        last.waste.forEach { (card, faceUp) ->
            card.isFaceUp = faceUp
            waste.add(card)
        }
        moveCount = last.moveCount
    }

    // ── Deal ──────────────────────────────────────────────────────────────────
    fun deal() {
        tableau.forEach { it.clear() }
        foundations.forEach { it.clear() }
        stock.clear()
        waste.clear()
        undoStack.clear()
        moveCount = 0

        val deck = Deck()
        deck.shuffle()

        for (col in 0..6) {
            for (row in 0..col) {
                val card = deck.draw() ?: break
                card.isFaceUp = (row == col)
                tableau[col].add(card)
            }
        }
        while (deck.size > 0) {
            val card = deck.draw() ?: break
            card.isFaceUp = false
            stock.add(card)
        }
    }

    // ── Move Validation ───────────────────────────────────────────────────────
    fun canPlaceOnTableau(card: Card, column: List<Card>): Boolean {
        if (column.isEmpty()) return card.rank == Rank.KING
        val target = column.last()
        if (!target.isFaceUp) return false
        return card.isRed != target.isRed &&
                card.rank.value == target.rank.value - 1
    }

    fun canPlaceOnFoundation(card: Card, foundation: List<Card>, index: Int): Boolean {
        val assignedSuit = foundationSuits[index]
        if (card.suit != assignedSuit) return false
        if (foundation.isEmpty()) return card.rank == Rank.ACE
        val target = foundation.last()
        return card.rank.value == target.rank.value + 1
    }

    // ── Move Execution ────────────────────────────────────────────────────────
    fun moveToTableau(card: Card, colIndex: Int): Boolean {
        val targetCol = tableau[colIndex]
        if (!canPlaceOnTableau(card, targetCol)) return false
        snapshot()                              // ← save state before moving
        val moved = removeCardFromSource(card) ?: return false
        targetCol.addAll(moved)
        autoFlipTopCards()
        moveCount++
        return true
    }

    fun moveToFoundation(card: Card): Boolean {
        if (!isTopCard(card)) return false
        val foundationIndex = foundations.indexOfFirst { foundation ->
            canPlaceOnFoundation(card, foundation, foundations.indexOf(foundation))
        }
        if (foundationIndex == -1) return false
        snapshot()                              // ← save state before moving
        removeCardFromSource(card)
        foundations[foundationIndex].add(card)
        autoFlipTopCards()
        moveCount++
        return true
    }

    fun drawFromStock() {
        snapshot()                              // ← save state before drawing
        if (stock.isNotEmpty()) {
            val card = stock.removeAt(stock.size - 1)
            card.isFaceUp = true
            waste.add(card)
        } else {
            val recycled = waste.reversed().toList()
            waste.clear()
            recycled.forEach { card ->
                card.isFaceUp = false
                stock.add(card)
            }
        }
        moveCount++
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private fun removeCardFromSource(card: Card): List<Card>? {
        if (waste.lastOrNull() == card) {
            waste.removeAt(waste.size - 1)
            return listOf(card)
        }
        for (col in tableau) {
            val idx = col.indexOf(card)
            if (idx != -1) {
                val stack = col.subList(idx, col.size).toList()
                repeat(stack.size) { col.removeAt(col.size - 1) }
                return stack
            }
        }
        for (foundation in foundations) {
            if (foundation.lastOrNull() == card) {
                foundation.removeAt(foundation.size - 1)
                return listOf(card)
            }
        }
        return null
    }

    private fun autoFlipTopCards() {
        for (col in tableau) {
            val top = col.lastOrNull()
            if (top != null && !top.isFaceUp) top.isFaceUp = true
        }
    }

    private fun isTopCard(card: Card): Boolean {
        if (waste.lastOrNull() == card) return true
        if (tableau.any { it.lastOrNull() == card }) return true
        if (foundations.any { it.lastOrNull() == card }) return true
        return false
    }

    // ── Win Detection ─────────────────────────────────────────────────────────
    val isWon: Boolean get() = foundations.all { it.size == 13 }
}
