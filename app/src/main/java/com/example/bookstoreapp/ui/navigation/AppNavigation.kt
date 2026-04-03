package com.example.bookstoreapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.bookstoreapp.ui.screens.auth.*
import com.example.bookstoreapp.ui.screens.home.*
import com.example.bookstoreapp.ui.screens.profile.*
import com.example.bookstoreapp.ui.screens.product.*
import com.example.bookstoreapp.ui.screens.search.*
import com.example.bookstoreapp.ui.screens.cart.*
import com.example.bookstoreapp.ui.screens.orders.*
import com.example.bookstoreapp.ui.screens.chatbot.*
import com.example.bookstoreapp.ui.viewmodels.*

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val cartViewModel: CartViewModel = viewModel()
    val bookViewModel: BookViewModel = viewModel()
    val orderViewModel: OrderViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        // Auth
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterScreen(navController) }
        composable(Screen.ForgotPassword.route) { ForgotPasswordScreen(navController) }
        composable(Screen.Otp.route) { OtpScreen(navController) }

        // Bottom Nav
        composable(Screen.Home.route) { HomeScreen(navController, bookViewModel) }
        composable(Screen.Profile.route) { ProfileScreen(navController) }
        composable(Screen.Chatbot.route) { ChatbotScreen(navController) }
        composable(Screen.Cart.route) { CartScreen(navController, cartViewModel) }

        // Profile Sub
        composable(Screen.EditProfile.route) { EditProfileScreen(navController, profileViewModel) }
        composable(Screen.AddressMap.route) { AddressScreen(navController, profileViewModel) }
        composable(Screen.AddAddress.route) { AddAddressScreen(navController, profileViewModel) }
        composable(Screen.PaymentMethod.route) { PaymentMethodScreen(navController, profileViewModel) }
        composable(Screen.AddPaymentMethod.route) { AddPaymentMethodScreen(navController, profileViewModel) }
        composable(Screen.ChangePassword.route) { ChangePasswordScreen(navController) }

        // Product List with optional categoryId
        composable(Screen.ProductList.route) { ProductListScreen(navController, bookViewModel = bookViewModel) }
        composable(
            "product_list?categoryId={categoryId}&title={title}",
            arguments = listOf(
                navArgument("categoryId") { type = NavType.IntType; defaultValue = -1 },
                navArgument("title") { type = NavType.StringType; defaultValue = "Tất cả sản phẩm" }
            )
        ) { backStackEntry ->
            val catId = backStackEntry.arguments?.getInt("categoryId")?.let { if (it == -1) null else it }
            val title = backStackEntry.arguments?.getString("title") ?: "Tất cả sản phẩm"
            ProductListScreen(navController, categoryId = catId, title = title, bookViewModel = bookViewModel)
        }

        // Product Detail with bookId
        composable(
            "product_detail/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.IntType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getInt("bookId") ?: 1
            ProductDetailScreen(navController, bookId, cartViewModel, bookViewModel)
        }

        composable(Screen.CategoryList.route) { CategoryListScreen(navController, bookViewModel) }
        composable(Screen.Search.route) { SearchScreen(navController, bookViewModel) }

        // Review List with bookId
        composable(
            "review_list/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.IntType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getInt("bookId") ?: 1
            ReviewListScreen(navController, bookId, bookViewModel)
        }

        // Cart & Checkout & Order
        composable(Screen.Checkout.route) { CheckoutScreen(navController, orderViewModel) }
        composable(Screen.OrderHistory.route) { OrderHistoryScreen(navController, orderViewModel) }

        // Order Detail with orderId
        composable(
            "order_detail/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.IntType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getInt("orderId") ?: 0
            OrderDetailScreen(navController, orderId, orderViewModel)
        }

        composable(Screen.VoucherSelection.route) { VoucherSelectionScreen(navController, orderViewModel) }
        composable(Screen.ShipmentSelection.route) { ShipmentSelectionScreen(navController, orderViewModel) }
    }
}
