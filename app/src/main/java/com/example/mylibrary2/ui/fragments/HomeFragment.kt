package com.example.mylibrary2.ui.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mylibrary2.R
import com.example.mylibrary2.adapter.BookRvAdapter
import com.example.mylibrary2.databinding.FragmentHomeBinding
import com.example.mylibrary2.model.Book
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase


class HomeFragment : Fragment() {
    private val bookCollectionRef =
        Firebase.firestore.collection("Books").orderBy("launchYear", Query.Direction.DESCENDING)
    private var booksItem: MutableList<Book> = ArrayList()
    private val bookRvAdapter: BookRvAdapter by lazy {
        BookRvAdapter(requireActivity())
    }

    private lateinit var binding: FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressBar.visibility = View.VISIBLE

        setUpRecycler()
        getBookListRealTime()

        binding.swipeContainer.setOnRefreshListener {
            getBookListRealTime()
            binding.swipeContainer.isRefreshing = false
        }
        binding.swipeContainer.setColorSchemeResources(R.color.baseColor)


        // region Change actionBar color
        val colorDrawable = ColorDrawable(Color.parseColor("#FFB300"))
        (activity as AppCompatActivity?)!!.supportActionBar!!.setBackgroundDrawable(colorDrawable)
        //endregion

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addBookFragment)
        }


    }

    private fun getBookListRealTime() {
        bookCollectionRef.addSnapshotListener { value, error ->
            error?.let {
                Log.w("TAG", "Listen failed.", error)
                return@addSnapshotListener
            }
            value?.let {
                booksItem.clear()
                for (document in it) {
                    val book = document.toObject<Book>()
                    booksItem.add(book)
                }
                booksItem.distinct()
                bookRvAdapter.setList(booksItem)
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun setUpRecycler() {
        val lm = LinearLayoutManager(requireActivity())
        binding.rvBooks.layoutManager = lm
        binding.rvBooks.adapter = bookRvAdapter
    }

}