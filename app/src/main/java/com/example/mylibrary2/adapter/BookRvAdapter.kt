package com.example.mylibrary2.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.mylibrary2.R
import com.example.mylibrary2.databinding.BookListBinding
import com.example.mylibrary2.model.Book
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.util.*


class BookRvAdapter(private var context: Context) :
    RecyclerView.Adapter<BookRvAdapter.ItemViewHolder>() {
    // Instance of FirebaseStorage
    private val storage = Firebase.storage

    // Points to the root reference [Root]
    var storageRef = storage.reference

    private var booksItem: MutableList<Book> = ArrayList()

    @SuppressLint("NotifyDataSetChanged")
    fun setList(itemsCells: MutableList<Book>) {
        this.booksItem = itemsCells
        notifyDataSetChanged()

    }


    inner class ItemViewHolder(val binding: BookListBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

        val binding = BookListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)

    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        holder.binding.tvBookName.text = booksItem[position].bookName
        holder.binding.tvBookAuthor.text = booksItem[position].bookAuthor
        holder.binding.tvRate.text = booksItem[position].rate.toString()
        holder.binding.rbRatingBar.rating = booksItem[position].rate!!
        holder.binding.tvPrice.text = "$  ${booksItem[position].price}"


        storageRef.child("images/${booksItem[position].bookImage}").downloadUrl.addOnSuccessListener { imageData ->

            Picasso.with(context).load(imageData).into(holder.binding.imgBookCover)
            Log.d("hhh", imageData.toString())

        }.addOnFailureListener {

            Log.d("hhh", it.message.toString())

        }


        val book = booksItem[position]
        val milliSeconds = book.launchYear?.time!!
        val mLaunchYear = DateFormat.format("yyyy", Date(milliSeconds)).toString()
        holder.binding.tvLaunchYear.text = mLaunchYear

        holder.binding.btnEdit.apply {
            setOnClickListener {
                val bundle = Bundle()
                bundle.putParcelable("books", book)
                findNavController().navigate(
                    R.id.action_homeFragment_to_editBookFragment,
                    bundle
                )
            }
        }

    }

    override fun getItemCount() = booksItem.size
}
