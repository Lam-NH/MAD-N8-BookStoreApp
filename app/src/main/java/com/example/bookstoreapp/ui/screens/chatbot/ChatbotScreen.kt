package com.example.bookstoreapp.ui.screens.chatbot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bookstoreapp.data.api.RetrofitClient
import com.example.bookstoreapp.data.model.ChatbotRequest
import kotlinx.coroutines.launch

data class Message(val text: String, val isUser: Boolean)

@Composable
fun ChatbotScreen(navController: NavController) {
    val messages = remember { mutableStateListOf<Message>(Message("Xin chào! Tôi có thể giúp gì cho bạn hôm nay?", isUser = false)) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().height(56.dp).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.CenterStart) {
            Text("Trợ lý ảo AI", color = Color.White, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 16.dp))
        }

        LazyColumn(modifier = Modifier.weight(1f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(messages) { msg ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (msg.isUser) MaterialTheme.colorScheme.primary else Color(0xFFE0E0E0)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(msg.text, color = if (msg.isUser) Color.White else Color.Black, modifier = Modifier.padding(12.dp))
                    }
                }
            }
            if (isLoading) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0)), shape = RoundedCornerShape(16.dp)) {
                            CircularProgressIndicator(modifier = Modifier.padding(12.dp).size(20.dp))
                        }
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Nhắn tin với trợ lý...") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = {
                if (inputText.isNotBlank() && !isLoading) {
                    val userMsg = inputText
                    messages.add(Message(userMsg, true))
                    inputText = ""
                    isLoading = true
                    scope.launch {
                        try {
                            val response = RetrofitClient.api.chatbot(ChatbotRequest(userMsg))
                            val reply = if (response.isSuccessful) {
                                response.body()?.reply ?: response.body()?.message ?: "Không có phản hồi"
                            } else { "Lỗi: Không thể kết nối chatbot" }
                            messages.add(Message(reply, false))
                        } catch (e: Exception) {
                            messages.add(Message("Lỗi kết nối: ${e.message}", false))
                        }
                        isLoading = false
                    }
                }
            }) {
                Icon(Icons.Filled.Send, contentDescription = "Gửi", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
