package com.androfi.androfi

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.Uri

class AppAdapter(private var appList: List<AppInfo>) : RecyclerView.Adapter<AppAdapter.AppViewHolder>(), Filterable {

    private var filteredAppList: List<AppInfo> = appList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = filteredAppList[position]
        holder.appName.text = app.name
        holder.appIcon.setImageBitmap(app.icon?.toBitmap())
        holder.itemView.setOnClickListener {
            app.launch(holder.itemView.context)
        }
    }

    override fun getItemCount(): Int {
        return filteredAppList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase() ?: ""
                val filteredList = if (query.isEmpty()) {
                    appList
                } else {
                    appList.filter {
                        it.name.lowercase().contains(query)
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredAppList = results?.values as List<AppInfo>
                notifyDataSetChanged()
            }
        }
    }

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appIcon: ImageView = itemView.findViewById(R.id.appIcon)
        val appName: TextView = itemView.findViewById(R.id.appName)
    }
}

// Extension function to convert Drawable to Bitmap
fun Drawable.toBitmap(): Bitmap {
    if (this is BitmapDrawable) {
        return this.bitmap
    }
    val bitmap = Bitmap.createBitmap(this.intrinsicWidth, this.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    this.setBounds(0, 0, canvas.width, canvas.height)
    this.draw(canvas)
    return bitmap
}

// Extension function to launch an app
fun AppInfo.launch(context: Context) {
    val intent = context.packageManager.getLaunchIntentForPackage(this.packageName)
    if (intent != null) {
        context.startActivity(intent)
    } else {
        val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${this.packageName}"))
        context.startActivity(playStoreIntent)
    }
}