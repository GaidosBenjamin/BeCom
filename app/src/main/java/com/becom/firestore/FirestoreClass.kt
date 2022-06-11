package com.becom.firestore

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.fragment.app.Fragment
import com.becom.model.CartItem
import com.becom.model.Product
import com.becom.model.User
import com.becom.ui.activities.*
import com.becom.ui.fragments.DashboardFragment
import com.becom.ui.fragments.ProductsFragment
import com.becom.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FirestoreClass {

    private val fireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: RegisterActivity, userInfo: User) {
        fireStore.collection(Constants.USERS)
            .document(userInfo.id)
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegistrationSuccess()
            }
            .addOnFailureListener { ex ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error on user registration.", ex)
            }
    }

    fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if(currentUser != null) {
            return currentUser.uid
        }

        return ""
    }

    fun getUserDetails(activity: Activity) {
        fireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.toString())
                val user = document.toObject(User::class.java)!!
                saveUserNameToSharedPreferences(user, activity)
                when (activity) {
                    is LoginActivity -> {
                        activity.userLoggedInSuccess(user)
                    }
                    is SettingsActivity -> {
                        activity.userDetailsSuccess(user)
                    }
                }
            }
            .addOnFailureListener { ex ->
                when (activity) {
                    is LoginActivity -> {
                        activity.hideProgressDialog()
                    }
                    is SettingsActivity -> {
                        //activity.hideProgressDialog()
                    }
                }

                Log.e(activity.javaClass.simpleName, "Error while getting user details.", ex)
            }
    }

    private fun saveUserNameToSharedPreferences(user: User, activity: Activity) {
        val sharedPreferences = activity.getSharedPreferences(Constants.BECOM_PREFERENCES, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(Constants.LOGGED_IN_USERNAME, "${user.firstName} ${user.lastName}")
        editor.apply()
    }

    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>) {
        fireStore.collection(Constants.USERS).document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                when (activity) {
                    is UserProfileActivity -> {
                        activity.userProfileUpdateSuccess()
                    }
                }
            }
            .addOnFailureListener { e ->
                when (activity) {
                    is UserProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(activity.javaClass.simpleName, "Error while updating the user details", e)
            }
    }

    fun uploadImageToCloudStorage(activity: Activity, imageFileUri: Uri?, imageType: String) {
        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
            imageType + System.currentTimeMillis() + "." + Constants.getFileExtension(activity, imageFileUri)
        )

        sRef.putFile(imageFileUri!!)
            .addOnSuccessListener { taskSnapshot ->
                Log.i("Firebase Image URL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    Log.i("Downloadable image URL", uri.toString())
                    when (activity) {
                        is UserProfileActivity -> {
                            activity.imageUploadSuccess(uri.toString())
                        }
                        is AddProductActivity -> {
                            activity.imageUploadSuccess(uri.toString())
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                when (activity) {
                    is UserProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                    is AddProductActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(activity.javaClass.simpleName, exception.message, exception)
            }
    }

    fun uploadProductDetails(activity: AddProductActivity, productInfo: Product) {
        fireStore.collection(Constants.PRODUCTS)
            .document()
            .set(productInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.productUploadSuccess()
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
            }
    }

    fun getProductsList(fragment: Fragment) {
        fireStore.collection(Constants.PRODUCTS)
            .whereEqualTo(Constants.USER_ID, getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                val products: ArrayList<Product> = ArrayList()
                for(doc in document.documents) {
                    val product = doc.toObject(Product::class.java)
                    product!!.product_id = doc.id

                    products.add(product)
                }

                when(fragment) {
                    is ProductsFragment -> {
                        fragment.successProductsListFromFireStore(products)
                    }
                }
            }
    }

    fun getDashboardItemsList(fragment: DashboardFragment) {
        fireStore.collection(Constants.PRODUCTS)
            .get()
            .addOnSuccessListener { document ->
                val products: ArrayList<Product> = ArrayList()

                for(doc in document.documents) {
                    val product = doc.toObject(Product::class.java)
                    product!!.product_id = doc.id

                    products.add(product)
                }

                fragment.successDashboardItemsList(products)
            }
            .addOnFailureListener { e ->
                fragment.hideProgressDialog()
                Log.e(fragment.javaClass.simpleName, "Error while getting dashboard items list", e)
            }
    }

    fun deleteProduct(fragment: ProductsFragment, productId: String) {
        fireStore.collection(Constants.PRODUCTS)
            .document(productId)
            .delete()
            .addOnSuccessListener {
                fragment.productDeleteSuccess()
            }
            .addOnFailureListener { ex ->
                fragment.hideProgressDialog()
                Log.e(fragment.requireActivity().javaClass.simpleName, "Error at deleting product ", ex)
            }
    }

    fun getProductDetails(activity: ProductDetailsActivity, productId: String) {
        fireStore.collection(Constants.PRODUCTS)
            .document(productId)
            .get()
            .addOnSuccessListener { document ->
                val product = document.toObject(Product::class.java)
                if(product != null) {
                    activity.productDetailsSuccess(product)
                }
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
            }
    }

    fun addCartItems(activity: ProductDetailsActivity, addToCart: CartItem) {
        fireStore.collection(Constants.CART_ITEMS)
            .document()
            .set(addToCart, SetOptions.merge())
            .addOnSuccessListener {
                activity.addToCartSuccess()
            }
            .addOnFailureListener {
                activity.hideProgressDialog();
            }
    }

    fun checkIfItemExistInCart(activity: ProductDetailsActivity, productId: String) {
        fireStore.collection(Constants.CART_ITEMS)
            .whereEqualTo(Constants.USER_ID, getCurrentUserId())
            .whereEqualTo(Constants.PRODUCT_ID, productId)
            .get()
            .addOnSuccessListener { document ->
                if(document.documents.size > 0) {
                    activity.productExistsInCart()
                } else {
                    activity.hideProgressDialog()
                }
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
            }
    }

    fun getCartList(activity: Activity) {
        fireStore.collection(Constants.CART_ITEMS)
            .whereEqualTo(Constants.USER_ID, getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                val list: ArrayList<CartItem> = ArrayList();
                for(doc in document.documents) {
                    val cartItem = doc.toObject(CartItem::class.java)!!
                    cartItem.id = doc.id

                    list.add(cartItem)
                }

                when(activity) {
                    is CartListActivity -> {
                        activity.successCartItemsList(list)
                    }
                }
            }
            .addOnFailureListener {
                when(activity) {
                    is CartListActivity -> {
                        activity.hideProgressDialog()
                    }
                }
            }
    }

    fun getAllProductsList(activity: CartListActivity) {
        fireStore.collection(Constants.PRODUCTS)
            .get()
            .addOnSuccessListener { document ->
                val products: ArrayList<Product> = ArrayList()
                for(doc in document.documents) {
                    val product = doc.toObject(Product::class.java)
                    product!!.product_id = doc.id
                    products.add(product)
                }
                activity.successProductsListFromFireStore(products)
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
            }
    }

    fun removeItemFromCart(context: Context, cart_id: String) {
        fireStore.collection(Constants.CART_ITEMS)
            .document(cart_id)
            .delete()
            .addOnSuccessListener {
                when(context) {
                    is CartListActivity -> {
                        context.itemRemovedSuccess()
                    }
                }
            }
            .addOnFailureListener {
                when(context) {
                    is CartListActivity -> {
                        context.hideProgressDialog()
                    }
                }
            }
    }

    fun updateMyCart(context: Context, cart_id: String, itemHashMap: HashMap<String, Any>) {
        fireStore.collection(Constants.CART_ITEMS)
            .document(cart_id)
            .update(itemHashMap)
            .addOnSuccessListener {
                when(context) {
                    is CartListActivity -> {
                        context.itemUpdateSuccess()
                    }
                }
            }
            .addOnFailureListener {
                when(context) {
                    is CartListActivity -> {
                        context.hideProgressDialog()
                    }
                }
            }
    }

}