package com.vmptf.mobile.features.posts.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vmptf.mobile.R
import com.vmptf.mobile.features.posts.domain.model.Post

class PostsAdapter(
    private val onItemClick: (Post) -> Unit = {}
) : ListAdapter<Post, PostsAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
        holder.itemView.setOnClickListener { onItemClick(post) }
    }

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvPostTitle)
        private val tvContent: TextView = itemView.findViewById(R.id.tvPostContent)
        private val tvAuthor: TextView = itemView.findViewById(R.id.tvPostAuthor)
        private val tvDate: TextView = itemView.findViewById(R.id.tvPostDate)
        private val llCategories: LinearLayout = itemView.findViewById(R.id.llCategories)

        fun bind(post: Post) {
            tvTitle.text = post.title
            tvContent.text = post.content ?: "Без змісту"
            tvAuthor.text = "✍ ${post.author ?: "Невідомий"}"

            // Format date — show first 10 chars of ISO string (yyyy-MM-dd)
            tvDate.text = if (post.createdAt.length >= 10) post.createdAt.substring(0, 10) else post.createdAt

            // Render category chips
            llCategories.removeAllViews()
            post.categories.forEach { category ->
                val chip = buildCategoryChip(itemView.context, category.name)
                llCategories.addView(chip)
            }
        }

        private fun buildCategoryChip(context: Context, name: String): TextView {
            return TextView(context).apply {
                text = name
                textSize = 11f
                setTextColor(0xFF818CF8.toInt())
                background = context.getDrawable(R.drawable.chip_category)
                val px8 = (8 * context.resources.displayMetrics.density).toInt()
                val px4 = (4 * context.resources.displayMetrics.density).toInt()
                val px6 = (6 * context.resources.displayMetrics.density).toInt()
                setPadding(px8, px4, px8, px4)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, px6, 0)
                layoutParams = params
            }
        }
    }

    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Post, newItem: Post) = oldItem == newItem
    }
}
