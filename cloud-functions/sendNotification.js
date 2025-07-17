const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

/**
 * Send notification to a specific user
 * @param {string} userId - The user ID to send notification to
 * @param {string} title - Notification title
 * @param {string} message - Notification message
 * @param {string} type - Notification type (match, transfer, general, system)
 * @param {Object} data - Additional data for deep linking
 */
exports.sendNotificationToUser = functions.https.onCall(async (data, context) => {
    // Check if user is authenticated
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }

    const { userId, title, message, type = 'general', data: notificationData = {} } = data;

    try {
        // Get user's FCM token from Firestore
        const userDoc = await admin.firestore().collection('users').doc(userId).get();
        
        if (!userDoc.exists) {
            throw new functions.https.HttpsError('not-found', 'User not found');
        }

        const userData = userDoc.data();
        const fcmToken = userData.fcmToken;
        const notificationEnabled = userData.notificationEnabled !== false; // Default to true

        if (!fcmToken || !notificationEnabled) {
            return { success: false, message: 'User has notifications disabled or no FCM token' };
        }

        // Prepare notification payload - use only data payload for background handling
        const payload = {
            data: {
                type: type,
                title: title,
                message: message,
                timestamp: Date.now().toString(),
                userId: userId, // Include user ID in notification data
                user_id: userId, // Alternative key for compatibility
                ...notificationData
            },
            token: fcmToken
        };

        // Send notification
        const response = await admin.messaging().send(payload);

        // Save notification to Firestore
        await admin.firestore()
            .collection('users')
            .doc(userId)
            .collection('notifications')
            .add({
                title: title,
                message: message,
                timestamp: admin.firestore.FieldValue.serverTimestamp(),
                isRead: false,
                type: type,
                data: notificationData
            });

        return { success: true, messageId: response };

    } catch (error) {
        console.error('Error sending notification:', error);
        throw new functions.https.HttpsError('internal', 'Error sending notification');
    }
});

/**
 * Send notification to multiple users
 * @param {Array} userIds - Array of user IDs
 * @param {string} title - Notification title
 * @param {string} message - Notification message
 * @param {string} type - Notification type
 * @param {Object} data - Additional data
 */
exports.sendNotificationToMultipleUsers = functions.https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }

    const { userIds, title, message, type = 'general', data: notificationData = {} } = data;

    try {
        // Get FCM tokens for all users
        const userDocs = await admin.firestore()
            .collection('users')
            .where(admin.firestore.FieldPath.documentId(), 'in', userIds)
            .get();

        const tokens = [];
        const validUserIds = [];

        userDocs.forEach(doc => {
            const userData = doc.data();
            if (userData.fcmToken && userData.notificationEnabled !== false) {
                tokens.push(userData.fcmToken);
                validUserIds.push(doc.id);
            }
        });

        if (tokens.length === 0) {
            return { success: false, message: 'No valid users found with notifications enabled' };
        }

        // Send to multiple tokens
        const response = await admin.messaging().sendMulticast({
            tokens: tokens,
            data: {
                type: type,
                title: title,
                message: message,
                timestamp: Date.now().toString(),
                userIds: validUserIds.join(','), // Include user IDs for multicast
                user_ids: validUserIds.join(','), // Alternative key for compatibility
                ...notificationData
            }
        });

        // Save notifications to Firestore for each user
        const batch = admin.firestore().batch();
        validUserIds.forEach(userId => {
            const notificationRef = admin.firestore()
                .collection('users')
                .doc(userId)
                .collection('notifications')
                .doc();

            batch.set(notificationRef, {
                title: title,
                message: message,
                timestamp: admin.firestore.FieldValue.serverTimestamp(),
                isRead: false,
                type: type,
                data: notificationData
            });
        });

        await batch.commit();

        return {
            success: true,
            successCount: response.successCount,
            failureCount: response.failureCount
        };

    } catch (error) {
        console.error('Error sending notifications:', error);
        throw new functions.https.HttpsError('internal', 'Error sending notifications');
    }
});

/**
 * Send notification to topic subscribers
 * @param {string} topic - Topic name
 * @param {string} title - Notification title
 * @param {string} message - Notification message
 * @param {string} type - Notification type
 * @param {Object} data - Additional data
 */
exports.sendNotificationToTopic = functions.https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }

    const { topic, title, message, type = 'general', data: notificationData = {} } = data;

    try {
        const response = await admin.messaging().send({
            topic: topic,
            data: {
                type: type,
                title: title,
                message: message,
                timestamp: Date.now().toString(),
                ...notificationData
            }
        });

        return { success: true, messageId: response };

    } catch (error) {
        console.error('Error sending topic notification:', error);
        throw new functions.https.HttpsError('internal', 'Error sending topic notification');
    }
});

/**
 * Trigger notification when a new transfer request is created
 */
exports.onTransferRequestCreated = functions.firestore
    .document('transfer_requests/{requestId}')
    .onCreate(async (snap, context) => {
        const requestData = snap.data();
        
        if (!requestData) return;

        try {
            // Find potential matches for this request
            const potentialMatches = await admin.firestore()
                .collection('transfer_requests')
                .where('currentDistrict', 'in', requestData.preferredDistricts)
                .where('teacherId', '!=', requestData.teacherId)
                .get();

            const userIds = [];
            potentialMatches.forEach(doc => {
                userIds.push(doc.data().teacherId);
            });

            if (userIds.length > 0) {
                // Send notification to potential matches
                await admin.messaging().sendMulticast({
                    tokens: userIds,
                    notification: {
                        title: 'New Transfer Request',
                        body: `A new transfer request has been created in ${requestData.currentDistrict}`
                    },
                    data: {
                        type: 'transfer_request',
                        requestId: context.params.requestId
                    }
                });
            }

        } catch (error) {
            console.error('Error sending transfer request notifications:', error);
        }
    });

/**
 * Trigger notification when a match is found
 */
exports.onMatchFound = functions.firestore
    .document('matches/{matchId}')
    .onCreate(async (snap, context) => {
        const matchData = snap.data();
        
        if (!matchData) return;

        try {
            // Send notification to both teachers
            const teacherIds = [matchData.teacher1Id, matchData.teacher2Id];
            
            await admin.messaging().sendMulticast({
                tokens: teacherIds,
                notification: {
                    title: 'New Match Found!',
                    body: 'A compatible transfer partner has been found for you'
                },
                data: {
                    type: 'match',
                    matchId: context.params.matchId
                }
            });

        } catch (error) {
            console.error('Error sending match notifications:', error);
        }
    }); 