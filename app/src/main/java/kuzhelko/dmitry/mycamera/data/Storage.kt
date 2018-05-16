package kuzhelko.dmitry.mycamera.data

import java.util.*

interface Storage {

    fun getAllPhotos(): ArrayList<String>
    fun deletePhoto(id: Int)
    fun getPathById(id: Int): String?
    fun isEmpty(): Boolean
    fun getLastPhoto(): String
}