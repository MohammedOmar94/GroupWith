const functions = require('firebase-functions');
let admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

/* Listens for new messages added to /messages/:pushId and sends a notification to subscribed users */
exports.chatroomPushNotification = functions.database.ref('/chatrooms/{groupId}/{pushId}').onWrite( event => {
console.log('Push notification event triggered ');
/* Grab the current value of what was written to the Realtime Database */
    var valueObject = event.data.val();
console.log('value Object is ' + JSON.stringify(valueObject));
console.log('event params ' + valueObject.imageUrl);
/* Create a notification and data payload. They contain the notification information, and message to be sent respectively */ 
    const payload = {
        notification: {
            title: valueObject.groupName,
            body: valueObject.messageUser + ": " + valueObject.messageText,
            tag: event.params.groupId,
            sound: "default"
        },
        data : {
            userid: valueObject.userId
        }
    };
/* Create an options object that contains the time to live for the notification and the priority. */
    const options = {
        priority: "high",
        timeToLive: 60 * 60 * 24 //24 hours
    };
return admin.messaging().sendToTopic(event.params.groupId, payload, options);
});

exports.joinRequestPushNotification = functions.database.ref('/users/{userId}/userRequest/{pushId}').onCreate( event => {
console.log('Push notification event triggered ');
/* Grab the current value of what was written to the Realtime Database */
    var valueObject = event.data.val();
/* Create a notification and data payload. They contain the notification information, and message to be sent respectively */ 
    const payload = {
        notification: {
            title: "Group Up",
            body: valueObject.username + " wants to join your group " + valueObject.groupName,
            tag: "joinRequest",
            sound: "default"
        }
    };
/* Create an options object that contains the time to live for the notification and the priority. */
    const options = {
        priority: "high",
        timeToLive: 60 * 60 * 24 //24 hours
    };
    // Sends to admin topic
return admin.messaging().sendToTopic(event.params.userId, payload, options);
});
 
 

