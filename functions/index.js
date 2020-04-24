const functions = require('firebase-functions');
const admin = require('firebase-admin');

// const Vision = require('@google-cloud/vision');
// const vision = new Vision();
// const spawn = require('child-process-promise').spawn;

// const path = require('path');
// const os = require('os');
// const fs = require('fs');

admin.initializeApp(functions.config().firebase);

// // Checks if uploaded images are flagged as Adult or Violence and if so blurs them.
// exports.blurOffensiveImages = functions.runWith({memory: '2GB'}).storage.object().onFinalize(
//     async (object) => {
//       const image = {
//         source: {imageUri: `gs://${object.bucket}/${object.name}`},
//       };

//       // Check the image content using the Cloud Vision API.
//       const batchAnnotateImagesResponse = await vision.safeSearchDetection(image);
//       const safeSearchResult = batchAnnotateImagesResponse[0].safeSearchAnnotation;
//       const Likelihood = Vision.types.Likelihood;
//       if (Likelihood[safeSearchResult.adult] >= Likelihood.LIKELY ||
//           Likelihood[safeSearchResult.violence] >= Likelihood.LIKELY) {
//         console.log('The image', object.name, 'has been detected as inappropriate.');
//         return blurImage(object.name);
//       }
//       console.log('The image', object.name, 'has been detected as OK.');
//     });

//     // Blurs the given image located in the given bucket using ImageMagick.
// async function blurImage(filePath) {
//     const tempLocalFile = path.join(os.tmpdir(), path.basename(filePath));
//     const messageId = filePath.split(path.sep)[1];
//     const bucket = admin.storage().bucket();
  
//     // Download file from bucket.
//     await bucket.file(filePath).download({destination: tempLocalFile});
//     console.log('Image has been downloaded to', tempLocalFile);
//     // Blur the image using ImageMagick.
//     await spawn('convert', [tempLocalFile, '-channel', 'RGBA', '-blur', '0x24', tempLocalFile]);
//     console.log('Image has been blurred');
//     // Uploading the Blurred image back into the bucket.
//     await bucket.upload(tempLocalFile, {destination: filePath});
//     console.log('Blurred image has been uploaded to', filePath);
//     // Deleting the local file to free up disk space.
//     fs.unlinkSync(tempLocalFile);
//     console.log('Deleted local file.');
//     // Indicate that the message has been moderated.
//     await admin.firestore().collection('messages').doc(messageId).update({moderated: true});
//     console.log('Marked the image as moderated in the database.');
//   }

//Now we're going to create a function that listens to when a 'Notifications' node changes and send a notificcation
//to all devices subscribed to a topic
exports.sendNotification = functions.database.ref("Notifications/{uid}")
.onWrite(event => {
    //This will be the notification model that we push to firebase
    var request = event.data.val();

    var payload = {
        data:{
            title: request.notificationTitle,
            body: request.notificationBody,
            sound: "default"
        }
    };

    //Create an options object that contains the time to live for the notification and the priority
    const options = {
        priority: "high",
        timeToLive: 60 * 60 * 24
    };

    //The topic variable can be anything from a username, to a uid
    //I find this approach much better than using the refresh token
    //as you can subscribe to someone's phone number, username, or some other unique identifier
    //to communicate between
    //Now let's move onto the code, but before that, let's push this to firebase

    admin.messaging().sendToTopic(request.topic, payload, options)
    .then((response) => {
        console.log("Successfully sent message: ", response);
        return true;
    })
    .catch((error) => {
        console.log("Error sending message: ", error);
        return false;
    })
})


// // Cleans up the tokens that are no longer valid.
// function cleanupTokens(response, tokens) {
//     // For each notification we check if there was an error.
//     const tokensDelete = [];
//     response.results.forEach((result, index) => {
//       const error = result.error;
//       if (error) {
//         console.error('Failure sending notification to', tokens[index], error);
//         // Cleanup the tokens who are not registered anymore.
//         if (error.code === 'messaging/invalid-registration-token' ||
//             error.code === 'messaging/registration-token-not-registered') {
//           const deleteTask = admin.firestore().collection('messages').doc(tokens[index]).delete();
//           tokensDelete.push(deleteTask);
//         }
//       }
//     });
//     return Promise.all(tokensDelete);
//    }