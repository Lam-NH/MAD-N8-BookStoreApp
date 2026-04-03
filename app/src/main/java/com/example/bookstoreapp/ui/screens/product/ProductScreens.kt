package com.example.bookstoreapp.ui.screens.product

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bookstoreapp.ui.components.MainTopAppBar
import com.example.bookstoreapp.ui.navigation.Screen
import com.example.bookstoreapp.ui.viewmodels.BookViewModel
import com.example.bookstoreapp.ui.viewmodels.CartViewModel

@Composable
fun ProductListScreen(navController: NavController, categoryId: Int? = null, title: String = "Tất cả sản phẩm", bookViewModel: BookViewModel = viewModel()) {
    LaunchedEffect(categoryId) { bookViewModel.loadBooksByCategory(categoryId, 1) }

    Column(modifier = Modifier.fillMaxSize()) {
        MainTopAppBar(title, navController)

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
                    Card(onClick = { navController.navigate("product_detail/${book.bookId}") }) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            if (book.primaryImageUrl.isNotEmpty()) {
                                AsyncImage(model = book.primaryImageUrl, contentDescription = book.title, modifier = Modifier.height(150.dp).fillMaxWidth().clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
                            } else {
                                Box(modifier = Modifier.height(150.dp).fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant))
                            }
                            Text(book.title, modifier = Modifier.padding(top = 8.dp), maxLines = 2)
                            if (!book.author.isNullOrEmpty()) {
                                Text(book.author, color = Color.Gray, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                            }
                            Text("${"%,.0f".format(book.price)}đ", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
        // Pagination
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { if (bookViewModel.currentPage > 1) bookViewModel.loadBooksByCategory(categoryId, bookViewModel.currentPage - 1) }, enabled = bookViewModel.currentPage > 1) { Text("<") }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Trang ${bookViewModel.currentPage} / ${bookViewModel.totalPages}")
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { if (bookViewModel.currentPage < bookViewModel.totalPages) bookViewModel.loadBooksByCategory(categoryId, bookViewModel.currentPage + 1) }, enabled = bookViewModel.currentPage < bookViewModel.totalPages) { Text(">") }
        }
    }
}

@Composable
fun ProductDetailScreen(navController: NavController, bookId: Int, cartViewModel: CartViewModel = viewModel(), bookViewModel: BookViewModel = viewModel()) {
    val context = LocalContext.current
    LaunchedEffect(bookId) { bookViewModel.loadBookDetail(bookId) }
    val detail = bookViewModel.bookDetail

    Scaffold(
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(onClick = {
                        cartViewModel.addToCart(bookId)
                        Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                    }, modifier = Modifier.weight(1f)) { Text("Thêm vào giỏ") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { navController.navigate(Screen.Checkout.route) }, modifier = Modifier.weight(1f)) { Text("Mua ngay") }
                }
            }
        }
    ) { paddingValues ->
        if (detail == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                MainTopAppBar("Chi tiết sách", navController)
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    // Ảnh sách
                    if (detail.book.primaryImageUrl.isNotEmpty()) {
                        AsyncImage(model = detail.book.primaryImageUrl, contentDescription = detail.book.title, modifier = Modifier.fillMaxWidth().height(250.dp), contentScale = ContentScale.Crop)
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().height(250.dp).background(MaterialTheme.colorScheme.surfaceVariant))
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(detail.book.title, style = MaterialTheme.typography.headlineMedium)
                        if (!detail.book.author.isNullOrEmpty()) {
                            Text("Tác giả: ${detail.book.author}", color = Color.Gray, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 4.dp))
                        }
                        Text("${"%,.0f".format(detail.book.price)}đ", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Mô tả sản phẩm", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(detail.book.description ?: "Chưa có mô tả.", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))

                        // Đánh giá
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Đánh giá sản phẩm", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Xem tất cả >", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { navController.navigate("review_list/$bookId") })
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(String.format("%.2f", detail.avgRating ?: 0.0), style = MaterialTheme.typography.headlineMedium, color = Color(0xFFFFB300))
                            Text("/5", color = Color.Gray, modifier = Modifier.padding(start = 4.dp))
                        }
                        detail.top3Reviews?.forEach { review ->
                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(review.customer?.fullName ?: "Ẩn danh", fontWeight = FontWeight.Bold)
                                    Text("⭐".repeat(review.rating.toInt()) + " ${review.comment}")
                                }
                            }
                        }

                        // Sản phẩm tương tự
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Sản phẩm tương tự", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(detail.similarBooks ?: emptyList()) { book ->
                                Card(modifier = Modifier.width(140.dp).clickable { navController.navigate("product_detail/${book.bookId}") }) {
                                    Column {
                                        if (book.primaryImageUrl.isNotEmpty()) {
                                            AsyncImage(model = book.primaryImageUrl, contentDescription = book.title, modifier = Modifier.height(140.dp).fillMaxWidth(), contentScale = ContentScale.Crop)
                                        } else {
                                            Box(modifier = Modifier.height(140.dp).fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant))
                                        }
                                        Text(book.title, modifier = Modifier.padding(8.dp), maxLines = 1)
                                        Text("${"%,.0f".format(book.price)}đ", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
