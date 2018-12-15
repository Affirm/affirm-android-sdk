package com.affirm.affirmsdk;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.widget.TextView;

import static com.affirm.affirmsdk.AffirmLogoType.AffirmDisplayTypeText;

public class PromoSpannable {
  private static final String LOGO_PLACEHOLDER = "{affirm_logo}";
  private final Paint paint;

  public PromoSpannable() {
    paint = new Paint();
    paint.setStyle(Paint.Style.FILL);
  }

  private ImageSpan getLogoSpan(float textSize, @NonNull Drawable logoDrawable, int color) {

    float logoHeight = textSize * 1.f;
    float ratio = (float) logoDrawable.getIntrinsicWidth() / logoDrawable.getIntrinsicHeight();

    logoDrawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

    logoDrawable.setBounds(0, 0, Math.round(logoHeight * ratio), Math.round(logoHeight));
    ImageSpan imageSpan = new ImageSpan(logoDrawable, ImageSpan.ALIGN_BASELINE);

    return imageSpan;
  }

  public SpannableString getSpannable(@NonNull String template, float textSize,
      @Nullable Drawable logoDrawable, @NonNull Typeface typeface, @Nullable int color) {

    paint.setTextSize(textSize);
    paint.setTypeface(typeface);
    Rect result = new Rect();
    paint.getTextBounds(template.toUpperCase(), 0, template.length(), result);

    SpannableString spannableString;

    int index = template.indexOf(LOGO_PLACEHOLDER);
    if (logoDrawable != null && index != -1) {
      spannableString = new SpannableString(template);
      ImageSpan imageSpan = getLogoSpan(textSize, logoDrawable, color);
      spannableString.setSpan(imageSpan, index, index + LOGO_PLACEHOLDER.length(),
          Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
    } else {
      String onlyText = template.replace(LOGO_PLACEHOLDER, "");
      spannableString = new SpannableString(onlyText);
    }

    return spannableString;
  }

  public SpannableString spannableFromEditText(@NonNull String template, float textSize,
      @NonNull Typeface typeface, @NonNull AffirmLogoType logoType,
      @NonNull AffirmColor affirmColor, @NonNull Context context) {

    Resources resources = context.getResources();

    Drawable logoDrawable = null;
    if (logoType != AffirmDisplayTypeText) {
      logoDrawable = resources.getDrawable(logoType.getDrawableRes());
    }

    int color = resources.getColor(affirmColor.getColorRes());

    return getSpannable(template, textSize, logoDrawable, typeface, color);
  }

  @Deprecated
  public SpannableString spannableFromEditText(@NonNull TextView textView, @NonNull String template,
      @NonNull String payment, @NonNull AffirmLogoType logoType, @NonNull AffirmColor affirmColor) {

    Resources resources = textView.getContext().getResources();

    Drawable logoDrawable = null;
    if (logoType != AffirmDisplayTypeText) {
      logoDrawable = resources.getDrawable(logoType.getDrawableRes());
    }

    int color = resources.getColor(affirmColor.getColorRes());

    return getSpannable(template, textView.getTextSize(), logoDrawable, textView.getTypeface(),
        color);
  }
}
