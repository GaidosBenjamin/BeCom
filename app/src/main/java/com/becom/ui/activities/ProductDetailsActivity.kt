package com.becom.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.becom.R
import com.becom.firestore.FirestoreClass
import com.becom.model.CartItem
import com.becom.model.Product
import com.becom.utils.Constants
import com.becom.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_add_product.*
import kotlinx.android.synthetic.main.activity_product_details.*

class ProductDetailsActivity : BaseActivity(), View.OnClickListener {

    private var productId: String = ""
    private lateinit var mProductDetails: Product

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)
        setUpActionBar()
        var productOwnerId: String = ""
        if(intent.hasExtra(Constants.EXTRA_PRODUCT_ID)) {
            productId = intent.getStringExtra(Constants.EXTRA_PRODUCT_ID)!!
        }
        if(intent.hasExtra(Constants.EXTRA_PRODUCT_OWNER_ID)) {
            productOwnerId = intent.getStringExtra(Constants.EXTRA_PRODUCT_OWNER_ID)!!
        }

        if(FirestoreClass().getCurrentUserId() == productOwnerId) {
            btn_add_to_cart.visibility = View.GONE
            btn_go_to_cart.visibility = View.GONE
        } else {
            btn_add_to_cart.visibility = View.VISIBLE
            btn_go_to_cart.visibility = View.VISIBLE
        }

        btn_add_to_cart.setOnClickListener(this)
        btn_go_to_cart.setOnClickListener(this)

        getProductDetails()
    }

    fun productDetailsSuccess(product: Product) {
        mProductDetails = product
        GlideLoader(this@ProductDetailsActivity).loadProductPicture(product.image, iv_product_detail_image)

        tv_product_details_title.text = product.title
        tv_product_details_price.text = "${product.price} ${resources.getString(R.string.currency)}"
        tv_product_details_description.text = product.description
        tv_product_details_stock_quantity.text = product.stock_quantity

        if(product.stock_quantity.toInt() == 0) {
            hideProgressDialog()
            btn_add_to_cart.visibility = View.GONE
            tv_product_details_stock_quantity.text = resources.getString(R.string.lbl_out_of_stock)
            tv_product_details_stock_quantity.setTextColor(ContextCompat.getColor(this@ProductDetailsActivity, R.color.colorSnackBarError))
        } else {
            if(FirestoreClass().getCurrentUserId() == product.user_id) {
                hideProgressDialog()
            } else {
                FirestoreClass().checkIfItemExistInCart(this@ProductDetailsActivity, productId)
            }
        }
    }

    private fun setUpActionBar() {

        setSupportActionBar(toolbar_product_details_activity)

        val actionBar = supportActionBar
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        toolbar_product_details_activity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun getProductDetails() {
        showProgressDialog()
        FirestoreClass().getProductDetails(this@ProductDetailsActivity, productId)
    }

    override fun onClick(v: View?) {
        if(v != null) {
            when(v.id) {
                R.id.btn_add_to_cart -> {
                    addToCart()
                }
                R.id.btn_go_to_cart -> {
                    startActivity(Intent(this@ProductDetailsActivity, CartListActivity::class.java))
                }
            }
        }
    }

    private fun addToCart() {
        val cartItem = CartItem(
            FirestoreClass().getCurrentUserId(),
            productId,
            mProductDetails.title,
            mProductDetails.price,
            mProductDetails.image,
            Constants.DEFAULT_CART_QUANTITY
        )

        showProgressDialog()
        FirestoreClass().addCartItems(this@ProductDetailsActivity, cartItem)
    }

    fun addToCartSuccess() {
        hideProgressDialog()

        Toast.makeText(this@ProductDetailsActivity, resources.getString(R.string.success_message_item_added_to_cart), Toast.LENGTH_SHORT).show()

        btn_add_to_cart.visibility = View.GONE
        btn_go_to_cart.visibility = View.VISIBLE
    }

    fun productExistsInCart() {
        hideProgressDialog()
        btn_add_to_cart.visibility = View.GONE
        btn_go_to_cart.visibility = View.VISIBLE
    }


}