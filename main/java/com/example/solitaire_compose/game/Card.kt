package com.example.solitaire_compose.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// Represents the four suits
enum class Suit {
    HEARTS, DIAMONDS, CLUBS, SPADES
}

// Represents card ranks 1 (Ace) through 13 (King)
enum class Rank(val value: Int, val display: String) {
    ACE(1, "A"),
    TWO(2, "2"),
    THREE(3, "3"),
    FOUR(4, "4"),
    FIVE(5, "5"),
    SIX(6, "6"),
    SEVEN(7, "7"),
    EIGHT(8, "8"),
    NINE(9, "9"),
    TEN(10, "10"),
    JACK(11, "J"),
    QUEEN(12, "Q"),
    KING(13, "K")
}

// A single playing card
class Card(
    val suit: Suit,
    val rank: Rank,
    initialFaceUp: Boolean = false   // Cards start face-down by default
) {
    var isFaceUp by mutableStateOf<Boolean>(initialFaceUp)

    // Red suits: Hearts and Diamonds
    val isRed: Boolean get() = suit == Suit.HEARTS || suit == Suit.DIAMONDS

    // The symbol to display for the suit
    val suitSymbol: String get() = when (suit) {
        Suit.HEARTS   -> "♥"
        Suit.DIAMONDS -> "♦"
        Suit.CLUBS    -> "♣"
        Suit.SPADES   -> "♠"
    }
}