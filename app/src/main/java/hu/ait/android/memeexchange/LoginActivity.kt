package hu.ait.android.memeexchange

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import hu.ait.android.memeexchange.data.User
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun loginClick(v: View) {
        if (!isFormValid()) {
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(
            etEmail.text.toString(),
            etPassword.text.toString()
        ).addOnSuccessListener {
            // show the MAIN SCREEN
            startActivity(
                Intent(this@LoginActivity,
                MainActivity::class.java)
            )
        }.addOnFailureListener{
            Toast.makeText(this@LoginActivity,
                    getString(R.string.login_err) + it.message,Toast.LENGTH_LONG).show()
        }

    }


    fun registerClick(v: View) {
        if (!isFormValid()) {
            return
        }


        FirebaseAuth.getInstance().createUserWithEmailAndPassword(
            etEmail.text.toString(), etPassword.text.toString()
        ).addOnSuccessListener {
            val user = it.user
            user.updateProfile(
                UserProfileChangeRequest.Builder().
                        setDisplayName(userNameFromEmail(user.email!!))
                    .build()
            )

            val newUser = User(100.0
            )

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(FirebaseAuth.getInstance().currentUser!!.uid).set(newUser)

            Toast.makeText(this@LoginActivity,
                getString(R.string.good_reg), Toast.LENGTH_LONG).show()

        }.addOnFailureListener{
            Toast.makeText(this@LoginActivity,
                    getString(R.string.reg_error) + it.message,Toast.LENGTH_LONG).show()
        }


    }



    private fun isFormValid(): Boolean {
        return when {
            etEmail.text.isEmpty() -> {
                etEmail.error = getString(R.string.empty_field)
                false
            }
            etPassword.text.isEmpty() -> {
                etPassword.error = getString(R.string.empty_field)
                false
            }
            else -> true
        }
    }


    private fun userNameFromEmail(email: String) =
        email.substringBefore("@")

}
