:no_entry: [DEPRECATED] Active at https://github.com/Affirm/affirm-merchant-sdk-android

## If this is your first time integrating this Affirm android SDK, please use the following repository:
```
github "Affirm/affirm-merchant-sdk-android"
```
Affirm Android SDK
==================

Easily integrate Affirm checkouts on merchant's native apps

## Run Sample
1. Copy the content of the `gradle.properties.template` to `sdk/gradle.properties`. Of course, this step is not necessary because we have already processed it in `build.gradle`.
2. Run the `sample` project

## Download

Download via Gradle:
```groovy
compile "com.affirm:affirm-android-sdk:1.0.14"
```
or Maven:
```xml
<dependency>
  <groupId>com.affirm</groupId>
  <artifactId>affirm-android-sdk</artifactId>
  <version>1.0.14</version>
</dependency>
```
Snapshots of the development version are available in [Sonatype's `snapshots` repository](https://oss.sonatype.org/content/repositories/snapshots/).

## Min SDK

The minimum sdk is 19. If your app supports lower versions you will have to add this to your manifest

```xml
<uses-sdk android:targetSdkVersion="your-target-version" android:minSdkVersion="your-min-sdk-version"
      tools:overrideLibrary="com.affirm.affirmsdk"/>
```

## Usage Overview
Start by creating an affirm instance.

```java
affirm = Affirm.builder()
        .setEnvironment(Affirm.Environment.SANDBOX)
        .setMerchantPublicKey("729JKW3C3DTZDTRY")
        .build();
```

### Promo Message & Prequal
```java
CancellableRequest aslowasPromo = affirm.writePromoToTextView(promo, null, 1100, AffirmDisplayTypeLogo, AffirmColorTypeBlue, true, new PromoCallback() {
      @Override public void onPromoWritten(TextView textView) {
        aslowasPromo = null;
      }

      @Override public void onFailure(TextView textView, final Throwable throwable) {
        runOnUiThread(new Runnable() {
          @Override public void run() {
            Toast.makeText(MainActivity.this, "As low as label : " + throwable.getMessage(),
                Toast.LENGTH_LONG).show();
            aslowasPromo = null;
          }
        });
      }
    });
```

And cancel request onPause
```java
@Override protected void onPause() {
  super.onPause();
  if (aslowasPromo != null) {
    aslowasPromo.cancelRequest();
  }
}
```  

### Checkout 
When you are ready to checkout with affirm create a checkout object
and launch the affirm checkout.


```java
final Checkout checkout = Checkout.builder()
        .setItems(items)
        .setBilling(shipping)
        .setShipping(shipping)
        .setShippingAmount(0f)
        .setTaxAmount(100f)
        .setTotal(1100f)
        .build();

affirm.launchCheckout(this, checkout);
```

Override onActivityResult so that affirm can handle the result.

```java
@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  if (affirm.onCheckoutActivityResult(this, requestCode, resultCode, data)) {
    return;
  }
  super.onActivityResult(requestCode, resultCode, data);
}
```

Implement Checkout callbacks.

```java
@Override public void onAffirmCheckoutSuccess(String token) {
  Toast.makeText(this, "Checkout token: " + token, Toast.LENGTH_LONG).show();
}

@Override public void onAffirmCheckoutCancelled() {
  Toast.makeText(this, "Checkout Cancelled", Toast.LENGTH_LONG).show();
}

@Override public void onAffirmCheckoutError(String message) {
  Toast.makeText(this, "Checkout Error: " + message, Toast.LENGTH_LONG).show();
}
```

### VCN Checkout
When you are ready to VCN checkout with affirm create a checkout object
and launch the affirm VCN checkout.

```java
final Checkout checkout = Checkout.builder()
        .setItems(items)
        .setBilling(shipping)
        .setShipping(shipping)
        .setShippingAmount(0f)
        .setTaxAmount(100f)
        .setTotal(1100f)
        .build();

affirm.launchVcnCheckout(this, checkout);
```

Override onActivityResult so that affirm can handle the result.

```java
@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  if (affirm.onVcnCheckoutActivityResult(this, requestCode, resultCode, data)) {
    return;
  }
  super.onActivityResult(requestCode, resultCode, data);
}
```

Implement Checkout callbacks.

```java
@Override public void onAffirmVcnCheckoutCancelled() {
  Toast.makeText(this, "Vcn Checkout Cancelled", Toast.LENGTH_LONG).show();
}

@Override public void onAffirmVcnCheckoutError(@Nullable String message) {
  Toast.makeText(this, "Vcn Checkout Error: " + message, Toast.LENGTH_LONG).show();
}

@Override public void onAffirmVcnCheckoutSuccess(@NonNull CardDetails cardDetails) {
  Toast.makeText(this, "Vcn Checkout Card: " + cardDetails.toString(), Toast.LENGTH_LONG).show();
}
```
