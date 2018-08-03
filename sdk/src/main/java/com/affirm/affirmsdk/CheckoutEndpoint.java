package com.affirm.affirmsdk;

import com.affirm.affirmsdk.models.Checkout;
import com.affirm.affirmsdk.models.Merchant;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

final class CheckoutEndpoint implements AffirmRequest.Endpoint {
  private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

  private final Merchant merchant;
  private final Checkout checkout;
  private final Gson gson;

  CheckoutEndpoint(Merchant merchant, Checkout checkout, Gson gson) {
    this.merchant = merchant;
    this.checkout = checkout;
    this.gson = gson;
  }

  @Override public String getPath() {
    return "/api/v2/checkout/";
  }

  @Override public Request completeBuilder(Request.Builder builder) {
    final JsonObject jsonRequest = buildJsonRequest(checkout);
    final RequestBody body = RequestBody.create(JSON, jsonRequest.toString());

    return builder.post(body).build();
  }

  private JsonObject buildJsonRequest(Checkout checkout) {
    final JsonObject configJson = new JsonObject();
    final JsonObject metadataJson = new JsonObject();
    final JsonObject jsonRequest = new JsonObject();
    final JsonParser jsonParser = new JsonParser();
    final JsonObject checkoutJson = jsonParser.parse(gson.toJson(checkout)).getAsJsonObject();
    final JsonObject merchantJson = jsonParser.parse(gson.toJson(merchant)).getAsJsonObject();

    configJson.addProperty("user_confirmation_url_action", "GET");
    metadataJson.addProperty("platform_type", "Affirm Android SDK");
    metadataJson.addProperty("platform_affirm", BuildConfig.VERSION_NAME);

    checkoutJson.add("merchant", merchantJson);
    checkoutJson.add("config", configJson);
    checkoutJson.addProperty("api_version", "v2");
    checkoutJson.add("metadata", metadataJson);

    jsonRequest.add("checkout", checkoutJson);

    return jsonRequest;
  }
}
