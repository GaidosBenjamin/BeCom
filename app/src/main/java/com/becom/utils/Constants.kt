package com.becom.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap

object Constants {
    const val BECOM_PREFERENCES: String = "BeComPrefs"
    const val LOGGED_IN_USERNAME: String = "logged_in_username"
    const val EXTRA_USER_DETAILS: String = "extra_user_details"
    const val READ_STORAGE_PERMISSION_CODE = 2
    const val PICK_IMAGE_REQUEST_CODE = 1
    const val EXTRA_PRODUCT_OWNER_ID = "extra_product_owner_id"
    const val DEFAULT_CART_QUANTITY = "1"
    const val EXTRA_PRODUCT_ID = "extra_product_id"
    const val PRODUCT_IMAGE = "product_image"
    const val USER_PROFILE_IMAGE = "user_profile_image"
    const val USER_ID = "user_id"
    const val USERS = "users"
    const val MALE = "male"
    const val FEMALE = "female"
    const val MOBILE = "mobile"
    const val GENDER = "gender"
    const val IMAGE = "image"
    const val PROFILE_COMPLETE = "profileCompleted"
    const val FIRST_NAME = "firstName"
    const val LAST_NAME = "lastName"

    const val PRODUCTS = "products"
    const val PRODUCT_ID = "product_id"
    const val CART_ITEMS = "cart_items"
    const val CART_QUANTITY = "cart_quantity"

    fun showImageChooser(activity: Activity) {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }
}