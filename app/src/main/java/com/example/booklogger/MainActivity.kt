package com.example.booklogger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
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
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") { MainScreen(navController) }
                        composable("bookLogger") { BookLogger(navController) }
                        composable(
                            "bookDetails/{title}/{author}/{pages}/{timeSpent}",
                            arguments = listOf(
                                navArgument("title") { type = NavType.StringType },
                                navArgument("author") { type = NavType.StringType },
                                navArgument("pages") { type = NavType.StringType },
                                navArgument("timeSpent") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val title = backStackEntry.arguments?.getString("title") ?: ""
                            val author = backStackEntry.arguments?.getString("author") ?: ""
                            val pages = backStackEntry.arguments?.getString("pages") ?: ""
                            val timeSpent = backStackEntry.arguments?.getString("timeSpent") ?: ""
                            BookDetails(
                                title = title,
                                author = author,
                                pages = pages,
                                timeSpent = timeSpent,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(navController: NavHostController) {
    var books by remember { mutableStateOf(listOf<Map<String, String>>()) }

    val newBook = navController.currentBackStackEntry?.savedStateHandle?.get<Map<String, String>>("newBook")
    newBook?.let {
        books = books + it
        navController.currentBackStackEntry?.savedStateHandle?.remove<Map<String, String>>("newBook")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Book Logger",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Recently Read",
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Button(
                onClick = { navController.navigate("bookLogger") },
                modifier = Modifier
                    .size(100.dp, 150.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text(text = "+", fontSize = 40.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            books.forEach { book ->
                Button(
                    onClick = {
                        navController.navigate("bookDetails/${book["title"]}/${book["author"]}/${book["pages"]}/${book["timeSpent"]}")
                    },
                    modifier = Modifier
                        .size(100.dp, 150.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text(text = book["title"] ?: "", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BookLogger(navController: NavHostController) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var pages by remember { mutableStateOf("") }
    var timeSpent by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = author,
            onValueChange = { author = it },
            label = { Text("Author") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = pages,
            onValueChange = { pages = it },
            label = { Text("Pages") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = timeSpent,
            onValueChange = { timeSpent = it },
            label = { Text("Time Spent (in hours)") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (title.isNotEmpty()) {
                    val book = mapOf(
                        "title" to title,
                        "author" to author,
                        "pages" to pages,
                        "timeSpent" to timeSpent
                    )
                    navController.previousBackStackEntry?.savedStateHandle?.set("newBook", book)
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit")
        }
    }
}

@Composable
fun BookDetails(title: String, author: String, pages: String, timeSpent: String, navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Title: $title",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Author: $author",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Pages: $pages",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Time Spent: $timeSpent hours",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Button to navigate back to MainScreen
        Button(
            onClick = {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Home")
        }
    }
}