package com.becom.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.becom.R
import com.becom.firestore.FirestoreClass
import com.becom.model.Product
import com.becom.utils.BCEditText
import com.becom.utils.Constants
import com.becom.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_add_product.*
import kotlinx.android.synthetic.main.activity_settings.*
import java.io.IOException

class AddProductActivity : BaseActivity(), View.OnClickListener {

    private var selectedImageFileURI: Uri? = null
    private var productImageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        setUpActionBar()

        iv_add_update_product.setOnClickListener(this)
        btn_submit_product.setOnClickListener(this)
    }

    private fun setUpActionBar() {

        setSupportActionBar(toolbar_add_product_activity)

        val actionBar = supportActionBar
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        toolbar_add_product_activity.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onClick(v: View?) {
        if(v != null) {
            when (v.id) {
                R.id.iv_add_update_product -> {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Constants.showImageChooser(this@AddProductActivity)
                    } else {
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), Constants.READ_STORAGE_PERMISSION_CODE)
                    }
                }
                R.id.btn_submit_product -> {
//                    if(validateProductDetails()) {
//                        uploadProductImage()
//                    }
                    uploadProductImage()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this@AddProductActivity)
            } else {
                Toast.makeText(this, resources.getString(R.string.read_storage_permission_denied), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == Activity.RESULT_FIRST_USER) {
            if(requestCode == Constants.PICK_IMAGE_REQUEST_CODE) {
                if(data != null) {
                    iv_add_update_product.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_vector_edit))
                    selectedImageFileURI = data.data!!
                    try {
                        GlideLoader(this).loadUserPicture(selectedImageFileURI!!, iv_product_image)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun editTextToString(input: BCEditText): String {
        return input.text.toString().trim { it <= ' ' }
    }

    private fun validateProductDetails(): Boolean {
        return when {
            selectedImageFileURI == null -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_select_product_image), true)
                false
            }
            TextUtils.isEmpty(findViewById(R.id.et_product_title)) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_title), true)
                false
            }
            TextUtils.isEmpty(findViewById(R.id.et_product_price)) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_price), true)
                false
            }
            TextUtils.isEmpty(findViewById(R.id.et_product_description)) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_description), true)
                false
            }
            TextUtils.isEmpty(findViewById(R.id.et_product_quantity)) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_quantity), true)
                false
            } else -> {
                true
            }
        }
    }

    fun imageUploadSuccess(imageURL: String) {
        productImageUrl = imageURL

        uploadProductDetails()
    }

    private fun uploadProductImage() {
        showProgressDialog()
        FirestoreClass().uploadImageToCloudStorage(this, selectedImageFileURI, Constants.PRODUCT_IMAGE)
    }

    private fun uploadProductDetails() {
        val username = this.getSharedPreferences(Constants.BECOM_PREFERENCES, Context.MODE_PRIVATE)
            .getString(Constants.LOGGED_IN_USERNAME, "")!!

        val product = Product(
            FirestoreClass().getCurrentUserId(),
            username,
            editTextToString(findViewById(R.id.et_product_title)),
            editTextToString(findViewById(R.id.et_product_price)),
            editTextToString(findViewById(R.id.et_product_description)),
            editTextToString(findViewById(R.id.et_product_quantity)),
            productImageUrl
        )

        FirestoreClass().uploadProductDetails(this@AddProductActivity, product)
    }

    fun productUploadSuccess() {
        hideProgressDialog()
        Toast.makeText(this@AddProductActivity, resources.getString(R.string.product_uploaded_success_message), Toast.LENGTH_SHORT).show()
        finish()
    }
}