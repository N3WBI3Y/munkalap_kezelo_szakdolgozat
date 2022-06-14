package com.giganet.giganet_worksheet.View;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.Network.Worksheet.MaterialDto;
import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationMaterialTableHandler;
import com.giganet.giganet_worksheet.Resources.Enums.TransactionType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class AddMaterialDialog extends DialogFragment implements AdapterView.OnItemSelectedListener , View.OnClickListener {
    private ImageButton scan, decrement, increment;
    private Button addButton,cancelButton;
    private TextView title,unit,serialNumber,serialNumberTitle;
    private Spinner materials;
    private EditText quantity;
    private List<MaterialDto.Material> materialList;
    private final String titleString;
    private ActivityResultLauncher<Intent> serialNumberScanResultLauncher;
    private final Integer transactionId;
    private final int quantityNum;
    private final String material;
    private final String serialNum;
    private final boolean used;
    private final String serviceType;



    public AddMaterialDialog(String title, Integer transactionId, Integer quantity
                            , String material, String serialNum, boolean used, String serviceType) {
        this.titleString = title;
        this.transactionId = transactionId;
        this.quantityNum = quantity == null ? 0 : quantity;
        this.material = material == null ? "" : material;
        this.serialNum = serialNum == null ? "" : serialNum;
        this.used = used;
        this.serviceType = serviceType;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v =  inflater.inflate(R.layout.dialog_add_material,container,false);
        init(v);
        return v;
    }

    private void init(View v){
        scan = v.findViewById(R.id.b_scann);
        decrement = v.findViewById(R.id.b_decrement);
        increment = v.findViewById(R.id.b_increment);
        addButton = v.findViewById(R.id.b_add_positive);
        cancelButton = v.findViewById(R.id.b_add_negative);
        title = v.findViewById(R.id.t_title);
        unit = v.findViewById(R.id.t_unit);
        serialNumber = v.findViewById(R.id.t_serialNumber);
        serialNumberTitle = v.findViewById(R.id.t_serialNumber_title);
        materials = v.findViewById(R.id.s_materials);
        quantity = v.findViewById(R.id.et_quantity);
        title.setText(titleString);
        if (transactionId != null){
            addButton.setText("Módosítás");
        }
        quantity.setText("" + quantityNum);
        setAdapter();
        setListeners();
        registerSerialNumberScannerResultLauncher();
        setSelection();
        serialNumber.setText(serialNum);
    }

    private void setSelection() {
        AtomicInteger index = new AtomicInteger(-1);
        Optional<MaterialDto.Material> result = materialList.stream().peek(x -> index.incrementAndGet())
                .filter((MaterialDto.Material materialEntity) -> materialEntity.getMaterial().equals(material))
                .findFirst();
        if (result.isPresent()){
            materials.setSelection(index.get());
        } else {
            materials.setSelection(0);
        }
    }

    private void registerSerialNumberScannerResultLauncher() {
        serialNumberScanResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                if (data.getStringExtra("itemNumber") != null) {
                                    serialNumber.setText(data.getStringExtra("itemNumber"));
                                }
                            }
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    private void setAdapter(){
        InstallationMaterialTableHandler materialTableHandler = new InstallationMaterialTableHandler(requireActivity());
        materialList = materialTableHandler.getMaterials(serviceType);
        materialList.sort(Comparator.comparing(MaterialDto.Material::getMaterial));
        ArrayList<String> materialNames = getMaterialNames(materialList);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireActivity(), R.layout.simple_spinner_item_black);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        arrayAdapter.addAll(materialNames);
        materials.setAdapter(arrayAdapter);
        materials.setOnItemSelectedListener(this);
    }

    private ArrayList<String> getMaterialNames(List<MaterialDto.Material> materialList) {
        ArrayList<String> materialNames = new ArrayList<>();
        for (MaterialDto.Material entity: materialList) {
            materialNames.add(entity.getMaterial());
        }
        return materialNames;
    }

    private void setListeners(){
        addButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        increment.setOnClickListener(this);
        decrement.setOnClickListener(this);
        scan.setOnClickListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        unit.setText(String.format("Mennyiség (%s)", materialList.get(position).getUnit()));
        if (materialList.get(position).isSerialNumber()){
            serialNumber.setVisibility(View.VISIBLE);
            scan.setVisibility(View.VISIBLE);
            serialNumberTitle.setVisibility(View.VISIBLE);
            unit.setVisibility(View.GONE);
            increment.setVisibility(View.GONE);
            decrement.setVisibility(View.GONE);
            quantity.setText("1");
            quantity.setVisibility(View.GONE);
        } else {
            serialNumber.setVisibility(View.GONE);
            scan.setVisibility(View.GONE);
            serialNumberTitle.setVisibility(View.GONE);
            unit.setVisibility(View.VISIBLE);
            increment.setVisibility(View.VISIBLE);
            decrement.setVisibility(View.VISIBLE);
            quantity.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.b_scann){
            Intent scannIntent = new Intent(requireActivity(),BarcodeReaderActivity.class);
            serialNumberScanResultLauncher.launch(scannIntent);
        } else if(id == R.id.b_increment){
            int value = Integer.parseInt(quantity.getText().toString());
            value++;
            quantity.setText(String.format("%d",value));
        } else if(id == R.id.b_decrement){
            int value = Integer.parseInt(quantity.getText().toString());
            value--;
            quantity.setText(String.format("%d", Math.max(value, 0)));
        } else if(id == R.id.b_add_positive){
            if (Integer.parseInt(quantity.getText().toString()) == 0){
                Toast.makeText(requireActivity(),"A mennyiség nem lehet 0",Toast.LENGTH_SHORT).show();
                return;
            }
            Bundle material = new Bundle();
            material.putString("material", materials.getSelectedItem().toString());
            material.putInt("quantity", used ? -Integer.parseInt(quantity.getText().toString()) :Integer.parseInt(quantity.getText().toString()));
            material.putString("serialNum", serialNumber.getText().toString());
            material.putInt("transactionId", transactionId == null ? 0 : transactionId);
            if (transactionId == null){
                requireActivity().getSupportFragmentManager().setFragmentResult(TransactionType.ADD.type,material);
            } else {
                requireActivity().getSupportFragmentManager().setFragmentResult(TransactionType.MODIFY.type,material);
            }
            dismiss();
        } else if(id == R.id.b_add_negative){
            dismiss();
        }
    }
}
