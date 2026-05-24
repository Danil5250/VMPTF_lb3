package com.vmptf.mobile

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vmptf.mobile.features.auth.view.LoginActivity
import com.vmptf.mobile.features.auth.view.RegisterActivity
import com.vmptf.mobile.features.posts.domain.view.PostsViewModel
import com.vmptf.mobile.features.posts.view.PostsAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: PostsViewModel by viewModels()
    private lateinit var adapter: PostsAdapter

    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var rvPosts: RecyclerView
    private lateinit var pbPosts: ProgressBar
    private lateinit var etSearch: EditText
    private lateinit var tvResultsCount: TextView
    private lateinit var layoutError: LinearLayout
    private lateinit var tvError: TextView
    private lateinit var layoutEmpty: LinearLayout

    private var searchDebounceJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
        rvPosts = findViewById(R.id.rvPosts)
        pbPosts = findViewById(R.id.pbPosts)
        etSearch = findViewById(R.id.etSearch)
        tvResultsCount = findViewById(R.id.tvResultsCount)
        layoutError = findViewById(R.id.layoutError)
        tvError = findViewById(R.id.tvError)
        layoutEmpty = findViewById(R.id.layoutEmpty)

        // Setup RecyclerView
        adapter = PostsAdapter()
        rvPosts.layoutManager = LinearLayoutManager(this)
        rvPosts.adapter = adapter

        // Observe posts list
        lifecycleScope.launch {
            viewModel.posts.collect { posts ->
                adapter.submitList(posts)
                if (posts.isNotEmpty()) {
                    tvResultsCount.text = "Знайдено: ${posts.size} ${pluralPosts(posts.size)}"
                    tvResultsCount.visibility = View.VISIBLE
                    rvPosts.visibility = View.VISIBLE
                    layoutEmpty.visibility = View.GONE
                } else if (!viewModel.isLoading.value && viewModel.error.value == null) {
                    tvResultsCount.visibility = View.GONE
                    rvPosts.visibility = View.GONE
                    layoutEmpty.visibility = View.VISIBLE
                }
            }
        }

        // Observe loading state
        lifecycleScope.launch {
            viewModel.isLoading.collect { loading ->
                pbPosts.visibility = if (loading) View.VISIBLE else View.GONE
                if (loading) {
                    rvPosts.visibility = View.GONE
                    layoutError.visibility = View.GONE
                    layoutEmpty.visibility = View.GONE
                    tvResultsCount.visibility = View.GONE
                }
            }
        }

        // Observe error state
        lifecycleScope.launch {
            viewModel.error.collect { errorMsg ->
                if (errorMsg != null) {
                    tvError.text = errorMsg
                    layoutError.visibility = View.VISIBLE
                    rvPosts.visibility = View.GONE
                    tvResultsCount.visibility = View.GONE
                } else {
                    layoutError.visibility = View.GONE
                }
            }
        }

        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Search with 400ms debounce — sends request to GET /api/blogs/qs?search=...
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                searchDebounceJob?.cancel()
                searchDebounceJob = lifecycleScope.launch {
                    delay(400)
                    viewModel.searchPosts(s?.toString()?.trim())
                }
            }
        })

        // Initial load — all posts via /api/blogs/qs
        viewModel.searchPosts()
    }

    private fun pluralPosts(count: Int): String = when {
        count % 100 in 11..19 -> "постів"
        count % 10 == 1 -> "пост"
        count % 10 in 2..4 -> "пости"
        else -> "постів"
    }
}