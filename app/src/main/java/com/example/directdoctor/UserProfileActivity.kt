package com.example.directdoctor

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class UserProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var etName: EditText
    private lateinit var etMobile: EditText
    private lateinit var etAge: EditText
    private lateinit var etGender: EditText
    private lateinit var btnEditMode: TextView
    private lateinit var btnSaveProfile: Button

    private lateinit var ivProfilePic: ImageView
    private lateinit var tvChangePic: TextView

    private var isEditMode = false
    private var imageUri: Uri? = null

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            ivProfilePic.setImageURI(uri)
            uploadProfilePicture(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        etName = findViewById(R.id.etEditName)
        etMobile = findViewById(R.id.etEditMobile)
        etAge = findViewById(R.id.etEditAge)
        etGender = findViewById(R.id.etEditGender)
        btnEditMode = findViewById(R.id.btnEditMode)
        btnSaveProfile = findViewById(R.id.btnSaveProfile)

        ivProfilePic = findViewById(R.id.ivProfilePic)
        tvChangePic = findViewById(R.id.tvChangePic)

        loadUserData()

        btnEditMode.setOnClickListener {
            isEditMode = !isEditMode
            toggleEditMode(isEditMode)
        }

        btnSaveProfile.setOnClickListener {
            saveUserData()
        }

        tvChangePic.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }
        ivProfilePic.setOnClickListener {
            if (isEditMode) selectImageLauncher.launch("image/*")
        }
    }

    private fun toggleEditMode(enabled: Boolean) {
        etName.isEnabled = enabled
        etMobile.isEnabled = enabled
        etAge.isEnabled = enabled
        etGender.isEnabled = enabled

        tvChangePic.visibility = if (enabled) View.VISIBLE else View.GONE
        btnSaveProfile.visibility = if (enabled) View.VISIBLE else View.GONE
        btnEditMode.text = if (enabled) "Cancel" else "✎ Edit"
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("Users").document(userId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                etName.setText(doc.getString("fullName") ?: "")
                etMobile.setText(doc.getString("mobileNumber") ?: "")
                etAge.setText(doc.getString("age") ?: "")
                etGender.setText(doc.getString("gender") ?: "")

                val profileImageUrl = doc.getString("profileImageUrl")
                if (!profileImageUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(profileImageUrl)
                        .circleCrop()
                        .into(ivProfilePic)
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Data load avvaledu", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadProfilePicture(uri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        val storageRef = storage.reference.child("profile_pictures/$userId.jpg")

        Toast.makeText(this, "Uploading photo... Please wait", Toast.LENGTH_SHORT).show()

        storageRef.putFile(uri).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                db.collection("Users").document(userId)
                    .update("profileImageUrl", downloadUrl.toString())
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profile Picture Updated! ✅", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Upload Failed ❌", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserData() {
        val userId = auth.currentUser?.uid ?: return

        val updates = hashMapOf<String, Any>(
            "fullName" to etName.text.toString().trim(),
            "mobileNumber" to etMobile.text.toString().trim(),
            "age" to etAge.text.toString().trim(),
            "gender" to etGender.text.toString().trim()
        )

        db.collection("Users").document(userId).set(updates, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                etName.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                etMobile.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                etAge.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                etGender.setTextColor(android.graphics.Color.parseColor("#4CAF50"))

                Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show()
                isEditMode = false
                toggleEditMode(false)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show()
            }
    }
}