package com.vmptf.mobile.features.admin

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.vmptf.mobile.R
import com.vmptf.mobile.features.posts.domain.model.Category
import com.vmptf.mobile.features.posts.domain.model.Post
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminEditPostActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_POST_JSON = "extra_post_json"
    }

    private val viewModel: AdminViewModel by viewModels()

    private lateinit var etTitle: EditText
    private lateinit var etAuthor: EditText
    private lateinit var etContent: EditText
    private lateinit var tvSelectCategories: TextView
    private lateinit var btnSave: Button

    private var editingPost: Post? = null
    private var allCategories: List<Category> = emptyList()
    private var selectedCategoryIds = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_edit_post)

        val toolbar: Toolbar = findViewById(R.id.toolbarEditPost)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        etTitle = findViewById(R.id.etEditTitle)
        etAuthor = findViewById(R.id.etEditAuthor)
        etContent = findViewById(R.id.etEditContent)
        tvSelectCategories = findViewById(R.id.tvSelectCategories)
        btnSave = findViewById(R.id.btnSavePost)

        val postJson = intent.getStringExtra(EXTRA_POST_JSON)
        if (postJson != null) {
            editingPost = Gson().fromJson(postJson, Post::class.java)
            supportActionBar?.title = "Редагувати пост"
            populateFields(editingPost!!)
        } else {
            supportActionBar?.title = "Новий пост"
        }

        lifecycleScope.launch {
            viewModel.categories.collect { categories ->
                allCategories = categories
                updateCategoriesText()
            }
        }

        lifecycleScope.launch {
            viewModel.operationSuccess.collect { success ->
                if (success == true) {
                    Toast.makeText(this@AdminEditPostActivity, "Успішно збережено", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.error.collect { errorMsg ->
                if (errorMsg != null) {
                    Toast.makeText(this@AdminEditPostActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    viewModel.resetOperationState()
                }
            }
        }

        tvSelectCategories.setOnClickListener {
            showCategorySelectionDialog()
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val author = etAuthor.text.toString().trim()
            val content = etContent.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Заповніть обов'язкові поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Map request for backend expectations
            val postPayload = com.vmptf.mobile.features.posts.data.response.PostRequest(
                title = title,
                content = content,
                author = author,
                categoryIds = selectedCategoryIds.toList()
            )

            // For now, call ViewModel
            if (editingPost != null) {
                viewModel.updatePost(editingPost!!.id, postPayload)
            } else {
                viewModel.createPost(postPayload)
            }
        }
    }

    private fun populateFields(post: Post) {
        etTitle.setText(post.title)
        etAuthor.setText(post.author ?: "")
        etContent.setText(post.content ?: "")
        selectedCategoryIds.clear()
        selectedCategoryIds.addAll(post.categories?.map { it.id }?: emptyList())
    }

    private fun updateCategoriesText() {
        if (selectedCategoryIds.isEmpty()) {
            tvSelectCategories.text = "Вибрати категорії"
        } else {
            val selectedNames = allCategories.filter { it.id in selectedCategoryIds }.joinToString(", ") { it.name }
            tvSelectCategories.text = selectedNames
        }
    }

    private fun showCategorySelectionDialog() {
        if (allCategories.isEmpty()) return

        val categoryNames = allCategories.map { it.name }.toTypedArray()
        val checkedItems = allCategories.map { selectedCategoryIds.contains(it.id) }.toBooleanArray()

        AlertDialog.Builder(this)
            .setTitle("Виберіть категорії")
            .setMultiChoiceItems(categoryNames, checkedItems) { _, which, isChecked ->
                val id = allCategories[which].id
                if (isChecked) {
                    selectedCategoryIds.add(id)
                } else {
                    selectedCategoryIds.remove(id)
                }
            }
            .setPositiveButton("ОК") { _, _ ->
                updateCategoriesText()
            }
            .setNegativeButton("Скасувати", null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
