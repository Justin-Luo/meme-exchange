package hu.ait.android.memeexchange

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import hu.ait.android.memeexchange.MainActivity.Companion.userID
import hu.ait.android.memeexchange.data.User
import kotlinx.android.synthetic.main.activity_portfolio.*

class PortfolioActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_portfolio)

        Thread {

            FirebaseFirestore.getInstance().collection("users").document(userID).collection("owned_posts")
                    .get()
                    .addOnSuccessListener { result->
                        for (document in result) {
                            var postRef = FirebaseFirestore.getInstance().collection("posts").document(document.id)
                            postRef.get()
                                    .addOnSuccessListener { postDocument ->
//                                        postScore += document.get("score").toString().toDouble()
                                        val shareEquity = postDocument.get("score").toString().toDouble() * postDocument.get("quantity").toString().toInt()
                                        tvEquity.text = (tvEquity.text.toString().toDouble() + shareEquity).toString()
                                    }
                        }

                        FirebaseFirestore.getInstance().collection("users").document(userID)
                                .get()
                                .addOnSuccessListener { documentSnapshot ->
                                    tvEquity.text = (tvEquity.text.toString().toDouble() + documentSnapshot.get("buyingPower").toString().toDouble()).toString()
                                }

                    }
        }.start()

    }
}
