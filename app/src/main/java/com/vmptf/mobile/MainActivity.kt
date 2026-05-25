package com.vmptf.mobile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
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
import com.google.gson.Gson
import com.vmptf.mobile.features.auth.view.LoginActivity
import com.vmptf.mobile.features.auth.view.RegisterActivity
import com.vmptf.mobile.features.posts.domain.model.Category
import com.vmptf.mobile.features.posts.domain.view.PostsViewModel
import com.vmptf.mobile.features.posts.view.PostDetailActivity
import com.vmptf.mobile.features.posts.view.PostsAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: PostsViewModel by viewModels()
    private lateinit var adapter: PostsAdapter

    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var btnAdminPanel: Button
    private lateinit var btnLogout: Button
    private lateinit var tvLoggedInAs: TextView
    private lateinit var rvPosts: RecyclerView
    private lateinit var pbPosts: ProgressBar
    private lateinit var etSearch: EditText
    private lateinit var tvResultsCount: TextView
    private lateinit var layoutError: LinearLayout
    private lateinit var tvError: TextView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var hsvCategories: HorizontalScrollView
    private lateinit var llCategoryChips: LinearLayout

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
        btnAdminPanel = findViewById(R.id.btnAdminPanel)
        btnLogout = findViewById(R.id.btnLogout)
        tvLoggedInAs = findViewById(R.id.tvLoggedInAs)
        rvPosts = findViewById(R.id.rvPosts)
        pbPosts = findViewById(R.id.pbPosts)
        etSearch = findViewById(R.id.etSearch)
        tvResultsCount = findViewById(R.id.tvResultsCount)
        layoutError = findViewById(R.id.layoutError)
        tvError = findViewById(R.id.tvError)
        layoutEmpty = findViewById(R.id.layoutEmpty)
        hsvCategories = findViewById(R.id.hsvCategories)
        llCategoryChips = findViewById(R.id.llCategoryChips)

        // Update auth UI based on stored session
        updateAuthUI()

        // Setup RecyclerView
        adapter = PostsAdapter { post ->
            val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
            val userId = prefs.getInt("user_id", -1)
            val userName = prefs.getString("user_name", null)
            val intent = Intent(this, PostDetailActivity::class.java)
            intent.putExtra(PostDetailActivity.EXTRA_POST_JSON, Gson().toJson(post))
            intent.putExtra(PostDetailActivity.EXTRA_USER_ID, if (userId != -1) userId else -1)
            intent.putExtra(PostDetailActivity.EXTRA_USER_NAME, userName)
            startActivity(intent)
        }
        rvPosts.layoutManager = LinearLayoutManager(this)
        rvPosts.adapter = adapter

        // Observe posts list
        lifecycleScope.launch {
            viewModel.posts.collect { posts ->
                adapter.submitList(posts.toList())
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
                    layoutError.visibility = View.GONE
                    layoutEmpty.visibility = View.GONE
//                    rvPosts.visibility = View.GONE
//                    tvResultsCount.visibility = View.GONE
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

        // Observe categories — render chips
        lifecycleScope.launch {
            viewModel.categories.collect { categories ->
                if (categories.isNotEmpty()) {
                    renderCategoryChips(categories)
                    hsvCategories.visibility = View.VISIBLE
                }
            }
        }

        // Observe selected category ids — update chip styles
        lifecycleScope.launch {
            viewModel.selectedCategoryIds.collect { selectedIds ->
                updateChipStyles(selectedIds)
                triggerSearch()
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

        btnAdminPanel.setOnClickListener {
            val intent = Intent(this, com.vmptf.mobile.features.admin.AdminActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            // Clear auth prefs
            getSharedPreferences("auth_prefs", MODE_PRIVATE).edit().clear().apply()
            updateAuthUI()
        }

        // Search with 400ms debounce — sends request to GET /api/blogs/qs?search=...&categoryIds=...
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                searchDebounceJob?.cancel()
                searchDebounceJob = lifecycleScope.launch {
                    delay(400)
                    triggerSearch()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // Initial load
        triggerSearch()
        // Refresh auth UI when returning from LoginActivity
        updateAuthUI()
    }

    private fun updateAuthUI() {
        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)
        val userName = prefs.getString("user_name", null)
        val userRole = prefs.getString("user_role", null)
        val isLoggedIn = userId != -1

        if (isLoggedIn) {
            btnLogin.visibility = View.GONE
            btnRegister.visibility = View.GONE
            btnLogout.visibility = View.VISIBLE
            tvLoggedInAs.text = "${userName ?: "Користувач"}"
            tvLoggedInAs.visibility = View.VISIBLE
            if (userRole == "ADMIN") {
                btnAdminPanel.visibility = View.VISIBLE
            } else {
                btnAdminPanel.visibility = View.GONE
            }
        } else {
            btnLogin.visibility = View.VISIBLE
            btnRegister.visibility = View.VISIBLE
            btnLogout.visibility = View.GONE
            tvLoggedInAs.visibility = View.GONE
            btnAdminPanel.visibility = View.GONE
        }
    }

    private fun triggerSearch() {
        val query = etSearch.text?.toString()?.trim()
        val categoryIds = viewModel.selectedCategoryIds.value.toList()
        viewModel.searchPosts(
            query = query,
            categoryIds = categoryIds
        )
    }

    private fun renderCategoryChips(categories: List<Category>) {
        llCategoryChips.removeAllViews()
        categories.forEach { category ->
            val chip = buildCategoryChip(this, category)
            chip.setOnClickListener {
                viewModel.toggleCategory(category.id)
                Log.d("Category", "${category.id}")
            }
            llCategoryChips.addView(chip)
        }
    }

    private fun updateChipStyles(selectedIds: Set<Int>) {
        for (i in 0 until llCategoryChips.childCount) { // i = 0; i < llCategoryChips.childCount
            val chip = llCategoryChips.getChildAt(i) as? TextView ?: continue
            val categoryId = chip.tag as? Int ?: continue
            val isSelected = selectedIds.contains(categoryId)
            if (isSelected) {
                chip.setBackgroundColor(0xFF6366F1.toInt())
                chip.setTextColor(0xFFFFFFFF.toInt())
            } else {
                chip.setBackgroundResource(R.drawable.chip_category)
                chip.setTextColor(0xFF818CF8.toInt())
            }
        }
    }

    private fun buildCategoryChip(context: Context, category: Category): TextView {
        return TextView(context).apply {
            tag = category.id
            text = category.name
            textSize = 12f
            setTextColor(0xFF818CF8.toInt())
            background = context.getDrawable(R.drawable.chip_category)
            val px10 = (10 * context.resources.displayMetrics.density).toInt()
            val px5 = (5 * context.resources.displayMetrics.density).toInt()
            val px8 = (8 * context.resources.displayMetrics.density).toInt()
            setPadding(px10, px5, px10, px5)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, px8, 0)
            layoutParams = params
        }
    }

    private fun pluralPosts(count: Int): String = when {
        count % 100 in 11..19 -> "постів"
        count % 10 == 1 -> "пост"
        count % 10 in 2..4 -> "пости"
        else -> "постів"
    }
}