package com.vmptf.mobile.features.posts.domain.view

import android.util.Log
import androidx.lifecycle.ViewModel
import com.vmptf.mobile.core.data.network.api
import com.vmptf.mobile.features.posts.domain.model.Post
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch



class PostsViewModel : ViewModel() {

    // Функция для получения данных
    fun fetchPosts() {
        // viewModelScope автоматически запустит корутину в фоновом потоке
        // и отменит её, если ViewModel будет уничтожена
        viewModelScope.launch {
            try {
                // Вызываем наш сетевой запрос
                val posts: List<Post> = api.retrofitService.getPosts().result

                // УРА! Данные получены!
                // Теперь с ними можно работать: вывести в лог, сохранить в StateFlow/LiveData для UI
                Log.d("NetworkResult", "Успешно загружено постов: ${posts.size}")
                posts.forEach { post ->
                    Log.d("NetworkResult", "Пост: ${post.title} от ${post.author}")
                }

            } catch (e: Exception) {
                // Если нет интернета или сервер вернул ошибку, мы попадем сюда
                Log.e("NetworkResult", "Ошибка при загрузке: ${e.message}")
            }
        }
    }
}