package com.vmptf.mobile.features.posts.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vmptf.mobile.R
import com.vmptf.mobile.features.posts.domain.model.Comment

//adapter for RecycleView for showing list of comments to post
class CommentsAdapter(
    private val currentUserId: Int,
    private val currentUserRole: String?,
    private val onDeleteCommentClick: (Comment) -> Unit
    //parent in <> type parameters (generics) with which object and markup it works to, DIFF_CALLBACK = argument of constructor
) : ListAdapter<Comment, CommentsAdapter.CommentViewHolder>(DIFF_CALLBACK) {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAuthor: TextView = itemView.findViewById(R.id.tvCommentAuthor)
        val tvContent: TextView = itemView.findViewById(R.id.tvCommentContent)
        val tvDate: TextView = itemView.findViewById(R.id.tvCommentDate)
        val ivDeleteComment: ImageView = itemView.findViewById(R.id.ivDeleteComment)
    }

    //create card for data inflating
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            //inflate = R.layout.item_comment example for one comment inflation
            //parent = container
            // false = fill the data but not attach to RecycleView because RecycleView do it itself
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = getItem(position)
        holder.tvAuthor.text = comment.user?.name ?: comment.user?.email ?: "Гість"
        holder.tvContent.text = comment.content
        holder.tvDate.text = if (comment.createdAt.length >= 10) comment.createdAt.substring(0, 10) else comment.createdAt

        val isOwner = comment.userId != null && comment.userId == currentUserId
        val isAdmin = currentUserRole == "ADMIN"

        if (isOwner || isAdmin) {
            holder.ivDeleteComment.visibility = View.VISIBLE
            holder.ivDeleteComment.setOnClickListener {
                onDeleteCommentClick(comment)
            }
        } else {
            holder.ivDeleteComment.visibility = View.GONE
            holder.ivDeleteComment.setOnClickListener(null)
        }
    }

    //companion object = static objects which are CommentsAdapter components
    //because if activity has 2 or more adapters, will be created only one this object
    // to save RAM and use single DIFF_CALLBACK
    companion object {
        //object : DiffUtil.ItemCallback<Comment>() = anonymous class, val = readonly
        //create object DIFF_CALLBACK without naming a new class
        //object contains logic of comparison if current comment is new
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Comment>() {
            override fun areItemsTheSame(oldItem: Comment, newItem: Comment) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Comment, newItem: Comment) = oldItem == newItem
        }
    }
}
