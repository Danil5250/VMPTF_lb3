package com.vmptf.mobile.features.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vmptf.mobile.R
import com.vmptf.mobile.features.posts.domain.model.Post
import java.text.SimpleDateFormat
import java.util.Locale

//manage list creation and relate data with it
// : = inheritance or implementation of class, interface
class AdminPostAdapter(
    private val onEditClick: (Post) -> Unit, //Unit = function returns nothing
    private val onDeleteClick: (Post) -> Unit
    //list adapter checks what elements has been changed from back and without flashing (мигання)
) : ListAdapter<Post, AdminPostAdapter.AdminPostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminPostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_post, parent, false)
        return AdminPostViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminPostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    //inner = gives access to parent fields
    //finds objects and saves links to them to not stop phone while scrolling
    //otherwise links would refound after scrolling which costs a lot of
    inner class AdminPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvAdminPostTitle)
        private val tvDate: TextView = itemView.findViewById(R.id.tvAdminPostDate)
        private val btnEdit: Button = itemView.findViewById(R.id.btnAdminEdit)
        private val btnDelete: Button = itemView.findViewById(R.id.btnAdminDelete)

        fun bind(post: Post) {
            tvTitle.text = post.title
            
            // Format date if needed, fallback to raw string
            try {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                val parsedDate = parser.parse(post.createdAt)
                tvDate.text = if (parsedDate != null) formatter.format(parsedDate) else post.createdAt
            } catch (e: Exception) {
                tvDate.text = post.createdAt
            }

            btnEdit.setOnClickListener { onEditClick(post) }
            btnDelete.setOnClickListener { onDeleteClick(post) }
        }
    }

    //how compare if current Post is new or no
    // : = inheritance or implementation of class, interface
    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem == newItem
    }
}
