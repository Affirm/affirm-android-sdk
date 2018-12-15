package com.affirm.affirmsdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.view.View;
import android.widget.TextView;
import com.affirm.affirmsdk.di.AffirmInjector;
import com.affirm.affirmsdk.models.CardDetails;
import com.affirm.affirmsdk.models.Checkout;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.affirm.affirmsdk.ModalActivity.ModalType.PRODUCT;
import static com.affirm.affirmsdk.ModalActivity.ModalType.SITE;

public final class Affirm {
  private static final int CHECKOUT_REQUEST = 8076;
  private static final int VCN_CHECKOUT_REQUEST = 8077;
  private static volatile Affirm instance;
  private String merchant;
  private Environment environment;
  private String name;

  private final AffirmInjector component;

  public interface CheckoutCallbacks {
    void onAffirmCheckoutError(@Nullable String message);

    void onAffirmCheckoutCancelled();

    void onAffirmCheckoutSuccess(@NonNull String token);
  }

  public interface VcnCheckoutCallbacks {
    void onAffirmVcnCheckoutError(@Nullable String message);

    void onAffirmVcnCheckoutCancelled();

    void onAffirmVcnCheckoutSuccess(@NonNull CardDetails cardDetails);
  }

  public enum Environment {
    SANDBOX("sandbox.affirm.com", "tracker.affirm.com"),
    PRODUCTION("api-cf.affirm.com", "tracker.affirm.com");

    final String baseUrl1;
    final String kibanaBaseUrl;

    Environment(String baseUrl1, String kibanaBaseUrl) {
      this.baseUrl1 = baseUrl1;
      this.kibanaBaseUrl = kibanaBaseUrl;
    }

    @Override public String toString() {
      return "Environment{" + baseUrl1 + ", " + kibanaBaseUrl + '}';
    }
  }

  private Affirm(String merchant, Environment environment, String name) {
    this.merchant = merchant;
    this.environment = environment;
    this.name = name;

    component = new AffirmInjector.Builder().setEnv(environment).setMerchantKey(merchant).build();

    synchronized (Affirm.class) {
      if (instance != null) {
        throw new IllegalStateException("Affirm instance already exist");
      }
      instance = this;
    }
  }

  /**
   * Gets Current Affirm instance
   */
  public static @Nullable Affirm getInstance() {
    return instance;
  }

  /**
   * Launches Product Modal Activity
   */
  public void launchProductModal(@NonNull Context context, float amount, @Nullable String modalId) {
    ModalActivity.launch(context, merchant, amount, environment.baseUrl1, PRODUCT, modalId);
  }

  /**
   * Launches Site Modal Activity
   */
  public void launchSiteModal(@NonNull Context context, @Nullable String modalId) {
    ModalActivity.launch(context, merchant, 0f, environment.baseUrl1, SITE, modalId);
  }

  /**
   * Launches a CheckoutActivity. Don't forget to call onActivityResult to get the processed result
   */
  public void launchCheckout(@NonNull Activity activity, @NonNull Checkout checkout) {
    CheckoutActivity.launchCheckout(activity, CHECKOUT_REQUEST, merchant, checkout, name);
  }

  /**
   * Launches a VcnCheckoutActivity. Don't forget to call onActivityResult to get the processed
   * result
   */
  public void launchVcnCheckout(@NonNull Activity activity, @NonNull Checkout checkout) {
    VcnCheckoutActivity.launchCheckout(activity, VCN_CHECKOUT_REQUEST, merchant, checkout, name);
  }

  /**
   * Helper method to get the Result from the launched CheckoutActivity
   */
  public boolean onCheckoutActivityResult(CheckoutCallbacks callbacks, int requestCode,
      int resultCode, Intent data) {
    if (requestCode == CHECKOUT_REQUEST) {
      switch (resultCode) {
        case RESULT_OK:
          callbacks.onAffirmCheckoutSuccess(data.getStringExtra(CheckoutActivity.CHECKOUT_TOKEN));
          break;
        case RESULT_CANCELED:
          callbacks.onAffirmCheckoutCancelled();
          break;
        case CheckoutActivity.RESULT_ERROR:
          callbacks.onAffirmCheckoutError(data.getStringExtra(CheckoutActivity.CHECKOUT_ERROR));
          break;
        default:
      }

      return true;
    }

    return false;
  }

