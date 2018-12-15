package com.affirm.affirmsdk;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import com.affirm.affirmsdk.models.PromoResponse;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;

class PromoJob {

  private final String baseUrl;
  private final String publicKey;
  private final String promoId;
  private final float dollarAmount;
  private final float textSize;
  private final Typeface typeface;
  private final AffirmLogoType logoType;
  private final AffirmColor affirmColor;
  private final Context context;
  private final SpannablePromoCallback callback;
  private final Gson gson;
  private final OkHttpClient client;
  private final Tracker tracker;

  private AffirmRequest currentRequest;
  private boolean isCancelled = false;

  PromoJob(Gson gson, OkHttpClient client, Tracker tracker, String publicKey, String baseUrl,
      float textSize, Typeface typeface, String promoId, float amount, AffirmLogoType logoType,
      AffirmColor affirmColor, Context context, SpannablePromoCallback callback) {
    this.baseUrl = baseUrl;
    this.textSize = textSize;
    this.typeface = typeface;
    this.publicKey = publicKey;
    this.promoId = promoId;
    this.dollarAmount = amount;
    this.logoType = logoType;
    this.affirmColor = affirmColor;
    this.callback = callback;
    this.gson = gson;
    this.client = client;
    this.tracker = tracker;
    this.context = context;
  }

  CancellableRequest getPromo() {
    getNewPromoResponse();

    return new CancellableRequest() {
      @Override public void cancelRequest() {
        isCancelled = true;
        if (currentRequest != null) {
          currentRequest.cancel();
        }
      }
    };
  }

  private void getNewPromoResponse() {
    int centAmount = AffirmUtils.decimalDollarsToIntegerCents(dollarAmount);
    final AffirmRequest.Endpoint endpoint = new PromoEndpoint(promoId, centAmount, publicKey);
    final AffirmRequest<PromoResponse> request =
        new AffirmRequest<>(PromoResponse.class, baseUrl, client, gson, endpoint, tracker);
    currentRequest = request;

    request.create(new AffirmRequest.Callback<PromoResponse>() {
      @Override public void onSuccess(PromoResponse result) {
        if (!isCancelled) {
          callback.onPromoWritten(updateSpan(result.promo().ala()));
        }
      }

      @Override public void onFailure(Throwable throwable) {
        returnError(throwable);
      }
    });
  }

  private void returnError(final Throwable e) {
    callback.onFailure(e);
  }

  private SpannableString updateSpan(String template) {
    final PromoSpannable promoSpannable = new PromoSpannable();
    return promoSpannable.spannableFromEditText(template, textSize, typeface, logoType,
        affirmColor, context);
  }
}
