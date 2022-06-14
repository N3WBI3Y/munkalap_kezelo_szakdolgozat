package com.giganet.giganet_worksheet.Utils;

public interface SSOResult {
    void onSuccess(String token);

    void onFailure(String token);
}
