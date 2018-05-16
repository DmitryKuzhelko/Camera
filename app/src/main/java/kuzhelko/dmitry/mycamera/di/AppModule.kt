package kuzhelko.dmitry.mycamera.di

import android.content.Context
import android.os.Environment
import dagger.Module
import dagger.Provides
import kuzhelko.dmitry.mycamera.data.Storage
import kuzhelko.dmitry.mycamera.data.StorageImpl
import kuzhelko.dmitry.mycamera.data.getExternalStorage
import kuzhelko.dmitry.mycamera.data.getInternalStorage
import java.io.File
import javax.inject.Singleton

@Module
class AppModule {

    @Singleton
    @Provides
    fun provideAvailableStorage(context: Context): File {
        val storageDir = when (Environment.getExternalStorageState()) {
            Environment.MEDIA_MOUNTED -> getExternalStorage()
            else -> context.getInternalStorage()
        }
        if (!storageDir.exists()) {
            storageDir.mkdir()
        }
        return storageDir
    }

    @Singleton
    @Provides
    fun provideStorage(availableStorage: File): Storage = StorageImpl(availableStorage)
}