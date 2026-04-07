package com.example.bookstoreapp.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bookstoreapp.ui.navigation.Screen
import com.example.bookstoreapp.ui.viewmodels.BookViewModel

@Composable
fun HomeScreen(navController: NavController, bookViewModel: BookViewModel = viewModel()) {
    LaunchedEffect(Unit) {
        bookViewModel.loadCategories()
        bookViewModel.loadForYou()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Thanh tìm kiếm giả
        Surface(modifier = Modifier.fillMaxWidth().padding(16.dp).clickable { navController.navigate(Screen.Search.route) }, shadowElevation = 4.dp, shape = RoundedCornerShape(8.dp)) {
            Text("Tìm kiếm sản phẩm...", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Danh mục
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Danh mục", style = MaterialTheme.typography.titleLarge)
            Text("Xem tất cả", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { navController.navigate(Screen.CategoryList.route) })
        }
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(bookViewModel.categories) { category ->
                Card(modifier = Modifier.clickable { navController.navigate("product_list?categoryId=${category.categoryId}&title=${category.categoryName}") }) {
                    Text(category.categoryName, modifier = Modifier.padding(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dành riêng cho bạn
        Text("Dành riêng cho bạn", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(bookViewModel.forYouBooks) { book ->
                Card(modifier = Modifier.fillMaxWidth().height(260.dp).clickable { navController.navigate("product_detail/${book.bookId}") }) {
                    Column(modifier = Modifier.padding(8.dp).fillMaxSize()) {
                        if (book.primaryImageUrl.isNotEmpty()) {
                            AsyncImage(model = book.primaryImageUrl, contentDescription = book.title, modifier = Modifier.height(120.dp).fillMaxWidth().clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Fit)
                        } else {
                            Box(modifier = Modifier.height(120.dp).fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(book.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                        if (!book.author.isNullOrEmpty()) {
                            Text(book.author, color = androidx.compose.ui.graphics.Color.Gray, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.weight(1f))
                        Text("${"%,.0f".format(book.price)}đ", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}
