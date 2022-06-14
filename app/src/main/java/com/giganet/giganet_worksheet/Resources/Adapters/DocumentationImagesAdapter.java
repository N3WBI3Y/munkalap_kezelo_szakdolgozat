package com.giganet.giganet_worksheet.Resources.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.Persistence.Documentation.DocumentationItemTableHandler;
import com.giganet.giganet_worksheet.Resources.Events.AddCommentEvent;
import com.giganet.giganet_worksheet.View.SingleDocumentActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

public class DocumentationImagesAdapter extends RecyclerView.Adapter<DocumentationImagesAdapter.ViewHolder> {
    private final Activity context;
    private ArrayList<String> pictures;

    public DocumentationImagesAdapter(ArrayList<String> pictures, Activity context) {
        this.pictures = pictures;
        this.context = context;
        EventBus.getDefault().register(this);
    }


    public void addPicture(ArrayList<String> pictures) {
        this.pictures = pictures;
        notifyDataSetChanged();
    }

    public void removePicture(ArrayList<String> pictures) {
        this.pictures = pictures;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_documentation_image, parent, false);
        return new ViewHolder(view);
    }

    @Subscribe
    public void onAddCommentEvent(AddCommentEvent event) {
        notifyDataSetChanged();
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            Glide.with(context).load(pictures.get(holder.getAbsoluteAdapterPosition())).into(holder.img);

            holder.documentation_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent pictureIntent = new Intent(context, SingleDocumentActivity.class);
                    pictureIntent.putExtra("picturepath", pictures.get(holder.getAbsoluteAdapterPosition()));
                    context.startActivity(pictureIntent);

                }
            });
            DocumentationItemTableHandler db = new DocumentationItemTableHandler(context);
            String comment = db.getItemSubject(pictures.get(holder.getAbsoluteAdapterPosition()));
            boolean commented = comment != null && comment.length() > 0;
            setCheckMarks(holder.checkMark, commented);

        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

    }

    public boolean areAllPictureCommented() {
        boolean allPictureCommented = true;
        DocumentationItemTableHandler db = new DocumentationItemTableHandler(context);
        for (String picture : pictures) {
            String comment = db.getItemSubject(picture);
            allPictureCommented = comment != null && comment.length() > 0;
        }

        return allPictureCommented;
    }

    @Override
    public int getItemCount() {
        return pictures.size();
    }

    private void setCheckMarks(ImageView markView, boolean type) {
        Drawable normalImage = type ? ResourcesCompat.getDrawable(context.getResources(), R.drawable.green_check_mark, null)
                : ResourcesCompat.getDrawable(context.getResources(), R.drawable.exclamation_mark_icon, null);
        markView.setImageDrawable(normalImage);

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView img;
        private final LinearLayout documentation_layout;
        private final ImageView checkMark;

        public ViewHolder(View view) {
            super(view);
            documentation_layout = view.findViewById(R.id.single_documentation_layout);
            img = (ImageView) view.findViewById(R.id.grid_image);
            checkMark = (ImageView) view.findViewById(R.id.iw_checkMark);
        }
    }
}
