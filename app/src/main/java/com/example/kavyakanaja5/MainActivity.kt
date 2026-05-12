package com.example.kavyakanaja5

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Theme Colors (Dark Green & White)
val DarkGreen = Color(0xFF1B5E20)
val LightCream = Color(0xFFF1F8E9)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KavyaKanajaApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KavyaKanajaApp() {
    val navController = rememberNavController()

    Surface(color = LightCream, modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = "login") {
            composable("login") { LoginScreen { navController.navigate("home") } }
            composable("home") { HomeScreen(navController) }
            composable("detail/{poemId}") { backStackEntry ->
                val poemId = backStackEntry.arguments?.getString("poemId")
                val poem = PoemRepository.poems.find { it.id == poemId }
                poem?.let { PoemDetailScreen(it) { navController.popBackStack() } }
            }
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var username by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(80.dp), tint = DarkGreen)
        Text("Kavya Kanaja", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DarkGreen,
                focusedLabelColor = DarkGreen,
                cursorColor = DarkGreen
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLoginSuccess,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
        ) {
            Text("Enter Granary", color = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: androidx.navigation.NavController) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredPoems = PoemRepository.poems.filter {
        it.title.contains(searchQuery, ignoreCase = true) || it.author.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Poetry Granary", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkGreen)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search poems...") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(filteredPoems) { poem ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable {
                            navController.navigate("detail/${poem.id}")
                        },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(poem.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
                            Text(poem.author, fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PoemDetailScreen(poem: Poem, onBack: () -> Unit) {
    val context = LocalContext.current
    // Initialize MediaPlayer - Ensure speech.mp3 is in res/raw/ folder
    val mediaPlayer = remember {
        try { MediaPlayer.create(context, R.raw.speech) } catch (e: Exception) { null }
    }
    var isPlaying by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { mediaPlayer?.release() }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
            Spacer(modifier = Modifier.weight(1f))

            // Audio Button
            Button(
                onClick = {
                    if (isPlaying) {
                        mediaPlayer?.pause()
                    } else {
                        mediaPlayer?.start()
                    }
                    isPlaying = !isPlaying
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
            ) {
                Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isPlaying) "Pause" else "Play Poem")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(poem.title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
        Text("By ${poem.author}", fontSize = 16.sp, color = DarkGreen.copy(alpha = 0.7f))

        Spacer(modifier = Modifier.height(32.dp))

        poem.kannadaLines.forEachIndexed { index, line ->
            Text(line, fontSize = 22.sp, fontWeight = FontWeight.Medium, color = DarkGreen)
            if (index < poem.englishLines.size) {
                Text(poem.englishLines[index], fontSize = 16.sp, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(40.dp))

        Card(colors = CardDefaults.cardColors(containerColor = DarkGreen), shape = RoundedCornerShape(24.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("BHAVARTHA", color = Color.White, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(poem.bhavartha, color = Color.White, fontSize = 16.sp)
            }
        }
    }
}

// Previews
@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    LoginScreen {}
}

@Preview(showBackground = true)
@Composable
fun DetailPreview() {
    PoemDetailScreen(PoemRepository.poems[0]) {}
}
