package com.giganet.giganet_worksheet.Resources.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.View.SinglePictureActivity;

import java.util.ArrayList;

public class WorksheetImagePreviewAdapter extends RecyclerView.Adapter<WorksheetImagePreviewAdapter.ViewHolder> {

    private final Context context;
    private final String type;
    private ArrayList<String> picturesPath;
    private boolean enabled;

    public WorksheetImagePreviewAdapter(ArrayList<String> pictures, Activity context, String type) {
        this.context = context;
        this.picturesPath = pictures;
        this.type = type;
    }

    @NonNull
    @Override
    public WorksheetImagePreviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_prev_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context).load(picturesPath.get(holder.getAbsoluteAdapterPosition())).into(holder.img);
        holder.imageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enabled) {
                    Intent pictureIntent = new Intent(context, SinglePictureActivity.class);
                    pictureIntent.putExtra("picturepath", picturesPath.get(holder.getAbsoluteAdapterPosition()));
                    pictureIntent.putExtra("type", type);
                    context.startActivity(pictureIntent);
                } else {
                    Toast.makeText(v.getContext(), "Indítsd el a munkamenetet", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void update(ArrayList<String> pictures) {
        picturesPath = pictures;
        notifyDataSetChanged();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


    @Override
    public int getItemCount() {
        return picturesPath == null ? 0 : picturesPath.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView img;
        private final LinearLayout imageLayout;

        public ViewHolder(View view) {
            super(view);
            imageLayout = view.findViewById(R.id.single_layout);
            img = (ImageView) view.findViewById(R.id.single_imageView);
        }
    }

}
