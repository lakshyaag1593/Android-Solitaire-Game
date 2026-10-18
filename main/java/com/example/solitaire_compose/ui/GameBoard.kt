package com.example.solitaire_compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.solitaire_compose.game.Card
import com.example.solitaire_compose.game.GameState
import com.example.solitaire_compose.game.Suit
import com.example.solitaire_compose.ui.CardView
import com.example.solitaire_compose.ui.theme.*
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf

@Composable
fun GameBoard() {
    val gameState = remember { GameState().also { it.deal() } }
    var selectedCard by remember { mutableStateOf<Card?>(null) }

    // ── Timer ────────────────────────────────────────────────────────────────
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var timerRunning by remember { mutableStateOf(true) }

    LaunchedEffect(timerRunning) {
        while (timerRunning) {
            delay(1000)
            elapsedSeconds++
        }
    }

    // ── Win Screen ───────────────────────────────────────────────────────────
    if (gameState.isWon) {
        timerRunning = false
        WinScreen(
            moves = gameState.moveCount,
            seconds = elapsedSeconds,
            onNewGame = {
                gameState.deal()
                selectedCard = null
                elapsedSeconds = 0
                timerRunning = true
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TableGreen)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── Top Row ──────────────────────────────────────────────────────────
        TopRow(
            gameState = gameState,
            selectedCard = selectedCard,
            onCardSelected = { tapped ->
                handleTap(tapped, selectedCard, gameState) { selectedCard = it }
            }
        )

        // ── Stats + Buttons Row ──────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Move counter + timer on the left
            Column {
                Text(
                    text = "Moves: ${gameState.moveCount}",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Time: ${formatTime(elapsedSeconds)}",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Undo + New Game buttons on the right
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Undo button
                Box(
                    modifier = Modifier
                        .background(
                            if (gameState.canUndo) Color.White.copy(alpha = 0.2f)
                            else Color.White.copy(alpha = 0.08f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable(enabled = gameState.canUndo) {
                            gameState.undo()
                            selectedCard = null
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "↩ Undo",
                        fontSize = 13.sp,
                        color = if (gameState.canUndo) Color.White
                        else Color.White.copy(alpha = 0.3f),
                        fontWeight = FontWeight.Bold
                    )
                }

                // New Game button
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .clickable {
                            gameState.deal()
                            selectedCard = null
                            elapsedSeconds = 0
                            timerRunning = true
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "New Game",
                        fontSize = 13.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ── Tableau ──────────────────────────────────────────────────────────
        TableauArea(
            gameState = gameState,
            selectedCard = selectedCard,
            onCardSelected = { tapped ->
                handleTap(tapped, selectedCard, gameState) { selectedCard = it }
            },
            onEmptyColumnTapped = { colIndex ->
                if (selectedCard != null) {
                    gameState.moveToTableau(selectedCard!!, colIndex)
                }
                selectedCard = null
            }
        )
    }
}

// Formats seconds into MM:SS
fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}

// ── Top Row ────────────────────────────────────────────────────────────────
@Composable
fun TopRow(
    gameState: GameState,
    selectedCard: Card?,
    onCardSelected: (Card?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Group 1: Stock + Waste (tightly packed)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            StockPileView(gameState, onCardSelected)
            WastePileView(gameState, selectedCard, onCardSelected)
        }

        // Gap between the two groups
        Spacer(modifier = Modifier.weight(1f))

        // Group 2: 4 Foundation piles (tightly packed)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(4) { index ->
                FoundationPileView(gameState, index, selectedCard, onCardSelected)
            }
        }
    }
}
@Composable
fun StockPileView(gameState: GameState, onCardSelected: (Card?) -> Unit) {
    Box(
        modifier = Modifier
            .width(TopRowCardWidth)
            .height(TopRowCardHeight)
            .border(2.dp, PileOutline, RoundedCornerShape(CardRadius))
            .clickable {
                gameState.drawFromStock()   // ← single clean call
                onCardSelected(null)
            },
        contentAlignment = Alignment.Center
    ) {
        if (gameState.stock.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .width(TopRowCardWidth)
                    .height(TopRowCardHeight)
                    .clip(RoundedCornerShape(CardRadius))
                    .background(Color(0xFF1A3A6B))
                    .border(0.5.dp, Color.Gray, RoundedCornerShape(CardRadius))
            )
        } else {
            Text("↺", fontSize = 28.sp, color = PileOutline)
        }
    }
}

@Composable
fun WastePileView(
    gameState: GameState,
    selectedCard: Card?,
    onCardSelected: (Card?) -> Unit
) {
    val topCard = gameState.waste.lastOrNull()
    if (topCard != null) {
        CardView(
            card = topCard,
            isSelected = selectedCard == topCard,
            onClick = { onCardSelected(if (selectedCard == topCard) null else topCard) },
            overrideWidth = TopRowCardWidth,
            overrideHeight = TopRowCardHeight
        )
    } else {
        Box(
            modifier = Modifier
                .width(TopRowCardWidth)
                .height(TopRowCardHeight)
                .border(2.dp, PileOutline, RoundedCornerShape(CardRadius))
        )
    }
}

@Composable
fun FoundationPileView(
    gameState: GameState,
    index: Int,
    selectedCard: Card?,
    onCardSelected: (Card?) -> Unit
) {
    val topCard = gameState.foundations[index].lastOrNull()

    // Label comes from the pre-assigned suit, not a hardcoded list
    val label = when (gameState.foundationSuits[index]) {
        Suit.HEARTS   -> "♥"
        Suit.DIAMONDS -> "♦"
        Suit.CLUBS    -> "♣"
        Suit.SPADES   -> "♠"
    }

    if (topCard != null) {
        CardView(
            card = topCard,
            isSelected = selectedCard == topCard,
            onClick = { onCardSelected(if (selectedCard == topCard) null else topCard) },
            overrideWidth = TopRowCardWidth,
            overrideHeight = TopRowCardHeight
        )
    } else {
        Box(
            modifier = Modifier
                .width(TopRowCardWidth)
                .height(TopRowCardHeight)
                .border(2.dp, PileOutline, RoundedCornerShape(CardRadius)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = label, fontSize = 18.sp, color = PileOutline)
        }
    }
}

// ── Tableau ─────────────────────────────────────────────────────────────────

@Composable
fun TableauArea(
    gameState: GameState,
    selectedCard: Card?,
    onCardSelected: (Card?) -> Unit,
    onEmptyColumnTapped: (Int) -> Unit    // ← add this
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        gameState.tableau.forEachIndexed { colIndex, column ->
            TableauColumn(
                column = column,
                selectedCard = selectedCard,
                onCardSelected = onCardSelected,
                onEmptyColumnTapped = { onEmptyColumnTapped(colIndex) },    // ← pass index
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TableauColumn(
    column: MutableList<Card>,
    selectedCard: Card?,
    onCardSelected: (Card?) -> Unit,
    onEmptyColumnTapped: () -> Unit,    // ← add this
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        if (column.isEmpty()) {
            Box(
                modifier = Modifier
                    .width(CardWidth)
                    .height(CardHeight)
                    .border(2.dp, PileOutline, RoundedCornerShape(CardRadius))
                    .clickable { onEmptyColumnTapped() },    // ← wire it up
                contentAlignment = Alignment.Center
            ) {
                Text("K", fontSize = 28.sp, color = PileOutline)
            }
        } else {
            column.forEachIndexed { index, card ->
                Box(modifier = Modifier.offset(y = (index * CardOverlap.value).dp)) {
                    CardView(
                        card = card,
                        isSelected = selectedCard == card,
                        onClick = {
                            if (card.isFaceUp) {
                                onCardSelected(if (selectedCard == card) null else card)
                            }
                        }
                    )
                }
            }
        }
    }
}

// ── Tap Logic ─────────────────────────────────────────────────────────────────

fun handleTap(
    tapped: Card?,
    selectedCard: Card?,
    gameState: GameState,
    setSelected: (Card?) -> Unit
) {
    when {
        // Nothing selected yet — select this card
        selectedCard == null -> setSelected(tapped)

        // Tapped the same card — deselect
        selectedCard == tapped -> setSelected(null)

        // A card is selected and we tapped a destination:
        else -> {
            // Try foundation first, then tableau
            val moved = gameState.moveToFoundation(selectedCard!!)
                    || tapped?.let { tryMoveToTableau(selectedCard!!, it, gameState) } == true

            setSelected(null)

            // If move failed, select the newly tapped card instead
            if (!moved && tapped != null && tapped.isFaceUp) {
                setSelected(tapped)
            }
        }
    }
}

private fun tryMoveToTableau(
    card: Card,
    tappedCard: Card,
    gameState: GameState
): Boolean {
    // Find which column the tapped card belongs to
    val colIndex = gameState.tableau.indexOfFirst { col ->
        col.contains(tappedCard)
    }
    if (colIndex == -1) return false
    return gameState.moveToTableau(card, colIndex)
}

@Composable
fun WinScreen(
    moves: Int,
    seconds: Int,
    onNewGame: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TableGreen),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎉 You Win!", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Congratulations!", fontSize = 24.sp, color = Color.White)
        Spacer(modifier = Modifier.height(24.dp))
        Text("Moves: $moves", fontSize = 20.sp, color = Color.White)
        Text("Time: ${formatTime(seconds)}", fontSize = 20.sp, color = Color.White)
        Spacer(modifier = Modifier.height(40.dp))
        Box(
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(12.dp))
                .clickable { onNewGame() }
                .padding(horizontal = 32.dp, vertical = 16.dp)
        ) {
            Text("New Game", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TableGreen)
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

@Composable
fun EmptyPileSlot(label: String) {
    Box(
        modifier = Modifier
            .width(CardWidth)
            .height(CardHeight)
            .border(2.dp, PileOutline, RoundedCornerShape(CardRadius)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontSize = 28.sp, color = PileOutline)
    }
}