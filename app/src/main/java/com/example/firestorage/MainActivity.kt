package com.example.firestorage

import android.app.Activity
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.firestorage.databinding.ActivityMainBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var storage: FirebaseStorage
    lateinit var reference: StorageReference
    lateinit var process: ProgressDialog
    lateinit var uri: Uri
    val REQUEST_CODE = 100
    var url = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = Firebase.storage
        reference = storage.reference
        process = ProgressDialog(this)
        process.setCancelable(false)

        binding.button.setOnClickListener {
            if (binding.button.text == "Select file") {
                selectFile()
            } else if (binding.button.text == "Upload") {
                uploadFile(uri)
            }else if (binding.button.text == "Download"){
                downloadFile(url)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                binding.textView.text = uri.lastPathSegment!!.substring(uri.lastPathSegment!!.lastIndexOf("/") + 1, uri.lastPathSegment!!.lastIndexOf("."))
                binding.button.text = "Upload"
                this.uri = uri
            }
        }
    }

    fun uploadFile(uri: Uri){
        process.setMessage("Uploading...")
        process.show()
        reference.child("docs/")
            .putFile(uri)
            .addOnSuccessListener { task ->
                task.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                    url = uri.toString()
                    process.dismiss()
                    Toast.makeText(this@MainActivity, "Uploading Successfully", Toast.LENGTH_SHORT).show()
                    binding.button.text = "Download"
                }
            }
            .addOnFailureListener {
                process.dismiss()
                Toast.makeText(this@MainActivity, "Uploading Failed", Toast.LENGTH_SHORT).show()
            }
    }

    fun selectFile(){
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "application/pdf"
        startActivityForResult(intent, REQUEST_CODE)
    }

    fun downloadFile(url: String){
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}