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
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
    var hasSearched by remember { mutableStateOf(bookViewModel.isSearchActive) }
    var showVoiceDialog by remember { mutableStateOf(false) }

    // Tự động xóa kết quả khi thoát khỏi màn hình Tìm kiếm
    DisposableEffect(Unit) {
        onDispose {
            bookViewModel.clearSearch()
        }
    }

    // Đồng bộ hasSearched với ViewModel
    LaunchedEffect(bookViewModel.isSearchActive) {
        hasSearched = bookViewModel.isSearchActive
    }

    // Tự động điền text nhận diện được vào ô tìm kiếm
    LaunchedEffect(bookViewModel.recognizedText) {
        bookViewModel.recognizedText?.let {
            query = it
            hasSearched = true
        }
    }

    // Debounce 400ms
    LaunchedEffect(query) {
        if (query.length >= 2 && query != bookViewModel.recognizedText) {
            delay(400)
            bookViewModel.search(query)
            hasSearched = true
        }
    }

    if (showVoiceDialog) {
        VoiceSearchDialog(
            onDismiss = { showVoiceDialog = false },
            bookViewModel = bookViewModel,
            onResult = { hasSearched = true }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MainTopAppBar("Tìm kiếm", navController)

        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Tìm những gì bạn muốn") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                IconButton(onClick = { showVoiceDialog = true }) {
                    Icon(Icons.Filled.Mic, contentDescription = "Giọng nói", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { navController.navigate("image_search") }) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = "Hình ảnh", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        if (bookViewModel.isLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (hasSearched && bookViewModel.searchResults.isEmpty() && bookViewModel.recognizedText == null) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Text("Không tìm thấy kết quả", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else if (hasSearched || bookViewModel.recognizedText != null) {
            Column(modifier = Modifier.weight(1f)) {
                if (bookViewModel.searchAuthorMatches.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clickable { navController.navigate("author_list") },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Xem các tác giả tìm được", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Xem")
                        }
                    }
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                items(bookViewModel.searchResults) { book ->
                    Card(onClick = { navController.navigate("product_detail/${book.bookId}") }, modifier = Modifier.height(280.dp)) {
                        Column(modifier = Modifier.padding(8.dp).fillMaxSize()) {
                            if (book.primaryImageUrl.isNotEmpty()) {
                                AsyncImage(model = book.primaryImageUrl, contentDescription = book.title, modifier = Modifier.height(150.dp).fillMaxWidth().clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Fit)
                            } else {
                                Box(modifier = Modifier.height(150.dp).fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant))
                            }
                            Text(book.title, modifier = Modifier.padding(top = 8.dp), maxLines = 2, style = MaterialTheme.typography.bodyMedium)
                            if (!book.author.isNullOrEmpty()) {
                                Text(book.author, color = androidx.compose.ui.graphics.Color.Gray, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.weight(1f))
                            val isAiSearch = bookViewModel.recognizedText != null
                            Text(
                                text = if (isAiSearch) "${"%,.0f".format(book.price * 100000)}đ" else "${"%,.0f".format(book.price)}đ",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun AuthorListScreen(navController: NavController, bookViewModel: BookViewModel = viewModel()) {
    Column(modifier = Modifier.fillMaxSize()) {
        MainTopAppBar("Tác giả tìm kiếm", navController)
        
        androidx.compose.foundation.lazy.LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(bookViewModel.searchAuthorMatches.size) { index ->
                val author = bookViewModel.searchAuthorMatches[index]
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        bookViewModel.selectedAuthor = author
                        navController.navigate("author_books/${author.authorID}")
                    },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(author.fullName ?: "Không rõ", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        Text("Xem chi tiết >>", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun AuthorBooksScreen(navController: NavController, authorId: Int, bookViewModel: BookViewModel = viewModel()) {
    val author = bookViewModel.selectedAuthor
    
    LaunchedEffect(authorId) { bookViewModel.loadBooksByAuthor(authorId) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        MainTopAppBar(author?.fullName ?: "Tác giả", navController)
        
        // Author info header
        if (author != null) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(author.fullName ?: "", style = MaterialTheme.typography.headlineSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    if (!author.bio.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(author.bio, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        
        // Books grid
        if (bookViewModel.isLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(bookViewModel.bookList) { book ->
                    Card(onClick = { navController.navigate("product_detail/${book.bookId}") }, modifier = Modifier.height(280.dp)) {
                        Column(modifier = Modifier.padding(8.dp).fillMaxSize()) {
                            if (book.primaryImageUrl.isNotEmpty()) {
                                AsyncImage(model = book.primaryImageUrl, contentDescription = book.title, modifier = Modifier.height(150.dp).fillMaxWidth().clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Fit)
                            } else {
                                Box(modifier = Modifier.height(150.dp).fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant))
                            }
                            Text(book.title, modifier = Modifier.padding(top = 8.dp), maxLines = 2, style = MaterialTheme.typography.bodyMedium)
                            if (!book.author.isNullOrEmpty()) {
                                Text(book.author, color = androidx.compose.ui.graphics.Color.Gray, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.weight(1f))
                             Text("${"%,.0f".format(book.price)}đ", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

