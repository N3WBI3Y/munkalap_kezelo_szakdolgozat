package com.giganet.giganet_worksheet.Resources.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.Persistence.Worksheet.Entities.InstallationTransactionEntity;
import com.giganet.giganet_worksheet.Resources.Events.ItemRemovedEvent;
import com.giganet.giganet_worksheet.View.AddMaterialDialog;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class WorksheetMaterialPreviewAdapter extends RecyclerView.Adapter<WorksheetMaterialPreviewAdapter.ViewHolder> {

    private ArrayList<InstallationTransactionEntity> items;
    private boolean enabled;
    private final FragmentManager fragmentManager;
    private final String serviceType;

    public WorksheetMaterialPreviewAdapter(ArrayList<InstallationTransactionEntity> items
                                            , FragmentManager fragmentManager, String serviceType) {
        this.items = items;
        this.fragmentManager = fragmentManager;
        this.serviceType = serviceType;
    }

    @NonNull
    @Override
    public WorksheetMaterialPreviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_transaction_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(items == null){
            return;
        }
        holder.item_serial.setText(items.get(holder.getAbsoluteAdapterPosition()).getSerialNum());
        holder.material.setText(items.get(holder.getAbsoluteAdapterPosition()).getMaterial());
        holder.quantity.setText("" + items.get(holder.getAbsoluteAdapterPosition()).getQuantity());
        holder.unit.setText("db");
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enabled) {
                    try {
                        EventBus.getDefault().post(new ItemRemovedEvent(items.get(holder.getAbsoluteAdapterPosition()).getRowId()));
                        notifyItemRemoved(holder.getAbsoluteAdapterPosition());
                    } catch (ArrayIndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(v.getContext(), "Indítsd el a munkamenetet", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enabled) {
                    try {
                        new AddMaterialDialog("Anyag módosítása",items.get(holder.getAbsoluteAdapterPosition()).getRowId()
                                , items.get(holder.getAbsoluteAdapterPosition()).getQuantity(),items.get(holder.getAbsoluteAdapterPosition()).getMaterial()
                                , items.get(holder.getAbsoluteAdapterPosition()).getSerialNum(), items.get(holder.getAbsoluteAdapterPosition()).getQuantity() > 0,serviceType).show(fragmentManager,"AddMaterial");
                    } catch (ArrayIndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(v.getContext(), "Indítsd el a munkamenetet", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void update(ArrayList<InstallationTransactionEntity> pictures) {
        items = pictures;
        notifyDataSetChanged();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView item_serial,material, quantity,unit;
        private final ImageButton delete,modify;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            item_serial = itemView.findViewById(R.id.t_barcode_serial);
            material = itemView.findViewById(R.id.t_material_name);
            quantity = itemView.findViewById(R.id.t_quantity);
            unit = itemView.findViewById(R.id.t_unit);
            delete = itemView.findViewById(R.id.b_barcode_delete);
            modify = itemView.findViewById(R.id.ib_edit);
        }
    }

}
