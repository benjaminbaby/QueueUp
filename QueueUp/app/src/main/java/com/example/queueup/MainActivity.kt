package com.example.queueup


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.queueup.databinding.MainActivityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DateFormat
import java.util.Calendar


class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainActivityBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userRef: DatabaseReference
    private lateinit var machinesRef: DatabaseReference


    companion object {
        var already = false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)

        setContentView(binding.root)


        firebaseAuth = requireNotNull(FirebaseAuth.getInstance())



        val user = firebaseAuth.currentUser

        if (user == null) {

            var intent = Intent(this, ActivityLogin::class.java)
            startActivity(intent)
            finish()

        } else {
            binding.userDetails.text = user.email
        }

        userRef = FirebaseDatabase.getInstance().getReference("users")
        machinesRef = FirebaseDatabase.getInstance().getReference("machines")




        // when use wants to join the queue, insert user information to database
        binding.joinTheQueue.setOnClickListener{

            if (!already) {

                checkIfMachineExists("A") {
                    if (!it) {
                        insertMachineData("A")
                    }
                }

                checkIfMachineExists("B") {
                    if (!it) {
                        insertMachineData("B")
                    }
                }




                already = true
            }

            // TODO check if the data already exits
            firebaseAuth.currentUser?.email?.let { it1 ->
                checkIfNameExists(it1) {
                    if (!it) {
                        insertUserData(firebaseAuth.currentUser)
                        val intent = Intent(this, ActivityUser::class.java)

                        startActivity(intent)


                    } else {
                        Toast.makeText(this, "You already joined", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, ActivityUser::class.java)

                        startActivity(intent)
                    }
                }
            }










        }


        // log out
        binding.logoutButton.setOnClickListener{

            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, ActivityLogin::class.java)
            startActivity(intent)

        }
    }

    private fun insertUserData(currentUser: FirebaseUser?) {
        val username = currentUser?.email
        val calendar = Calendar.getInstance().time
        val timeFormat = DateFormat.getTimeInstance().format(calendar)

        val userKey = userRef.push().key!!
        val user = Users(userKey, username, timeFormat)

        userRef.child(userKey).setValue(user).addOnCompleteListener{
            Toast.makeText(this, "successfully", Toast.LENGTH_LONG).show()
        }.addOnFailureListener{
            Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()

        }






    }



    private fun insertMachineData(name:String) {



        val machineId = machinesRef.push().key!!
        val machine  = Machines(machineId, name, "available", "N")

        machinesRef.child(machineId).setValue(machine).addOnCompleteListener{
            Toast.makeText(this, "successfully", Toast.LENGTH_LONG).show()
        }.addOnFailureListener{
            Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()

        }






    }


    private fun checkIfNameExists(name: String, callback: (Boolean) -> Unit) {
        // Reference to the "users" node in the database
        val usersReference = FirebaseDatabase.getInstance().getReference("users")

        // Check if data with the same name already exists
        usersReference.orderByChild("userName").equalTo(name)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    callback(dataSnapshot.exists())
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors
                    callback(false)
                }
            })
    }


    private fun checkIfMachineExists(name: String, callback: (Boolean) -> Unit) {
        // Reference to the "users" node in the database
        val usersReference = FirebaseDatabase.getInstance().getReference("machines")

        // Check if data with the same name already exists
        usersReference.orderByChild("name").equalTo(name)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    callback(dataSnapshot.exists())
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors
                    callback(false)
                }
            })
    }







}

