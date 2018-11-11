package com.rahuljindal.simplepictureapp.ui;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.rahuljindal.simplepictureapp.Adapters.ImageAdapter;
import com.rahuljindal.simplepictureapp.Constants.Constants;
import com.rahuljindal.simplepictureapp.HelperClass.MyProgressDialog;
import com.rahuljindal.simplepictureapp.HelperClass.PaginationScrollListener;
import com.rahuljindal.simplepictureapp.Modals.ImageDataModal;
import com.rahuljindal.simplepictureapp.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements ImageAdapter.ImageClickListner {

    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.images_rv)
    RecyclerView imagesRv;
    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout swiperefresh;

    private ImageAdapter imageAdapter;
    private static final int PAGE_START = 0;
    private int currentPage = PAGE_START;
    DocumentSnapshot lastvisible;
    private int noOfItemsPerPage = 20;
    Query query;
    ArrayList<ImageDataModal> imageDataModalList;
    private boolean isLoading = false;
    // If current page is the last page (Pagination will stop after this page load)
    private boolean isLastPage = false;
    FirebaseFirestore firestoreDb;
    private MyProgressDialog progressDialog;

    FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        firestoreDb = FirebaseFirestore.getInstance();
        imageDataModalList = new ArrayList<>();

        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, UploadImageActivity.class);
                startActivity(i);
            }
        });

        progressConfigurations();

        imageAdapter = new ImageAdapter(this, imageDataModalList);
        imageAdapter.setImageClickListner(this);
        query = firestoreDb.collection(Constants.ADD);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        imagesRv.setLayoutManager(gridLayoutManager);
        imagesRv.addOnScrollListener(new PaginationScrollListener(gridLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                loadMorePages(query);

            }

            @Override
            public int getTotalPageCount() {
                return 0;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });

        imagesRv.setAdapter(imageAdapter);
        loadInitialData(query);

        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (query != null) {
                    loadInitialData(query);
                }
                swiperefresh.setRefreshing(false);
            }
        });
    }

    private void progressConfigurations() {
        progressDialog = new MyProgressDialog(this, "Loading");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    public void showProgress(String Message) {
        // progressWindow.showProgress();
        progressDialog.setMessage(Message);
        progressDialog.show();
    }


    private void loadInitialData(Query query) {
        imageAdapter.clear();
        isLastPage = false;
        showProgress(" ");
        query.orderBy(Constants.SERVER_TIME_STAMP, Query.Direction.DESCENDING)
                .limit(noOfItemsPerPage)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                if (documentSnapshots.size() < noOfItemsPerPage) {
                    isLastPage = true;
                    for (DocumentSnapshot dc : documentSnapshots) {
                        addDocumentTOList(dc);
                    }
                    Log.d("tydsfsdg", "onSuccess: " + isLastPage);
                } else {
                    if (documentSnapshots.isEmpty()) {
                        return;
                    }
                    lastvisible = documentSnapshots.getDocuments().get(noOfItemsPerPage - 1);
                    for (DocumentSnapshot dc : documentSnapshots) {
                        addDocumentTOList(dc);
                    }
                    imageAdapter.addLoadingFooter();
                }
                hideProgress();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("sdfghj", "onFailure: " + e);
                if (isLoading) {
                    isLoading = false;
                    imageAdapter.removeLoadingFooter();  // 2
                }
                hideProgress();

            }
        });
    }


    private void addDocumentTOList(DocumentSnapshot dc) {
        Log.d("  ", "onSuccess: ");
        ImageDataModal imageDataModal = new ImageDataModal();


        if (dc.contains(Constants.DESCRIPTION)) {
            imageDataModal.setImageDescription(dc.getString(Constants.DESCRIPTION));
        }

        if (dc.contains(Constants.IMAGE_URL)) {
            imageDataModal.setImageUrl(dc.getString(Constants.IMAGE_URL));

        }

        imageAdapter.add(imageDataModal);


    }


    private void loadMorePages(Query query) {
        query.orderBy(Constants.SERVER_TIME_STAMP, Query.Direction.DESCENDING)
                .startAfter(lastvisible)
                .limit(noOfItemsPerPage)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                isLoading = false;
                imageAdapter.removeLoadingFooter();  // 2
                if (documentSnapshots.size() < noOfItemsPerPage) {
                    isLastPage = true;

                    for (DocumentSnapshot dc : documentSnapshots) {
                        addDocumentTOList(dc);
                    }

                    Log.d("fghj", "onSuccess: " + isLastPage);
                } else {
                    lastvisible = documentSnapshots.getDocuments().get(noOfItemsPerPage - 1);
                    for (DocumentSnapshot dc : documentSnapshots) {
                        addDocumentTOList(dc);
                    }
                    Log.d("fghj", "onSuccess: " + isLastPage);

                    imageAdapter.addLoadingFooter();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                isLoading = false;
                Log.d("hjkl", "onFailure: " + e);
                imageAdapter.removeLoadingFooter();  // 2

            }
        });

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
        //   progressWindow.hideProgress();
    }


    @Override
    public void imageClicked(int position) {

        DetailDialogFragment dialog = new DetailDialogFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Bundle b = new Bundle();
        ImageDataModal p = imageAdapter.getItem(position);
        b.putString(Constants.IMAGE_URL, p.getImageUrl());
        b.putString(Constants.DESCRIPTION, p.getImageDescription());
        dialog.setArguments(b);
        dialog.show(ft, DetailDialogFragment.TAG);


    }
}
