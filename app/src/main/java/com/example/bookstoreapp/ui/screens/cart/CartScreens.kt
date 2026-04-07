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
                                AsyncImage(model = item.fullImageUrl, contentDescription = null, modifier = Modifier.size(60.dp), contentScale = ContentScale.Crop)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                                Text(item.bookTitle, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                                Text(format.format(item.bookPrice * 100000), color = MaterialTheme.colorScheme.primary)
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                    OutlinedButton(onClick = { if (item.quantity > 1) cartViewModel.updateQuantity(item.cartItemId, item.quantity - 1) }, enabled = item.quantity > 1, modifier = Modifier.size(28.dp), contentPadding = PaddingValues(0.dp)) { Text("-") }
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
                Text("Tổng: ${format.format(cartViewModel.selectedTotalPrice * 100000)}", style = MaterialTheme.typography.titleLarge)
                Button(onClick = { navController.navigate(Screen.Checkout.route) }) { Text("Thanh toán") }
            }
        }
    }
}

@Composable
fun CheckoutScreen(navController: NavController, orderViewModel: OrderViewModel = viewModel(), cartViewModel: CartViewModel = viewModel()) {
    LaunchedEffect(Unit) { orderViewModel.loadShipments() }

    Column(modifier = Modifier.fillMaxSize()) {
        MainTopAppBar("Thanh toán", navController)
        Column(modifier = Modifier.padding(16.dp).weight(1f)) {
            Spacer(modifier = Modifier.height(8.dp))

            // Địa chỉ giao hàng
            Card(modifier = Modifier.fillMaxWidth().clickable { navController.navigate("address_selection") }) {
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
            Card(modifier = Modifier.fillMaxWidth().clickable { navController.navigate("payment_selection") }) {
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
            Card(modifier = Modifier.fillMaxWidth().clickable {
                navController.navigate("voucher_selection/${cartViewModel.selectedTotalPrice}")
            }) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Khuyến mãi / Voucher", fontWeight = FontWeight.Bold)
                        Text(orderViewModel.selectedVoucher?.let { "${it.description}" } ?: "Chọn voucher", color = Color(0xFF2E7D32), maxLines = 1)
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray)
                }
            }
        }

        // Bottom checkout button
        Surface(shadowElevation = 8.dp) {
            val finalPrice = cartViewModel.selectedTotalPrice // Can subtract voucher discount here if needed
            Button(
                onClick = {
                    orderViewModel.checkout(
                        addressId = orderViewModel.selectedAddress?.addressId ?: 0,
                        paymentId = orderViewModel.selectedPayment?.paymentId ?: 0,
                        shipmentId = orderViewModel.selectedShipment?.shipmentId ?: 0,
                        voucherId = orderViewModel.selectedVoucher?.voucherId,
                        onSuccess = { 
                            cartViewModel.loadCart() // Clear or refresh local cart
                            navController.navigate(Screen.OrderHistory.route) { popUpTo(Screen.Home.route) { inclusive = true } } 
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
                enabled = !orderViewModel.isLoading && orderViewModel.selectedAddress != null && orderViewModel.selectedPayment != null && orderViewModel.selectedShipment != null
            ) {
                if (orderViewModel.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                else {
                    Text("Xác nhận Đặt hàng")
                }
            }
        }
    }
}

@Composable
fun AddressSelectionScreen(navController: NavController, orderViewModel: OrderViewModel = viewModel(), profileViewModel: com.example.bookstoreapp.ui.viewmodels.ProfileViewModel = viewModel()) {
    LaunchedEffect(Unit) { profileViewModel.loadAddresses() }

    Column(modifier = Modifier.fillMaxSize()) {
        MainTopAppBar("Chọn địa chỉ giao hàng", navController)
        Column(modifier = Modifier.padding(16.dp).weight(1f)) {
            if (profileViewModel.addresses.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Chưa có địa chỉ nào", color = Color.Gray)
                }
            } else {
                profileViewModel.addresses.forEach { addr ->
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), onClick = {
                        orderViewModel.selectedAddress = addr
                        navController.popBackStack()
                    }) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(addr.receiverName, fontWeight = FontWeight.Bold)
                                Text(addr.addressString, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                            }
                            RadioButton(
                                selected = orderViewModel.selectedAddress?.addressId == addr.addressId,
                                onClick = { orderViewModel.selectedAddress = addr; navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
        Button(onClick = { navController.navigate(Screen.AddAddress.route) }, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("+ Thêm địa chỉ mới")
        }
    }
}

@Composable
fun PaymentSelectionScreen(navController: NavController, orderViewModel: OrderViewModel = viewModel(), profileViewModel: com.example.bookstoreapp.ui.viewmodels.ProfileViewModel = viewModel()) {
    LaunchedEffect(Unit) { profileViewModel.loadPayments() }

    Column(modifier = Modifier.fillMaxSize()) {
        MainTopAppBar("Chọn phương thức thanh toán", navController)
        Column(modifier = Modifier.padding(16.dp).weight(1f)) {
            if (profileViewModel.payments.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Chưa có phương thức nào", color = Color.Gray)
                }
            } else {
                profileViewModel.payments.forEach { payment ->
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), onClick = {
                        orderViewModel.selectedPayment = payment
                        navController.popBackStack()
                    }) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(payment.paymentMethod, modifier = Modifier.weight(1f))
                            RadioButton(
                                selected = orderViewModel.selectedPayment?.paymentId == payment.paymentId,
                                onClick = { orderViewModel.selectedPayment = payment; navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
        Button(onClick = { navController.navigate(Screen.AddPaymentMethod.route) }, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("+ Thêm phương thức mới")
        }
    }
}
