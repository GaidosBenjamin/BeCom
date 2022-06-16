package com.becom.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.becom.R
import com.becom.firestore.FirestoreClass
import com.becom.model.CartItem
import com.becom.model.Product
import com.becom.ui.adapters.CartItemsListAdapter
import com.becom.utils.Constants
import kotlinx.android.synthetic.main.activity_cart_list.*
import kotlinx.android.synthetic.main.activity_product_details.*

class CartListActivity : BaseActivity() {

    private lateinit var mProducts: ArrayList<Product>
    private lateinit var mCartItems: ArrayList<CartItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart_list)

        setUpActionBar()

        btn_checkout.setOnClickListener {
            val intent = Intent(this@CartListActivity, AddressListActivity::class.java)
            intent.putExtra(Constants.EXTRA_SELECT_ADDRESS, true)
            startActivity(intent)
        }
    }

    private fun setUpActionBar() {

        setSupportActionBar(toolbar_cart_list_activity)

        val actionBar = supportActionBar
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        toolbar_cart_list_activity.setNavigationOnClickListener { onBackPressed() }
    }

    @SuppressLint("SetTextI18n")
    fun successCartItemsList(cartList: ArrayList<CartItem>) {
        hideProgressDialog()

        for(product in mProducts) {
            for(cart in cartList) {
                if(product.product_id == cart.product_id) {
                    cart.stock_quantity = product.stock_quantity

                    if(product.stock_quantity.toInt() == 0) {
                        cart.cart_quantity = product.stock_quantity
                    }
                }
            }
        }

        mCartItems = cartList

        if(mCartItems.size > 0) {
            rv_cart_items_list.visibility = View.VISIBLE
            ll_checkout.visibility = View.VISIBLE
            tv_no_cart_item_found.visibility = View.GONE

            rv_cart_items_list.layoutManager = LinearLayoutManager(this@CartListActivity)
            rv_cart_items_list.setHasFixedSize(true)
            val cartListAdapter = CartItemsListAdapter(this@CartListActivity, cartList, true)
            rv_cart_items_list.adapter = cartListAdapter

            var subTotal: Double = 0.0
            for(item in mCartItems) {
                val availableQuantity = item.stock_quantity.toInt()
                if(availableQuantity > 0) {
                    val price = item.price.toDouble()
                    val quantity = item.cart_quantity.toInt()
                    subTotal += (price * quantity)
                }
            }
            tv_sub_total.text = "$subTotal ${resources.getString(R.string.currency)}"
            tv_shipping_charge.text = "10.0 ${resources.getString(R.string.currency)}"

            if(subTotal > 0) {
                ll_checkout.visibility = View.VISIBLE

                val total = subTotal + 10
                tv_total_amount.text = "$total ${resources.getString(R.string.currency)}"
            } else {
                ll_checkout.visibility = View.GONE
            }
        }
        else {
            rv_cart_items_list.visibility = View.GONE
            ll_checkout.visibility = View.GONE
            tv_no_cart_item_found.visibility = View.VISIBLE
        }
    }

    private fun getCartItemsList() {
        //showProgressDialog()
        FirestoreClass().getCartList(this@CartListActivity)
    }

    override fun onResume() {
        super.onResume()
        getProductList()
    }

    fun successProductsListFromFireStore(products: ArrayList<Product>) {
        hideProgressDialog()
        mProducts = products

        getCartItemsList()
    }

    private fun getProductList() {
        showProgressDialog()
        FirestoreClass().getAllProductsList(this@CartListActivity)
    }

    fun itemRemovedSuccess() {
        hideProgressDialog()
        Toast.makeText(this@CartListActivity, resources.getString(R.string.msg_item_removed_successfully), Toast.LENGTH_SHORT).show()

        getCartItemsList()
    }

    fun itemUpdateSuccess() {
        hideProgressDialog()

        getCartItemsList()
    }
}