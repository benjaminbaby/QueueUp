package com.example.queueup

import android.Manifest
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.queueup.databinding.ActivityUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class ActivityUser : AppCompatActivity() {
    private lateinit var binding: ActivityUserBinding
    private lateinit var userRef: DatabaseReference
    private lateinit var machineRef: DatabaseReference
    private lateinit var currentUser: FirebaseUser
    private var timeSelected : Int = 0
    private var timeCountDown: CountDownTimer? = null
    private var timeProgress = 0
    private var pauseOffSet: Long = 0
    private var isStart = true
    var usersBeforeCurrentUser = 0
    private var alreadyAdd = false
    var statusMachine = "unavailable"
    var availableMachine = ""
    var availableMachinekey = ""
    var currentUserKey = ""
    var currentMachineKey =""

    // notification
    private val CHANNEL_ID = "channel_id_01"
    private val notificationId = 101

    //handler
    val handler = Handler(Looper.getMainLooper())



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)



        userRef = FirebaseDatabase.getInstance().getReference("users")
        machineRef = FirebaseDatabase.getInstance().getReference("machines")
        currentUser = FirebaseAuth.getInstance().currentUser!!
        var currentUserName = currentUser.email








        // show how many people before the user
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {




                var currentUserJoinTime: Long? = null

                for (userSnapshot in dataSnapshot.children) {
                    val userName = userSnapshot.child("userName").getValue(String::class.java)
                    val userJoinTimeString = userSnapshot.child("joinTime").getValue(String::class.java)
                    val userKey = userSnapshot.child("userId").getValue(String::class.java)
                    if (userName != null && userJoinTimeString != null && userKey != null && userName == currentUserName) {
                        // Found the current user, store their join time and break out of the loop
                        currentUserJoinTime = convertTimeStringToMillis(userJoinTimeString)
                        currentUserKey = userKey
                        break
                    }
                }

                if (currentUserJoinTime != null) {
                    for (otherUserSnapshot in dataSnapshot.children) {
                        val otherUserJoinTimeString = otherUserSnapshot.child("joinTime").getValue(String::class.java)

                        if (otherUserJoinTimeString != null) {
                            val otherUserJoinTimeMillis = convertTimeStringToMillis(otherUserJoinTimeString)

                            // Compare join times
                            if (otherUserJoinTimeMillis < currentUserJoinTime) {
                                usersBeforeCurrentUser++
                            }
                        }
                    }
                }

                val text = getString(
                    R.string.people_in_the_line,
                    currentUserName?.substringBefore("@")?.capitalize() ?: null,
                    usersBeforeCurrentUser
                )
                binding.countPeople.text = text
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Error reading data: ${databaseError.message}")
            }
        })

        //show the available machine
        machineRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (machineSnapshot in dataSnapshot.children) {
                    val machineId = machineSnapshot.key
                    val machineName = machineSnapshot.child("name").getValue(String::class.java)
                    val status = machineSnapshot.child("status").getValue(String::class.java)

                    if (machineId != null && machineName != null && status != null) {

                        if (status == "available" && usersBeforeCurrentUser < 2) {

                            createNotificationChannel()
                            sendNotification()
                            statusMachine = status
                            availableMachine = machineName
                            availableMachinekey = machineId

                            Log.d("users before you", "usersBeforeCurrentUser: $usersBeforeCurrentUser")
                            binding.machineStatus.text = "Machine $machineName is $status now, you can set a time to start!"
                            break
                        }



                    }

                }

                if (statusMachine == "unavailable") {
                    binding.nomachineStatus.text = "Oops! No machine is available now..."


                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
                Log.e("MachineStatus", "Error: ${databaseError.message}")
            }
        })


        Log.d("available", "available: $statusMachine")

        handler.postDelayed({
            if (usersBeforeCurrentUser < 2) {
                deleteUserFromDatabase()
                val intent = Intent(applicationContext, ActivityTimeOut::class.java)
                startActivity(intent)
                finish()

            } }, 12000)






        binding.add.setOnClickListener{
            if (usersBeforeCurrentUser < 2) {
                handler.removeCallbacksAndMessages(null)
                checkMachineStatus()
            } else {
                Toast.makeText(applicationContext, "Please wait...", Toast.LENGTH_SHORT).show()
            }

        }


    }

    override fun onStop() {
        handler.removeCallbacksAndMessages(null)
        super.onStop()


    }


    // create notification

    private fun createNotificationChannel() {
        Log.d("createNotificationChannel", "createNotificationChannel:")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notification title"
            val descriptionText = "Notification Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel: NotificationChannel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText

            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d("createNotificationChannel1", "createNotificationChannel:")
        }
    }


    private fun sendNotification() {
        //handler.removeCallbacksAndMessages(null)
        val intent = Intent(this, ActivityUser::class.java)
        val contentIntent = PendingIntent.getActivity(this,0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle("Queue up")
            .setContentText("Please start your service! ")
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with (NotificationManagerCompat.from(applicationContext)) {
            Log.d("sendNotification1", "sendNotification1:")
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(notificationId, builder.build())
        }
    }
    private fun checkMachineStatus() {
        machineRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var machineStatus = "unavailable"
                for (machineSnapshot in dataSnapshot.children) {
                    val machineId = machineSnapshot.key
                    val machineName = machineSnapshot.child("name").getValue(String::class.java)
                    val status = machineSnapshot.child("status").getValue(String::class.java)
                    if (machineId != null && machineName != null && status != null) {
                        if (status == "available"  && usersBeforeCurrentUser < 2) {
                            machineStatus = status
                            // Machine is available, check user input
                            Log.d("available", "machineId: $machineId")
                            currentMachineKey = machineId
                            updateMachineStatus("unavailable", machineId)

                            Log.d("available", "machineId: $machineId")



                            processUserInput(machineId)

                            return
                        }
                    }


                }

            }
            override fun onCancelled(error: DatabaseError) {

                Log.e("MachineStatus", "Error: ${error.message}")
            }



        })
    }
    private fun processUserInput(machineId:String) {
        setTimeFunction()

        binding.start.setOnClickListener{

            startTimerSetup()


        }



    }
    private fun setTimeFunction() {
        Log.d("test3", "test3: ")
        val timeDialog = Dialog(this)
        timeDialog.setContentView(R.layout.add_dialog)
        val timeSet = timeDialog.findViewById<EditText>(R.id.etGetTime)



        val progressBar = binding.pbTimer.progress

        timeDialog.findViewById<Button>(R.id.btnOk).setOnClickListener {
            alreadyAdd = true
            if (timeSet.text.isEmpty()) {
                Toast.makeText(this, "Enter Time Duration", Toast.LENGTH_SHORT).show()
            } else {
                binding.tvTimeLeft.text = timeSet.text

                timeSelected = timeSet.text.toString().toInt()
                binding.pbTimer.max = timeSelected

            }
            timeDialog.dismiss()
        }

        timeDialog.show()
    }
    private fun startTimerSetup()
    {

        if (timeSelected>timeProgress)
        {
            if (isStart)
            {
                binding.start.text = "Pause"
                startTimer(pauseOffSet)
                isStart = false
            }
            else
            {
                isStart =true
                binding.start.text = "Resume"
                timePause()
            }
        }
        else
        {
            Toast.makeText(this,"Enter Time",Toast.LENGTH_SHORT).show()
        }
    }
    private fun timePause()
    {
        if (timeCountDown!=null)
        {
            timeCountDown!!.cancel()
        }
    }

    private fun startTimer(pauseOffSetL: Long)
    {

        binding.pbTimer.progress = timeProgress
        timeCountDown = object :CountDownTimer(
            (timeSelected*1000).toLong() - pauseOffSetL*1000, 1000)
        {
            override fun onTick(p0: Long) {
                timeProgress++
                pauseOffSet = timeSelected.toLong()- p0/1000
                binding.pbTimer.progress = timeSelected-timeProgress

                binding.tvTimeLeft.text = (timeSelected - timeProgress).toString()
            }

            override fun onFinish() {

                Toast.makeText(applicationContext,"Your service is done!", Toast.LENGTH_SHORT).show()

                deleteUserFromDatabase()

                updateMachineStatus("available", currentMachineKey)

                val intent = Intent(applicationContext, ActivityWelcome::class.java)
                startActivity(intent)
                finish()



            }

        }.start()
    }
    private fun deleteUserFromDatabase() {
        val userKey = FirebaseDatabase.getInstance().getReference("users").child(currentUserKey)

        userKey.removeValue()
            .addOnSuccessListener {
                // Deletion successful
                Toast.makeText(applicationContext, "User data deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Deletion failed
                Toast.makeText(applicationContext, "Failed to delete user data", Toast.LENGTH_SHORT).show()
            }
    }




    private fun updateMachineStatus(status: String, machineId: String) {
        val machineRef = FirebaseDatabase.getInstance().getReference("machines").child(machineId)

        machineRef.child("status").setValue(status).addOnSuccessListener {
            Log.d("available", "Machine ID: $machineId, Name: $status")
            Toast.makeText(this, "Machine status updated to $status", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            // Update failed
            Toast.makeText(this, "Failed to update machine status", Toast.LENGTH_SHORT).show()
        }

    }

    private fun String.capitalize(): String =
        replaceFirstChar {
            if (it.isLowerCase()) {
                it.titlecase(Locale.getDefault())
            } else {
                it.toString()
            }
        }



    private fun convertTimeStringToMillis(timeString: String?): Long {
        if (timeString == null) return 0

        // Assuming timeString is in the format "hh:mm:ss a"
        val dateFormat = SimpleDateFormat("hh:mm:ss a", Locale.US)
        val date = dateFormat.parse(timeString)

        // Return the time in milliseconds
        return date?.time ?: 0
    }






}