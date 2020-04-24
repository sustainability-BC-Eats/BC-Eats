package com.example.bc_eats;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import android.os.Build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

public class CreatePostActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "Create_Post_Activity";

    //request codes
    private int REQUEST_CAMERA = 0;
    private int SELECT_FILE = 1;

    //Firebase db - holds data about each meal post
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myDatabaseRef;

    //Firebase storage - holds file image of each meal post
    private StorageReference storageRef;

    //Linking Firebase db to storage
    private String key;
    private String fileName;

    //Camera and Gallery
    private StorageTask mUploadTask;
    private Uri selectedImage;
    private String pictureFilePath;

    //Notifications
    String NOTIFICATION_CHANNEL_ID = "0101";
    String NOTIFICATION_CHANNEL_NAME = "bc-eats";

    //Widgets
    private EditText mTitle;
    private Spinner mBuildingSpinner;
    private EditText mRoom;
    private EditText mComment;
    private ImageView mImage;
    private Button mUploadButton;
    private Button mPostButton;
    private Button mCancelButton;
    private ProgressBar mProgressBar;
    private TextView mProgressText;

    private String mUserChosenTask;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        mContext = this;

        //initialize firebase db
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myDatabaseRef = mFirebaseDatabase.getReference("listings");

        //initialize firebase storage
        storageRef = FirebaseStorage.getInstance().getReference();

        //initialize notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW));
        }

        //initialize widgets
        mTitle = (EditText) findViewById(R.id.title);
        mBuildingSpinner = (Spinner) findViewById(R.id.spinner);
        mRoom = (EditText) findViewById(R.id.room_number_edit_text);
        mComment = (EditText) findViewById(R.id.comments_edit_text);
        mImage = (ImageView) findViewById(R.id.food_pic_img_view);
        mUploadButton = (Button) findViewById(R.id.upload_food_pic_button);
        mPostButton = (Button) findViewById(R.id.post_listing_button);
        mCancelButton = (Button) findViewById(R.id.post_cancel_button);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mProgressText = (TextView) findViewById(R.id.progressText);
        mProgressBar.setVisibility(View.GONE);

        //setup spinner widget
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext,
                R.array.spinner_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBuildingSpinner.setAdapter(adapter);
        mBuildingSpinner.setOnItemSelectedListener(this);

        //program upload button
        mUploadButton.setOnClickListener(view -> selectImage());

        //program post button
        mPostButton.setOnClickListener(view -> {
            uploadFood();
            if (mUploadTask != null && mUploadTask.isInProgress()) {
                Toast.makeText(mContext, "file is being uploaded, pls wait",
                        Toast.LENGTH_LONG).show();
            } else {
                uploadImage();

            }
            //sendNotificationToUsers();
            //subscribeToTopic("bc-eats");
        });

        //program cancel button
        mCancelButton.setOnClickListener(view -> finish());
    }

    /*****************************************SPINNER WIDGET**************************************************************************************************************/

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
        mBuildingSpinner.setSelection(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    /*****************************************CAMERA/ALBUM****************************************************************************************************************/

    //Open corresponding dialog boxes
    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Add Photo!");
        builder.setItems(items, (dialog, item) -> {
            boolean result = Utility.checkPermission(mContext);
            if (items[item].equals("Take Photo")) {
                mUserChosenTask = "Take Photo";
                if (result)
                    cameraIntent();
            } else if (items[item].equals("Choose from Library")) {
                mUserChosenTask = "Choose from Library";
                if (result)
                    galleryIntent();
            } else if (items[item].equals("Cancel")) {
                if (result)
                    dialog.dismiss();
            }
        });
        builder.show();
    }

    private void cameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
        if (cameraIntent.resolveActivity(mContext.getPackageManager()) != null) {
            File pictureFile = null;
            try {
                pictureFile = getPictureFile();
            } catch (IOException ex) {
                Toast.makeText(mContext,
                        "Photo file can't be created, please try again",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (pictureFile != null) {
                Uri photoURI = FileProvider.getUriForFile(mContext,
                        "com.example.bc_eats.fileprovider",
                        pictureFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, REQUEST_CAMERA);
            }
        }
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private File getPictureFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String pictureFile = "bc_eats_" + timeStamp;
        File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(pictureFile, ".jpg", storageDir);
        pictureFilePath = image.getAbsolutePath();
        return image;
    }

    private void addToGallery(Bitmap finalBitmap) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/food_images");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-" + n + ".jpg";
        Log.d(TAG, "Got here, fname: " + fname);
        File file = new File(myDir, fname);
        Log.d(TAG, "file created");
        if (file.exists())
            file.delete();
        try {
            Log.d(TAG, "1");
            FileOutputStream out = new FileOutputStream(file);
            Log.d(TAG, "2");
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            Log.d(TAG, "3");
            out.flush();
            out.close();
            Log.d(TAG, "done");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(TAG, "onRequestPermissionResult");
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (mUserChosenTask.equals("Take Photo")) {
                        cameraIntent();
                    } else if (mUserChosenTask.equals("Choose from Library")) {
                        galleryIntent();
                    }
                } else {
                    //code for deny
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)  //user gave permission to app to access camera and gallery
        {
            if (requestCode == SELECT_FILE) //user wants to select file from gallery
            {
                selectedImage = data.getData();
                mImage.setImageURI(selectedImage);
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), selectedImage);
                    mImage.setBackground(null);
                    mImage.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == REQUEST_CAMERA) //user wants to take a picture
            {
                Log.d(TAG, "What is the picture file path: "+ pictureFilePath);
                File imgFile = new File(pictureFilePath);
                if (imgFile.exists()) {
                    selectedImage = Uri.fromFile(imgFile); //return URI
                    mImage.setImageURI(Uri.fromFile(imgFile));
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), selectedImage);
                        mImage.setBackground(null);
                        mImage.setImageBitmap(bitmap);
                        addToGallery(bitmap); //only works if camera has SIM card (Internal storage)
                    } catch (IOException er) {
                        er.printStackTrace();
                    }
                }
            }
        }
    }

    /*****************************************FIREBASE STORAGE*********************************************************************************************************/

    //upload image to firebase storage
    private void uploadImage() {
        if (selectedImage != null) {
            fileName = key + ".jpg";

            mProgressBar.setVisibility(View.GONE);

            StorageReference ref = storageRef.child("images/" + fileName);
            ref.putFile(selectedImage)
                    .addOnSuccessListener(taskSnapshot -> {
                        mProgressBar.setVisibility(View.GONE);
                        Toast.makeText(mContext, "Uploaded", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        mProgressBar.setVisibility(View.GONE);
                        Toast.makeText(mContext, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener(taskSnapshot -> {
                        mProgressBar.setVisibility(View.VISIBLE);
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                .getTotalByteCount());
                        mProgressBar.setProgress((int) progress);
                        mProgressText.setText("Progress: " + (int) progress + "%");
                    });
        }
    }


    /*****************************************FIREBASE DATABASE*********************************************************************************************************/

    //add food posting to firebase realtime database
    private void uploadFood() {
        String notificationTitle = mTitle.getText().toString().trim();
        String building = mBuildingSpinner.getSelectedItem().toString().trim();
        String room = mRoom.getText().toString().trim();
        String comment = mComment.getText().toString().trim();

        //check if fields are empty
        if (TextUtils.isEmpty(notificationTitle)) {
            mTitle.setError("enter a title");
            mTitle.requestFocus();
        } else if (TextUtils.isEmpty(building)) {
            mBuildingSpinner.requestFocus();
        } else if (TextUtils.isEmpty(room)) {
            mTitle.setError("enter a room number");
            mTitle.requestFocus();
        } else if (TextUtils.isEmpty("comment")) {
            mTitle.setError("give a little description of whats available");
            mTitle.requestFocus();
        } else { //fields are not empty -> store to firebase rtdb
            key = myDatabaseRef.push().getKey();
            Food food = new Food(notificationTitle, building, room, comment, key);
            myDatabaseRef.child(key).setValue(food);
        }
    }


    /*****************************************NOTIFICATION*********************************************************************************************************/

    //INCOMPLETE!
    //TODO: App is able to receive notifications but cannot send them
    //TODO: App needs to create cloud function to handle upstream notification payload
    private void subscribeToTopic(String topic) {
        FirebaseMessaging.getInstance().
        subscribeToTopic(topic).
        addOnCompleteListener(task -> {
            String msg = "Successfully subscribed";
            if (!task.isSuccessful()) {
                msg = "Un-successfully subscribed";
            }
            Log.d(TAG, msg);
            Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
        });
    }
}

