package com.shadowfox.loginapp

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class FirebaseHelper(private val context: Context) {

    private var auth: FirebaseAuth? = null
    private var isFirebaseAvailable = false

    // Local fallback user storage (in-memory)
    private val localUsers = mutableMapOf<String, String>()

    init {
        try {
            // Check if Firebase is initialized on the device
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                auth = FirebaseAuth.getInstance()
                isFirebaseAvailable = true
            }
        } catch (e: Exception) {
            // Firebase initialization failed due to missing google-services.json
            isFirebaseAvailable = false
        }
        
        // Seed default local user for testing
        localUsers["guest@example.com"] = "password123"
    }

    fun isUsingFirebase(): Boolean {
        return isFirebaseAvailable
    }

    fun login(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        if (isFirebaseAvailable && auth != null) {
            auth!!.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        callback(true, null)
                    } else {
                        callback(false, task.exception?.localizedMessage ?: "Unknown error")
                    }
                }
        } else {
            // Local fallback logic
            val storedPassword = localUsers[email]
            if (storedPassword != null && storedPassword == password) {
                callback(true, null)
            } else if (storedPassword != null) {
                callback(false, "Invalid password")
            } else {
                callback(false, "User does not exist. Please sign up first.")
            }
        }
    }

    fun signUp(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        if (isFirebaseAvailable && auth != null) {
            auth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        callback(true, null)
                    } else {
                        callback(false, task.exception?.localizedMessage ?: "Unknown error")
                    }
                }
        } else {
            // Local fallback logic
            if (localUsers.containsKey(email)) {
                callback(false, "User already exists")
            } else {
                localUsers[email] = password
                callback(true, null)
            }
        }
    }

    fun getCurrentUserEmail(): String? {
        return if (isFirebaseAvailable && auth != null) {
            auth!!.currentUser?.email
        } else {
            // Return default/last logged in user
            "guest@example.com"
        }
    }

    fun logout() {
        if (isFirebaseAvailable && auth != null) {
            auth!!.signOut()
        }
    }
}
