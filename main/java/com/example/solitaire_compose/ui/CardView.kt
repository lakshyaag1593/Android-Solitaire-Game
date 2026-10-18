// ui/CardView.kt
package com.example.solitaire_compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.solitaire_compose.game.Card
import com.example.solitaire_compose.ui.theme.*

@Composable
fun CardView(
    card: Card,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    overrideWidth: Dp = CardWidth,
    overrideHeight: Dp = CardHeight
) {
    val shape = RoundedCornerShape(CardRadius)
    val borderColor = if (isSelected) SelectedGold else Color.Gray
    val borderWidth = if (isSelected) 3.dp else 0.5.dp

    Box(
        modifier = Modifier
            .width(overrideWidth)
            .height(overrideHeight)
            .clip(shape)
            .background(if (card.isFaceUp) CardWhite else Color(0xFF1A3A6B))
            .border(borderWidth, borderColor, shape)
            .clickable { onClick() }
    ) {
        if (card.isFaceUp) {
            FaceUpContent(card, overrideWidth, overrideHeight)
        } else {
            FaceDownContent()
        }
    }
}

@Composable
private fun BoxScope.FaceUpContent(card: Card, cardWidth: Dp, cardHeight: Dp) {
    val textColor = if (card.isRed) CardRed else CardBlack

    // Scale center symbol — larger multiplier for small top-row cards
    val isSmallCard = cardHeight < 100.dp
    val centerSize = if (isSmallCard) (cardHeight.value * 0.42f).sp
    else             (cardHeight.value * 0.32f).sp

    // Shift center symbol down slightly more on small cards
    val centerOffsetY = if (isSmallCard) cardHeight * 0.08f else cardHeight * 0.04f

    // Top-left: align baseline of rank and suit symbol
    Row(
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(4.dp),
        verticalAlignment = Alignment.Bottom    // ← Bottom alignment fixes the offset
    ) {
        Text(
            text = card.rank.display,
            color = textColor,
            fontSize = RankFontSize,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(1.dp))
        Text(
            text = card.suitSymbol,
            color = textColor,
            fontSize = SuitFontSize,
            fontWeight = FontWeight.Bold
        )
    }

    // Center symbol — shifted down and sized based on card height
    Text(
        text = card.suitSymbol,
        color = textColor,
        fontSize = centerSize,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .align(Alignment.Center)
            .offset(y = centerOffsetY)
    )
}

@Composable
private fun BoxScope.FaceDownContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .border(2.dp, Color(0xFF4A90D9), RoundedCornerShape(4.dp))
    ) {
        Text(
            text = "🂠",
            fontSize = 48.sp,
            modifier = Modifier.align(Alignment.Center),
            color = Color(0xFF4A90D9)
        )
    }
}