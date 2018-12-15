package com.affirm.affirmsdk;

import android.support.annotation.NonNull;
import com.affirm.affirmsdk.AffirmRequest.Endpoint;
import okhttp3.Request;

final class PromoEndpoint implements Endpoint {
  private final String promoId;
  private final String publicKey;
  private final int centAmount;

  PromoEndpoint(@NonNull String promoId, int centAmount, @NonNull String publicKey) {
    this.promoId = promoId;
    this.centAmount = centAmount;
    this.publicKey = publicKey;
  }

  @Override public String getPath() {
    return String.format(
        "/api/promos/v2/%s?field=ala&amount=%d&page_type=product&promo_external_id=%s", publicKey,
        centAmount, promoId);
  }

  @Override public Request completeBuilder(Request.Builder builder) {
    return builder.get().build();
  }
}
