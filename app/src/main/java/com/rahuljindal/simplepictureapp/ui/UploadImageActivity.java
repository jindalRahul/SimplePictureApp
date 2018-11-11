package com.rahuljindal.simplepictureapp.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rahuljindal.simplepictureapp.Constants.Constants;
import com.rahuljindal.simplepictureapp.GlideApp;
import com.rahuljindal.simplepictureapp.HelperClass.MyProgressDialog;
import com.rahuljindal.simplepictureapp.Modals.CompressedImageDataModal;
import com.rahuljindal.simplepictureapp.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import id.zelory.compressor.Compressor;

public class UploadImageActivity extends AppCompatActivity {

    private static final int REQUEST_MEDIA = 67;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.add_image_btn)
    ImageView addImageBtn;
    @BindView(R.id.add_discription_txt)
    EditText addDiscriptionTxt;
    @BindView(R.id.submit_btn)
    Button submitBtn;
    private CompressedImageDataModal compressedImage;
    private MyProgressDialog progressDialog;
    private FirebaseStorage firebaseStorage;
    private FirebaseFirestore firestoreDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);
        ButterKnife.bind(this);
        progressConfigurations();
        firebaseStorage=FirebaseStorage.getInstance();
        firestoreDb = FirebaseFirestore.getInstance();
    }

    @OnClick(R.id.add_image_btn)
    public void onAddImageBtnClicked() {
        ImagePicker.create(this)
                .toolbarImageTitle("Tap to select") // image selection title
                .toolbarArrowColor(Color.WHITE) // Toolbar 'up' arrow color// single mode
                .single() // multi mode (default mode)
                .limit(1) // max images can be selected (99 by default)
                .theme(R.style.Pickertheme) // must inherit ef_BaseTheme. please refer to sample
                .start(REQUEST_MEDIA);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA) {
            if (resultCode == RESULT_OK) {

                List<Image> images = ImagePicker.getImages(data);


                if(images.size()>0){
                    CropImage.activity(Uri.parse("file://" +images.get(0).getPath()))
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .start(this);



                }else{Toast.makeText(this,"Please Select at least One Image",Toast.LENGTH_SHORT).show();}


                }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                new ImageCompressionAsyncTask(this).execute(resultUri.getPath());

                GlideApp.with(this).load(resultUri.getPath()).into(addImageBtn);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d("TAG", "onActivityResult: "+error);
            }
        }
    }


    private void progressConfigurations() {
        progressDialog = new MyProgressDialog(this, "Loading");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    public void showProgress(String Message) {
        progressDialog.setMessage(Message);
        progressDialog.show();
    }


    @Override
    public void onPause() {
        hideProgress();
        super.onPause();

    }

    public void hideProgress() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }



    @OnClick(R.id.submit_btn)
    public void onSubmitBtnClicked() {

        if (validateForm())

        {
            uploadData();
        }


    }

    private void uploadData() {
        showProgress("Uploading...");
        final HashMap<String, Object> data = new HashMap<>();

        final DocumentReference reference = firestoreDb.collection(Constants.ADD).document();

        data.put(Constants.DESCRIPTION, addDiscriptionTxt.getText().toString().trim());

        data.put(Constants.ADD_ID, reference.getId());

        showProgress(" Uploading ");
        StorageReference storageReference = firebaseStorage.getReference().child(Constants.IMAGE_URL).child("Images");
            storageReference.child(Uri.parse(compressedImage.getImageUrl()).getLastPathSegment()).putFile(Uri.parse("file://" + compressedImage.getImageUrl()))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            if (taskSnapshot != null) {
                                compressedImage.setImageDownloaded(taskSnapshot.getDownloadUrl().toString());
                                uploadImageWithData(data, reference);

                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    hideProgress();
                    Toast.makeText(UploadImageActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        }



    private void uploadImageWithData(final HashMap<String, Object> data, final DocumentReference documentReference) {


        data.put(Constants.IMAGE_URL, compressedImage.getImageDownloaded());
        data.put(Constants.SERVER_TIME_STAMP , FieldValue.serverTimestamp());

        documentReference.set(data, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {


                Log.d("TAG", "uploadDataToDataBase: ");

                finish();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgress();
                Toast.makeText(UploadImageActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.d("TAG", "uploadDataToDataBase: ");
            }
        });

    }




    private boolean validateForm() {


            if (compressedImage == null) {
                Toast.makeText(this, "Please Wait Images Are Loading", Toast.LENGTH_SHORT).show();
                return false;
            }


        if (TextUtils.isEmpty(addDiscriptionTxt.getText())) {
            addDiscriptionTxt.setError("Please Enter A Valid Input");
            return false;
        } else {
            addDiscriptionTxt.setError(null);
        }


        return true;
    }

    class ImageCompressionAsyncTask extends AsyncTask<String, Void, String> {

        Context mContext;

        public ImageCompressionAsyncTask(Context context) {
            mContext = context;
        }

        @Override
        protected String doInBackground(String... params) {

            File compressedImageFile = null;
            try {

                compressedImageFile = new Compressor(mContext).compressToFile(new File(params[0]));
            } catch (IOException e) {
                e.printStackTrace();
            }
            //  String filePath = SiliCompressor.with(mContext).compress(params[0] );


            if (compressedImageFile != null) {
                return compressedImageFile.getAbsolutePath();
            } else {
                return null;
            }


        }

        @Override
        protected void onPostExecute(String s) {


            Log.d("TAG", "onPostExecute: " + s);

            if (s == null) {
                return;
            }
  compressedImage = new CompressedImageDataModal(s);



        }
    }

}
