package hu.ait.android.memeexchange.adapter

import android.content.Context
import android.content.Intent
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import hu.ait.android.memeexchange.MainActivity
import hu.ait.android.memeexchange.MainActivity.Companion.KEY_POST_ID
import hu.ait.android.memeexchange.MainActivity.Companion.KEY_USER_ID
import hu.ait.android.memeexchange.MainActivity.Companion.REQUEST_DETAILS
import hu.ait.android.memeexchange.PortfolioActivity
import hu.ait.android.memeexchange.PostViewActivity
import hu.ait.android.memeexchange.R
import hu.ait.android.memeexchange.data.Post
import kotlinx.android.synthetic.main.row_post.view.*

class PostsAdapter(var context: Context, var uid:String) : RecyclerView.Adapter<PostsAdapter.ViewHolder>() {

    private var postsList = mutableListOf<Post>()
    private var postKeys = mutableListOf<String>()
    private var lastPosition = -1


    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.row_post, parent, false
        )

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return postsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = postsList[holder.adapterPosition]

        holder.tvTitle.text = post.title
        holder.tvScore.text = post.score.toString()

        if (!TextUtils.isEmpty(post.imgUrl)) {
            holder.ivPhoto.visibility = View.VISIBLE
            Glide.with(context).load(post.imgUrl).into(holder.ivPhoto)
        } else {
            holder.ivPhoto.visibility = View.GONE
        }

//        if (post.uid == uid) {
//            holder.btnDelete.visibility = View.VISIBLE
//            holder.btnDelete.setOnClickListener {
//                removePost(holder.adapterPosition)
//            }
//        } else {
//            holder.btnDelete.visibility = View.GONE
//        }

        holder.btnUp.setOnClickListener {
            upvotePost(holder.adapterPosition)
        }

        holder.btnDown.setOnClickListener {
            downVotePost(holder.adapterPosition)
        }

        Log.d("test", context.toString())
        holder.itemView.setOnClickListener {
            if (context is MainActivity) {
                (context as MainActivity).openPost(postKeys[holder.adapterPosition])
            } else {
                (context as PortfolioActivity).openPost(postKeys[holder.adapterPosition])

            }

        }

        setAnimation(holder.itemView, position)
    }

    fun addPost(post: Post, key: String) {
        postsList.add(post)
        postKeys.add(key)
        notifyDataSetChanged()
    }

    fun updatePost(post: Post, key: String) {
        var index = postKeys.indexOf(key)
        postsList[index] = post
        notifyDataSetChanged()
    }

    private fun upvotePost(index: Int) {
        FirebaseFirestore.getInstance().collection("posts").document(
                postKeys[index]
        ).update("score", postsList[index].score + 1)
        postsList[index].score += 1
        notifyItemChanged(index)
    }

    private fun downVotePost(index: Int) {
        FirebaseFirestore.getInstance().collection("posts").document(
                postKeys[index]
        ).update("score", postsList[index].score - 1)
        postsList[index].score -= 1
        notifyItemChanged(index)
    }


//    private fun removePost(index: Int) {
//        FirebaseFirestore.getInstance().collection("posts").document(
//            postKeys[index]
//        ).delete()
//
//        postsList.removeAt(index)
//        postKeys.removeAt(index)
//        notifyItemRemoved(index)
//    }

    fun removePostByKey(key: String) {
        val index = postKeys.indexOf(key)
        if (index != -1) {
            postsList.removeAt(index)
            postKeys.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(context,
                android.R.anim.slide_in_left)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }

    class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val tvTitle: TextView = itemView.tvTitle
        val tvScore: TextView = itemView.tvScore
        val btnUp: Button = itemView.btnUp
        val btnDown: Button = itemView.btnDown
        val ivPhoto: ImageView = itemView.ivPhoto
    }

}