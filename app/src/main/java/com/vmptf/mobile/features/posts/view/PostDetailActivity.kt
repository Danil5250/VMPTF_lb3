package com.vmptf.mobile.features.posts.view

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.vmptf.mobile.R
import com.vmptf.mobile.features.posts.domain.model.Category
import com.vmptf.mobile.features.posts.domain.model.Post
import com.google.gson.Gson

class PostDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_POST_JSON = "extra_post_json"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        // Edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.postDetailRoot)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Toolbar with back navigation
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        // Parse post from intent
        val postJson = intent.getStringExtra(EXTRA_POST_JSON)
        val post = Gson().fromJson(postJson, Post::class.java)

        // Bind data
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
