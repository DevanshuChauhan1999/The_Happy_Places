package com.devanshu.thehappyplaces.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.devanshu.thehappyplaces.R
import com.devanshu.thehappyplaces.database.DatabaseHandler
import com.devanshu.thehappyplaces.databinding.ActivityAddHappyPlacesBinding
import com.devanshu.thehappyplaces.models.HapplyPlaceModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class AddHappyPlacesActivity : AppCompatActivity(), View.OnClickListener {

    companion object{
        private const val IMAGE_DIRECTORY ="HappyPlacesImages"
    }

    private lateinit var galleryImageResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraImageResultLauncher: ActivityResultLauncher<Intent>

    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var binding:ActivityAddHappyPlacesBinding?= null

    private var saveImageToInternalStorage: Uri?= null
    private var mLatitude: Double =0.0
    private var mLongitude : Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHappyPlacesBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setSupportActionBar(binding?.toolbarAddPlace) // Use the toolbar to set the action bar.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding?.toolbarAddPlace?.setNavigationOnClickListener {
            onBackPressed()
        }

        dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR,year)
            cal.set(Calendar.MONTH,month)
            cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            updateDateInView()
        }
        updateDateInView()
        binding?.etDate?.setOnClickListener(this)
        binding?.tvAddImage?.setOnClickListener(this)

        registerOnActivityForGalleryResult()
        registerOnActivityForCameraResult()

        binding?.btnSave?.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.et_date ->{
                DatePickerDialog(this@AddHappyPlacesActivity,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
            }
            R.id.tv_add_image ->{
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems = arrayOf("Select photo from Gallery",
                    "Capture Photo From Camera")
                pictureDialog.setItems(pictureDialogItems){
                    _, which ->
                    when(which){
                        0 -> choosePhotoFromGallery()
                        1 -> takePictureWithCamera()
                    }
                }
                pictureDialog.show()
            }
            R.id.btn_save ->{
                when{
                    binding?.etTitle?.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show()
                    }
                    binding?.etDescription?.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter Description", Toast.LENGTH_SHORT).show()
                    }
                    binding?.etLocation?.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter Location", Toast.LENGTH_SHORT).show()
                    }
                    saveImageToInternalStorage == null ->{
                        Toast.makeText(this, "Please select a Image", Toast.LENGTH_SHORT).show()
                    }
                    else ->{
                        val happyPlaceModel = HapplyPlaceModel(
                            0, 
                            binding?.etTitle?.text.toString(),
                            saveImageToInternalStorage.toString(),
                            binding?.etDescription?.text.toString(),
                            binding?.etDate?.text.toString(),
                            binding?.etLocation?.text.toString(),
                            mLatitude,
                            mLongitude
                        )    
                        val dbHandler  = DatabaseHandler(this)
                        val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModel)
                        
                        if (addHappyPlace > 0){
                            Toast.makeText(this, "The Happy Place details are inserted successfully", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                
            }
        }

    }


    //for gallery functionality
    private fun choosePhotoFromGallery() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ).withListener(object : MultiplePermissionsListener{
                override fun onPermissionsChecked(report: MultiplePermissionsReport?)
                {
                    if (report!!.areAllPermissionsGranted()) {
                        val galleryIntent =
                            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        galleryImageResultLauncher.launch(galleryIntent)
                    }else showRationalDialogForPermissions()

                }
                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest?>?, token: PermissionToken?)
                {
                    token?.continuePermissionRequest()
                    showRationalDialogForPermissions()
                }
            }).onSameThread().check()
    }


    private fun registerOnActivityForGalleryResult() {
        galleryImageResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    if (data != null) {
                        val contentUri = data.data
                        try {
                            val selectedImageBitmap:Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentUri)
                             saveImageToInternalStorage = saveImageInternalStorage(selectedImageBitmap)
                            Log.e("Saved Image : ", "Path :: $saveImageToInternalStorage")
                            binding?.ivPlaceImage?.setImageBitmap(selectedImageBitmap)
                        } catch (e: IOException) {
                            e.printStackTrace()
                            Toast.makeText(
                                this,
                                "Failed to load image from gallery",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                }
            }

    }


    //for camera functionality
    private fun takePictureWithCamera(){
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener{
                override fun onPermissionsChecked(report: MultiplePermissionsReport?)
                {
                    if (report!!.areAllPermissionsGranted()) {
                        val galleryIntent =
                            Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        cameraImageResultLauncher.launch(galleryIntent)
                    }else showRationalDialogForPermissions()

                }
                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest?>?, token: PermissionToken?)
                {
                    showRationalDialogForPermissions()
                    token?.continuePermissionRequest()
                }
            }).onSameThread().check()
    }

    private fun registerOnActivityForCameraResult() {
        cameraImageResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result ->
                if (result.resultCode == Activity.RESULT_OK) {

                    val data: Intent? = result.data
                    if (data != null) {
                        try {
                            val thumbNail: Bitmap = result!!.data!!.extras?.get("data") as Bitmap

                            saveImageToInternalStorage = saveImageInternalStorage(thumbNail)
                            Log.e("Saved Image : ", "Path :: $saveImageToInternalStorage")

                            binding?.ivPlaceImage?.setImageBitmap(thumbNail)
                        } catch (e: IOException) {
                            e.printStackTrace()
                            Toast.makeText(
                                this,
                                "Failed to take photo from Camera",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                }
            }

    }



    //for changing the permission using alertDialogBox
    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this).setMessage("It looks like you have turned off permission required for this feature. You can change them in Application settings.")
            .setPositiveButton("Go To Setting"){
                _,_ ->
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("Package",packageName,null)
                        intent.data = uri
                        startActivity(intent)
                    }catch (e: ActivityNotFoundException){
                        e.printStackTrace()
                    }
            }.setNegativeButton("Cancel"){
                dialog,_ ->
                dialog.dismiss()
            }.show()
    }


    private fun updateDateInView(){
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding?.etDate?.setText(sdf.format(cal.time).toString())
    }

    //for storing image in private folder
    private fun saveImageInternalStorage(bitmap: Bitmap):Uri{
        val wrapper =ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file,"${UUID.randomUUID()}.jpeg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()

        }catch (e: IOException){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }


}