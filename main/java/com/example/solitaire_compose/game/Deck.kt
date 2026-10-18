package com.example.solitaire_compose.game

class Deck {
    // Build a full 52-card deck by combining every Suit with every Rank
    private val cards: MutableList<Card> = Suit.entries.flatMap { suit ->
        Rank.entries.map { rank ->
            Card(suit, rank)
        }
    }.toMutableList()

    // Shuffle the deck randomly
    fun shuffle() {
        cards.shuffle()
    }

    // Take one card off the top of the deck
    fun draw(): Card? {
        return if (cards.isNotEmpty()) cards.removeAt(cards.size - 1) else null
    }

    // How many cards are left
    val size: Int get() = cards.size
}