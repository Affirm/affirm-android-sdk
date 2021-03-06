package com.affirm.affirmsdk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

class PopUpWebChromeClient extends WebChromeClient {

  interface Callbacks {
    void chromeLoadCompleted();
  }

  private final Callbacks callback;

  PopUpWebChromeClient(@NonNull Callbacks callback) {
    this.callback = callback;
  }

  @Override public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture,
      Message resultMsg) {
    final WebView.HitTestResult result = view.getHitTestResult();
    final String data = result.getExtra();
    final Context context = view.getContext();
    final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(data));
    context.startActivity(browserIntent);
    return false;
  }

  @Override public boolean onConsoleMessage(ConsoleMessage cm) {
    if (BuildConfig.DEBUG && cm.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
      Log.e("Affirm", cm.message() + " -- From line " + cm.lineNumber() + " of " + cm.sourceId());
      return true;
    }
    return false;
  }

  @Override
  public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
    new AlertDialog.Builder(view.getContext()).setTitle("Affirm")
        .setMessage(message)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            result.confirm();
          }
        })
        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            result.cancel();
          }
        })
        .create()
        .show();
    return true;
  }

  public void onProgressChanged(WebView view, int progress) {
    if (progress > 99) {
      callback.chromeLoadCompleted();
    }
  }
}
