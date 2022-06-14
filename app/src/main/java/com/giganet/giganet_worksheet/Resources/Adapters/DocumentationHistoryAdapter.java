package com.giganet.giganet_worksheet.Resources.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.Persistence.Documentation.Entities.DocumentationHistoryEntity;
import com.giganet.giganet_worksheet.View.DocumentationHistoryItemActivity;

import java.util.ArrayList;

public class DocumentationHistoryAdapter extends RecyclerView.Adapter<DocumentationHistoryAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<DocumentationHistoryEntity> documentationHistoryEntityArrayList;

    public DocumentationHistoryAdapter(ArrayList<DocumentationHistoryEntity> documentationHistoryEntityArrayList, Context context) {
        this.documentationHistoryEntityArrayList = documentationHistoryEntityArrayList;
        this.context = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_documentation_history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        DocumentationHistoryEntity documentationHistoryEntity = documentationHistoryEntityArrayList.get(position);
        byte[] imageByteArray = Base64.decode(documentationHistoryEntity.getImage(), Base64.DEFAULT);

        Glide.with(context)
                .load(imageByteArray)
                .placeholder(R.drawable.image_placeholder)
                .into(holder.thumbnail);
        holder.type.setText(documentationHistoryEntity.getTaskType());
        String description = ((documentationHistoryEntity.getDescription() != null && !documentationHistoryEntity.getDescription().equals("null")) ? documentationHistoryEntity.getDescription() : "")
                + ((documentationHistoryEntity.getSubject() != null && !documentationHistoryEntity.getSubject().equals("null")) ? ("\n" + documentationHistoryEntity.getSubject()) : "");
        holder.description.setText(description);
        if (description.length() == 0) {
            holder.description.setHeight(0);
        }
        holder.timestamp.setText(documentationHistoryEntity.getTimestamp());
        holder.coordinate.setText(String.format("Lat: %s\nLon: %s", (double) Math.round(documentationHistoryEntity.getGpsLat() * 1000000) / 1000000
                , (double) Math.round(documentationHistoryEntity.getGpsLon() * 1000000) / 1000000));
        holder.city.setText(documentationHistoryEntity.getCity());
        holder.markerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String destination = documentationHistoryEntity.getGpsLat() + "," + documentationHistoryEntity.getGpsLon();
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + destination);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                context.startActivity(mapIntent);
            }
        });

        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageViewerIntent = new Intent(context, DocumentationHistoryItemActivity.class);
                imageViewerIntent.putExtra("id", documentationHistoryEntity.getImageId());
                context.startActivity(imageViewerIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return documentationHistoryEntityArrayList.size();
    }

    public void addItem(ArrayList<DocumentationHistoryEntity> items) {
        for (DocumentationHistoryEntity item : items) {
            if (!documentationHistoryEntityArrayList.contains(item)) {
                documentationHistoryEntityArrayList.add(item);
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView city, coordinate, timestamp, description, type;
        private final ImageView thumbnail;
        private final ImageButton markerIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.documentation_thumbnail);
            city = itemView.findViewById(R.id.documentation_city);
            coordinate = itemView.findViewById(R.id.documentation_coordinate);
            timestamp = itemView.findViewById(R.id.documentation_date);
            description = itemView.findViewById(R.id.documentation_description);
            type = itemView.findViewById(R.id.documentation_type);
            markerIcon = itemView.findViewById(R.id.documentation_map);
        }
    }
}
