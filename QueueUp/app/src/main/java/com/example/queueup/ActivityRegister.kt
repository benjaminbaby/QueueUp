package com.example.queueup

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.queueup.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class ActivityRegister : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
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

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = requireNotNull(FirebaseAuth.getInstance())

        binding.loginNow.setOnClickListener{
            val intent = Intent(this, ActivityLogin::class.java)
            startActivity(intent)
            finish()
        }

        binding.registerButton.setOnClickListener{
            binding.progressBar.visibility = View.VISIBLE
            val email: String = binding.email.text.toString()
            val password: String = binding.password.text.toString()
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this,"Enter email", Toast.LENGTH_SHORT).show()
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this,"Enter password", Toast.LENGTH_SHORT).show()
            }

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    binding.progressBar.visibility = View.GONE
                    if (task.isSuccessful) {

                        Toast.makeText(
                            this,
                            "Account created.",
                            Toast.LENGTH_SHORT,
                        ).show()
                        val intent = Intent(this, ActivityLogin::class.java)
                        startActivity(intent)
                        finish()




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




