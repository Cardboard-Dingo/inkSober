package app.unpolished.inksober

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.unpolished.inksober.ui.InkSoberApp
import app.unpolished.inksober.ui.theme.InkSoberTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InkSoberApp()
        }
    }
}

@Preview(showBackground = true, widthDp = 480, heightDp = 800)
@Composable
fun InkSoberAppPreview() {
    InkSoberTheme {
        InkSoberApp()
    }
}
