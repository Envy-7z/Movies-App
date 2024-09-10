package com.wisnua.starterproject.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.wisnua.starterproject.R
import com.wisnua.starterproject.databinding.ContentMovieBinding
import com.wisnua.starterproject.domain.model.Search

class MovieAdapter : PagingDataAdapter<Search, MovieAdapter.MovieViewHolder>(MovieDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ContentMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = getItem(position)
        movie?.let { holder.bind(it) }
    }

    inner class MovieViewHolder(private val binding: ContentMovieBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: Search) {
            binding.apply {
                tvTitle.text = movie.title
                tvSubTitle.text = movie.type ?: "Unknown Type"
                tvDesc.text = movie.year ?: "No Year"

                if (movie.poster != null) {
                    Glide.with(root)
                        .load(movie.poster)
                        .centerCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(ivImage)
                } else {
                    // Load placeholder image
                    ivImage.setImageDrawable(ContextCompat.getDrawable(root.context, R.drawable.baseline_browser_not_supported_24))
                }
            }
        }
    }

    class MovieDiffCallback : DiffUtil.ItemCallback<Search>() {
        override fun areItemsTheSame(oldItem: Search, newItem: Search): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Search, newItem: Search): Boolean {
            return oldItem == newItem
        }
    }
}
