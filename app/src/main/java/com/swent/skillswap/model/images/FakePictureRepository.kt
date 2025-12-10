package com.swent.skillswap.model.images

import android.net.Uri
import java.net.URL

/** AI generated class for testing purposes */
class FakePictureRepository : PictureRepositoryInterface {

    // Map to store uploaded pictures: key = "path/uid", value = URL
    private val uploadedPictures = mutableMapOf<String, URL>()

    // Variables to test error cases
    var shouldFailUpload = false
    var shouldFailDelete = false

    override suspend fun uploadPicture(uid: String, imageURI: Uri, path: String): URL {
        if (shouldFailUpload) {
            throw Exception("Upload failed - Test exception")
        }

        val key = "$path/$uid"
        val fakeUrl = URL("https://fake-storage.example.com/$path/$uid.jpg")
        uploadedPictures[key] = fakeUrl

        return fakeUrl
    }

    override suspend fun deletePicture(uid: String, path: String) {
        if (shouldFailDelete) {
            throw Exception("Delete failed - Test exception")
        }

        val key = "$path/$uid"
        uploadedPictures.remove(key)
    }

    // Utility methods for testing

    /**
     * Checks if a picture has been uploaded
     */
    fun isPictureUploaded(uid: String, path: String): Boolean {
        val key = "$path/$uid"
        return uploadedPictures.containsKey(key)
    }

    /**
     * Retrieves the URL of an uploaded picture
     */
    fun getPictureUrl(uid: String, path: String): URL? {
        val key = "$path/$uid"
        return uploadedPictures[key]
    }

    /**
     * Resets the repository (useful between tests)
     */
    fun clear() {
        uploadedPictures.clear()
        shouldFailUpload = false
        shouldFailDelete = false
    }

    /**
     * Returns the number of stored pictures
     */
    fun getUploadedPicturesCount(): Int = uploadedPictures.size
}