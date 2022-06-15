package com.becom.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap

object Constants {
    const val BECOM_PREFERENCES = "BeComPrefs"
    const val LOGGED_IN_USERNAME = "logged_in_username"
    const val EXTRA_USER_DETAILS = "extra_user_details"
    const val EXTRA_ADDRESS_DETAILS = "AddressDetails"
    const val EXTRA_SELECT_ADDRESS = "extra_select_address"
    const val EXTRA_SELECTED_ADDRESS = "extra_selected_address"
    const val EXTRA_MY_ORDER_DETAILS = "extra_my_order_details"
    const val EXTRA_SOLD_PRODUCT_DETAILS = "extra_sold_product_details"
    const val READ_STORAGE_PERMISSION_CODE = 2
    const val PICK_IMAGE_REQUEST_CODE = 1
    const val ADD_ADDRESS_REQUEST_CODE: Int = 121
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
    const val STOCK_QUANTITY = "stock_quantity"

    const val ADDRESSES: String = "addresses"
    const val ORDERS: String = "orders"
    const val SOLD_PRODUCTS: String = "sold_products"

    const val HOME = "Home"
    const val OFFICE = "Office"
    const val OTHER = "Other"

    fun showImageChooser(activity: Activity) {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }
}