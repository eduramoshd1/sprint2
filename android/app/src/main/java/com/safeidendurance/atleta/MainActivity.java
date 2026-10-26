package com.safeidendurance.atleta;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Base de homologação. Não contém credenciais nem ponte JavaScript nativa. */
public final class MainActivity extends Activity {
    private WebView web;
    private TextView status;
    private final Uri portal = Uri.parse(BuildConfig.PORTAL_URL);

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout toolbar = new LinearLayout(this);
        Button back = new Button(this); back.setText("Voltar");
        Button home = new Button(this); home.setText("Início");
        Button refresh = new Button(this); refresh.setText("Atualizar");
        Button share = new Button(this); share.setText("Compartilhar");
        for (Button b : new Button[]{back, home, refresh, share})
            toolbar.addView(b, new LinearLayout.LayoutParams(0, 56, 1));
        layout.addView(toolbar);
        status = new TextView(this);
        status.setPadding(16, 12, 16, 12);
        status.setVisibility(View.GONE);
        layout.addView(status);
        web = new WebView(this);
        layout.addView(web, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(layout);
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setSupportMultipleWindows(false);
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, false);
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if ("https".equals(uri.getScheme()) && portal.getHost().equals(uri.getHost())
                        && (uri.getPort() == -1 || uri.getPort() == 443)) return false;
                if (request.isForMainFrame()) openExternal(uri);
                return true;
            }
            @Override public void onPageFinished(WebView view, String url) {
                CookieManager.getInstance().flush();
            }
            @Override public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    status.setText("Não foi possível conectar. Verifique a internet e toque em Atualizar.");
                    status.setVisibility(View.VISIBLE);
                }
            }
        });
        back.setOnClickListener(v -> { if (web.canGoBack()) web.goBack(); });
        home.setOnClickListener(v -> web.loadUrl(portal.toString()));
        refresh.setOnClickListener(v -> { status.setVisibility(View.GONE); web.reload(); });
        share.setOnClickListener(v -> {
            Intent message = new Intent(Intent.ACTION_SEND);
            message.setType("text/plain");
            // Compartilha somente a entrada pública, nunca a URL privada da tela.
            message.putExtra(Intent.EXTRA_TEXT, portal.toString());
            startActivity(Intent.createChooser(message, "Compartilhar ID-ENTUTA"));
        });
        web.loadUrl(portal.toString());
    }

    private void openExternal(Uri uri) {
        String scheme = uri.getScheme();
        if (!("https".equals(scheme) || "tel".equals(scheme) || "mailto".equals(scheme))) return;
        try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); }
        catch (ActivityNotFoundException error) {
            new AlertDialog.Builder(this).setMessage("Nenhum aplicativo disponível para abrir este link.")
                .setPositiveButton("OK", null).show();
        }
    }

    @Override public void onBackPressed() {
        if (web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
    @Override protected void onDestroy() {
        web.destroy();
        super.onDestroy();
    }
}
