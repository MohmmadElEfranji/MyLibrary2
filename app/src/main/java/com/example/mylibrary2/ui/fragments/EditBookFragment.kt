package com.example.mylibrary2.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.mylibrary2.R
import com.example.mylibrary2.databinding.FragmentEditBookBinding
import com.example.mylibrary2.model.Book
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.shasin.notificationbanner.Banner
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


class EditBookFragment : Fragment() {
    private lateinit var books: Book
    private val bookCollectionRef = Firebase.firestore.collection("Books")
    private var mRate: Float = 1.5f
    private var curFile: Uri? = null

    // Instance of FirebaseStorage
    private val storage = Firebase.storage

    // Points to the root reference [Root]
    private var storageRef = storage.reference
    private lateinit var binding: FragmentEditBookBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentEditBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // region Change actionBar color
        val colorDrawable = ColorDrawable(Color.parseColor("#FFB300"))
        (activity as AppCompatActivity?)!!.supportActionBar!!.setBackgroundDrawable(colorDrawable)
        //endregion

        //region Fill in the book information
        val arg = this.arguments
        books = arg?.getParcelable("books")!!
        binding.edNewBookName.setText(books.bookName)
        binding.edNewBookAuthor.setText(books.bookAuthor)
        binding.edNewPrice.setText(books.price.toString())
        binding.rbNewRatingBar.rating = books.rate!!
        val milliSeconds = books.launchYear?.time!!
        val mLaunchYear = DateFormat.format("yyyy", Date(milliSeconds)).toString()
        binding.edNewLaunchYear.setText(mLaunchYear)
        //endregion

        val resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent: Intent? = result.data
                    val uri = intent?.data
                    uri?.let {
                        curFile = it
                    }

