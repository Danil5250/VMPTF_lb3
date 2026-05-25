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
import com.vmptf.mobile.R
import com.vmptf.mobile.features.auth.domain.view.AuthState
import com.vmptf.mobile.features.auth.domain.view.AuthViewModel

class RegisterActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()

    private lateinit var etName: android.widget.EditText
    private lateinit var etEmail: android.widget.EditText
    private lateinit var etPassword: android.widget.EditText
    private lateinit var btnRegister: Button
    private lateinit var tvError: TextView
    private lateinit var pbLoading: ProgressBar
    private lateinit var tvGoToLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etName = findViewById(R.id.etRegisterName)
        etEmail = findViewById(R.id.etRegisterEmail)
        etPassword = findViewById(R.id.etRegisterPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvError = findViewById(R.id.tvRegisterError)
        pbLoading = findViewById(R.id.pbRegister)
        tvGoToLogin = findViewById(R.id.tvGoToLogin)

        // Toolbar with back navigation
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            viewModel.register(email, password, name)
        }

        tvGoToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            //підход потрібен щоб стек відкритих activity не забивався і перехід назад працював
            /*
                якщо RegisterActivity до цього була викликана - очищуємо всі activity,
                що були в стеці перед викликом RegisterActivity
                якщо не була просто відкриваємо RegisterActivity
             */
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()    //закриваємо поточний register
        }

        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Idle -> {
                    pbLoading.visibility = View.GONE
                    btnRegister.isEnabled = true
                    tvError.visibility = View.GONE
                }
                is AuthState.Loading -> {
                    pbLoading.visibility = View.VISIBLE
                    btnRegister.isEnabled = false
                    tvError.visibility = View.GONE
                }
                is AuthState.RegisterSuccess -> {
                    pbLoading.visibility = View.GONE
                    btnRegister.isEnabled = true
                    Toast.makeText(this, "Реєстрація успішна! Тепер увійдіть.", Toast.LENGTH_LONG).show()
                    finish() // повернутись до LoginActivity
                }
                is AuthState.Error -> {
                    pbLoading.visibility = View.GONE
                    btnRegister.isEnabled = true
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
