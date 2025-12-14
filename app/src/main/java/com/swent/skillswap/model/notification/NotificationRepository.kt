/** Created with the help of Cursor */
package com.swent.skillswap.model.notification

/**
 * Interface for the notification repository, which handles all the database operations related to
 * notifications.
 *
 * WARNING: This class does not handle error handling. Make sure to catch exceptions and handle them
 * appropriately in the UI.
 */
interface NotificationRepository {

    /**
     * Generates a new unique ID for a notification.
     *
     * @return A unique string identifier.
     */
    fun getNewUid(): String

    /**
     * Retrieves all notifications for a specific user.
     *
     * @param userId The ID of the user whose notifications to retrieve.
     * @return A list of notifications for the user, sorted by timestamp (newest first).
     * @throws Exception Database exceptions will be thrown.
     */
    suspend fun getNotificationsForUser(userId: String): List<Notification>

    /**
     * Retrieves unread notifications for a specific user.
     *
     * @param userId The ID of the user whose unread notifications to retrieve.
     * @return A list of unread notifications for the user, sorted by timestamp (newest first).
     * @throws Exception Database exceptions will be thrown.
     */
    suspend fun getUnreadNotificationsForUser(userId: String): List<Notification>

    /**
     * Retrieves a single notification by its ID.
     *
     * @param notificationId The unique identifier of the notification to retrieve.
     * @return The Notification object.
     * @throws Exception Database exceptions will be thrown.
     */
    suspend fun getNotification(notificationId: String): Notification

    /**
     * Adds a new notification to the database.
     *
     * @param notification The notification object to add.
     * @throws Exception Database exceptions will be thrown.
     */
    suspend fun addNotification(notification: Notification)

    /**
     * Marks a notification as read.
     *
     * @param notificationId The unique identifier of the notification to mark as read.
     * @throws Exception Database exceptions will be thrown.
     */
    suspend fun markAsRead(notificationId: String)

    /**
     * Marks all notifications for a user as read.
     *
     * @param userId The ID of the user whose notifications to mark as read.
     * @throws Exception Database exceptions will be thrown.
     */
    suspend fun markAllAsRead(userId: String)

    /**
     * Deletes a notification from the database.
     *
     * @param notificationId The unique identifier of the notification to delete.
     * @throws Exception Database exceptions will be thrown.
     */
    suspend fun deleteNotification(notificationId: String)

    /**
     * Deletes all notifications for a specific user.
     *
     * @param userId The ID of the user whose notifications to delete.
     * @throws Exception Database exceptions will be thrown.
     */
    suspend fun deleteAllNotificationsForUser(userId: String)

    /**
     * Marks all unread notifications for a user related to a specific chat as read.
     *
     * @param chatId The Id of the chat opened by the user
     * @param userId The Id of the user who opened the chat
     */
    suspend fun markChatNotificationsAsRead(chatId: String, userId: String)

    /**
     * Marks all unread post-related notifications for a user related to a specific post as read.
     *
     * @param postId The Id of the post being viewed by the user
     * @param userId The Id of the user viewing the post
     */
    suspend fun markPostNotificationsAsRead(postId: String, userId: String)
}
