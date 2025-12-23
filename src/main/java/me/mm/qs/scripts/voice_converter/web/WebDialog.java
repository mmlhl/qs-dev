package me.mm.qs.scripts.voice_converter.web;

import me.mm.qs.script.QScriptBase;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import static me.mm.qs.script.Globals.*;

/**
 * Web 对话框工具类
 * 显示一个充满对话框的 WebView
 */
public class WebDialog extends QScriptBase {

    /**
     * 显示 WebView 对话框（支持返回按钮导航）
     * 
     * @param title 对话框标题
     * @param url 要加载的 URL（可以是 http、https 或本地路径）
     */
    public void showWebDialog(String title, String url) {
        final Activity activity = getActivity();
        if (activity == null) {
            toast("无法获取Activity");
            return;
        }

        // 必须在主线程显示对话框
        activity.runOnUiThread(new Runnable() {
            public void run() {
                // 创建 WebView
                final WebView webView = new WebView(context);
                
                // 使用自定义 WebViewClient 拦截 bsh:// 协议
                webView.setWebViewClient(new WebViewClient() {
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        if (url != null && url.startsWith("bsh://")) {
                            try {
                                // 解析 bsh://run?code=xxx
                                String code = url.substring("bsh://run?".length());
                                code = java.net.URLDecoder.decode(code, "UTF-8");
                                eval(code);
                            } catch (Exception e) {
                                error(e);
                            }
                            return true;
                        }
                        return false;
                    }
                });
                
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setDomStorageEnabled(true);
                webView.getSettings().setDatabaseEnabled(true);

                // 加载 URL
                webView.loadUrl(url);

                // 创建并显示对话框
                AlertDialog dialog = new AlertDialog.Builder(activity, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
                        .setTitle(title)
                        .setView(webView)
                        .setNegativeButton("关闭", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();

                // 设置返回按钮拦截
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                            if (webView.canGoBack()) {
                                webView.goBack();
                                return true;
                            } else {
                                return true;
                            }
                        }
                        return true;
                    }
                });

                dialog.show();
            }
        });
    }

    /**
     * 显示本地 HTML 内容（支持返回按钮导航）
     * 
     * @param title 对话框标题
     * @param htmlContent HTML 内容
     */
    public void showWebDialogWithHtml(String title, String htmlContent) {
        final Activity activity = getActivity();
        if (activity == null) {
            toast("无法获取Activity");
            return;
        }

        // 必须在主线程显示对话框
        activity.runOnUiThread(new Runnable() {
            public void run() {
                // 创建 WebView
                final WebView webView = new WebView(context);
                
                // 使用自定义 WebViewClient 拦截 bsh:// 协议
                webView.setWebViewClient(new WebViewClient() {
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        if (url != null && url.startsWith("bsh://")) {
                            try {
                                // 解析 bsh://run?code=xxx
                                String code = url.substring("bsh://run?".length());
                                code = java.net.URLDecoder.decode(code, "UTF-8");
                                eval(code);
                            } catch (Exception e) {
                                error(e);
                            }
                            return true;
                        }
                        return false;
                    }
                });
                
                webView.getSettings().setJavaScriptEnabled(true);

                // 加载 HTML 内容
                webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);

                // 创建并显示对话框
                AlertDialog dialog = new AlertDialog.Builder(activity, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
                        .setTitle(title)
                        .setView(webView)
                        .setNegativeButton("关闭", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();

                // 设置返回按钮拦截
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                            if (webView.canGoBack()) {
                                webView.goBack();
                                return true;
                            } else {
                                dialog.dismiss();
                                return true;
                            }
                        }
                        return false;
                    }
                });

                dialog.show();
            }
        });
    }
}
