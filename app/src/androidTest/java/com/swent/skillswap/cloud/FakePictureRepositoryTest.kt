package com.swent.skillswap.cloud

import android.net.Uri
import com.swent.skillswap.model.images.FakePictureRepository
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/** AI-Generated unit tests for FakePictureRepository */
class FakePictureRepositoryTest {

    private lateinit var repository: FakePictureRepository
    private lateinit var mockUri: Uri

    @Before
    fun setup() {
        repository = FakePictureRepository()
        mockUri = Uri.parse("content://com.example.app/mockimage.jpg")
    }

    @After
    fun tearDown() {
        repository.clear()
    }

    @Test
    fun uploadPicture_success_returnsValidUrl() = runTest {
        // Arrange
        val uid = "test-uid-123"
        val path = "profile-pictures"

        // Act
        val result = repository.uploadPicture(uid, mockUri, path)

        // Assert
        Assert.assertNotNull(result)
        Assert.assertTrue(result.toString().contains(uid))
        Assert.assertTrue(result.toString().contains(path))
    }

    @Test
    fun uploadPicture_storesPictureInRepository() = runTest {
        // Arrange
        val uid = "test-uid-456"
        val path = "posts"

        // Act
        repository.uploadPicture(uid, mockUri, path)

        // Assert
        Assert.assertTrue(repository.isPictureUploaded(uid, path))
    }

    @Test
    fun uploadPicture_multipleUploads_storesAll() = runTest {
        // Arrange
        val uid1 = "uid-1"
        val uid2 = "uid-2"
        val path = "images"

        // Act
        repository.uploadPicture(uid1, mockUri, path)
        repository.uploadPicture(uid2, mockUri, path)

        // Assert
        Assert.assertEquals(2, repository.getUploadedPicturesCount())
        Assert.assertTrue(repository.isPictureUploaded(uid1, path))
        Assert.assertTrue(repository.isPictureUploaded(uid2, path))
    }

    @Test
    fun uploadPicture_whenShouldFailUpload_throwsException() = runTest {
        // Arrange
        repository.shouldFailUpload = true
        val uid = "test-uid"
        val path = "path"

        // Act & Assert
        try {
            repository.uploadPicture(uid, mockUri, path)
            Assert.fail("Expected exception was not thrown")
        } catch (e: Exception) {
            Assert.assertEquals("Upload failed - Test exception", e.message)
        }
    }

    @Test
    fun deletePicture_removesUploadedPicture() = runTest {
        // Arrange
        val uid = "test-uid-delete"
        val path = "images"
        repository.uploadPicture(uid, mockUri, path)
        Assert.assertTrue(repository.isPictureUploaded(uid, path))

        // Act
        repository.deletePicture(uid, path)

        // Assert
        Assert.assertFalse(repository.isPictureUploaded(uid, path))
    }

    @Test
    fun deletePicture_nonExistentPicture_doesNotThrowException() = runTest {
        // Arrange
        val uid = "non-existent"
        val path = "images"

        // Act & Assert - should not throw
        repository.deletePicture(uid, path)
    }

    @Test
    fun deletePicture_whenShouldFailDelete_throwsException() = runTest {
        // Arrange
        repository.shouldFailDelete = true
        val uid = "test-uid"
        val path = "path"

        // Act & Assert
        try {
            repository.deletePicture(uid, path)
            Assert.fail("Expected exception was not thrown")
        } catch (e: Exception) {
            Assert.assertEquals("Delete failed - Test exception", e.message)
        }
    }

    @Test
    fun getPictureUrl_existingPicture_returnsCorrectUrl() = runTest {
        // Arrange
        val uid = "test-uid-url"
        val path = "profile"
        val uploadedUrl = repository.uploadPicture(uid, mockUri, path)

        // Act
        val retrievedUrl = repository.getPictureUrl(uid, path)

        // Assert
        Assert.assertNotNull(retrievedUrl)
        Assert.assertEquals(uploadedUrl, retrievedUrl)
    }

    @Test
    fun getPictureUrl_nonExistentPicture_returnsNull() {
        // Arrange
        val uid = "non-existent"
        val path = "images"

        // Act
        val result = repository.getPictureUrl(uid, path)

        // Assert
        Assert.assertNull(result)
    }

    @Test
    fun isPictureUploaded_existingPicture_returnsTrue() = runTest {
        // Arrange
        val uid = "test-uid-check"
        val path = "images"
        repository.uploadPicture(uid, mockUri, path)

        // Act
        val result = repository.isPictureUploaded(uid, path)

        // Assert
        Assert.assertTrue(result)
    }

    @Test
    fun isPictureUploaded_nonExistentPicture_returnsFalse() {
        // Arrange
        val uid = "non-existent"
        val path = "images"

        // Act
        val result = repository.isPictureUploaded(uid, path)

        // Assert
        Assert.assertFalse(result)
    }

    @Test
    fun getUploadedPicturesCount_emptyRepository_returnsZero() {
        // Act
        val count = repository.getUploadedPicturesCount()

        // Assert
        Assert.assertEquals(0, count)
    }

    @Test
    fun getUploadedPicturesCount_afterUploads_returnsCorrectCount() = runTest {
        // Arrange
        repository.uploadPicture("uid1", mockUri, "path")
        repository.uploadPicture("uid2", mockUri, "path")
        repository.uploadPicture("uid3", mockUri, "path")

        // Act
        val count = repository.getUploadedPicturesCount()

        // Assert
        Assert.assertEquals(3, count)
    }

    @Test
    fun clear_removesAllPictures() = runTest {
        // Arrange
        repository.uploadPicture("uid1", mockUri, "path")
        repository.uploadPicture("uid2", mockUri, "path")
        Assert.assertEquals(2, repository.getUploadedPicturesCount())

        // Act
        repository.clear()

        // Assert
        Assert.assertEquals(0, repository.getUploadedPicturesCount())
    }

    @Test
    fun clear_resetsErrorFlags() {
        // Arrange
        repository.shouldFailUpload = true
        repository.shouldFailDelete = true

        // Act
        repository.clear()

        // Assert
        Assert.assertFalse(repository.shouldFailUpload)
        Assert.assertFalse(repository.shouldFailDelete)
    }

    @Test
    fun uploadPicture_samePath_differentUids_storesSeparately() = runTest {
        // Arrange
        val uid1 = "uid-1"
        val uid2 = "uid-2"
        val path = "same-path"

        // Act
        val url1 = repository.uploadPicture(uid1, mockUri, path)
        val url2 = repository.uploadPicture(uid2, mockUri, path)

        // Assert
        Assert.assertNotEquals(url1, url2)
        Assert.assertTrue(repository.isPictureUploaded(uid1, path))
        Assert.assertTrue(repository.isPictureUploaded(uid2, path))
        Assert.assertEquals(2, repository.getUploadedPicturesCount())
    }

    @Test
    fun uploadPicture_sameUid_differentPaths_storesSeparately() = runTest {
        // Arrange
        val uid = "same-uid"
        val path1 = "path-1"
        val path2 = "path-2"

        // Act
        val url1 = repository.uploadPicture(uid, mockUri, path1)
        val url2 = repository.uploadPicture(uid, mockUri, path2)

        // Assert
        Assert.assertNotEquals(url1, url2)
        Assert.assertTrue(repository.isPictureUploaded(uid, path1))
        Assert.assertTrue(repository.isPictureUploaded(uid, path2))
        Assert.assertEquals(2, repository.getUploadedPicturesCount())
    }
}
