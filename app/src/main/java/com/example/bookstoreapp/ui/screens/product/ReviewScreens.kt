package com.example.bookstoreapp.ui.screens.product

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bookstoreapp.ui.components.MainTopAppBar
import com.example.bookstoreapp.ui.viewmodels.BookViewModel

@Composable
fun ReviewListScreen(navController: NavController, bookId: Int, bookViewModel: BookViewModel = viewModel()) {
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(bookId) { bookViewModel.loadAllReviews(bookId) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Filled.Edit, contentDescription = "Viết đánh giá", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            MainTopAppBar("Tất cả đánh giá", navController)

            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${bookViewModel.allReviews.size} Lượt nhận xét", color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()

            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                items(bookViewModel.allReviews) { review ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(review.customer?.fullName ?: "Khách hàng", fontWeight = FontWeight.Bold)
                                Text(review.createdAt ?: "", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                            }
                            Text("⭐".repeat(review.rating.toInt()), modifier = Modifier.padding(vertical = 4.dp))
                            Text(review.comment)
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        var rating by remember { mutableStateOf(5) }
        var comment by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Viết đánh giá") },
            text = {
                Column {
                    Text("Chọn số sao:", fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.padding(vertical = 8.dp)) {
                        (1..5).forEach { star ->
                            TextButton(onClick = { rating = star }) {
                                Text(if (star <= rating) "⭐" else "☆", style = MaterialTheme.typography.headlineSmall)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = comment, onValueChange = { comment = it }, label = { Text("Nhận xét của bạn") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                }
            },
            confirmButton = {
                Button(onClick = {
                    bookViewModel.postReview(bookId, rating, comment) {
                        showDialog = false
                        Toast.makeText(context, "Đã gửi đánh giá!", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Gửi đánh giá") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Hủy") } }
        )
    }
}
