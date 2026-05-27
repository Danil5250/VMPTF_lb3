package com.vmptf.mobile.features.auth.view

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputEditText
import com.vmptf.mobile.R
import com.vmptf.mobile.features.auth.domain.view.AuthState
import com.vmptf.mobile.features.auth.domain.view.AuthViewModel
import com.vmptf.mobile.MainActivity

class LoginActivity : AppCompatActivity() {
    //after each screen rotating activity recreates, data can be lost
    //it stores current application state
    private val viewModel: AuthViewModel by viewModels()

    private lateinit var etEmail: android.widget.EditText
    private lateinit var etPassword: android.widget.EditText
    private lateinit var btnLogin: Button
    private lateinit var tvError: TextView
    private lateinit var pbLoading: ProgressBar
    private lateinit var tvGoToRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etLoginEmail)
        etPassword = findViewById(R.id.etLoginPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvError = findViewById(R.id.tvLoginError)
        pbLoading = findViewById(R.id.pbLogin)
        tvGoToRegister = findViewById(R.id.tvGoToRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            viewModel.login(email, password)
        }

        // Toolbar with back navigation
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar) //toolbar is set to main ActionBar (upper line) of activity
        //show in left corner '<-' go back
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        tvGoToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            //підход потрібен щоб стек відкритих activity не забивався і перехід назад працював
            /*
                якщо RegisterActivity до цього була викликана - очищуємо всі activity,
                що були в стеці перед викликом RegisterActivity
                якщо не була просто відкриваємо RegisterActivity
             */
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()    //закриваємо поточний login
        }

        // activity subscribes on updating from viewModel
        //when authState changes based on changes do something
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Idle -> {
                    pbLoading.visibility = View.GONE
                    btnLogin.isEnabled = true
                    tvError.visibility = View.GONE
                }
                is AuthState.Loading -> {
                    pbLoading.visibility = View.VISIBLE
                    btnLogin.isEnabled = false
                    tvError.visibility = View.GONE
                }
                is AuthState.LoginSuccess -> {
                    pbLoading.visibility = View.GONE
                    btnLogin.isEnabled = true
                    Toast.makeText(this, "Ласкаво просимо, ${state.user.name}!", Toast.LENGTH_SHORT).show()
                    // Persist logged-in user info
                    // for saving small data in memory of application in xml
                    // key -> value
                    val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
                    prefs.edit()
                        .putInt("user_id", state.user.id)
                        .putString("user_name", state.user.name)
                        .putString("user_email", state.user.email)
                        .putString("user_role", state.user.role)
                        .putString("token", state.token)
                        .apply()
                    // Navigate to main screen
                    finish()
                }
                is AuthState.Error -> {
                    pbLoading.visibility = View.GONE
                    btnLogin.isEnabled = true
                    tvError.text = state.message
                    tvError.visibility = View.VISIBLE
                }
                else -> Unit
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
}
