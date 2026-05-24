package com.vmptf.mobile.features.auth.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.vmptf.mobile.R
import com.vmptf.mobile.features.auth.domain.view.AuthState
import com.vmptf.mobile.features.auth.domain.view.AuthViewModel
import com.vmptf.mobile.MainActivity

class LoginActivity : AppCompatActivity() {

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

        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

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
                    // Navigate to main screen
                    startActivity(Intent(this, MainActivity::class.java))
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
}
