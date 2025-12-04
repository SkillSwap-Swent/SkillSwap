// functions/index.js

// Import ONLY the main functions namespace
// We use 'firebase-functions/v1' because the function signature is V1 style.
const functions = require("firebase-functions/v1");

// Import admin
const admin = require("firebase-admin");
admin.initializeApp();

// NOTE: setGlobalOptions is a V2-only feature and has been removed.
// V1 settings (like maxInstances) must be applied using .runWith()

// Firestore trigger to listen for new notifications
exports.sendNotificationOnCreate = functions.firestore
    .document("notifications/{notificationId}")
    .onCreate(async (snapshot, context) => {
      const notificationData = snapshot.data();

      // Validate notification data
      if (!notificationData || !notificationData.userId ||
          !notificationData.title || !notificationData.message) {
        console.error("Invalid notification data:", notificationData);
        return;
      }

      const {userId, title, message, type, relatedId} = notificationData;

      // Fetch the user's FCM token
      const userDoc = await admin.firestore()
          .collection("users").doc(userId).get();
      const userData = userDoc.data();

      if (!userData || !userData.fcmToken) {
        console.error(`FCM token not found for userId: ${userId}`);
        return;
      }

      const recipientToken = userData.fcmToken;

      // Construct the FCM payload
      const payload = {
        notification: {
          title: title,
          body: message,
        },
        data: {
          type: type,
          relatedId: relatedId || "",
        },
      };

      // Send the notification
      try {
        const multicastMessage = {
          tokens: [recipientToken],
          notification: payload.notification,
          data: payload.data,
        };
        await admin.messaging().sendEachForMulticast(multicastMessage);
        console.log("Notification sent successfully to userId:", userId);
      } catch (error) {
        console.error("Error sending notification:", error);
      }
    });
