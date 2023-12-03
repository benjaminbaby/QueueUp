package com.example.queueup

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.queueup.databinding.ActivityTimeOutBinding
import com.google.firebase.auth.FirebaseAuth

class ActivityTimeOut : AppCompatActivity() {
    private lateinit var binding: ActivityTimeOutBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimeOutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = requireNotNull(FirebaseAuth.getInstance())

        binding.joinTheQueue.setOnClickListener{

            val intent = Intent(this, ActivityLogin::class.java)
            startActivity(intent)
        }


        binding.logoutButton.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, ActivityLogin::class.java)
            startActivity(intent)
        }
    }



}