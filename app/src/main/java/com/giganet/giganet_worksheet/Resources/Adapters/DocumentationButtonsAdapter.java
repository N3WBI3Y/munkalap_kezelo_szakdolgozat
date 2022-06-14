package com.giganet.giganet_worksheet.Resources.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.Network.Documentation.RetrofitClientDocumentation;
import com.giganet.giganet_worksheet.Utils.SSOResult;
import com.giganet.giganet_worksheet.Persistence.Documentation.Entities.DocumentationHistoryEntity;
import com.giganet.giganet_worksheet.Network.Documentation.DocumentationStatusDto;
import com.giganet.giganet_worksheet.Resources.Enums.DocumentationOrder;
import com.giganet.giganet_worksheet.Utils.SSOService;
import com.giganet.giganet_worksheet.View.DocumentationHistoryActivity;
import com.giganet.giganet_worksheet.View.DocumentationWorkflowActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DocumentationButtonsAdapter extends RecyclerView.Adapter<DocumentationButtonsAdapter.ViewHolder> {

    private final Activity context;
    private List<DocumentationStatusDto> types;

    public DocumentationButtonsAdapter(List<DocumentationStatusDto> types, Activity context) {
        this.types = types;
        this.context = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_documentation_button, parent, false);
        view.findViewById(R.id.iv_lastDocumentedPicture).setZ(100.0f);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.lastPicture.setZ(100.0f);
        holder.name.setText(types.get(holder.getAbsoluteAdapterPosition()).getName());
        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (types.get(holder.getAbsoluteAdapterPosition()).getName().equals("BEKÜLDÖTT KÉPEK")) {
                    Intent intent = new Intent(context, DocumentationHistoryActivity.class);
                    context.startActivity(intent);
                } else {
                    Intent documentationIntent = new Intent(context, DocumentationWorkflowActivity.class);
                    documentationIntent.putExtra("type", types.get(holder.getAbsoluteAdapterPosition()).getName());
                    context.startActivity(documentationIntent);
                }

            }
        });
        if (types.get(holder.getAbsoluteAdapterPosition()).getName().equals("BEKÜLDÖTT KÉPEK")) {
            SSOService.getToken(context, new SSOResult() {
                @Override
                public void onSuccess(String token) {
                    Call<ArrayList<DocumentationHistoryEntity>> call = RetrofitClientDocumentation.getInstance(context).getApi().getLastDocumentedImage(token
                                                                                    , DocumentationOrder.DESCENDING.getValue()
                                                                                    , 1, 1);
                    call.enqueue(new Callback<ArrayList<DocumentationHistoryEntity>>() {
                        @Override
                        public void onResponse(Call<ArrayList<DocumentationHistoryEntity>> call, Response<ArrayList<DocumentationHistoryEntity>> response) {
                            if (response.body() != null && response.body().size() > 0) {
                                DocumentationHistoryEntity documentationHistoryEntity = response.body().get(0);
                                if (documentationHistoryEntity.getImage() != null) {
                                    byte[] imageByteArray = Base64.decode(documentationHistoryEntity.getImage(), Base64.DEFAULT);
                                    Glide.with(context).load(imageByteArray).apply(new RequestOptions().override(50, 50)).into(holder.lastPicture);
                                }
                            }
                        }
                        @Override
                        public void onFailure(Call<ArrayList<DocumentationHistoryEntity>> call, Throwable t) {
                        }
                    });
                }
                @Override
                public void onFailure(String token) {
                }
            });

            holder.lastPicture.setZ(100.0f);
        } else {
            holder.lastPicture.setZ(-100.0f);
        }
    }

    @Override
    public int getItemCount() {
        return types.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout llc_view;
        private final AppCompatButton name;
        private final ImageView lastPicture;

        public ViewHolder(View view) {
            super(view);
            name = (AppCompatButton) view.findViewById(R.id.t_documentationName);
            llc_view = (ConstraintLayout) view.findViewById(R.id.llc_view);
            lastPicture = (ImageView) view.findViewById(R.id.iv_lastDocumentedPicture);
            lastPicture.setZ(100.0f);
        }
    }
}
