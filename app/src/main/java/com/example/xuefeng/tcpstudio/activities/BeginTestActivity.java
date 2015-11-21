package com.example.xuefeng.tcpstudio.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.xuefeng.tcpstudio.R;
import com.example.xuefeng.tcpstudio.widget.ProgressButton;


/**
 * Created by xuefeng on 2015/11/18.
 */
public class BeginTestActivity extends Activity {
    EditText etTestWebSite,etLinkTime,etTestName;
    Spinner spTestNetworkType;
    ProgressButton pgbStartTest;
    private static WebView wvWeb;

    private static int LoadUrlTime;
    private String Url;
    private static final int START_TEST = 1;
    private static final int CONTINUE_TEST = 2;
    private static Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String Url = msg.getData().getString("Url","");
            switch (msg.what){
                case START_TEST:
                    wvWeb.loadUrl(Url);
                    LoadUrlTime --;
                    Log.e("start load time",String.valueOf(LoadUrlTime));
                    break;
                case CONTINUE_TEST:
                    if(0 != LoadUrlTime){
                        wvWeb.loadUrl(Url);
                        LoadUrlTime --;
                    }
                    Log.e("continue load time",String.valueOf(LoadUrlTime));
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_test);

        etTestWebSite = (EditText)findViewById(R.id.etTestWebSite);
        etLinkTime = (EditText)findViewById(R.id.etLinkTime);
        etTestName = (EditText)findViewById(R.id.etTestName);

        spTestNetworkType = (Spinner)findViewById(R.id.spTestNetworkType);

        wvWeb = (WebView)findViewById(R.id.wvWeb);

        pgbStartTest = (ProgressButton)findViewById(R.id.pgbStartTest);

        wvWeb.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // TODO Auto-generated method stub
                super.onPageFinished(view, url);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = handler.obtainMessage();
                        msg.what = CONTINUE_TEST;
                        Bundle bundle = new Bundle();
                        bundle.putString("Url", Url);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }
                }).start();
                Log.e("page finished", url);
            }
        });

        wvWeb.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                // TODO Auto-generated method stub
                super.onProgressChanged(view, newProgress);
                pgbStartTest.setProgress(newProgress);
            }
        });

        wvWeb.getSettings().setJavaScriptEnabled(true);

        pgbStartTest.setOnProgressButtonClickListener(new ProgressButton.OnProgressButtonClickListener() {
            @Override
            public void onClickListener() {
                Url = etTestWebSite.getText().toString();
                LoadUrlTime = Integer.parseInt(etLinkTime.getText().toString());
                if (!TextUtils.isEmpty(Url)) {
                    //判断url前面有没有加上http://前缀，如果没有则加上
                    if (!Url.substring(0, 7).equals("http://")) {
                        Url = "http://" + Url;
                    }


                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Message msg = handler.obtainMessage();
                            msg.what = START_TEST;
                            Bundle bundle = new Bundle();
                            bundle.putString("Url", Url);
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                        }
                    }).start();
                }
            }

        });
    }
}
