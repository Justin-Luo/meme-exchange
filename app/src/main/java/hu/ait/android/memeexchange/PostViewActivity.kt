package hu.ait.android.memeexchange

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hu.ait.android.memeexchange.MainActivity.Companion.KEY_POST_ID
import hu.ait.android.memeexchange.MainActivity.Companion.KEY_USER_ID
import hu.ait.android.memeexchange.MainActivity.Companion.userID
import hu.ait.android.memeexchange.data.Post
import hu.ait.android.memeexchange.data.Share
import kotlinx.android.synthetic.main.activity_create_post.*
import kotlinx.android.synthetic.main.activity_post_view.*

class PostViewActivity : AppCompatActivity() {

    private var postID = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_view)

        if (intent.hasExtra(KEY_POST_ID)) {
            postID = intent.getStringExtra(KEY_POST_ID)
        }

        var post : Post?

        if (postID != "") {
            val postRef= FirebaseFirestore.getInstance().collection("posts").
                    document(postID)
            postRef.get()
                    .addOnSuccessListener { documentSnapshot ->
                        post = documentSnapshot.toObject(Post::class.java)
                        tvTitle.text = post?.title
                        Glide.with(this@PostViewActivity).load(post?.imgUrl).into(ivPhoto)
                        tvScore.text = post?.score.toString()

                        btnUp.setOnClickListener {

                        }

                        btnDown.setOnClickListener {

                        }

                        btnBuy.setOnClickListener {
                            val buyDialog = TransactionDialog()

                            val bundle = Bundle()
                            bundle.putSerializable(KEY_POST_ID, postID)
                            bundle.putSerializable("KEY_ITEM_TO_BUY", 0)
                            buyDialog.arguments = bundle
                            buyDialog.show(supportFragmentManager, "BUYDIALOG")
                        }

                        btnSell.setOnClickListener{
                            val sellDialog = TransactionDialog()

                            val bundle = Bundle()
                            bundle.putSerializable(KEY_POST_ID, postID)
                            sellDialog.arguments = bundle
                            sellDialog.show(supportFragmentManager, "SELLDIALOG")
                        }
                    }

                    .addOnFailureListener { exception ->
                        Log.d("Error","Get failed with ", exception)
                    }
        }
    }
}
