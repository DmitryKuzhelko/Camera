package kuzhelko.dmitry.mycamera.data

import java.io.File
import javax.inject.Inject

class StorageImpl
@Inject constructor(private val storage: File) : Storage {

    override fun getLastPhoto() = storage.getLastPhoto()

    override fun isEmpty() = storage.checkStorageState()

    override fun getAllPhotos() = storage.getAllPhotos()

    override fun deletePhoto(id: Int) {
        storage.deletePhoto(id)
    }

    override fun getPathById(id: Int) = storage.getPathById(id)
}