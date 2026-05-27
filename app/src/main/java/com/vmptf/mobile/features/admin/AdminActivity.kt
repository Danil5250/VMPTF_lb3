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
    //after each screen rotating activity recreates, data can be lost
    //it stores current application state
    private val viewModel: AdminViewModel by viewModels()
    //lateinit = will be initialized later
    private lateinit var adapter: AdminPostAdapter
    private lateinit var rvPosts: RecyclerView
    private lateinit var pbLoading: ProgressBar
    //FloatingActionButton = плаваюча floating button with '+' in right down corner
    private lateinit var fabAddPost: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val toolbar: Toolbar = findViewById(R.id.toolbarAdmin)
        setSupportActionBar(toolbar) //toolbar is set to main ActionBar (upper line) of activity
        //show in left corner '<-' go back
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Verify ADMIN role
        //getSharedPreferences = small data is stored in app xml (key -> value)
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

        //adapter for posts recycle viewer
        adapter = AdminPostAdapter(
            //what to do when editting
            onEditClick = { post ->
                //go to activity for editing
                val intent = Intent(this, AdminEditPostActivity::class.java)
                Log.d("Updating page coming", post.toString())
                // pass object post as json to new activity
                intent.putExtra(AdminEditPostActivity.EXTRA_POST_JSON, Gson().toJson(post))
                startActivity(intent)
            },
            //what to do when deleting
            onDeleteClick = { post ->
                viewModel.deletePost(post.id)
            }
        )

        rvPosts.layoutManager = LinearLayoutManager(this)
        rvPosts.adapter = adapter

        //lifecycleScope = CoroutineScope for starting coroutines (async methods to not block interface)
        // safely activity destroyed -> coroutine destroyed; no memory leaks; no manual threads stopping
        // lifecycleScope lives until Activity, Fragment (part of activity) lives
        lifecycleScope.launch { //launch = start coroutine
            //when new posts are occurred - we display them
            viewModel.posts.collect { posts ->
                adapter.submitList(posts)
            }
        }

        lifecycleScope.launch {
            //when isLoading changes - code in {} reworks = state changes
            viewModel.isLoading.collect { isLoading ->
                pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            //
            viewModel.error.collect { errorMsg ->
                if (errorMsg != null) {
                    Toast.makeText(this@AdminActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    viewModel.resetOperationState()
                }
            }
        }

        fabAddPost.setOnClickListener {
            // open new activity
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
