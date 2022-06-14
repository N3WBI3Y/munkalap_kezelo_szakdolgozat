package com.giganet.giganet_worksheet.View;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.Network.Documentation.RetrofitClientDocumentation;
import com.giganet.giganet_worksheet.Utils.SSOResult;
import com.giganet.giganet_worksheet.Persistence.Documentation.Entities.DocumentationHistoryEntity;
import com.giganet.giganet_worksheet.Network.Documentation.DocumentationStatusDto;
import com.giganet.giganet_worksheet.Persistence.Documentation.DocumentationStatusTableHandler;
import com.giganet.giganet_worksheet.Resources.Adapters.DocumentationHistoryAdapter;
import com.giganet.giganet_worksheet.Resources.Enums.DocumentationOrder;
import com.giganet.giganet_worksheet.Utils.SSOService;
import com.giganet.giganet_worksheet.Utils.SharedPreference;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DocumentationHistoryActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private AppCompatButton backButton;
    private RecyclerView imagesHolder;
    private ImageButton orderImage;
    private AutoCompleteTextView search;
    private Spinner typesSpinner;
    private ArrayList<DocumentationHistoryEntity> documentationHistoryEntityArrayList;
    private DocumentationHistoryAdapter userRVAdapter;
    private ProgressBar loadingBar;
    private NestedScrollView nestedView;
    private boolean asc = false;
    private String currentCategory = "ALL";

    private int page = 1, pageSize = 8, limit = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documentation_history);
        backButton = findViewById(R.id.b_back);
        imagesHolder = findViewById(R.id.documentation_layout);
        loadingBar = findViewById(R.id.documentation_progress_bar);
        nestedView = findViewById(R.id.nsw_documentation);
        documentationHistoryEntityArrayList = new ArrayList<>();
        userRVAdapter = new DocumentationHistoryAdapter(documentationHistoryEntityArrayList, this);
        orderImage = findViewById(R.id.documentation_order);
        typesSpinner = findViewById(R.id.documentation_types);

        backButton.setOnClickListener(this);
        orderImage.setOnClickListener(this);


        search = findViewById(R.id.actw_history_Search);
        search.addTextChangedListener(new TextWatcher() {
            private final long DELAY = 500;
            private Timer timer = new Timer();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                timer.cancel();
                timer = new Timer();
                timer.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                applyFilter();
                            }
                        },
                        DELAY
                );
            }
        });

        String username = new SharedPreference(this).getSharedPreferences().getString("USERNAME","");

        DocumentationStatusTableHandler db = new DocumentationStatusTableHandler(this);
        ArrayList<DocumentationStatusDto> statusEntities = db.getTaskTypes(username);
        String[] types = new String[statusEntities.size() + 1];
        types[0] = "ALL";
        for (int i = 0; i < statusEntities.size(); ++i) {
            types[i + 1] = statusEntities.get(i).getName();
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,
                        types);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        typesSpinner.setAdapter(spinnerArrayAdapter);
        typesSpinner.setOnItemSelectedListener(this);

        nestedView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    page++;
                    loadingBar.setVisibility(View.VISIBLE);
                    getDataFromAPI(page, limit, search.getText().toString());
                }
            }
        });
    }

    private void applyFilter() {
        page = 1;
        limit = 100;
        documentationHistoryEntityArrayList = new ArrayList<>();
        userRVAdapter = new DocumentationHistoryAdapter(documentationHistoryEntityArrayList, this);
        getDataFromAPI(page, limit, search.getText().toString());
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    private void getDataFromAPI(int page, int limit, String charSearch) {
        if (page > limit) {
            Toast.makeText(getApplicationContext(), "Elérted a lap alját!", Toast.LENGTH_SHORT).show();
            loadingBar.setVisibility(View.GONE);
            return;
        }
        SSOService.getToken(getApplicationContext(), new SSOResult() {
            @Override
            public void onSuccess(String token) {
                Call<ArrayList<DocumentationHistoryEntity>> call = RetrofitClientDocumentation.getInstance(getApplicationContext()).getApi().getDocumentedImages(token
                                                                , asc ? DocumentationOrder.ASCENDING.getValue() : DocumentationOrder.DESCENDING.getValue()
                                                                , pageSize, page, currentCategory, charSearch);

                call.enqueue(new Callback<ArrayList<DocumentationHistoryEntity>>() {
                    @Override
                    public void onResponse(Call<ArrayList<DocumentationHistoryEntity>> call, Response<ArrayList<DocumentationHistoryEntity>> response) {
                        if (response.body() != null) {
                            if (response.body().size() < pageSize) {
                                Toast.makeText(getApplicationContext(), "Elérted a lap alját!", Toast.LENGTH_SHORT).show();
                                loadingBar.setVisibility(View.GONE);
                                setLimit(page - 1);
                            }
                            userRVAdapter.addItem(response.body());
                            imagesHolder.setLayoutManager(new LinearLayoutManager(DocumentationHistoryActivity.this));
                            imagesHolder.setAdapter(userRVAdapter);
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<DocumentationHistoryEntity>> call, Throwable t) {
                        Toast.makeText(getApplicationContext(), "Nem sikerült elérnem a szervert", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String token) {
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.b_back) {
            finish();
        } else if (v.getId() == R.id.documentation_order) {
            changeOrder();
        }
    }

    private void changeOrder() {
        page = 1;
        limit = 100;
        asc = !asc;
        if (asc) {
            orderImage.setBackground(ResourcesCompat.getDrawable(getApplicationContext().getResources(), R.drawable.sort_amount_asc_icon, null));
        } else {
            orderImage.setBackground(ResourcesCompat.getDrawable(getApplicationContext().getResources(), R.drawable.sort_amount_desc_icon, null));
        }
        documentationHistoryEntityArrayList = new ArrayList<>();
        userRVAdapter = new DocumentationHistoryAdapter(documentationHistoryEntityArrayList, this);
        getDataFromAPI(page, limit, search.getText().toString());

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        page = 1;
        limit = 100;
        currentCategory = parent.getItemAtPosition(position).toString();
        documentationHistoryEntityArrayList = new ArrayList<>();
        userRVAdapter = new DocumentationHistoryAdapter(documentationHistoryEntityArrayList, this);
        getDataFromAPI(page, limit, search.getText().toString());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}