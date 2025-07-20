const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

const db = admin.firestore();

/**
 * Send notification to a single user
 */
exports.sendNotification = functions.https.onCall(async (data, context) => {
    try {
        const { userId, title, body, data: notificationData, type = "general" } = data;
        
        // Get user's FCM token
        const userDoc = await db.collection('users').doc(userId).get();
        if (!userDoc.exists) {
            throw new Error('User not found');
        }
        
        const userData = userDoc.data();
        const fcmToken = userData.fcmToken;
        
        if (!fcmToken) {
            throw new Error('FCM token not found for user');
        }
        
        // Prepare message payload
        const message = {
            token: fcmToken,
            data: {
                title: title,
                body: body,
                messageType: type,
                timestamp: Date.now().toString(),
                ...notificationData
            },
            android: {
                priority: 'high',
                notification: {
                    title: title,
                    body: body,
                    sound: 'default',
                    priority: 'high',
                    channelId: 'high_importance_channel'
                }
            },
            apns: {
                payload: {
                    aps: {
                        alert: {
                            title: title,
                            body: body
                        },
                        sound: 'default',
                        badge: 1
                    }
                }
            }
        };
        
        // Send the message
        const response = await admin.messaging().send(message);
        console.log('Successfully sent message:', response);
        
        return { success: true, messageId: response };
        
    } catch (error) {
        console.error('Error sending notification:', error);
        throw new functions.https.HttpsError('internal', error.message);
    }
});

/**
 * Send notification to multiple users
 */
exports.sendMulticastNotification = functions.https.onCall(async (data, context) => {
    try {
        const { userIds, title, body, data: notificationData, type = "general" } = data;
        
        // Get FCM tokens for all users
        const userDocs = await Promise.all(
            userIds.map(userId => db.collection('users').doc(userId).get())
        );
        
        const tokens = userDocs
            .filter(doc => doc.exists)
            .map(doc => doc.data().fcmToken)
            .filter(token => token);
        
        if (tokens.length === 0) {
            throw new Error('No valid FCM tokens found');
        }
        
        // Prepare message payload
        const message = {
            tokens: tokens,
            data: {
                title: title,
                body: body,
                messageType: type,
                timestamp: Date.now().toString(),
                userIds: userIds.join(','),
                ...notificationData
            },
            android: {
                priority: 'high',
                notification: {
                    title: title,
                    body: body,
                    sound: 'default',
                    priority: 'high',
                    channelId: 'high_importance_channel'
                }
            },
            apns: {
                payload: {
                    aps: {
                        alert: {
                            title: title,
                            body: body
                        },
                        sound: 'default',
                        badge: 1
                    }
                }
            }
        };
        
        // Send the message
        const response = await admin.messaging().sendMulticast(message);
        console.log('Successfully sent multicast message:', response);
        
        return { 
            success: true, 
            successCount: response.successCount,
            failureCount: response.failureCount
        };
        
    } catch (error) {
        console.error('Error sending multicast notification:', error);
        throw new functions.https.HttpsError('internal', error.message);
    }
});

/**
 * Send notification to a topic
 */
exports.sendTopicNotification = functions.https.onCall(async (data, context) => {
    try {
        const { topic, title, body, data: notificationData, type = "general" } = data;
        
        // Prepare message payload
        const message = {
            topic: topic,
            data: {
                title: title,
                body: body,
                messageType: type,
                timestamp: Date.now().toString(),
                topic: topic,
                ...notificationData
            },
            android: {
                priority: 'high',
                notification: {
                    title: title,
                    body: body,
                    sound: 'default',
                    priority: 'high',
                    channelId: 'high_importance_channel'
                }
            },
            apns: {
                payload: {
                    aps: {
                        alert: {
                            title: title,
                            body: body
                        },
                        sound: 'default',
                        badge: 1
                    }
                }
            }
        };
        
        // Send the message
        const response = await admin.messaging().send(message);
        console.log('Successfully sent topic message:', response);
        
        return { success: true, messageId: response };
        
    } catch (error) {
        console.error('Error sending topic notification:', error);
        throw new functions.https.HttpsError('internal', error.message);
    }
});

/**
 * Send app update notification
 */
exports.sendAppUpdateNotification = functions.https.onCall(async (data, context) => {
    try {
        const { 
            userIds, 
            title, 
            body, 
            version, 
            isForceUpdate = false, 
            forceUpdateVersion = null,
            type = "app_update" 
        } = data;
        
        // Get FCM tokens for all users
        const userDocs = await Promise.all(
            userIds.map(userId => db.collection('users').doc(userId).get())
        );
        
        const tokens = userDocs
            .filter(doc => doc.exists)
            .map(doc => doc.data().fcmToken)
            .filter(token => token);
        
        if (tokens.length === 0) {
            throw new Error('No valid FCM tokens found');
        }
        
        // Prepare app update message payload
        const message = {
            tokens: tokens,
            data: {
                title: title,
                body: body,
                messageType: type,
                isAppUpdate: "true",
                isForceUpdate: isForceUpdate.toString(),
                version: version,
                forceUpdateVersion: forceUpdateVersion || version,
                timestamp: Date.now().toString(),
                userIds: userIds.join(','),
                // Additional data for the app
                notification_action: "navigate_to_updates"
            },
            android: {
                priority: 'high',
                notification: {
                    title: title,
                    body: body,
                    sound: 'default',
                    priority: 'high',
                    channelId: 'high_importance_channel'
                }
            },
            apns: {
                payload: {
                    aps: {
                        alert: {
                            title: title,
                            body: body
                        },
                        sound: 'default',
                        badge: 1
                    }
                }
            }
        };
        
        // Send the message
        const response = await admin.messaging().sendMulticast(message);
        console.log('Successfully sent app update notification:', response);
        
        return { 
            success: true, 
            successCount: response.successCount,
            failureCount: response.failureCount
        };
        
    } catch (error) {
        console.error('Error sending app update notification:', error);
        throw new functions.https.HttpsError('internal', error.message);
    }
});

/**
 * Send app update notification to a topic
 */
exports.sendAppUpdateTopicNotification = functions.https.onCall(async (data, context) => {
    try {
        const { 
            topic, 
            title, 
            body, 
            version, 
            isForceUpdate = false, 
            forceUpdateVersion = null,
            type = "app_update" 
        } = data;
        
        // Prepare app update message payload
        const message = {
            topic: topic,
            data: {
                title: title,
                body: body,
                messageType: type,
                isAppUpdate: "true",
                isForceUpdate: isForceUpdate.toString(),
                version: version,
                forceUpdateVersion: forceUpdateVersion || version,
                timestamp: Date.now().toString(),
                topic: topic,
                // Additional data for the app
                notification_action: "navigate_to_updates"
            },
            android: {
                priority: 'high',
                notification: {
                    title: title,
                    body: body,
                    sound: 'default',
                    priority: 'high',
                    channelId: 'high_importance_channel'
                }
            },
            apns: {
                payload: {
                    aps: {
                        alert: {
                            title: title,
                            body: body
                        },
                        sound: 'default',
                        badge: 1
                    }
                }
            }
        };
        
        // Send the message
        const response = await admin.messaging().send(message);
        console.log('Successfully sent app update topic notification:', response);
        
        return { success: true, messageId: response };
        
    } catch (error) {
        console.error('Error sending app update topic notification:', error);
        throw new functions.https.HttpsError('internal', error.message);
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