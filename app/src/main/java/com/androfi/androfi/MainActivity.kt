package com.androfi.androfi

import android.Manifest
import android.app.WallpaperManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : ComponentActivity() {
    private lateinit var appAdapter: AppAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true) {
            setupUI()
        } else {
            handlePermissionDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        } else {
            setupUI()
        }
    }

    private fun setupUI() {
        val wallpaperManager = WallpaperManager.getInstance(this)
        val wallpaperDrawable = wallpaperManager.drawable
        val wallpaperBitmap = (wallpaperDrawable as BitmapDrawable).bitmap

        val wallpaperImageView: ImageView = findViewById(R.id.wallpaperImageView)
        wallpaperImageView.setImageBitmap(wallpaperBitmap)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val installedApps = getInstalledApps(this).sortedBy { it.name }
        appAdapter = AppAdapter(installedApps)
        recyclerView.adapter = appAdapter

        val searchView: SearchView = findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                appAdapter.filter.filter(newText)
                return true
            }
        })
    }

    private fun handlePermissionDenied() {
        // Handle the case where the permission is not granted
        // For example, show a message to the user or disable certain features
    }

    /**
     * Retrieves a list of installed apps, including non-system apps and specific essential pre-installed apps.
     *
     * @param context The context of the calling component.
     * @return A list of AppInfo objects representing the installed apps.
     */
    private fun getInstalledApps(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        // List of essential apps to include
        val essentialApps = listOf(
            "com.android.chrome", // Chrome
            "com.google.android.apps.maps", // Maps
            "com.google.android.calendar", // Calendar
            "com.google.android.deskclock", // Clock
            "com.google.android.contacts", // Contacts
            "com.google.android.apps.photos", // Photos
            "com.google.android.apps.docs", // Drive
            "com.google.android.gm", // Gmail
            "com.google.android.youtube", // YouTube
            "com.google.android.apps.messaging", // Messaging
            "com.google.android.apps.camera", // Camera
            "com.google.android.apps.tachyon", // Google Duo
            "com.google.android.apps.meetings", // Google Meet
            "com.google.android.apps.tasks", // Google Tasks
            "com.google.android.apps.keep", // Google Keep
            "com.google.android.apps.docs.editors.sheets", // Google Sheets
            "com.google.android.apps.docs.editors.slides", // Google Slides
            "com.google.android.apps.docs.editors.docs", // Google Docs
            "com.google.android.apps.photos.scanner", // Google Photos Scanner
            "com.google.android.apps.walletnfcrel", // Google Wallet
            "com.google.android.apps.youtube.music", // YouTube Music
            "com.google.android.apps.books", // Google Play Books
            "com.google.android.apps.magazines", // Google Play Newsstand
            "com.google.android.apps.podcasts", // Google Podcasts
            "com.google.android.apps.fitness", // Google Fit
            "com.google.android.apps.wellbeing", // Digital Wellbeing
            "com.google.android.apps.nbu.files", // Files by Google
            "com.google.android.apps.subscriptions.red", // Google One
            "com.google.android.music", // Play Music
            "com.google.android.dialer", // Phone
            "com.android.dialer" // Phone (AOSP Dialer)
        )

        return apps.filter {
            // Include non-system apps
            (it.flags and ApplicationInfo.FLAG_SYSTEM == 0) ||
                    // Include updated system apps
                    (it.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0) ||
                    // Include essential system apps
                    (it.flags and ApplicationInfo.FLAG_SYSTEM != 0 && it.packageName in essentialApps)
        }.map {
            AppInfo(
                name = it.loadLabel(pm).toString(),
                packageName = it.packageName,
                icon = it.loadIcon(pm)
            )
        }
    }
}