    package com.example.drinkup
    
    import android.os.Bundle
    import androidx.activity.ComponentActivity
    import androidx.activity.compose.setContent
    import androidx.activity.enableEdgeToEdge
    import androidx.compose.foundation.layout.*
    import androidx.compose.material3.*
    import androidx.compose.runtime.* // ✅ PENTING
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.tooling.preview.Preview
    import com.example.drinkup.ui.theme.DrinkUpTheme
    import androidx.compose.runtime.saveable.rememberSaveable
    
    class MainActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContent {
                DrinkUpTheme {
    
                    var air by rememberSaveable { mutableStateOf(0) }
                    val target = 2000
    
                    val progress = (air.toFloat() / target).coerceIn(0f, 1f)
    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
    
                        Text(
                            text = "💧 DrinkUp",
                            style = MaterialTheme.typography.headlineMedium
                        )
    
                        Spacer(modifier = Modifier.height(20.dp))
                        
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )
                            ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
    
                                Text("Minum hari ini:")
                                Spacer(modifier = Modifier.height(8.dp))
    
                                Text(
                                    text = "$air ml",
                                    style = MaterialTheme.typography.headlineLarge
                                )
    
                                Spacer(modifier = Modifier.height(12.dp))
    
                                var air by rememberSaveable { mutableStateOf(0) }
    
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                )
    
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Progress: ${(progress * 100).toInt()}%")
                                Text("Target: $target ml")
                            }
                        }
    
                        Spacer(modifier = Modifier.height(24.dp))
    
                        Button(
                            onClick = { air += 250 },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Tambah Air +250ml")
                        }
    
                        Spacer(modifier = Modifier.height(10.dp))
    
                        OutlinedButton(
                            onClick = { air = 0 },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reset")
                        }
                    }
                }
            }
        }
    } // ✅ WAJIB ADA
    
    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {
        Text(text = "Hello $name!", modifier = modifier)
    }
    
    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        DrinkUpTheme {
            Greeting("Android")
        }
    }