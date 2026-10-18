package com.example.solitaire_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.solitaire_compose.game.GameState
import com.example.solitaire_compose.ui.GameBoard
import com.example.solitaire_compose.ui.theme.Solitaire_ComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameBoard()
        }
        val game = GameState()
        game.deal()
        println("Stock pile has: ${game.stock.size} cards")       // Should print 24
        println("Column 6 has:   ${game.tableau[6].size} cards")  // Should print 7
    }
}
