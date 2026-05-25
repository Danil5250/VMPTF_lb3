package com.vmptf.mobile.features.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.vmptf.mobile.R
import kotlinx.coroutines.launch

class AdminActivity : AppCompatActivity() {

    private val viewModel: AdminViewModel by viewModels()
    private lateinit var adapter: AdminPostAdapter
    private lateinit var rvPosts: RecyclerView
    private lateinit var pbLoading: ProgressBar
    private lateinit var fabAddPost: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val toolbar: Toolbar = findViewById(R.id.toolbarAdmin)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Verify ADMIN role
        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val role = prefs.getString("user_role", null)
        if (role != "ADMIN") {
            Toast.makeText(this, "Access Denied", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        rvPosts = findViewById(R.id.rvAdminPosts)
        pbLoading = findViewById(R.id.pbAdminPosts)
        fabAddPost = findViewById(R.id.fabAddPost)

        adapter = AdminPostAdapter(
            onEditClick = { post ->
                val intent = Intent(this, AdminEditPostActivity::class.java)
                Log.d("Updating page coming", post.toString())
                intent.putExtra(AdminEditPostActivity.EXTRA_POST_JSON, Gson().toJson(post))
                startActivity(intent)
            },
            onDeleteClick = { post ->
                viewModel.deletePost(post.id)
            }
        )

        rvPosts.layoutManager = LinearLayoutManager(this)
        rvPosts.adapter = adapter

        lifecycleScope.launch {
            viewModel.posts.collect { posts ->
                adapter.submitList(posts)
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.error.collect { errorMsg ->
                if (errorMsg != null) {
                    Toast.makeText(this@AdminActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    viewModel.resetOperationState()
                }
            }
        }

        fabAddPost.setOnClickListener {
            val intent = Intent(this, AdminEditPostActivity::class.java)
            startActivity(intent)
        }
    }

    //when we went from activity and back to it method is called
    override fun onResume() {
        super.onResume()
        viewModel.loadPosts()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
