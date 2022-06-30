package com.becom.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.becom.R
import com.becom.firestore.FirestoreClass
import com.becom.model.User
import com.becom.utils.*
import com.becom.utils.views.BCButton
import com.becom.utils.views.BCEditText
import com.becom.utils.views.BCRadioButton
import kotlinx.android.synthetic.main.activity_user_profile.*
import java.io.IOException

class UserProfileActivity : BaseActivity(), View.OnClickListener {

    private var userDetails: User = User()
    private var selectedImageFileUri: Uri? = null
    private var userProfileImageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        if(intent.hasExtra(Constants.EXTRA_USER_DETAILS)) {
            userDetails = intent.getParcelableExtra(Constants.EXTRA_USER_DETAILS)!!
        }
        setUserDetails()


        iv_user_photo.setOnClickListener(this@UserProfileActivity)

        btn_save.setOnClickListener(this@UserProfileActivity)
    }

    private fun setUserDetails() {
        val firstName: BCEditText = findViewById(R.id.et_first_name)
        val lastName: BCEditText = findViewById(R.id.et_last_name)
        val email: BCEditText = findViewById(R.id.et_email)
        lastName.setText(userDetails.lastName)
        firstName.setText(userDetails.firstName)
        email.isEnabled = false
        email.setText(userDetails.email)
        if(userDetails.profileCompleted == 1) {
            tv_title.text = resources.getString(R.string.title_edit_profile)
            setupActionBar(toolbar_user_profile_activity)

            GlideLoader(this@UserProfileActivity).loadUserPicture(userDetails.image, iv_user_photo)

            if(userDetails.mobile != "") {
                et_mobile_number.setText(userDetails.mobile)
            }
            if(userDetails.gender == Constants.MALE) {
                rb_male.isChecked = true
            } else {
                rb_female.isChecked = true
            }
        } else {
            tv_title.text = resources.getString(R.string.title_complete_profile)
            firstName.isEnabled = false
            lastName.isEnabled = false
        }





    }

    override fun onClick(v: View?) {
        val image: ImageView = findViewById(R.id.iv_user_photo)
        val saveButton: BCButton = findViewById(R.id.btn_save)
        if (v != null) {
            when(v.id) {
                image.id -> {
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Constants.showImageChooser(this@UserProfileActivity)
                    } else {
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), Constants.READ_STORAGE_PERMISSION_CODE)
                    }
                }
                saveButton.id -> {
                    if(validateUserProfileDetails()) {
                        showProgressDialog()
                        if(selectedImageFileUri != null) {
                            FirestoreClass().uploadImageToCloudStorage(this@UserProfileActivity, selectedImageFileUri, Constants.USER_PROFILE_IMAGE)
                        } else {
                            updateUserProfileDetails()
                        }

                    }
                }
            }
        }
    }

    private fun updateUserProfileDetails() {
        val userMap = HashMap<String, Any>()
        val mobile = editTextToString(findViewById(R.id.et_mobile_number))
        val firstName = editTextToString(findViewById(R.id.et_first_name))
        val lastName = editTextToString(findViewById(R.id.et_last_name))
        val rb: BCRadioButton = findViewById(R.id.rb_male)

        val gender = if (rb.isChecked) {
            Constants.MALE
        } else {
            Constants.FEMALE
        }

        if(mobile.isNotEmpty() && mobile != userDetails.mobile) {
            userMap[Constants.MOBILE] = mobile
        }

        if(gender.isNotEmpty() && gender != userDetails.gender) {
            userMap[Constants.GENDER] = gender
        }

        if(userProfileImageUrl.isNotEmpty()) {
            userMap[Constants.IMAGE] = userProfileImageUrl
        }

        if(firstName != userDetails.firstName) {
            userMap[Constants.FIRST_NAME] = firstName
        }

        if(lastName != userDetails.lastName) {
            userMap[Constants.LAST_NAME] = lastName
        }

        userMap[Constants.PROFILE_COMPLETE] = 1

        FirestoreClass().updateUserProfileData(this@UserProfileActivity, userMap)
    }

    fun userProfileUpdateSuccess() {
        hideProgressDialog()

        Toast.makeText(this@UserProfileActivity, resources.getString(R.string.msg_profile_update_success), Toast.LENGTH_SHORT).show()

        startActivity(Intent(this@UserProfileActivity, DashboardActivity::class.java))
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this@UserProfileActivity)
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
                    try {
                        selectedImageFileUri = data.data!!
                        val image: ImageView = findViewById(R.id.iv_user_photo)
                        GlideLoader(this@UserProfileActivity).loadUserPicture(selectedImageFileUri!!, image)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this@UserProfileActivity, resources.getString(R.string.image_selection_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun validateUserProfileDetails(): Boolean {
        val mobile: BCEditText = findViewById(R.id.et_mobile_number)
        return when {
            TextUtils.isEmpty(mobile.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_mobile_number), true)
                false
            } else -> {
                true
            }
        }
    }

    private fun editTextToString(input: BCEditText): String {
        return input.text.toString().trim { it <= ' ' }
    }

    fun imageUploadSuccess(imageURL: String) {
        userProfileImageUrl = imageURL
        updateUserProfileDetails()
    }
}