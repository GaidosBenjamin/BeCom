package com.becom.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.becom.R
import com.becom.firestore.FirestoreClass
import com.becom.model.User
import com.becom.utils.*
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val tvRegister: BCTextViewBold = findViewById(R.id.tv_register)
        tvRegister.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }

        val loginButton: BCButton = findViewById(R.id.btn_login)
        loginButton.setOnClickListener {
            logInUser()
        }

        val forgotPassword: BCTextView = findViewById(R.id.tv_forgot_password)
        forgotPassword.setOnClickListener {
            startActivity(Intent(this@LoginActivity, ForgotPasswordActivity::class.java))
        }

    }

    private fun validateInput(input: String): Boolean {
        return TextUtils.isEmpty(input.trim { it <= ' ' })
    }

    private fun validateLoginDetails(): Boolean {
        val email: BCEditText = findViewById(R.id.et_email)
        val password: BCEditText = findViewById(R.id.et_password)

        return when {
            validateInput(email.text.toString()) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            validateInput(password.text.toString()) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }
            else -> {
                true
            }
        }
    }

    private fun editTextToString(input: BCEditText): String {
        return input.text.toString().trim { it <= ' ' }
    }

    private fun logInUser() {

        if(validateLoginDetails()) {

            val email: String = editTextToString(findViewById(R.id.et_email))
            val password: String = editTextToString(findViewById(R.id.et_password))

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        FirestoreClass().getUserDetails(this@LoginActivity)
                    } else {
                        showErrorSnackBar(resources.getString(R.string.err_msg_login), true)
                    }
                }
        }
    }

    fun userLoggedInSuccess(user: User) {

        Log.i("email", user.email)
        Log.i("profileCompleted", user.profileCompleted.toString())

        if(user.profileCompleted == 0) {
            val intent = Intent(this@LoginActivity, UserProfileActivity::class.java)
            intent.putExtra(Constants.EXTRA_USER_DETAILS, user)
            startActivity(intent)
        } else {
            startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
        }

        finish()
    }
}