// functions/index.js

// Import ONLY the main functions namespace
// We use 'firebase-functions/v1' because the function signature is V1 style.
const functions = require("firebase-functions/v1");

// Import admin
const admin = require("firebase-admin");
admin.initializeApp();

// NOTE: setGlobalOptions is a V2-only feature and has been removed.
// V1 settings (like maxInstances) must be applied using .runWith()

// V1-style Firestore trigger with maxInstances applied via runWith
exports.sendChatMessageNotification = functions
    // Apply maxInstances setting directly to the function
    .runWith({maxInstances: 10})
    .firestore
    .document("chats/{chatId}")
    .onUpdate(async (change, context) => {
      // --- Message Extraction and Validation ---
      const beforeMessages = change.before.get("messages") || [];
      const afterMessages = change.after.get("messages") || [];

      if (afterMessages.length <= beforeMessages.length) return;

      const newMessage = afterMessages[afterMessages.length - 1];
      if (!newMessage) return;

      let messageObj = newMessage;
      if (typeof newMessage === "string") {
        try {
          // Assuming messages might be stored as stringified JSON
          messageObj = JSON.parse(newMessage);
        } catch (e) {
          console.error("Failed to parse message string:", e);
          return;
        }
      }

      if (!messageObj || !messageObj.senderId || !messageObj.content) return;

      // --- Recipient Identification ---
      const participants = change.after.get("participants") || [];
      // Find the other participant who is not the sender
      const recipientId = participants.find((id) => id !== messageObj.senderId);
      if (!recipientId) return;

      // --- Recipient Token Lookup ---
      const users = admin.firestore().collection("users");
      const userDoc = await users.doc(recipientId).get();
      const userData = userDoc.data();

      if (!userData) {
        console.log(`User document not found for recipientId: ${recipientId}`);
        return;
      }

      const recipientToken = userData.fcmToken;
      if (!recipientToken) {
        console.log(`FCM token not found for recipientId: ${recipientId}`);
        return;
      }

      // --- FCM Payload Construction and Sending ---
      const payload = {
        notification: {
          title: "New Message",
          body: messageObj.content,
        },
        data: {
          chatId: context.params.chatId,
        },
      };

      try {
        await admin.messaging().sendToDevice(recipientToken, payload);
        console.log("Notification sent successfully to:", recipientId);
      } catch (error) {
        console.error("Error sending notification:", error);
      }
    });
