package com.vmptf.mobile.features.posts.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vmptf.mobile.R
import com.vmptf.mobile.features.posts.domain.model.Comment

class CommentsAdapter : ListAdapter<Comment, CommentsAdapter.CommentViewHolder>(DIFF_CALLBACK) {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAuthor: TextView = itemView.findViewById(R.id.tvCommentAuthor)
        val tvContent: TextView = itemView.findViewById(R.id.tvCommentContent)
        val tvDate: TextView = itemView.findViewById(R.id.tvCommentDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = getItem(position)
        holder.tvAuthor.text = comment.user?.name ?: comment.user?.email ?: "Гість"
        holder.tvContent.text = comment.content
        holder.tvDate.text = if (comment.createdAt.length >= 10) comment.createdAt.substring(0, 10) else comment.createdAt
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Comment>() {
            override fun areItemsTheSame(oldItem: Comment, newItem: Comment) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Comment, newItem: Comment) = oldItem == newItem
        }
    }
}
