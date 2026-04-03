package com.example.bookstoreapp.ui.screens.search

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bookstoreapp.ui.components.MainTopAppBar
import com.example.bookstoreapp.ui.viewmodels.BookViewModel
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(navController: NavController, bookViewModel: BookViewModel = viewModel()) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var hasSearched by remember { mutableStateOf(false) }

    // Debounce 400ms
    LaunchedEffect(query) {
        if (query.length >= 2) {
            delay(400)
            bookViewModel.search(query)
            hasSearched = true
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MainTopAppBar("Tìm kiếm", navController)

        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Nhập tên sách...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                IconButton(onClick = { Toast.makeText(context, "Sẽ hiện popup thu âm", Toast.LENGTH_SHORT).show() }) {
                    Icon(Icons.Filled.Mic, contentDescription = "Giọng nói")
                }
                IconButton(onClick = { Toast.makeText(context, "Mở Camera", Toast.LENGTH_SHORT).show() }) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = "Hình ảnh")
                }
            }
        }

        if (bookViewModel.isLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (hasSearched && bookViewModel.searchResults.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Text("Không tìm thấy kết quả", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else if (hasSearched) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(bookViewModel.searchResults) { book ->
                    Card(onClick = { navController.navigate("product_detail/${book.bookId}") }) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            if (book.primaryImageUrl.isNotEmpty()) {
                                AsyncImage(model = book.primaryImageUrl, contentDescription = book.title, modifier = Modifier.height(120.dp).fillMaxWidth().clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
                            } else {
                                Box(modifier = Modifier.height(120.dp).fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant))
                            }
                            Text(book.title, modifier = Modifier.padding(top = 4.dp), maxLines = 2)
                            Text("${"%,.0f".format(book.price)}đ", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
