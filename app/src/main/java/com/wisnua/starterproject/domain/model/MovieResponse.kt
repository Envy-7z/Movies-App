package com.wisnua.starterproject.domain.model
import com.google.gson.annotations.Expose

import android.os.Parcelable

import kotlinx.parcelize.Parcelize

import com.google.gson.annotations.SerializedName


@Parcelize
data class MovieResponse(
    @SerializedName("Response")
    @Expose
    var response: String? = null,
    @SerializedName("Search")
    @Expose
    var search: List<Search?>? = null,
    @SerializedName("totalResults")
    @Expose
    var totalResults: String? = null
) : Parcelable

@Parcelize
data class Search(
    @SerializedName("imdbID")
    @Expose
    var imdbID: String? = null,
    @SerializedName("Poster")
    @Expose
    var poster: String? = null,
    @SerializedName("Title")
    @Expose
    var title: String? = null,
    @SerializedName("Type")
    @Expose
    var type: String? = null,
    @SerializedName("Year")
    @Expose
    var year: String? = null
) : Parcelable