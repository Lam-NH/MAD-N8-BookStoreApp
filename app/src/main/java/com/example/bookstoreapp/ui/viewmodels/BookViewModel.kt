package com.example.bookstoreapp.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookstoreapp.data.api.RetrofitClient
import com.example.bookstoreapp.data.model.*
import kotlinx.coroutines.launch

class BookViewModel : ViewModel() {
    private val api = RetrofitClient.api

    var categories by mutableStateOf<List<Category>>(emptyList())
    var forYouBooks by mutableStateOf<List<Book>>(emptyList())
    var bookList by mutableStateOf<List<Book>>(emptyList())
    var bookDetail by mutableStateOf<BookDetailResponse?>(null)
    var allReviews by mutableStateOf<List<ReviewItem>>(emptyList())
    var searchResults by mutableStateOf<List<Book>>(emptyList())
    var currentPage by mutableStateOf(1)
    var totalPages by mutableStateOf(1)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun loadCategories() {
        viewModelScope.launch {
            try {
                val response = api.getCategories()
                if (response.isSuccessful) categories = response.body() ?: emptyList()
            } catch (_: Exception) {}
        }
    }

    fun loadForYou() {
        viewModelScope.launch {
            try {
                val response = api.getBooksForYou()
                if (response.isSuccessful) forYouBooks = response.body() ?: emptyList()
            } catch (_: Exception) {}
        }
    }

    fun loadBooksByCategory(categoryId: Int?, page: Int = 1) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = api.getBooks(page = page, categoryId = categoryId)
                if (response.isSuccessful && response.body() != null) {
                    bookList = response.body()!!.data
                    currentPage = response.body()!!.pagination?.page ?: page
                    totalPages = response.body()!!.pagination?.totalPages ?: 1
                }
            } catch (_: Exception) {}
            isLoading = false
        }
    }

    fun loadBookDetail(bookId: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = api.getBookDetail(bookId)
                if (response.isSuccessful) bookDetail = response.body()
            } catch (_: Exception) {}
            isLoading = false
        }
    }

    fun loadAllReviews(bookId: Int) {
        viewModelScope.launch {
            try {
                val response = api.getReviews(bookId)
                if (response.isSuccessful) allReviews = response.body() ?: emptyList()
            } catch (_: Exception) {}
        }
    }

    fun postReview(bookId: Int, rating: Int, comment: String, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                api.postReview(PostReviewRequest(bookId, rating, comment))
                loadAllReviews(bookId) // Refresh
                onDone()
            } catch (_: Exception) {}
        }
    }

    fun search(query: String, page: Int = 1) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = api.searchBooks(query, page)
                if (response.isSuccessful && response.body() != null) {
                    searchResults = response.body()!!.data
                    totalPages = response.body()!!.pagination?.totalPages ?: 1
                }
            } catch (_: Exception) {}
            isLoading = false
        }
    }
}
