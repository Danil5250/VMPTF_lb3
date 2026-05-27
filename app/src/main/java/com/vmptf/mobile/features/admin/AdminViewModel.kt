package com.vmptf.mobile.features.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vmptf.mobile.core.data.network.api
import com.vmptf.mobile.features.posts.domain.model.Category
import com.vmptf.mobile.features.posts.domain.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {
    /*
            Mutable = можно змінювати значення (value яке можна перезаписувати)
            StateFlow = зберігає текущий стан та повідомляє підписників
         */
    //incapsulation
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _operationSuccess = MutableStateFlow<Boolean?>(null) //для зміни model та value
    //екран може тільки підписуватись на поток даних щоб перемальовувати інтерфейс
    val operationSuccess: StateFlow<Boolean?> = _operationSuccess

    // when AdminViewModel is created data from server uploaded
    init {
        loadPosts()
        loadCategories()
    }

    fun loadPosts() {
        //requests will be automatically cancelled when user closes activity
        // chose viewModelScope but not lifecycleScope, because
        // viewModelScope for data, lifecycleScope for ui, viewModelScope has longer life
        // after phone rotation lifecycleScope dies, but viewModelScope works
        viewModelScope.launch { // start coroutine
            _isLoading.value = true
            try {
                val result = api.retrofitService.getPosts()
                _posts.value = result.result
            } catch (e: Exception) {
                _error.value = "Помилка завантаження постів: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                val response = api.categoriesService.getCategories()
                _categories.value = response.result
            } catch (e: Exception) {
                Log.e("AdminVM", "Error loading categories: ${e.message}")
            }
        }
    }

    fun createPost(post: com.vmptf.mobile.features.posts.data.response.PostRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _operationSuccess.value = null
            try {
                api.retrofitService.createPost(post)
                _operationSuccess.value = true
                loadPosts()
            } catch (e: Exception) {
                _error.value = "Помилка створення посту: ${e.message}"
                _operationSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePost(id: Int, post: com.vmptf.mobile.features.posts.data.response.PostRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _operationSuccess.value = null
            try {
                api.retrofitService.updatePost(id, post)
                _operationSuccess.value = true
                loadPosts()
            } catch (e: Exception) {
                _error.value = "Помилка оновлення посту: ${e.message}"
                _operationSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePost(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                api.retrofitService.deletePost(id)
                loadPosts()
            } catch (e: Exception) {
                _error.value = "Помилка видалення посту: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun resetOperationState() {
        _operationSuccess.value = null
        _error.value = null
    }
}
