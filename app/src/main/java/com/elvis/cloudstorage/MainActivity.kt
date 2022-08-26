package com.elvis.cloudstorage

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btnUpload: Button = findViewById(R.id.btnUpload)
        if ((ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1000)
        }

        btnUpload.setOnClickListener {
            openGalleryForImages()
        }
    }

    val REQUEST_CODE = 200

    private fun openGalleryForImages() {

        if (Build.VERSION.SDK_INT < 19) {
            var intent = Intent()
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Choose Pictures"), REQUEST_CODE
            )
        } else { // For latest versions API LEVEL 19+
            var intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE);
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {

            // if multiple images are selected
            var imagesArray = arrayListOf<Uri>()
            if (data?.getClipData() != null) {
                var count = data.clipData.itemCount

                for (i in 0 until count) {
                    var imageUri: Uri = data!!.clipData.getItemAt(i).uri
                    imagesArray.add(imageUri)
                    //     iv_image.setImageURI(imageUri) Here you can assign your Image URI to the ImageViews
                }

            } else if (data?.getData() != null) {
                // if single image is selected

                var imageUri: Uri = data.data!!
                imagesArray.add(imageUri)
                //   iv_image.setImageURI(imageUri) Here you can assign the picked image uri to your imageview

            }
            uploadImages(imagesArray)


        }
    }

    //Uploading to firebase
    private fun uploadImages(imagesArray: ArrayList<Uri>) {
        for (imageUri in imagesArray) {
            val fileName = /*This generates a random filename*/UUID.randomUUID().toString() + "jpg"
            val ref = FirebaseStorage.getInstance().reference.child("pictures/$fileName")
            ref.putFile(imageUri).addOnSuccessListener{
                Toast.makeText(this, "Uploaded file", Toast.LENGTH_LONG).show()
            }.addOnFailureListener{
                Toast.makeText(this, "Failed to upload", Toast.LENGTH_LONG).show()
            }
            //you must allow only authenticated users
            //go to the firebase console->open your project->build->storage->get started for storage ->under test mode->done->run app on your phone
            //https://console.firebase.google.com/u/O/
        }
    }

}