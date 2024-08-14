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
import androidx.compose.ui.Alignment
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
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        Modifier.padding(innerPadding)
                    ) {
                        composable("home") { HomeScreen(navController) }
                        composable("recentlyRead") { RecentlyReadScreen() }

                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp) // Adjusted spacing
    ) {
        Text(
            text = "Welcome to Book Logger!",
            modifier = Modifier.padding(16.dp)
        )

        Text(
            text = "Recently Read",
            modifier = Modifier.padding(16.dp)
        )

        Box(
            modifier = Modifier
                .width(200.dp)
                .height(100.dp)
                .background(Color.Gray)
                .clickable {
                    navController.navigate("recentlyRead") // Updated navigation route
                }
        ) {
            Text(
                text = "Go to Recently Read",
                color = Color.White,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.Center) // Center text within the Box
            )
        }
    }
}

@Composable
fun RecentlyReadScreen(){
    Text(text = "This is a new page")
}



@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    BookLoggerTheme {
        HomeScreen(navController = rememberNavController())
    }
}