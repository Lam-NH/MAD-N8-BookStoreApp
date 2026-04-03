package com.example.bookstoreapp.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bookstoreapp.data.api.TokenManager
import com.example.bookstoreapp.data.local.DatabaseHelper
import com.example.bookstoreapp.ui.components.MainTopAppBar
import com.example.bookstoreapp.ui.navigation.Screen
import com.example.bookstoreapp.ui.screens.auth.LoginScreen
import com.example.bookstoreapp.ui.viewmodels.AuthViewModel
import com.example.bookstoreapp.ui.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    var isLoggedIn by remember { mutableStateOf(dbHelper.isLoggedIn()) }

    if (!isLoggedIn) {
        LoginScreen(navController = navController)
    } else {
        ProfileContent(navController = navController) {
            dbHelper.setLoginData(false)
            TokenManager.token = null
            TokenManager.customerId = -1
            isLoggedIn = false
            navController.navigate(Screen.Home.route) { popUpTo(0) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(navController: NavController, onLogout: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(MaterialTheme.colorScheme.primary)) {
            Text("Tài khoản", modifier = Modifier.align(Alignment.Center), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Card(modifier = Modifier.fillMaxWidth().padding(16.dp).clickable { navController.navigate(Screen.OrderHistory.route) }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Đơn hàng của tôi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OrderStateItem(Icons.Outlined.CreditCard, "Chờ thanh toán")
                    OrderStateItem(Icons.Outlined.Inventory2, "Đang xử lý")
                    OrderStateItem(Icons.Outlined.LocalShipping, "Đang giao hàng")
                    OrderStateItem(Icons.Outlined.CheckCircleOutline, "Hoàn tất")
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp).fillMaxWidth().background(Color(0xFFF3F3F3)))

        ProfileMenuItem(Icons.Outlined.PersonOutline, "Hồ sơ cá nhân", onClick = { navController.navigate(Screen.EditProfile.route) })
        HorizontalDivider(color = Color(0xFFF3F3F3), thickness = 1.dp)
        ProfileMenuItem(Icons.Outlined.LocationOn, "Quản lý địa chỉ", onClick = { navController.navigate(Screen.AddressMap.route) })
        HorizontalDivider(color = Color(0xFFF3F3F3), thickness = 1.dp)
        ProfileMenuItem(Icons.Outlined.Payment, "Phương thức thanh toán", onClick = { navController.navigate(Screen.PaymentMethod.route) })
        HorizontalDivider(color = Color(0xFFF3F3F3), thickness = 1.dp)
        ProfileMenuItem(Icons.Outlined.Lock, "Đổi mật khẩu", onClick = { navController.navigate(Screen.ChangePassword.route) })

        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onLogout, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
            Text("Đăng xuất", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun OrderStateItem(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = label, tint = Color.DarkGray)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = title, tint = if (title == "Hồ sơ cá nhân" || title == "Đổi mật khẩu") Color(0xFFE91E63) else if (title == "Quản lý địa chỉ") Color(0xFF4CAF50) else Color(0xFF2196F3))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, modifier = Modifier.weight(1f), fontSize = 16.sp)
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController, profileViewModel: ProfileViewModel = viewModel()) {
    LaunchedEffect(Unit) { profileViewModel.loadProfile() }
    val profile = profileViewModel.profile

    var name by remember(profile) { mutableStateOf(profile?.fullName ?: "") }
    var email by remember(profile) { mutableStateOf(profile?.email ?: "") }
    var phone by remember(profile) { mutableStateOf(profile?.phoneNumber ?: "") }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        TopAppBar(
            title = { Text("Cập nhật thông tin", color = Color.White) },
            navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Filled.ArrowBack, contentDescription = "Trở về", tint = Color.White) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
        )

        Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
            Text("Họ và tên", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, placeholder = { Text("Nhập họ và tên") }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))
            Text("Email", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
            OutlinedTextField(value = email, onValueChange = {}, placeholder = { Text("Email") }, modifier = Modifier.fillMaxWidth(), enabled = false)

            Spacer(modifier = Modifier.height(16.dp))
            Text("Số điện thoại", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
            OutlinedTextField(value = phone, onValueChange = { phone = it }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = {
                profileViewModel.updateProfile(name, phone) {
                    Toast.makeText(context, "Đã cập nhật!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                Text("Lưu thay đổi", color = Color.White)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun AddressScreen(navController: NavController, profileViewModel: ProfileViewModel = viewModel()) {
    LaunchedEffect(Unit) { profileViewModel.loadAddresses() }

    Column(modifier = Modifier.fillMaxSize()) {
        MainTopAppBar("Quản lý địa chỉ", navController)
        Column(modifier = Modifier.padding(16.dp).weight(1f)) {
            profileViewModel.addresses.forEach { addr ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), onClick = { navController.popBackStack() }) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${addr.receiverName}", fontWeight = FontWeight.Bold)
                            Text(addr.addressString, color = Color.Gray)
                        }
                        IconButton(onClick = { profileViewModel.deleteAddress(addr.addressId ?: 0) }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error)
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
fun PaymentMethodScreen(navController: NavController, profileViewModel: ProfileViewModel = viewModel()) {
    LaunchedEffect(Unit) { profileViewModel.loadPayments() }

    Column(modifier = Modifier.fillMaxSize()) {
        MainTopAppBar("Phương thức thanh toán", navController)
        Column(modifier = Modifier.padding(16.dp).weight(1f)) {
            profileViewModel.payments.forEach { payment ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), onClick = { navController.popBackStack() }) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(payment.paymentMethod, modifier = Modifier.weight(1f))
                        IconButton(onClick = { profileViewModel.deletePayment(payment.paymentId ?: 0) }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error)
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

@Composable
fun ChangePasswordScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(authViewModel.passwordChanged) {
        if (authViewModel.passwordChanged) {
            authViewModel.passwordChanged = false
            Toast.makeText(context, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MainTopAppBar("Đổi mật khẩu", navController)
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(value = oldPass, onValueChange = { oldPass = it }, label = { Text("Mật khẩu cũ") }, modifier = Modifier.fillMaxWidth(), visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation())
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = newPass, onValueChange = { newPass = it }, label = { Text("Mật khẩu mới") }, modifier = Modifier.fillMaxWidth(), visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation())
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = confirmPass, onValueChange = { confirmPass = it }, label = { Text("Xác nhận mật khẩu") }, modifier = Modifier.fillMaxWidth(), visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation())
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { authViewModel.changePassword(oldPass, newPass, confirmPass) }, modifier = Modifier.fillMaxWidth(), enabled = !authViewModel.isLoading) {
                Text("Cập nhật mật khẩu")
            }
        }
    }
}

@Composable
fun AddAddressScreen(navController: NavController, profileViewModel: ProfileViewModel = viewModel()) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var ward by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        MainTopAppBar("Thêm địa chỉ giao hàng", navController)
        Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Họ và Tên") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Số điện thoại") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("Tỉnh / Thành phố") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("Quận / Huyện") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = ward, onValueChange = { ward = it }, label = { Text("Phường / Xã") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = detail, onValueChange = { detail = it }, label = { Text("Chi tiết (Số nhà, đường...)") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                val addressStr = "$detail, $ward, $district, $city | SĐT: $phone"
                profileViewModel.createAddress(name, addressStr) {
                    Toast.makeText(context, "Đã lưu địa chỉ!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Lưu địa chỉ") }
        }
    }
}

@Composable
fun AddPaymentMethodScreen(navController: NavController, profileViewModel: ProfileViewModel = viewModel()) {
    var cardNumber by remember { mutableStateOf("") }
    var cardName by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        MainTopAppBar("Thêm thẻ thanh toán", navController)
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(value = cardNumber, onValueChange = { cardNumber = it }, label = { Text("Số thẻ") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = cardName, onValueChange = { cardName = it }, label = { Text("Tên in trên thẻ") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = expiry, onValueChange = { expiry = it }, label = { Text("Ngày hết hạn") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                val method = "$cardName - ****${cardNumber.takeLast(4)}"
                profileViewModel.createPayment(method) {
                    Toast.makeText(context, "Đã lưu thẻ!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Lưu thẻ") }
        }
    }
}