  /**
   * Helper method to get the Result from the launched VcnCheckoutActivity
   */
  public boolean onVcnCheckoutActivityResult(VcnCheckoutCallbacks callbacks, int requestCode,
      int resultCode, Intent data) {
    if (requestCode == VCN_CHECKOUT_REQUEST) {
      switch (resultCode) {
        case RESULT_OK:
          callbacks.onAffirmVcnCheckoutSuccess(
              (CardDetails) data.getParcelableExtra(VcnCheckoutActivity.CREDIT_DETAILS));
          break;
        case RESULT_CANCELED:
          callbacks.onAffirmVcnCheckoutCancelled();
          break;
        case CheckoutActivity.RESULT_ERROR:
          callbacks.onAffirmVcnCheckoutError(
              data.getStringExtra(VcnCheckoutActivity.CHECKOUT_ERROR));
          break;
        default:
      }

      return true;
    }

    return false;
  }

  /**
   * Starts a job that will write the as low as span (text and logo) on a TextView
   *
   * @param amount (Float) eg 112.02 as $112 and ¢2
   */
  public CancellableRequest writePromoToTextView(@NonNull String promoId, final float amount,
      float textSize, Typeface typeface, @NonNull AffirmLogoType logoType,
      @NonNull AffirmColor affirmColor, @NonNull Context context,
      @NonNull SpannablePromoCallback promoCallback) {

    final PromoJob promoJob = new PromoJob(component.provideGson(), component.provideOkHttpClient(),
        component.provideTracking(), merchant, environment.baseUrl1, textSize, typeface, promoId,
        amount, logoType, affirmColor, context, promoCallback);
    return promoJob.getPromo();
  }

  /**
   * DEPRECATED - use the above method
   *
   * Starts a job that will write the as low as span (text and logo) on a TextView
   *
   * @param amount (Float) eg 112.02 as $112 and ¢2
   */
  @Deprecated public CancellableRequest writePromoToTextView(@NonNull final TextView textView,
      @NonNull String promoId, final float amount, @NonNull AffirmLogoType logoType,
      @NonNull AffirmColor affirmColor, @NonNull final PromoCallback promoCallback) {

    textView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        launchProductModal(textView.getContext(), amount, null);
      }
    });

    final SpannablePromoCallback spannablePromoCallback = new SpannablePromoCallback() {
      @Override public void onFailure(Throwable throwable) {
        promoCallback.onFailure(textView, throwable);
      }

      @Override public void onPromoWritten(final SpannableString editable) {
        textView.post(new Runnable() {
          @Override public void run() {
            textView.setText(editable);
            promoCallback.onPromoWritten(textView);
          }
        });
      }
    };

    final PromoJob promoJob = new PromoJob(component.provideGson(), component.provideOkHttpClient(),
        component.provideTracking(), merchant, environment.baseUrl1, textView.getTextSize(),
        textView.getTypeface(), promoId, amount, logoType, affirmColor, textView.getContext(),
        spannablePromoCallback);
    return promoJob.getPromo();
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Affirm Builder.
   */
  public static final class Builder {
    private String publicKey;
    private Environment environment = Environment.SANDBOX;
    private String name;

    private Builder() {
    }

    public Builder setMerchantPublicKey(@NonNull String publicKey) {
      this.publicKey = publicKey;
      return this;
    }

    public Builder setEnvironment(@NonNull Environment environment) {
      this.environment = environment;
      return this;
    }

    @Deprecated public Builder setFinancialProductKey(@NonNull String financialProductKey) {
      return this;
    }

    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    public Affirm build() {
      if (publicKey == null) {
        throw new IllegalArgumentException("public key cannot be null");
      }

      return new Affirm(publicKey, environment, name);
    }
  }
}
