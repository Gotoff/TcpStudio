package com.example.xuefeng.tcpstudio;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.xuefeng.tcpstudio.utils.ShellUtils;
import com.example.xuefeng.tcpstudio.utils.TcpdumpCmdHelper;


public class MainActivity extends Activity {
    EditText etWebSite;
    WebView wvWebPage;
    Button btnConnect;
    ProgressBar pbWebPage;
    Button btnStartCapture,btnStopCapture;
    boolean CaptureSuccessed;
    boolean isRooted = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etWebSite = (EditText) findViewById(R.id.etWebSite);
        wvWebPage = (WebView) findViewById(R.id.wvWebPage);
        btnConnect = (Button) findViewById(R.id.btnConnect);
        pbWebPage = (ProgressBar) findViewById(R.id.pbWebPage);
        btnStartCapture = (Button) findViewById(R.id.btnStartCapture);
        btnStopCapture = (Button) findViewById(R.id.btnStopCapture);

        wvWebPage.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // TODO Auto-generated method stub
                super.onPageFinished(view, url);

            }
        });

        wvWebPage.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                // TODO Auto-generated method stub
                super.onProgressChanged(view, newProgress);
                pbWebPage.setProgress(newProgress);
            }


        });

        wvWebPage.getSettings().setJavaScriptEnabled(true);

        btnConnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String Url = etWebSite.getText().toString();
                if(!TextUtils.isEmpty(Url)){
                    //判断url前面有没有加上http://前缀，如果没有则加上
                    if(!Url.substring(0, 7).equals("http://")){
                        Url = "http://"+Url;
                    }
                    wvWebPage.loadUrl(Url);
                    pbWebPage.setProgress(0);

                }
            }
        });

        btnStartCapture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        CaptureSuccessed = TcpdumpCmdHelper.startCapture(MainActivity.this,
                                Environment.getExternalStorageDirectory() + "/" + System.currentTimeMillis() + "capture.pcap");
                        if(CaptureSuccessed){
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(MainActivity.this, "Capture Success!", Toast.LENGTH_LONG).show();
                                }
                            });
                        }else{
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(MainActivity.this, "Capture Failed.", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }).start();

            }
        });

        btnStopCapture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        TcpdumpCmdHelper.stopCapture(MainActivity.this);
                    }
                }).start();

            }
        });

        isRooted = ShellUtils.checkRootPermission();
        Toast.makeText(MainActivity.this, "Root State:"+String.valueOf(isRooted), Toast.LENGTH_LONG).show();
    }
}
