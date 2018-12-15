package com.affirm.affirmsdk;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.affirm.affirmsdk.models.ErrorResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.affirm.affirmsdk.Tracker.TrackingEvent.NETWORK_ERROR;
import static com.affirm.affirmsdk.Tracker.TrackingLevel.ERROR;

class AffirmRequest<T> {
  interface Endpoint {
    String getPath();

    Request completeBuilder(Request.Builder builder);
  }

  interface Callback<T> {
    void onSuccess(T result);

    void onFailure(Throwable throwable);
  }

  private final Class<T> clazz;
  private final String baseUrl;
  private final OkHttpClient client;
  private final Endpoint endpoint;
  private final Gson gson;
  private final Tracker tracker;

  private Call call;

  AffirmRequest(@NonNull Class<T> clazz, @NonNull String baseUrl, @NonNull OkHttpClient client,
      @NonNull Gson gson, @NonNull Endpoint endpoint, @NonNull Tracker tracker) {
    this.baseUrl = baseUrl;
    this.client = client;
    this.gson = gson;
    this.endpoint = endpoint;
    this.clazz = clazz;
    this.tracker = tracker;
  }

  void create(@NonNull final Callback<T> callback) {
    final String protocol = baseUrl.contains("http") ? "" : "https://";

    final Request.Builder builder =
        new Request.Builder().url(protocol + baseUrl + endpoint.getPath());

    final Request request = endpoint.completeBuilder(builder);
    call = client.newCall(request);
    call.enqueue(new okhttp3.Callback() {
      @Override public void onFailure(Call call, IOException e) {
        tracker.track(NETWORK_ERROR, ERROR, createNetworkJsonObj(request, null));
        callback.onFailure(e);
      }

      @Override public void onResponse(Call call, Response response) throws IOException {
        if (!response.isSuccessful()) {
          tracker.track(NETWORK_ERROR, ERROR, createNetworkJsonObj(request, response));
          if (response.code() == 403) {
            callback.onFailure(
                new Exception("Got error for request: " + response.code()));
          } else if (response.code() >= 400 && response.code() < 500 && response.code() != 404) {
            final ErrorResponse errorResponse =
                gson.fromJson(response.body().string(), ErrorResponse.class);

            callback.onFailure(new ServerError(errorResponse));
          } else {
            callback.onFailure(
                new Exception("Got error for request: " + response.code()));
          }

          return;
        }
        final String bodyString = response.body().string();
        final T res = gson.fromJson(bodyString, clazz);
        callback.onSuccess(res);
      }
    });
  }

  void cancel() {
    if (call != null) {
      call.cancel();
    }
  }

  private static JsonObject createNetworkJsonObj(@NonNull Request request,
      @Nullable Response response) {
    final JsonObject jsonObject = new JsonObject();
    final String affirmRequestIDHeader = "X-Affirm-Request-Id";
    jsonObject.addProperty("url", request.url().toString());
    jsonObject.addProperty("method", request.method());
    if (response != null) {
      final Headers headers = response.headers();
      jsonObject.addProperty("status_code", response.code());
      jsonObject.addProperty(affirmRequestIDHeader, headers.get(affirmRequestIDHeader));
      jsonObject.addProperty("x-amz-cf-id", headers.get("x-amz-cf-id"));
      jsonObject.addProperty("x-affirm-using-cdn", headers.get("x-affirm-using-cdn"));
      jsonObject.addProperty("x-cache", headers.get("x-cache"));
    } else {
      jsonObject.add("status_code", null);
      jsonObject.add(affirmRequestIDHeader, null);
    }
    return jsonObject;
  }
}
