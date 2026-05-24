package com.vmptf.mobile.features.posts.domain.view

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vmptf.mobile.core.data.network.api
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

    private var searchJob: Job? = null

    // Викликається при запуску та кожному пошуку/скиданні
    fun searchPosts(query: String? = null) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = api.retrofitService.getFilteredPosts(
                    search = if (query.isNullOrBlank()) null else query
                )
                _posts.value = result
                Log.d("PostsVM", "Loaded ${result.size} posts, query='$query'")
            } catch (e: Exception) {
                _error.value = "Помилка завантаження: ${e.message}"
                Log.e("PostsVM", "Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
