package com.swent.skillswap.model.images

import android.net.Uri
import java.net.URL

/**
 * Repository interface to manage picture uploads and retrieval, of pictures from a data source.
 * STORING POLICY : Pictures are stored under specific paths defined in CloudReferences. And have a
 * unique identifier as name. This uid should be guaranteed to be unique at least in the path scope.
 * That way we can easily manage and retrieve them.
 */
interface PictureRepositoryInterface {
    /**
     * Uploads a picture to the data source.
     *
     * @param uid The unique identifier for the picture.
     * @param imageURI The URI of the image to be uploaded.
     * @param path The storage path where the picture will be uploaded.
     * @return The public URL of the uploaded picture.
     */
    suspend fun uploadPicture(uid: String, imageURI: Uri, path: String): URL

    /**
     * Deletes a picture from the data source.
     *
     * @param uid The unique identifier for the picture.
     * @param path The storage path where the picture is located.
     */
    suspend fun deletePicture(uid: String, path: String)
}
