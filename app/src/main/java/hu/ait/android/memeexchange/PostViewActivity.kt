package hu.ait.android.memeexchange

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import hu.ait.android.memeexchange.MainActivity.Companion.KEY_POST_ID
import hu.ait.android.memeexchange.MainActivity.Companion.userID
import hu.ait.android.memeexchange.data.Post
import hu.ait.android.memeexchange.data.Share
import hu.ait.android.memeexchange.data.User
import hu.ait.android.memeexchange.data.Vote
import kotlinx.android.synthetic.main.activity_create_post.*
import kotlinx.android.synthetic.main.activity_post_view.*
import kotlinx.android.synthetic.main.owned_posts_info.*

class PostViewActivity : AppCompatActivity() {

    private var postID = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Post"
        setContentView(R.layout.activity_post_view)

        if (intent.hasExtra(KEY_POST_ID)) {
            postID = intent.getStringExtra(KEY_POST_ID)
        }


        if (postID != "") {
            val postRef= FirebaseFirestore.getInstance().collection("posts").
                    document(postID)

            val ownedPostRef = FirebaseFirestore.getInstance().collection("users")
                    .document(userID)
                    .collection("owned_posts")
                    .document(postID)

            setButtons()



            updatePostDetails(postRef, ownedPostRef)
        }

    }

    fun setButtons() {
        btnUp.setOnClickListener {
            upVotePost()
        }

        btnDown.setOnClickListener {
            downVotePost()
        }

        btnBuy.setOnClickListener {
            val buyDialog = TransactionDialog()

            val bundle = Bundle()
            bundle.putSerializable(KEY_POST_ID, postID)
            bundle.putSerializable("KEY_ITEM_TO_BUY", 0)
            buyDialog.arguments = bundle
            buyDialog.show(supportFragmentManager, "BUYDIALOG")
        }
    }

    @SuppressLint("SetTextI18n")
    fun updatePostDetails(postRef: DocumentReference, ownedPostRef: DocumentReference) {
        postRef.get()
                .addOnSuccessListener { documentSnapshot ->

                    val post: Post? = documentSnapshot.toObject(Post::class.java)
                    var ownedPost: Share?

                    tvTitle.text = post?.title
                    Glide.with(this@PostViewActivity).load(post?.imgUrl).into(ivPhoto)
                    tvScore.text = post?.score.toString()

                    ownedPostRef.get().addOnSuccessListener { documentSnapshot ->
                        ownedPost = documentSnapshot.toObject(Share::class.java)


                        var tvStockBuffer = setPostDetails(ownedPost, post)

                        setSellButton(tvStockBuffer)

                    }

                }
    }

    fun setSellButton(tvStockBuffer: Int) {
        if (tvStockBuffer.toString() == "0") {
            btnSell.isEnabled = false
        } else {
            btnSell.isEnabled = true
            btnSell.setOnClickListener {
                val sellDialog = TransactionDialog()

                val bundle = Bundle()
                bundle.putSerializable(KEY_POST_ID, postID)
                sellDialog.arguments = bundle
                sellDialog.show(supportFragmentManager, "SELLDIALOG")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun setPostDetails(ownedPost: Share?, post: Post?): Int {
        var tvStockBuffer = ownedPost?.quantity ?: 0
        var avgCostBuffer = ownedPost?.avgCost ?: 0.0
        tvAvgCost.text = getString(R.string.avg_cost) + avgCostBuffer.toString()
        var equity = (tvStockBuffer.toString()
                .toDouble() * post?.score.toString().toDouble())
                .toString()

        if (equity == "-0.0") {
            equity = "0.0"
        }

        tvEquity.text = getString(R.string.equity) + equity

        tvStocks.text = getString(R.string.shares_owned) + tvStockBuffer.toString()
        tvDifference.text = getString(R.string.net_equity) + (equity.toDouble() - avgCostBuffer * tvStockBuffer.toString().toDouble())
        return tvStockBuffer
    }

    private fun upVotePost() {
        val votesCollection: CollectionReference? = FirebaseFirestore.getInstance().collection("users").
                document(FirebaseAuth.getInstance().currentUser!!.uid).collection("voted_posts")
        votesCollection!!.document(postID).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val voteTime: Double = documentSnapshot.get("time").toString().toDouble()
                        if ((System.currentTimeMillis() - voteTime) < 10000) {
                            Toast.makeText(this@PostViewActivity,
                                    getString(R.string.voting_string),
                                    Toast.LENGTH_LONG).show()
                        }
                        else {
                            increasePostScore()
                            votesCollection.document(postID).update("time", System.currentTimeMillis())
                        }
                    }
                    else {
                        increasePostScore()
                        handleNewVote(postID)
                    }
                }
    }


    fun handleNewVote(postId: String) {
        val newVote = Vote(
                postId,
                System.currentTimeMillis()
        )

        FirebaseFirestore.getInstance().collection("users").
                document(FirebaseAuth.getInstance().currentUser!!.uid).collection("voted_posts").document(postId).set(newVote)
    }

    private fun downVotePost() {
        val votesCollection: CollectionReference? = FirebaseFirestore.getInstance().collection("users").
                document(FirebaseAuth.getInstance().currentUser!!.uid).collection("voted_posts")
        votesCollection!!.document(postID).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val voteTime: Double = documentSnapshot.get("time").toString().toDouble()
                        if ((System.currentTimeMillis() - voteTime) < 10000) {
                            Toast.makeText(this@PostViewActivity,
                                    getString(R.string.voting_string),
                                    Toast.LENGTH_LONG).show()
                        }
                        else {
                            decreasePostScore()
                            votesCollection.document(postID).update("time", System.currentTimeMillis())
                        }
                    }
                    else {
                        decreasePostScore()
                        handleNewVote(postID)
                    }
                }
    }

    fun increasePostScore() {
        val postRef = FirebaseFirestore.getInstance()
                .collection("posts").document(postID)

        val ownedPostRef = FirebaseFirestore.getInstance().collection("users")
                .document(userID)
                .collection("owned_posts")
                .document(postID)

        postRef.get().
                addOnSuccessListener { documentSnapshot ->
                    val postDocScore = documentSnapshot.get("score").toString().toInt()
                    postRef.update("score", postDocScore + 1)
                    val post = documentSnapshot.toObject(Post::class.java)
                    tvScore.text = (post?.score.toString().toInt() + 1).toString()
                    updatePostDetails(postRef, ownedPostRef)
                }
    }

    fun decreasePostScore() {
        val postRef = FirebaseFirestore.getInstance()
                .collection("posts").document(postID)


        val ownedPostRef = FirebaseFirestore.getInstance().collection("users")
                .document(userID)
                .collection("owned_posts")
                .document(postID)

        postRef.get().
                addOnSuccessListener { documentSnapshot ->
                    val postDocScore = documentSnapshot.get("score").toString().toInt()
                    postRef.update("score", postDocScore - 1)
                    val post = documentSnapshot.toObject(Post::class.java)
                    tvScore.text = (post?.score.toString().toInt() - 1).toString()
                    updatePostDetails(postRef, ownedPostRef)
                }
    }



}
