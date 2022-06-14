package com.giganet.giganet_worksheet.View;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.Resources.Events.CancelWorkEvent;
import com.giganet.giganet_worksheet.Resources.Events.LocationEvent;
import com.giganet.giganet_worksheet.Resources.Events.PauseWorkEvent;
import com.giganet.giganet_worksheet.Resources.Events.RestartWorkEvent;
import com.giganet.giganet_worksheet.Resources.Events.SubmitWorkEvent;

import org.greenrobot.eventbus.EventBus;

public class TextDialogs {

    private final Dialog dialog;
    private final Context context;

    public TextDialogs(Context context) {
        this.dialog = new Dialog(context);
        this.context = context;
    }

    public void showPauseDialog() {
        dialog.setCanceledOnTouchOutside(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_warning_pause);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
        final Button dialogNegative = dialog.findViewById(R.id.b_CANCEL);
        final Button dialogPositive = dialog.findViewById(R.id.b_OK);
        final EditText comment = dialog.findViewById(R.id.et_WARNING);
        dialogPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = comment.getText().toString().length() == 0 ? "" : comment.getText().toString();
                EventBus.getDefault().post(new PauseWorkEvent(str));
                dialog.dismiss();
            }
        });


        dialogNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
    }

    public void showEndDialog(String message, boolean submitable) {
        dialog.setCanceledOnTouchOutside(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_warning_end);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
        final Button dialogNegative = dialog.findViewById(R.id.dialog_end_negative);
        final Button dialogPositive = dialog.findViewById(R.id.dialog_end_positive);
        final TextView comment = dialog.findViewById(R.id.t_WARNING);
        comment.setText(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY));
        dialogPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new SubmitWorkEvent());
                dialog.dismiss();
            }
        });
        dialogNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        if (!submitable) {
            dialogPositive.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.UnSubmitable, null)));
            dialogPositive.setEnabled(false);
        }
    }


    public void showLocationDialog(String message, Location location) {
        dialog.setCanceledOnTouchOutside(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_warning_end);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
        final Button dialogNegative = (Button) dialog.findViewById(R.id.dialog_end_negative);
        final Button dialogPositive = (Button) dialog.findViewById(R.id.dialog_end_positive);
        final TextView dialogPositiveText = (TextView) dialog.findViewById(R.id.t_POSITIVE);
        final TextView dialogNegativeText = (TextView) dialog.findViewById(R.id.t_NEGATIVE);
        dialogPositiveText.setText("IGEN");
        dialogNegativeText.setText("NEM");
        final TextView comment = dialog.findViewById(R.id.t_WARNING);
        comment.setText(message);
        dialogPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new LocationEvent(location));
                dialog.dismiss();
            }
        });


        dialogNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

    }

    public void showSignatureDialog(String message) {
        dialog.setCanceledOnTouchOutside(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_signature);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
        final Button dialogPositive = (Button) dialog.findViewById(R.id.b_ACCEPT);
        final TextView terms = (TextView) dialog.findViewById(R.id.t_TERMS);
        terms.setText(message);
        dialogPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    public void showRestartDialog() {
        dialog.setCanceledOnTouchOutside(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_warning_pause);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
        final Button dialogNegative = dialog.findViewById(R.id.b_CANCEL);
        final Button dialogPositive = dialog.findViewById(R.id.b_OK);
        final EditText comment = dialog.findViewById(R.id.et_WARNING);
        final TextView title = dialog.findViewById(R.id.t_TITLE);
        title.setText("Befejezett munkamenet újranyitása");
        dialogPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                EventBus.getDefault().post(new RestartWorkEvent(comment.getText().toString()));
            }
        });


        dialogNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
    }

    public void showPatchNotes() {
        dialog.setCanceledOnTouchOutside(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_patch_note);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
        final Button dialogPositive = dialog.findViewById(R.id.b_close);
        dialogPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }


    public void showCancelDialog() {
        dialog.setCanceledOnTouchOutside(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_warning_cancel);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
        final Button dialogNegative = dialog.findViewById(R.id.b_CANCEL);
        final Button dialogPositive = dialog.findViewById(R.id.b_OK);
        final EditText comment = dialog.findViewById(R.id.et_WARNING);
        dialogPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (comment.getText().length() < 5) {
                    Toast.makeText(context, "Írd le a meghíúsulás okát", Toast.LENGTH_SHORT).show();
                    return;
                }
                String str = comment.getText().toString();
                dialog.dismiss();
                EventBus.getDefault().post(new CancelWorkEvent(str));
            }
        });

        comment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setOkButton(s);
            }

            private void setOkButton(CharSequence sequence) {
                if (sequence.length() > 4) {
                    dialogPositive.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.Giganet_green, null)));
                } else {

                    dialogPositive.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.UnSubmitable, null)));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        dialogNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
    }
}

