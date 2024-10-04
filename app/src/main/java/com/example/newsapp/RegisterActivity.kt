package com.example.newsapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var registerName: TextInputEditText
    private lateinit var registerEmail: TextInputEditText
    private lateinit var registerPassword: TextInputEditText
    private lateinit var registerConfirmPassword: TextInputEditText
    private lateinit var registerButton: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        registerName = findViewById(R.id.register_name)
        registerEmail = findViewById(R.id.register_email)
        registerPassword = findViewById(R.id.register_password)
        registerConfirmPassword = findViewById(R.id.register_confirm_password)
        registerButton = findViewById(R.id.register_button)
        progressBar = findViewById(R.id.progressBar)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().getReference("users")

        registerButton.setOnClickListener { registerUser() }
        findViewById<TextView>(R.id.login_existing_account).setOnClickListener { handleLogin() }
    }

    private fun registerUser() {
        val name = registerName.text.toString().trim()
        val email = registerEmail.text.toString().trim()
        val password = registerPassword.text.toString().trim()
        val confirmPassword = registerConfirmPassword.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showToast("All fields must be filled")
            return
        }

        if (password != confirmPassword) {
            showToast("Passwords do not match")
            return
        }

        progressBar.visibility = View.VISIBLE

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    val user = User(userId, name, email) // Create a User object

                    // Save user data to Firebase Database
                    database.child(userId!!).setValue(user).addOnCompleteListener { dbTask ->
                        if (dbTask.isSuccessful) {
                            showToast("Registration Successful")
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            showToast("Failed to save user data: ${dbTask.exception?.message}")
                        }
                    }
                } else {
                    showToast("Registration Failed: ${task.exception?.message}")
                }
            }
    }

    private fun handleLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

// User data class
data class User(val userId: String?, val name: String, val email: String)
