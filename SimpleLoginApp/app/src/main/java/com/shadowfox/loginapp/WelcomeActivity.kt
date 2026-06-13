package com.shadowfox.loginapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.shadowfox.loginapp.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var firebaseHelper: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseHelper = FirebaseHelper(this)

        val email = intent.getStringExtra("EXTRA_EMAIL") ?: "Guest"
        val attempts = intent.getIntExtra("EXTRA_ATTEMPTS", 0)

        // Show welcome content
        binding.tvWelcomeTitle.text = getString(R.string.welcome_message, email)
        binding.tvWelcomeDesc.text = "You have successfully authenticated.\n(Previous failed login attempts: $attempts)"

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnLogout.setOnClickListener {
            // Sign out of Firebase (if using Firebase)
            firebaseHelper.logout()

            // Navigate back to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
