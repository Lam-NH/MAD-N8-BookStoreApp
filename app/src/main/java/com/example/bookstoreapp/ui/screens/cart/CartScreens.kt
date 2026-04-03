package com.example.bookstoreapp.ui.screens.cart

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bookstoreapp.ui.components.MainTopAppBar
import com.example.bookstoreapp.ui.navigation.Screen
import com.example.bookstoreapp.ui.viewmodels.CartViewModel
import com.example.bookstoreapp.ui.viewmodels.OrderViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CartScreen(navController: NavController, cartViewModel: CartViewModel = viewModel()) {
    val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    LaunchedEffect(Unit) { cartViewModel.loadCart() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Giỏ hàng của bạn", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (cartViewModel.isLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (cartViewModel.cartItems.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Giỏ hàng trống", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(cartViewModel.cartItems) { item ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { navController.navigate("product_detail/${item.bookId}") }) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = cartViewModel.isSelected(item.cartItemId), onCheckedChange = { cartViewModel.toggleSelection(item.cartItemId, it) })
                            if (!item.bookImage.isNullOrEmpty()) {
                                AsyncImage(model = item.bookImage, contentDescription = null, modifier = Modifier.size(60.dp), contentScale = ContentScale.Crop)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                                Text(item.bookTitle ?: "Sách", style = MaterialTheme.typography.titleMedium, maxLines = 2)
                                Text(format.format(item.bookPrice ?: 0.0), color = MaterialTheme.colorScheme.primary)
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                    OutlinedButton(onClick = { cartViewModel.updateQuantity(item.cartItemId, item.quantity - 1) }, modifier = Modifier.size(28.dp), contentPadding = PaddingValues(0.dp)) { Text("-") }
                                    Text(item.quantity.toString(), modifier = Modifier.padding(horizontal = 12.dp))
                                    OutlinedButton(onClick = { cartViewModel.updateQuantity(item.cartItemId, item.quantity + 1) }, modifier = Modifier.size(28.dp), contentPadding = PaddingValues(0.dp)) { Text("+") }
                                }
                            }
                            IconButton(onClick = { cartViewModel.deleteItem(item.cartItemId) }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        if (cartViewModel.hasSelectedItems) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Tổng: ${format.format(cartViewModel.selectedTotalPrice)}", style = MaterialTheme.typography.titleLarge)
                Button(onClick = { navController.navigate(Screen.Checkout.route) }) { Text("Thanh toán") }
            }
        }
    }
}

@Composable
fun CheckoutScreen(navController: NavController, orderViewModel: OrderViewModel = viewModel()) {
    LaunchedEffect(Unit) { orderViewModel.loadShipments() }

    Column(modifier = Modifier.fillMaxSize()) {
        MainTopAppBar("Thanh toán", navController)
        Column(modifier = Modifier.padding(16.dp)) {
            Spacer(modifier = Modifier.height(8.dp))

            // Địa chỉ giao hàng
            Card(modifier = Modifier.fillMaxWidth().clickable { navController.navigate(Screen.AddressMap.route) }) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Địa chỉ giao hàng", fontWeight = FontWeight.Bold)
                        Text(orderViewModel.selectedAddress?.let { "${it.receiverName} - ${it.addressString}" } ?: "Chọn địa chỉ", color = Color.Gray, maxLines = 1)
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            // Phương thức thanh toán
            Card(modifier = Modifier.fillMaxWidth().clickable { navController.navigate(Screen.PaymentMethod.route) }) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Phương thức thanh toán", fontWeight = FontWeight.Bold)
                        Text(orderViewModel.selectedPayment?.paymentMethod ?: "Chọn phương thức", color = Color.Gray, maxLines = 1)
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            // Đơn vị vận chuyển
            Card(modifier = Modifier.fillMaxWidth().clickable { navController.navigate(Screen.ShipmentSelection.route) }) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Đơn vị vận chuyển", fontWeight = FontWeight.Bold)
                        Text(orderViewModel.selectedShipment?.shipmentMethod ?: "Chọn đơn vị", color = Color.Gray, maxLines = 1)
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            // Voucher
            Card(modifier = Modifier.fillMaxWidth().clickable { navController.navigate(Screen.VoucherSelection.route) }) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Khuyến mãi / Voucher", fontWeight = FontWeight.Bold)
                        Text(orderViewModel.selectedVoucher?.let { "${it.description}" } ?: "Chọn voucher", color = Color(0xFF2E7D32), maxLines = 1)
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    orderViewModel.checkout(
                        addressId = orderViewModel.selectedAddress?.addressId ?: 0,
                        paymentId = orderViewModel.selectedPayment?.paymentId ?: 0,
                        shipmentId = orderViewModel.selectedShipment?.shipmentId ?: 0,
                        voucherId = orderViewModel.selectedVoucher?.voucherId,
                        onSuccess = { navController.navigate(Screen.OrderHistory.route) { popUpTo(Screen.Home.route) } }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !orderViewModel.isLoading
            ) {
                if (orderViewModel.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text("Xác nhận Đặt hàng")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
