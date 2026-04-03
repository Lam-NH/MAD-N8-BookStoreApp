package com.example.bookstoreapp.ui.screens.orders

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bookstoreapp.ui.components.MainTopAppBar
import com.example.bookstoreapp.ui.viewmodels.OrderViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun OrderHistoryScreen(navController: NavController, orderViewModel: OrderViewModel = viewModel()) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Tất cả", "Chờ thanh toán", "Đang xử lý", "Đang giao", "Hoàn tất", "Đã hủy")
    val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    val statusQuery = when (selectedTab) {
        0 -> null
        1 -> "Chờ thanh toán"
        2 -> "Đang xử lý"
        3 -> "Đang giao"
        4 -> "Hoàn tất"
        5 -> "Đã hủy"
        else -> null
    }

    LaunchedEffect(selectedTab) { orderViewModel.loadOrders(statusQuery) }

    Column(modifier = Modifier.fillMaxSize()) {
        MainTopAppBar("Lịch sử đơn hàng", navController)

        ScrollableTabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
            }
        }

        if (orderViewModel.isLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (orderViewModel.orders.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Text("Chưa có đơn hàng nào", color = Color.Gray) }
        } else {
            LazyColumn(modifier = Modifier.weight(1f).padding(8.dp)) {
                items(orderViewModel.orders) { order ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), onClick = { navController.navigate("order_detail/${order.orderId}") }) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Đơn hàng #${order.orderId}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(order.status ?: "", color = when(order.status) {
                                    "Hoàn tất" -> Color(0xFF2E7D32)
                                    "Đã hủy" -> Color.Red
                                    else -> MaterialTheme.colorScheme.primary
                                })
                            }
                            if (order.orderDate != null) Text("Ngày: ${order.orderDate}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                            Text("Tổng: ${format.format(order.finalAmount ?: order.totalAmount ?: 0.0)}", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderDetailScreen(navController: NavController, orderId: Int, orderViewModel: OrderViewModel = viewModel()) {
    val context = LocalContext.current
    val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    LaunchedEffect(orderId) { orderViewModel.loadOrderDetail(orderId) }
    val detail = orderViewModel.orderDetail

    Column(modifier = Modifier.fillMaxSize()) {
        MainTopAppBar("Chi tiết đơn hàng", navController)

        if (detail == null) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                Text("Đơn hàng #${detail.orderId}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Trạng thái: ${detail.status}", color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                if (detail.address != null) {
                    Text("Giao đến: ${detail.address.receiverName} - ${detail.address.addressString}", color = Color.Gray)
                }
                if (detail.payment != null) {
                    Text("Thanh toán: ${detail.payment.paymentMethod}", color = Color.Gray)
                }
                if (detail.shipment != null) {
                    Text("Vận chuyển: ${detail.shipment.shipmentMethod}", color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Sản phẩm:", fontWeight = FontWeight.Bold)
                detail.items?.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (!item.bookImage.isNullOrEmpty()) {
                            AsyncImage(model = item.bookImage, contentDescription = null, modifier = Modifier.size(50.dp), contentScale = ContentScale.Crop)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.bookTitle ?: "Sách", maxLines = 1)
                            Text("SL: ${item.quantity ?: 1} × ${format.format(item.bookPrice ?: 0.0)}", color = Color.Gray)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tổng cộng: ${format.format(detail.totalAmount ?: 0.0)}")
                if (detail.voucher != null) Text("Giảm giá: ${detail.voucher.description}", color = Color(0xFF2E7D32))
                Text("Thanh toán: ${format.format(detail.finalAmount ?: detail.totalAmount ?: 0.0)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            }

            if (detail.status == "Chờ thanh toán" || detail.status == "Đang xử lý") {
                Button(
                    onClick = {
                        orderViewModel.cancelOrder(orderId) {
                            Toast.makeText(context, "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) { Text("Hủy Đơn Hàng") }
            }
        }
    }
}
