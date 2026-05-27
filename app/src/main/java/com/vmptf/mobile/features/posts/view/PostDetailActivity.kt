package com.vmptf.mobile.features.posts.view

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vmptf.mobile.R
import com.vmptf.mobile.core.data.network.api
import com.vmptf.mobile.features.posts.domain.model.Category
import com.vmptf.mobile.features.posts.domain.model.Comment
import com.vmptf.mobile.features.posts.domain.model.CreateCommentRequest
import com.vmptf.mobile.features.posts.domain.model.Post
import com.google.gson.Gson
import kotlinx.coroutines.launch

class PostDetailActivity : AppCompatActivity() {

    //static fields of class PostDetailActivity
    companion object {
        const val EXTRA_POST_JSON = "extra_post_json"
        const val EXTRA_USER_ID = "extra_user_id"
        const val EXTRA_USER_NAME = "extra_user_name"
    }

    private lateinit var commentsAdapter: CommentsAdapter
    private var postId: Int = -1
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        // Edge-to-edge = content красиво затікав під системні елементи, але важливі залишались
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.postDetailRoot)) { v, insets ->
            //get gaps of system elements
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            //set appropriate paddings
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            //return configured object
            insets
        }

        // Toolbar with back navigation
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar) //toolbar is set to main ActionBar (upper line) of activity
        //show in left corner '<-' go back
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        // Parse post from intent
        val postJson = intent.getStringExtra(EXTRA_POST_JSON)
        val post = Gson().fromJson(postJson, Post::class.java)
        postId = post.id
        userId = intent.getIntExtra(EXTRA_USER_ID, -1)
        val userName = intent.getStringExtra(EXTRA_USER_NAME)
        val isLoggedIn = userId != -1

        // Bind post data
        val tvTitle: TextView = findViewById(R.id.tvDetailTitle)
        val tvAuthor: TextView = findViewById(R.id.tvDetailAuthor)
        val tvDate: TextView = findViewById(R.id.tvDetailDate)
        val tvContent: TextView = findViewById(R.id.tvDetailContent)
        val llCategories: LinearLayout = findViewById(R.id.llDetailCategories)

        tvTitle.text = post.title
        tvAuthor.text = "${post.author ?: "Невідомий"}"
        tvDate.text = if (post.createdAt.length >= 10) post.createdAt.substring(0, 10) else post.createdAt
        tvContent.text = post.content ?: "Без змісту"

        // Render category chips
        if (post.categories.isNotEmpty()) {
            llCategories.visibility = View.VISIBLE
            post.categories.forEach { category ->
                val chip = buildCategoryChip(this, category)
                llCategories.addView(chip)
            }
        }

        val layoutAddComment: LinearLayout = findViewById(R.id.layoutAddComment)
        val tvLoginToComment: TextView = findViewById(R.id.tvLoginToComment)
        val etCommentInput: EditText = findViewById(R.id.etCommentInput)
        val btnSubmitComment: Button = findViewById(R.id.btnSubmitComment)
        val pbComments: ProgressBar = findViewById(R.id.pbComments)
        val tvNoComments: TextView = findViewById(R.id.tvNoComments)
        val rvComments: RecyclerView = findViewById(R.id.rvComments)

        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val currentUserId = prefs.getInt("user_id", -1)
        val currentUserRole = prefs.getString("user_role", null)

        // Setup comments RecyclerView
        commentsAdapter = CommentsAdapter(
            currentUserId = currentUserId,
            currentUserRole = currentUserRole,
            onDeleteCommentClick = { comment ->
                AlertDialog.Builder(this)
                    .setTitle("Видалити коментар")
                    .setMessage("Ви впевнені, що хочете видалити цей коментар?")
                    .setPositiveButton("Видалити") { _, _ ->
                        deleteComment(comment, pbComments, tvNoComments, rvComments)
                    }
                    .setNegativeButton("Скасувати", null)
                    .show()
            }
        )
        rvComments.layoutManager = LinearLayoutManager(this)
        rvComments.adapter = commentsAdapter

        // Show/hide add comment form based on auth
        if (isLoggedIn) {
            layoutAddComment.visibility = View.VISIBLE
            tvLoginToComment.visibility = View.GONE
        } else {
            layoutAddComment.visibility = View.GONE
            tvLoginToComment.visibility = View.VISIBLE
        }

        // Submit comment handler
        btnSubmitComment.setOnClickListener {
            val content = etCommentInput.text?.toString()?.trim()
            if (content.isNullOrBlank()) {
                Toast.makeText(this, "Коментар не може бути порожнім", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            submitComment(content, etCommentInput, pbComments, tvNoComments, rvComments)
        }

        // Load comments
        loadComments(pbComments, tvNoComments, rvComments)
    }

    private fun loadComments(
        pbComments: ProgressBar,
        tvNoComments: TextView,
        rvComments: RecyclerView
    ) {
        lifecycleScope.launch {
            pbComments.visibility = View.VISIBLE
            tvNoComments.visibility = View.GONE
            rvComments.visibility = View.GONE
            try {
                val comments = api.commentsService.getCommentsByPost(postId)
                commentsAdapter.submitList(comments)
                if (comments.isEmpty()) {
                    tvNoComments.visibility = View.VISIBLE
                    rvComments.visibility = View.GONE
                } else {
                    tvNoComments.visibility = View.GONE
                    rvComments.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                tvNoComments.text = "Помилка завантаження коментарів"
                tvNoComments.visibility = View.VISIBLE
                rvComments.visibility = View.GONE
            } finally {
                pbComments.visibility = View.GONE
            }
        }
    }

    private fun submitComment(
        content: String,
        etCommentInput: EditText,
        pbComments: ProgressBar,
        tvNoComments: TextView,
        rvComments: RecyclerView
    ) {
        lifecycleScope.launch { // create a new coroutine
            try {
                api.commentsService.createComment(
                    CreateCommentRequest(
                        content = content,
                        postId = postId,
                        userId = userId
                    )
                )
                etCommentInput.text?.clear()
                //this@PostDetailActivity this of not coroutine, but this of PostDetailActivity
                Toast.makeText(this@PostDetailActivity, "Коментар додано!", Toast.LENGTH_SHORT).show()
                // Reload comments
                loadComments(pbComments, tvNoComments, rvComments)
            } catch (e: Exception) {
                Toast.makeText(this@PostDetailActivity, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteComment(
        comment: Comment,
        pbComments: ProgressBar,
        tvNoComments: TextView,
        rvComments: RecyclerView
    ) {
        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val token = prefs.getString("token", null)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Помилка авторизації", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = api.commentsService.deleteComment(comment.id)
                if (response.isSuccessful) {
                    Toast.makeText(this@PostDetailActivity, "Коментар видалено!", Toast.LENGTH_SHORT).show()
                    // Reload comments
                    loadComments(pbComments, tvNoComments, rvComments)
                } else {
                    Toast.makeText(this@PostDetailActivity, "Не вдалося видалити коментар", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PostDetailActivity, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Back button in toolbar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun buildCategoryChip(context: Context, category: Category): TextView {
        return TextView(context).apply {
            text = category.name
            textSize = 11f
            setTextColor(0xFF818CF8.toInt())
            background = context.getDrawable(R.drawable.chip_category)
            val px8 = (8 * context.resources.displayMetrics.density).toInt()
            val px4 = (4 * context.resources.displayMetrics.density).toInt()
            val px6 = (6 * context.resources.displayMetrics.density).toInt()
            setPadding(px8, px4, px8, px4)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, px6, 0)
            layoutParams = params
        }
    }
}
