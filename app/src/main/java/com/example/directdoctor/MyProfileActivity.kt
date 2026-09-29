package com.example.directdoctor

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import java.net.URL

class MyProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var etPortfolio: EditText

    private lateinit var etWebsite: EditText

    private lateinit var etAbout: EditText

    private var userRoleCollection = ""
    private var imageUri: Uri? = null

    // Views
    private lateinit var etName: EditText
    private lateinit var etMobile: EditText
    private lateinit var etAge: EditText
    private lateinit var etGender: EditText
    private lateinit var ivProfilePic: ImageView
    private lateinit var btnSaveProfile: Button
    private lateinit var tvChangePic: TextView
    private lateinit var btnEditMode: TextView

    // Image Picker
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            ivProfilePic.setImageURI(it)
            imageUri = it
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_doc_profile)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val userId = auth.currentUser?.uid ?: return

        etName = findViewById(R.id.etEditName)
        etMobile = findViewById(R.id.etEditMobile)
        etAge = findViewById(R.id.etEditAge)
        etGender = findViewById(R.id.etEditGender)
        ivProfilePic = findViewById(R.id.ivProfilePic)
        btnSaveProfile = findViewById(R.id.btnSaveProfile)
        tvChangePic = findViewById(R.id.tvChangePic)
        btnEditMode = findViewById(R.id.btnEditMode)
        etPortfolio = findViewById(R.id.etEditPortfolio)
        etWebsite = findViewById(R.id.etEditWebsite)
        etAbout = findViewById(R.id.etEditAbout)

        loadUserData(userId)

        // Top Right Edit Button Toggle
        var isEditMode = false
        btnEditMode.setOnClickListener {
            isEditMode = !isEditMode
            val enabled = isEditMode

            etName.isEnabled = enabled
            etMobile.isEnabled = enabled
            etAge.isEnabled = enabled
            etGender.isEnabled = enabled
            etPortfolio.isEnabled = enabled
            etWebsite.isEnabled = enabled
            etAbout.isEnabled = enabled

            tvChangePic.visibility = if (enabled) View.VISIBLE else View.GONE
            btnSaveProfile.visibility = if (enabled) View.VISIBLE else View.GONE
            btnEditMode.text = if (enabled) "✖ Cancel" else "✎ Edit"
        }

        // Profile Pic Click
        ivProfilePic.setOnClickListener {
            if (tvChangePic.visibility == View.VISIBLE) {
                pickImage.launch("image/*")
            }
        }

        // Save Button Logic
        btnSaveProfile.setOnClickListener {
            btnSaveProfile.text = "Saving..."
            btnSaveProfile.isEnabled = false

            if (imageUri != null) {
                uploadImageAndSaveData(userId)
            } else {
                saveDataToFirestore(userId, null)
            }
        }
    }

    private fun loadUserData(userId: String) {
        db.collection("Doctors").document(userId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                userRoleCollection = "Doctors"
                populateFields(doc)
            } else {
                db.collection("Users").document(userId).get().addOnSuccessListener { userDoc ->
                    if (userDoc.exists()) {
                        userRoleCollection = "Users"
                        populateFields(userDoc)
                    }
                }
            }
        }
    }

    private fun populateFields(doc: com.google.firebase.firestore.DocumentSnapshot) {
        etName.setText(doc.getString("fullName"))
        etMobile.setText(doc.getString("mobileNumber"))
        etAge.setText(doc.getString("age") ?: "")
        etGender.setText(doc.getString("gender") ?: "")
        etPortfolio.setText(doc.getString("portfolioUrl") ?: "")
        etWebsite.setText(doc.getString("websiteUrl") ?: "")
        etAbout.setText(doc.getString("aboutUs") ?: "")

        val profileUrl = doc.getString("profileImageUrl")
        if (!profileUrl.isNullOrEmpty()) {
            Thread {
                try {
                    val url = URL(profileUrl)
                    val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                    runOnUiThread { ivProfilePic.setImageBitmap(bmp) }
                } catch (e: Exception) { e.printStackTrace() }
            }.start()
        }
    }

    private fun uploadImageAndSaveData(userId: String) {
        val storageRef = storage.reference.child("profile_pictures/$userId.jpg")
        storageRef.putFile(imageUri!!).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                saveDataToFirestore(userId, uri.toString())
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            btnSaveProfile.text = "Save Changes"
            btnSaveProfile.isEnabled = true
        }
    }

    private fun saveDataToFirestore(userId: String, imageUrl: String?) {
        if (userRoleCollection.isEmpty()) {
            Toast.makeText(this, "Error finding user role", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = hashMapOf<String, Any>(
            "fullName" to etName.text.toString().trim(),
            "mobileNumber" to etMobile.text.toString().trim(),
            "age" to etAge.text.toString().trim(),
            "gender" to etGender.text.toString().trim(),
            "portfolioUrl" to etPortfolio.text.toString().trim(),
            "websiteUrl" to etWebsite.text.toString().trim(),
            "aboutUs" to etAbout.text.toString().trim()
        )

        if (imageUrl != null) {
            updates["profileImageUrl"] = imageUrl
        }

        db.collection(userRoleCollection).document(userId)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Profile Saved Successfully!", Toast.LENGTH_SHORT).show()
                etName.setTextColor(android.graphics.Color.parseColor("#00621E"))
                etMobile.setTextColor(android.graphics.Color.parseColor("#00621E"))
                etAge.setTextColor(android.graphics.Color.parseColor("#00621E"))
                etGender.setTextColor(android.graphics.Color.parseColor("#00621E"))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show()
                btnSaveProfile.text = "Save Changes"
                btnSaveProfile.isEnabled = true
            }
    }
}