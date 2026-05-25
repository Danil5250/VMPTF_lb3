package com.vmptf.mobile.features.posts.domain.view

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vmptf.mobile.core.data.network.api
import com.vmptf.mobile.features.posts.domain.model.Category
import com.vmptf.mobile.features.posts.domain.model.Post
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PostsViewModel : ViewModel() {

    // MutableStateFlow = ui recognize when data is changed = change the ui
    private val _posts = MutableStateFlow<List<Post>>(emptyList())

    //observable state-holder Kotlin Coroutines
    //notifies ui when is changed
    val posts: StateFlow<List<Post>> = _posts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Categories state
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    // Selected category IDs for filtering
    private val _selectedCategoryIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedCategoryIds: StateFlow<Set<Int>> = _selectedCategoryIds

    private var searchJob: Job? = null

    init {
        loadCategories()
    }

    // Load all categories from /api/categories
    fun loadCategories() {
        viewModelScope.launch {
            try {
                val response = api.categoriesService.getCategories()
                _categories.value = response.result
                Log.d("PostsVM", "Loaded ${response.result.size} categories")
            } catch (e: Exception) {
                Log.e("PostsVM", "Error loading categories: ${e.message}")
            }
        }
    }

    // Toggle category selection
    fun toggleCategory(categoryId: Int) {
        val current = _selectedCategoryIds.value.toMutableSet()
        if (current.contains(categoryId)) {
            current.remove(categoryId)
        } else {
            current.add(categoryId)
        }
        _selectedCategoryIds.value = current
    }

    // Викликається при запуску та кожному пошуку/скиданні
    fun searchPosts(query: String? = null, categoryIds: List<Int>? = null) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = api.retrofitService.getFilteredPosts(
                    search = if (query.isNullOrBlank()) null else query,
                    categoryIds = if (categoryIds.isNullOrEmpty()) null else categoryIds
                )
                _posts.value = result
                Log.d("PostsVM", "Loaded ${result.size} posts, query='$query', categoryIds=$categoryIds")
            } catch (e: Exception) {
                _error.value = "Помилка завантаження: ${e.message}"
                Log.e("PostsVM", "Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
