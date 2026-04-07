package com.example.bookstoreapp.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookstoreapp.data.api.RetrofitClient
import com.example.bookstoreapp.data.model.*
import android.util.Log
import kotlinx.coroutines.launch

class BookViewModel : ViewModel() {
    private val api = RetrofitClient.api

    var categories by mutableStateOf<List<Category>>(emptyList())
    var forYouBooks by mutableStateOf<List<Book>>(emptyList())
    var bookList by mutableStateOf<List<Book>>(emptyList())
    var bookDetail by mutableStateOf<BookDetailResponse?>(null)
    var allReviews by mutableStateOf<List<ReviewItem>>(emptyList())
    var searchResults by mutableStateOf<List<Book>>(emptyList())
    var searchAuthorMatches by mutableStateOf<List<AuthorInfo>>(emptyList())
    var selectedAuthor by mutableStateOf<AuthorInfo?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var recognizedText by mutableStateOf<String?>(null)
    var isSearchActive by mutableStateOf(false)

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

    fun loadBooksByCategory(categoryId: Int?) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = api.getBooks(categoryId = categoryId)
                if (response.isSuccessful && response.body() != null) {
                    bookList = response.body()!!
                }
            } catch (_: Exception) {}
            isLoading = false
        }
    }

    fun loadBooksByAuthor(authorId: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = api.getBooksByAuthor(authorId)
                if (response.isSuccessful && response.body() != null) {
                    bookList = response.body()!!
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

    fun search(query: String) {
        viewModelScope.launch {
            isLoading = true
            isSearchActive = true
            recognizedText = null // Reset AI text on normal search
            searchAuthorMatches = emptyList()
            searchResults = emptyList()
            try {
                val response = api.searchBooks(query)
                if (response.isSuccessful && response.body() != null) {
                    val element = response.body()!!
                    val gson = com.google.gson.Gson()
                    if (element.isJsonArray) {
                        searchResults = gson.fromJson(element, Array<Book>::class.java).toList()
                    } else if (element.isJsonObject) {
                        val obj = element.asJsonObject
                        if (obj.has("authorMatches")) {
                            searchAuthorMatches = gson.fromJson(obj.get("authorMatches"), Array<AuthorInfo>::class.java).toList()
                        }
                        if (obj.has("data")) {
                            searchResults = gson.fromJson(obj.get("data"), Array<Book>::class.java).toList()
                        }
                    }
                }
            } catch (_: Exception) {}
            isLoading = false
        }
    }

    fun searchByVoice(audioPart: okhttp3.MultipartBody.Part, onDone: () -> Unit = {}) {
        Log.d("BookViewModel", "searchByVoice called with audio part")
        viewModelScope.launch {
            isLoading = true
            isSearchActive = true
            recognizedText = null
            searchResults = emptyList()
            try {
                val response = api.voiceSearch(audioPart)
                Log.d("BookViewModel", "searchByVoice response: ${response.code()}")
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    recognizedText = body.recognizedText
                    searchResults = body.results
                    Log.d("BookViewModel", "searchByVoice found: ${body.results.size} books")
                }
            } catch (e: Exception) {
                Log.e("BookViewModel", "searchByVoice error", e)
            }
            isLoading = false
            onDone()
        }
    }

    fun clearSearch() {
        searchResults = emptyList()
        searchAuthorMatches = emptyList()
        recognizedText = null
        isSearchActive = false
    }

    fun searchByImage(imagePart: okhttp3.MultipartBody.Part, onDone: () -> Unit = {}) {
        Log.d("BookViewModel", "searchByImage called")
        viewModelScope.launch {
            isLoading = true
            isSearchActive = true
            recognizedText = null
            searchResults = emptyList()
            try {
                val response = api.imageSearch(imagePart)
                Log.d("BookViewModel", "searchByImage response: ${response.code()}")
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    recognizedText = body.recognizedText
                    searchResults = body.results
                    Log.d("BookViewModel", "searchByImage found: ${body.results.size} books")
                }
            } catch (e: Exception) {
                Log.e("BookViewModel", "searchByImage error", e)
            }
            isLoading = false
            onDone()
        }
    }
}