                    binding.edBookCover.setText(getString(R.string.imageSelected))
                }
            }

        binding.edBookCover.setOnClickListener {
            val intent = Intent()
                .setType("image/*")
                .setAction(Intent.ACTION_GET_CONTENT)
            resultLauncher.launch(Intent.createChooser(intent, "Select a file"))


        }

        //region Book review
        binding.rbNewRatingBar.stepSize = .5f
        binding.rbNewRatingBar.setOnRatingBarChangeListener { _, rating, _ ->
            mRate = rating
        }
        //endregion

        //region Update Button
        binding.btnEditBook.setOnClickListener {
            val mNewBookMap = getNewBooksData()

            MaterialAlertDialogBuilder(
                requireActivity(),
                R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog

            )
                .setTitle("Edit")
                .setMessage("Do you want to Edit this book?")
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("Yes") { _, _ ->
                    updateBookDate(mNewBookMap)
                }
                .setIcon(R.drawable.ic_edit_book)

                .show()


        }
        //endregion

        //region Delete Button
        binding.btnDeleteBook.setOnClickListener {
            MaterialAlertDialogBuilder(
                requireActivity(),
                R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog
            )
                .setTitle("Delete")
                .setMessage("Do you want to delete this book?")
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("Yes") { _, _ ->
                    binding.progressBar.visibility = View.VISIBLE
                    deleteBookDate()
                }
                .setIcon(R.drawable.ic_delete_book)

                .show()
        }
        //endregion
    }


    private fun getNewBooksData(): Map<String, Any> {


        val idBook = books.bookId
        val bookName = binding.edNewBookName.text.toString()
        val bookAuthor = binding.edNewBookAuthor.text.toString()
        val launchYear = binding.edNewLaunchYear.text.toString()
        val formatter = SimpleDateFormat("yyyy", Locale.UK)
        val mLaunchYear = formatter.parse(launchYear) as Date
        val price = binding.edNewPrice.text.toString()
        val rate = mRate
        val bookImage = binding.edBookCover.text.toString()

        val map = mutableMapOf<String, Any>()

        if (bookImage == getString(R.string.imageSelected)) {
            val file = getFile(requireContext(), curFile!!)
            val stream = FileInputStream(file)
            val uploadTask = storageRef.child("images/${books.bookImage}").putStream(stream)

            uploadTask.addOnFailureListener { e ->
                Log.d("sss", "Fail ! ${e.message}")
            }.addOnSuccessListener { taskSnapshot ->
                Log.d("sss", "Done ! ${taskSnapshot.metadata?.sizeBytes}")

            }
        }
        if (bookName.isNotEmpty() && bookAuthor.isNotEmpty() && launchYear.isNotEmpty() && price.isNotEmpty()) {
            map["bookId"] = idBook
            map["bookName"] = bookName
            map["bookAuthor"] = bookAuthor
            map["launchYear"] = mLaunchYear
            map["price"] = price.toDouble()
            map["rate"] = rate

        } else {
            Banner.make(
                binding.root, requireActivity(), Banner.WARNING,
                "Fill in all fields !!", Banner.TOP, 3000
            ).show()
        }
        return map
    }

    private fun deleteBookDate() {

        bookCollectionRef.document(books.bookId).delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                storageRef.child("images/${books.bookImage}").delete()
                Banner.make(
                    binding.root, requireActivity(), Banner.SUCCESS,
                    "Delete succeeded:)", Banner.TOP, 3000
                ).show()

                val navOptions =
                    NavOptions.Builder().setPopUpTo(R.id.homeFragment, true).build()
                findNavController().navigate(
                    R.id.action_editBookFragment_to_homeFragment,
                    null,
                    navOptions
                )

            }
        }.addOnFailureListener {
            Toast.makeText(requireActivity(), "${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateBookDate(newBookMap: Map<String, Any>) {

        bookCollectionRef.whereEqualTo("bookId", books.bookId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result!!.documents.isNotEmpty()) {
                        for (documents in task.result!!.documents) {
                            bookCollectionRef.document(documents.id).set(
                                newBookMap, SetOptions.merge()
                            )
                        }
                    } else {
                        Banner.make(
                            binding.root, requireActivity(), Banner.ERROR,
                            "Edit failed :(", Banner.TOP, 3000
                        ).show()
                    }
                    Banner.make(
                        binding.root, requireActivity(), Banner.SUCCESS,
                        "Edit succeeded:)", Banner.TOP, 3000
                    ).show()

                    val navOptions =
                        NavOptions.Builder().setPopUpTo(R.id.homeFragment, true).build()
                    findNavController().navigate(
                        R.id.action_editBookFragment_to_homeFragment,
                        null,
                        navOptions
                    )
                    binding.progressBar.visibility = View.GONE
                }
            }.addOnFailureListener {

                Toast.makeText(requireActivity(), "${it.message}", Toast.LENGTH_SHORT).show()

            }


    }

    private fun getFile(context: Context, uri: Uri): File {
        val destinationFilename =
            File(context.filesDir.path + File.separatorChar + queryName(context, uri))
        try {
            context.contentResolver.openInputStream(uri).use { ins ->
                createFileFromStream(
                    ins!!,
                    destinationFilename
                )
            }
        } catch (ex: Exception) {
            // Log.e("Save File", ex.message!!)
            ex.printStackTrace()
        }
        return destinationFilename
    }


    private fun createFileFromStream(ins: InputStream, destination: File?) {
        try {
            FileOutputStream(destination).use { os ->
                val buffer = ByteArray(4096)
                var length: Int
                while (ins.read(buffer).also { length = it } > 0) {
                    os.write(buffer, 0, length)
                }
                os.flush()
            }
        } catch (ex: Exception) {
            // Log.e("Save File", ex.message!!)
            ex.printStackTrace()
        }
    }

    private fun queryName(context: Context, uri: Uri): String {
        val returnCursor: Cursor = context.contentResolver.query(uri, null, null, null, null)!!
        val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name: String = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(
            true // default to enabled
        ) {
            override fun handleOnBackPressed() {

                val navOptions = NavOptions.Builder().setPopUpTo(R.id.homeFragment, true).build()

                findNavController().navigate(
                    R.id.action_editBookFragment_to_homeFragment,
                    null,
                    navOptions
                )
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this,  // LifecycleOwner
            callback
        )
    }
}