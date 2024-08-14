package com.example.booklogger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button // New import for button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.booklogger.ui.theme.BookLoggerTheme

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
                        composable("bookLog") { LogBookRead(navController) }
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Welcome to Book Logger!",
            modifier = Modifier.padding(16.dp)
        )

        Text(
            text = "Recently Read",
            modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
        )

        Box(
            modifier = Modifier
                .padding(16.dp)
                .width(100.dp)
                .height(150.dp)
                .background(Color.Gray)
                .clickable {
                    navController.navigate("bookLog")
                }
        ) {
            Text(
                text = "+",
                color = Color.White,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.Center)
            )
        }
    }
}

@Composable
fun LogBookRead(navController: NavHostController) {
    val bookName = remember { mutableStateOf("") }
    val numPages = remember { mutableStateOf("") }
    val readingTime = remember { mutableStateOf("") }
    val bookRating = remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextField(
            value = bookName.value,
            onValueChange = { bookName.value = it },
            label = { Text("Enter book name") }
        )
        TextField(
            value = numPages.value,
            onValueChange = { numPages.value = it },
            label = { Text("Number of pages") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        TextField(
            value = readingTime.value,
            onValueChange = { readingTime.value = it },
            label = { Text("Time reading") }
        )
        TextField(
            value = bookRating.value,
            onValueChange = { bookRating.value = it },
            label = { Text("Current book rating") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Button(
            onClick = {
                // Handle the saving of data here
                // For example, you might print the values or save them to a database
                println("Book Name: ${bookName.value}")
                println("Number of Pages: ${numPages.value}")
                println("Reading Time: ${readingTime.value}")
                println("Book Rating: ${bookRating.value}")

                // Navigate back to the home screen
                navController.popBackStack() // Pops the current screen off the stack
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Submit")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    BookLoggerTheme {
        HomeScreen(navController = rememberNavController())
    }
}
