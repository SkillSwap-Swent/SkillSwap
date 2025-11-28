package com.swent.skillswap.model.images

import android.net.Uri
import android.util.Log
import com.swent.skillswap.firebase.CloudReferences
import java.net.URL
import kotlinx.coroutines.tasks.await

/**
 * Repository class to manage picture uploads and retrieval, from firebase storage. Extends
 * PictureRepositoryInterface.
 *
 * @param storage The Firebase Storage instance used for picture operations.
 *
 * The Images are stored in Firebase Storage under specific paths defined in CloudReferences. And
 * have a unique identifier as name. That way we can easily manage and retrieve them.
 */
class PictureRepository(private val storage: com.google.firebase.storage.FirebaseStorage) :
    PictureRepositoryInterface {

    override suspend fun uploadPicture(uid: String, imageURI: Uri, path: String): URL {
        /** Preconditions */
        /** Note : Storage can only handle these formats */
        require(
            imageURI.toString().startsWith("content://") ||
                imageURI.toString().startsWith("file://")
        )
        require(CloudReferences.values.contains(path))

        /** Upload the picture to Firebase Storage */
        val fileName = imageURI.path!!.substringAfterLast('/')
        val storageRef = storage.reference.child("$path/${uid}")
        storageRef.putFile(imageURI).await()

        val downloadUri = storageRef.downloadUrl.await()

        /** Return the download URL or throw exeption */
        return URL(downloadUri.toString())
    }

    override suspend fun deletePicture(uid: String, path: String) {
        /** Preconditions */
        require(CloudReferences.values.contains(path))
        require(uid.isNotBlank())

        /** Delete the picture from Firebase Storage */
        try {
            val storageRef = storage.reference.child("${path}/${uid}")
            storageRef.delete().await()
        }catch (e: Exception) {
            if(e is com.google.firebase.storage.StorageException){
                /** exception likely be thrown because picture does not exist, nothing to delete */
                return
            } else {
                /** Other error occurred, rethrow the exception */
                throw e
            }
        }
    }
}
