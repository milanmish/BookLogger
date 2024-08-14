package com.example.booklogger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background // New import
import androidx.compose.foundation.clickable // New import
import androidx.compose.foundation.layout.Box // New import
import androidx.compose.foundation.layout.height // New import
import androidx.compose.foundation.layout.width // New import
import androidx.compose.ui.graphics.Color // New import
import androidx.compose.ui.unit.dp // New import
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.booklogger.ui.theme.BookLoggerTheme
import androidx.navigation.NavHostController // For navigation controller
import androidx.navigation.compose.NavHost // For setting up the navigation host
import androidx.navigation.compose.composable // For defining composable destinations
import androidx.navigation.compose.rememberNavController // For creating the navigation controller


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BookLoggerTheme {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Book Logger",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(-25.dp)) {
        Text(
            text = "Welcome to $name!",
            modifier = modifier
        )

        Text(
            text = "Recently Read",
            modifier = modifier
        )

        Box(
            modifier = Modifier
                .width(200.dp)
                .height(100.dp)
                .background(Color.Gray)
                .clickable {
                    navController.navigate("new_page")
                }
        )
    }
}

@Composable
fun NewPage(){
    Text(text = "This is a new page")
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BookLoggerTheme {
        Greeting("Android")
    }
}