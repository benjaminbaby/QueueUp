package com.example.queueup

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.queueup.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseApp.initializeApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.provider.FirebaseInitProvider

class ActivityLogin : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    public override fun onStart() {
        super.onStart()

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)

            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        FirebaseInitProvider()
        initializeApp(this)
        setContentView(binding.root)

        firebaseAuth = requireNotNull(FirebaseAuth.getInstance())
        binding.registerNow.setOnClickListener {
            val intent = Intent(this, ActivityRegister::class.java)
            startActivity(intent)
            finish()
        }



        binding.loginButton.setOnClickListener {

            binding.progressBar.visibility = View.VISIBLE
            val email: String = binding.email.text.toString()
            val password: String = binding.password.text.toString()
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    binding.progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Login Successful.",
                            Toast.LENGTH_SHORT,
                        ).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)


                    } else {


                        Toast.makeText(
                            this,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()

                    }
                }


        }


    }
}
