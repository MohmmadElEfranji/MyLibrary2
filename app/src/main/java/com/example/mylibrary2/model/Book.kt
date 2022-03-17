package com.example.mylibrary2.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*


@Parcelize
data class Book(
    var bookId: String = "",
    var bookImage: String? = "",
    var bookName: String? = null,
    var bookAuthor: String? = null,
    var launchYear: Date? = Date(),
    var price: Double? = null,
    var rate: Float? = null
) : Parcelable
