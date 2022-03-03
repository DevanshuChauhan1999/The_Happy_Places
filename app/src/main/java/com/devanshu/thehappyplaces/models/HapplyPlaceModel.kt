package com.devanshu.thehappyplaces.models

data class HapplyPlaceModel(
    val id : Int,
    val title : String,
    val image: String,
    val description: String,
    val date: String,
    val location: String,
    val latitude: Double,
    val longitude: Double
)