package com.example.directdoctor

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var btnAsUser: Button
    private lateinit var infoWebView: WebView
    private var isDoctor = false

    private lateinit var rvSuggestedDoctors: RecyclerView
    private lateinit var doctorAdapter: DoctorAdapter
    private val doctorList = mutableListOf<Doctor>()
    private val fullDoctorList = mutableListOf<Doctor>()

    private lateinit var rvBlankList: RecyclerView
    private lateinit var hospitalAdapter: HospitalAdapter
    private val hospitalList = mutableListOf<Hospital>()
    private val fullHospitalList = mutableListOf<Hospital>()

    // 🚀 In-App Update Download ID variable
    private var downloadId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        btnAsUser = findViewById(R.id.btnAsUser)
        val mainLogo = findViewById<ImageView>(R.id.mainLogo)
        infoWebView = findViewById(R.id.infoWebView)
        val etSearchBox = findViewById<EditText>(R.id.etSearchBox)

        setupWebView()
        updateButtonState()

        // Call Update Checker Here
        checkForUpdates()

        mainLogo.setOnClickListener {
            infoWebView.isVisible = !infoWebView.isVisible
        }

        btnAsUser.setOnClickListener { view ->
            if (auth.currentUser != null) {
                if (isDoctor) {
                    btnAsUser.setBackgroundResource(R.drawable.control_docprofile)
                } else {
                    btnAsUser.setBackgroundResource(R.drawable.control_profile)
                }

                val popup = PopupMenu(this, view)
                popup.menu.add("My Profile")
                if (isDoctor) {
                    popup.menu.add("Doctor Dashboard")
                } else {
                    popup.menu.add("My Data")
                }
                popup.menu.add("Logout")

                popup.setOnDismissListener {
                    if (auth.currentUser != null) {
                        if (isDoctor) {
                            btnAsUser.setBackgroundResource(R.drawable.doctor_profile)
                        } else {
                            btnAsUser.setBackgroundResource(R.drawable.user_profile)
                        }
                    }
                }

                popup.setOnMenuItemClickListener { item ->
                    when (item.title) {
                        "My Profile" -> {
                            if (isDoctor) {
                                startActivity(Intent(this, MyProfileActivity::class.java))
                            } else {
                                startActivity(Intent(this, UserProfileActivity::class.java))
                            }
                            true
                        }
                        "Doctor Dashboard" -> {
                            startActivity(Intent(this, DoctorDashboardActivity::class.java))
                            true
                        }
                        "My Data" -> {
                            startActivity(Intent(this, UserDataActivity::class.java))
                            true
                        }
                        "Logout" -> {
                            auth.signOut()
                            isDoctor = false
                            updateButtonState()
                            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                            true
                        }
                        else -> false
                    }
                }
                popup.show()

            } else {
                val loginPopup = PopupMenu(this, view)
                loginPopup.menu.add("Login as User")
                loginPopup.menu.add("Login as Doctor")

                loginPopup.setOnMenuItemClickListener { item ->
                    when (item.title) {
                        "Login as User" -> {
                            startActivity(Intent(this@MainActivity, LoginSignupActivity::class.java))
                            true
                        }
                        "Login as Doctor" -> {
                            startActivity(Intent(this@MainActivity, DoctorLoginActivity::class.java))
                            true
                        }
                        else -> false
                    }
                }
                loginPopup.show()
            }
        }

        rvSuggestedDoctors = findViewById(R.id.rvSuggestedDoctors)
        rvSuggestedDoctors.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        doctorAdapter = DoctorAdapter(doctorList)
        rvSuggestedDoctors.adapter = doctorAdapter

        fetchSuggestedDoctors()
        setupCarouselEffect(rvSuggestedDoctors)

        rvBlankList = findViewById(R.id.rvBlankList)
        rvBlankList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        hospitalAdapter = HospitalAdapter(hospitalList)
        rvBlankList.adapter = hospitalAdapter

        fetchHospitals()
        setupCarouselEffect(rvBlankList)

        setupBottomNavigation()

        etSearchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim().lowercase()

                doctorList.clear()
                if (query.isEmpty()) {
                    doctorList.addAll(fullDoctorList)
                } else {
                    doctorList.addAll(fullDoctorList.filter {
                        it.fullName.lowercase().contains(query) || it.specialisation.lowercase().contains(query)
                    })
                }
                doctorAdapter.notifyDataSetChanged()

                hospitalList.clear()
                if (query.isEmpty()) {
                    hospitalList.addAll(fullHospitalList)
                } else {
                    hospitalList.addAll(fullHospitalList.filter {
                        it.name.lowercase().contains(query) || it.address.lowercase().contains(query)
                    })
                }
                hospitalAdapter.notifyDataSetChanged()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onResume() {
        super.onResume()
        updateButtonState()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(onDownloadComplete)
        } catch (e: Exception) {
            // Unregister fails if it wasn't registered, safe to ignore
        }
    }

    private fun updateButtonState() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("Doctors").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        isDoctor = true
                        btnAsUser.setBackgroundResource(R.drawable.doctor_profile)
                    } else {
                        isDoctor = false
                        btnAsUser.setBackgroundResource(R.drawable.user_profile)
                    }
                }
                .addOnFailureListener {
                    isDoctor = false
                    btnAsUser.setBackgroundResource(R.drawable.user_profile)
                }
        } else {
            isDoctor = false
            btnAsUser.setBackgroundResource(R.drawable.login)
        }
    }

    private fun setupWebView() {
        infoWebView.settings.builtInZoomControls = true
        infoWebView.settings.displayZoomControls = false
        infoWebView.settings.javaScriptEnabled = true

        val guideHtml = """
            <html>
            <head>
                <style>
                    body { font-family: sans-serif; padding: 20px; color: #003366; line-height: 1.6; background-color: #F8F9FA; }
                    h2, h3 { color: #00621E; }
                    b { color: #1C53FF; }
                    .btn { background-color: #00621E; color: white; padding: 15px 20px; text-decoration: none; border-radius: 10px; font-weight: bold; display: block; text-align: center; margin-top: 15px; }
                    .btn-update { background-color: #0A32B3; }
                </style>
            </head>
            <body>
                <h2>Direct Doctor: Your Digital Gateway to Trusted Healthcare</h2>
                <b>Save Time. Save Money. Get the Best Treatment Instantly.</b>
                <p>In today’s fast-paced world, finding the right healthcare at the right time is a challenge. Direct Doctor is designed to bridge the gap between patients and verified medical professionals.</p>
                <br>
                <a href="dd://register_hospital" class="btn">Register Your Hospital</a>
                <a href="dd://check_updates" id="btnUpdate" class="btn btn-update">Check for Updates 🔄</a>
            </body>
            </html>
        """.trimIndent()

        infoWebView.loadDataWithBaseURL(null, guideHtml, "text/html", "UTF-8", null)

        infoWebView.webViewClient = object : android.webkit.WebViewClient() {
            override fun shouldOverrideUrlLoading(view: android.webkit.WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                if (url == "dd://register_hospital") {
                    startActivity(Intent(this@MainActivity, HospitalRegisterActivity::class.java))
                    return true
                } else if (url == "dd://check_updates") {
                    manualCheckForUpdates()
                    return true
                }
                return super.shouldOverrideUrlLoading(view, request)
            }
        }
    }

    private fun manualCheckForUpdates() {
        infoWebView.evaluateJavascript("document.getElementById('btnUpdate').innerText = 'Scanning... ⏳';", null)

        val remoteConfig = com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance()
        remoteConfig.fetch(0).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                remoteConfig.activate()
                val latestVersionStr = remoteConfig.getString("latest_app_version").trim()
                val updateUrl = remoteConfig.getString("update_url").trim()

                val currentVersionStr = try {
                    packageManager.getPackageInfo(packageName, 0)?.versionName?.trim() ?: "1.0"
                } catch (e: Exception) {
                    "1.0"
                }

                if (latestVersionStr == currentVersionStr) {
                    infoWebView.evaluateJavascript(
                        "document.getElementById('btnUpdate').innerText = 'Up-to-date ✅'; " +
                                "document.getElementById('btnUpdate').style.backgroundColor = '#4CAF50';", null
                    )
                    Toast.makeText(this, "Your app is up to date!", Toast.LENGTH_SHORT).show()
                } else {
                    val latestVersion = latestVersionStr.toDoubleOrNull() ?: 0.0
                    val currentVersion = currentVersionStr.toDoubleOrNull() ?: 0.0

                    if (latestVersion > currentVersion && updateUrl.isNotEmpty()) {
                        infoWebView.evaluateJavascript(
                            "document.getElementById('btnUpdate').innerText = 'Update Available! 🚀'; " +
                                    "document.getElementById('btnUpdate').style.backgroundColor = '#D32F2F';", null
                        )
                        showUpdateDialog(updateUrl)
                    } else {
                        infoWebView.evaluateJavascript(
                            "document.getElementById('btnUpdate').innerText = 'Up-to-date ✅'; " +
                                    "document.getElementById('btnUpdate').style.backgroundColor = '#4CAF50';", null
                        )
                    }
                }
            } else {
                infoWebView.evaluateJavascript("document.getElementById('btnUpdate').innerText = 'Check Failed ❌';", null)
            }
        }
    }

    private fun setupCarouselEffect(recyclerView: RecyclerView) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)
                val centerY = rv.height / 2f
                for (i in 0 until rv.childCount) {
                    val child = rv.getChildAt(i)
                    val childCenterY = (child.top + child.bottom) / 2f
                    val distance = abs(centerY - childCenterY)

                    val scaleFactor = 1f - (distance / centerY) * 0.2f
                    val finalScale = scaleFactor.coerceIn(0.8f, 1f)

                    child.scaleX = finalScale
                    child.scaleY = finalScale
                }
            }
        })
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun setupBottomNavigation() {
        val navDoctor = findViewById<ImageView>(R.id.navDoctor)
        val navHospital = findViewById<ImageView>(R.id.navHospital)
        val navHome = findViewById<ImageView>(R.id.navHome)
        val navPromotion = findViewById<ImageView>(R.id.navPromotion)
        val navContact = findViewById<ImageView>(R.id.navContact)

        val tvDoctorsHeader = findViewById<TextView>(R.id.tvDoctorsHeader)
        val rvSuggestedDoctors = findViewById<RecyclerView>(R.id.rvSuggestedDoctors)
        val dividerView = findViewById<View>(R.id.navDividerLine)
        val tvHospitalsHeader = findViewById<TextView>(R.id.tvHospitalsHeader)
        val rvBlankList = findViewById<RecyclerView>(R.id.rvBlankList)
        val etSearchBox = findViewById<EditText>(R.id.etSearchBox)

        val paramsDoc = rvSuggestedDoctors.layoutParams
        val paramsHosp = rvBlankList.layoutParams

        val icons = listOf(navDoctor, navHospital, navHome, navPromotion, navContact)

        fun highlightIcon(selected: ImageView) {
            for (icon in icons) {
                icon.setColorFilter(android.graphics.Color.parseColor("#A0A0A0"))
            }
            selected.setColorFilter(android.graphics.Color.parseColor("#FFFFFF"))
        }

        fun resetSearchState() {
            etSearchBox.text.clear()
            etSearchBox.clearFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(etSearchBox.windowToken, 0)
        }

        highlightIcon(navHome)

        navHome.setOnClickListener {
            resetSearchState()
            highlightIcon(navHome)
            tvDoctorsHeader.visibility = View.VISIBLE
            rvSuggestedDoctors.visibility = View.VISIBLE
            dividerView.visibility = View.VISIBLE
            tvHospitalsHeader.visibility = View.VISIBLE
            rvBlankList.visibility = View.VISIBLE

            paramsDoc.height = dpToPx(329)
            rvSuggestedDoctors.layoutParams = paramsDoc

            paramsHosp.height = dpToPx(400)
            rvBlankList.layoutParams = paramsHosp
        }

        navDoctor.setOnClickListener {
            resetSearchState()
            highlightIcon(navDoctor)
            tvDoctorsHeader.visibility = View.VISIBLE
            rvSuggestedDoctors.visibility = View.VISIBLE
            dividerView.visibility = View.GONE
            tvHospitalsHeader.visibility = View.GONE
            rvBlankList.visibility = View.GONE

            paramsDoc.height = ViewGroup.LayoutParams.WRAP_CONTENT
            rvSuggestedDoctors.layoutParams = paramsDoc
        }

        navHospital.setOnClickListener {
            resetSearchState()
            highlightIcon(navHospital)
            tvDoctorsHeader.visibility = View.GONE
            rvSuggestedDoctors.visibility = View.GONE
            dividerView.visibility = View.GONE
            tvHospitalsHeader.visibility = View.VISIBLE
            rvBlankList.visibility = View.VISIBLE

            paramsHosp.height = ViewGroup.LayoutParams.WRAP_CONTENT
            rvBlankList.layoutParams = paramsHosp
        }

        navPromotion.setOnClickListener {
            resetSearchState()
            highlightIcon(navPromotion)
            Toast.makeText(this, "Special Offers Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        navContact.setOnClickListener {
            resetSearchState()
            highlightIcon(navContact)
            val intent = Intent(Intent.ACTION_DIAL, android.net.Uri.parse("tel:6300893650"))
            startActivity(intent)
        }
    }

    private fun fetchSuggestedDoctors() {
        val db = FirebaseFirestore.getInstance()
        db.collection("Doctors").get()
            .addOnSuccessListener { result ->
                doctorList.clear()
                fullDoctorList.clear()
                for (document in result) {
                    val doctor = document.toObject(Doctor::class.java)
                    doctor.id = document.id
                    doctorList.add(doctor)
                    fullDoctorList.add(doctor)
                }
                doctorAdapter.notifyDataSetChanged()
            }
    }

    private fun fetchHospitals() {
        val db = FirebaseFirestore.getInstance()
        db.collection("Hospitals").get().addOnSuccessListener { result ->
            hospitalList.clear()
            fullHospitalList.clear()
            for (document in result) {
                val hosp = document.toObject(Hospital::class.java)
                hosp.id = document.id
                hospitalList.add(hosp)
                fullHospitalList.add(hosp)
            }
            hospitalAdapter.notifyDataSetChanged()
        }
    }

    private fun checkForUpdates() {
        val remoteConfig = com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance()
        val configSettings = com.google.firebase.remoteconfig.remoteConfigSettings {
            minimumFetchIntervalInSeconds = 0
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val latestVersionStr = remoteConfig.getString("latest_app_version").trim()
                val updateUrl = remoteConfig.getString("update_url").trim()

                val currentVersionStr = try {
                    packageManager.getPackageInfo(packageName, 0)?.versionName?.trim() ?: "1.0"
                } catch (e: Exception) {
                    "1.0"
                }

                val latest = latestVersionStr.toDoubleOrNull() ?: 0.0
                val current = currentVersionStr.toDoubleOrNull() ?: 0.0

                if (latest > current && updateUrl.isNotEmpty()) {
                    showUpdateDialog(updateUrl)
                }
            }
        }
    }

    // 🚀 Update Dialog In-App Update Kosam Marchindi
    private fun showUpdateDialog(url: String) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Update Available! 🚀")
            .setMessage("A new version is ready. We will download it in the background.")
            .setPositiveButton("Download & Install") { _, _ ->
                startApkDownload(url)
            }
            .setCancelable(false)
            .show()
    }

    // 🚀 Background lo Download start chese logic
    // 🚀 Background lo Download start chese logic (Android 14 Crash Fix)
    private fun startApkDownload(url: String) {
        val fileName = "DirectDoctorUpdate.apk"
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)

        if (file.exists()) file.delete() // Paatha update files delete cheyali

        val request = DownloadManager.Request(android.net.Uri.parse(url))
            .setTitle("Direct Doctor Update")
            .setDescription("Downloading latest features...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadId = downloadManager.enqueue(request)
        Toast.makeText(this, "Download started in background...", Toast.LENGTH_LONG).show()

        // 🚀 Super Safe Android 14 Fix using ContextCompat
        androidx.core.content.ContextCompat.registerReceiver(
            this,
            onDownloadComplete,
            android.content.IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            androidx.core.content.ContextCompat.RECEIVER_EXPORTED
        )
    }

    // 🚀 Download 100% ayyaka catch chese Receiver
    private val onDownloadComplete = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadId == id) {
                installApk()
            }
        }
    }

    // 🚀 Install Screen pampinche FileProvider Logic
    private fun installApk() {
        try {
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "DirectDoctorUpdate.apk")
            if (file.exists()) {
                val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(installIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Installation Failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}