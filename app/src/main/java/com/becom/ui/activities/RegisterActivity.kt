package com.becom.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.widget.AppCompatCheckBox
import com.becom.R
import com.becom.firestore.FirestoreClass
import com.becom.model.User
import com.becom.utils.BCButton
import com.becom.utils.BCEditText
import com.becom.utils.BCTextViewBold
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class RegisterActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        setupActionBar(findViewById(R.id.toolbar_register_activity))

        val tvLogin: BCTextViewBold = findViewById(R.id.tv_login)
        tvLogin.setOnClickListener {
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            finish()
        }

        val registerButton: BCButton = findViewById(R.id.btn_register)
        registerButton.setOnClickListener {
            registerUser()
        }
    }

    private fun validateInput(input: String): Boolean {
        return TextUtils.isEmpty(input.trim { it <= ' ' })
    }

    private fun comparePasswords(password: String, confirmPassword: String): Boolean {
        return password.trim { it <= ' ' } != confirmPassword.trim { it <= ' ' }
    }

    private fun validateRegisterDetails(): Boolean {
        val firstName: BCEditText = findViewById(R.id.et_first_name)
        val lastName: BCEditText = findViewById(R.id.et_last_name)
        val email: BCEditText = findViewById(R.id.et_email)
        val password: BCEditText = findViewById(R.id.et_password)
        val confirmPassword: BCEditText = findViewById(R.id.et_confirm_password)
        val termsBox: AppCompatCheckBox = findViewById(R.id.cb_terms_and_condition)

        return when {
            validateInput(firstName.text.toString()) -> { showErrorSnackBar(resources.getString(R.string.err_msg_enter_first_name), true)
            false
            }
            validateInput(lastName.text.toString()) -> { showErrorSnackBar(resources.getString(R.string.err_msg_enter_last_name), true)
            false
            }
            validateInput(email.text.toString()) -> { showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
            false
            }
            validateInput(password.text.toString()) -> { showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
            false
            }
            validateInput(confirmPassword.text.toString()) -> { showErrorSnackBar(resources.getString(R.string.err_msg_enter_confirm_password), true)
            false
            }
            comparePasswords(password.text.toString(), confirmPassword.text.toString()) -> { showErrorSnackBar(resources.getString(R.string.err_msg_password_and_confirm_password_mismatch), true)
                false
            }
            !termsBox.isChecked -> { showErrorSnackBar(resources.getString(R.string.err_msg_agree_terms_and_condition), true)
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

    private fun registerUser() {
        if(validateRegisterDetails()) {

            showProgressDialog()

            val email: String = editTextToString(findViewById(R.id.et_email))
            val password: String = editTextToString(findViewById(R.id.et_password))
            val firstName: String = editTextToString(findViewById(R.id.et_first_name))
            val lastName: String = editTextToString(findViewById(R.id.et_last_name))

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!

                        val user = User(firebaseUser.uid, firstName, lastName, email)

                        FirestoreClass().registerUser(this@RegisterActivity, user)

                        FirebaseAuth.getInstance().signOut()
                        finish()
                    } else {
                        hideProgressDialog()
                        showErrorSnackBar(task.exception!!.message.toString(), true)
                    }
                }
        }
    }

    fun userRegistrationSuccess() {
        hideProgressDialog()
    }
}