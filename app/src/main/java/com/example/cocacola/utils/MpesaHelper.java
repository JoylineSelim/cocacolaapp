package com.example.cocacola.utils;

import android.content.Context;
import android.util.Base64;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MpesaHelper {
    // M-Pesa Sandbox Credentials
    private static final String CONSUMER_KEY = "DilVUYG7suVCzfbndY8y4F3AoKPVDzWzmeEqs23H2ZrHfYKG";
    private static final String CONSUMER_SECRET = "fBMUqHnC96DW0mJbaIqipAPQMKdyvZCXgNH9AAEpjqChrm7PymIbcA2ivZ7oWDyy";
    private static final String BUSINESS_SHORT_CODE = "174379";
    private static final String PASSKEY = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919";
    private static final String CALLBACK_URL = "https://webhook.site/d373aeed-8cab-4b6d-bd80-bd7fb517b0bdk";

    private static final String AUTH_URL = "https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials";
    private static final String STK_PUSH_URL = "https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest";
    private static final String QUERY_URL = "https://sandbox.safaricom.co.ke/mpesa/stkpushquery/v1/query";

    private OkHttpClient client;

    public interface MpesaCallback {
        void onSuccess(String checkoutRequestId);
        void onFailure(String error);
    }

    public interface QueryCallback {
        void onResult(String resultCode, String resultDesc);
        void onError(String error);
    }

    public MpesaHelper(Context context) {
        this.client = new OkHttpClient();
    }

    public void initiateSTKPush(String phoneNumber, int amount, MpesaCallback callback) {
        getAccessToken(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { callback.onFailure(e.getMessage()); }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String accessToken = new JSONObject(response.body().string()).getString("access_token");
                    performSTKPush(accessToken, phoneNumber, amount, callback);
                } catch (Exception e) { callback.onFailure(e.getMessage()); }
            }
        });
    }

    private void getAccessToken(Callback callback) {
        String credentials = CONSUMER_KEY + ":" + CONSUMER_SECRET;
        String authHeader = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        Request request = new Request.Builder().url(AUTH_URL).addHeader("Authorization", authHeader).build();
        client.newCall(request).enqueue(callback);
    }

    private void performSTKPush(String accessToken, String phoneNumber, int amount, MpesaCallback callback) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
            String password = Base64.encodeToString((BUSINESS_SHORT_CODE + PASSKEY + timestamp).getBytes(), Base64.NO_WRAP);

            JSONObject requestBody = new JSONObject();
            requestBody.put("BusinessShortCode", BUSINESS_SHORT_CODE);
            requestBody.put("Password", password);
            requestBody.put("Timestamp", timestamp);
            requestBody.put("TransactionType", "CustomerPayBillOnline");
            requestBody.put("Amount", amount);
            requestBody.put("PartyA", phoneNumber);
            requestBody.put("PartyB", BUSINESS_SHORT_CODE);
            requestBody.put("PhoneNumber", phoneNumber);
            requestBody.put("CallBackURL", CALLBACK_URL);
            requestBody.put("AccountReference", "FreshMart");
            requestBody.put("TransactionDesc", "SchoolProjectPayment");

            RequestBody body = RequestBody.create(MediaType.parse("application/json"), requestBody.toString());
            Request request = new Request.Builder().url(STK_PUSH_URL).addHeader("Authorization", "Bearer " + accessToken).post(body).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) { callback.onFailure(e.getMessage()); }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        if ("0".equals(json.optString("ResponseCode"))) {
                            callback.onSuccess(json.getString("CheckoutRequestID"));
                        } else {
                            callback.onFailure(json.optString("errorMessage", "Request Failed"));
                        }
                    } catch (Exception e) { callback.onFailure(e.getMessage()); }
                }
            });
        } catch (Exception e) { callback.onFailure(e.getMessage()); }
    }

    public void queryTransactionStatus(String checkoutRequestId, QueryCallback callback) {
        getAccessToken(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { callback.onError(e.getMessage()); }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String accessToken = new JSONObject(response.body().string()).getString("access_token");
                    String timestamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
                    String password = Base64.encodeToString((BUSINESS_SHORT_CODE + PASSKEY + timestamp).getBytes(), Base64.NO_WRAP);

                    JSONObject requestBody = new JSONObject();
                    requestBody.put("BusinessShortCode", BUSINESS_SHORT_CODE);
                    requestBody.put("Password", password);
                    requestBody.put("Timestamp", timestamp);
                    requestBody.put("CheckoutRequestID", checkoutRequestId);

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), requestBody.toString());
                    Request request = new Request.Builder().url(QUERY_URL).addHeader("Authorization", "Bearer " + accessToken).post(body).build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) { callback.onError(e.getMessage()); }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                JSONObject json = new JSONObject(response.body().string());
                                // resultCode 0 = Success, 1032 = Cancelled
                                callback.onResult(json.optString("ResultCode", "vp"), json.optString("ResultDesc"));
                            } catch (Exception e) { callback.onError(e.getMessage()); }
                        }
                    });
                } catch (Exception e) { callback.onError(e.getMessage()); }
            }
        });
    }
}