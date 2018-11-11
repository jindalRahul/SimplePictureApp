package com.rahuljindal.simplepictureapp.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.rahuljindal.simplepictureapp.GlideApp;
import com.rahuljindal.simplepictureapp.Modals.ImageDataModal;
import com.rahuljindal.simplepictureapp.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Rahul jindal on 3/1/2018.
 */

public class ImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private Context mContext;
    private List<ImageDataModal> imageDataModals;
    private static final int LOADING = 1;
    private static final int ITEM = 2;
    private ImageClickListner imageClickListner;

    private boolean isLoadingAdded = false;

    public ImageAdapter(Context mContext, List<ImageDataModal> imageDataModals) {
        this.mContext = mContext;
        this.imageDataModals = imageDataModals;
    }

    public ImageClickListner getOffersClickListner() {
        return imageClickListner;
    }

    public void setImageClickListner(ImageClickListner imageClickListner) {
        this.imageClickListner = imageClickListner;
    }

    public interface ImageClickListner {
        void imageClicked(int position);

//        void ticketDeleteClicked(String LinkUrl, int position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        if (viewType == ITEM) {
            view = layoutInflater.inflate(R.layout.single_item_photos, parent, false);
            return new ImageViewHolder(view);
        } else if (viewType == LOADING) {
            view = layoutInflater.inflate(R.layout.loading_view, parent, false);
            return new LoadingViewHolder(view);
        } else return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        if (!(holder instanceof ImageViewHolder)) {
            return;
        }
        final ImageViewHolder holder1 = (ImageViewHolder) holder;
        final ImageDataModal imageDataModal = imageDataModals.get(position);

        GlideApp.with(mContext).load(imageDataModal.getImageUrl()).thumbnail(0.3f).into(holder1.imageSingleItem);

        holder1.imageSingleItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageClickListner!=null){
                    imageClickListner.imageClicked(position);
                }
            }
        });

    }


    @Override
    public int getItemViewType(int position) {
        return (position == imageDataModals.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
    }

    @Override
    public int getItemCount() {
        return imageDataModals == null ? 0 : imageDataModals.size();
    }


    public void add(ImageDataModal image) {
        imageDataModals.add(image);
        notifyItemInserted(imageDataModals.size() - 1);
    }

    public void addAll(List<ImageDataModal> imageDataModalList) {
        for (ImageDataModal imageDataModal : imageDataModalList) {
            add(imageDataModal);
        }
    }

    public void remove(ImageDataModal imageDataModal) {
        int position = imageDataModals.indexOf(imageDataModal);
        if (position > -1) {
            imageDataModals.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        isLoadingAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new ImageDataModal());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = imageDataModals.size() - 1;
        ImageDataModal item = getItem(position);

        if (item != null) {
            imageDataModals.remove(position);
            notifyItemRemoved(position);
        }
    }

    public ImageDataModal getItem(int position) {
        return imageDataModals.get(position);
    }


    public class LoadingViewHolder extends RecyclerView.ViewHolder {


        public LoadingViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {


        @BindView(R.id.image_single_item)
        ImageView imageSingleItem;

        View view;

        public ImageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            view = itemView;


        }
    }
}
