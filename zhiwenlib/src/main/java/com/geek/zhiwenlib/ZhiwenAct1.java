package com.geek.zhiwenlib;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.geek.zhiwenlib.utils.FingerprintUtil;

import io.reactivex.observers.DisposableObserver;

public class ZhiwenAct1 extends AppCompatActivity {

    private FingerPrinterView fingerPrinterView;
    private RxFingerPrinter rxfingerPrinter;
    private DisposableObserver<IdentificationInfo> observer;
    private Button mBtnOpen;
    private Button fingerprint_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zhiwen1);
        fingerPrinterView = (FingerPrinterView) findViewById(R.id.fpv);
        mBtnOpen = (Button) findViewById(R.id.btn_open);
        fingerprint_password = (Button) findViewById(R.id.fingerprint_password);
        fingerPrinterView.setOnStateChangedListener(new FingerPrinterView.OnStateChangedListener() {
            @Override
            public void onChange(int state) {
                if (state == FingerPrinterView.STATE_WRONG_PWD) {
                    fingerPrinterView.setState(FingerPrinterView.STATE_NO_SCANING);
                }
            }
        });
        rxfingerPrinter = new RxFingerPrinter(this);
        rxfingerPrinter.setLogging(true);
        mBtnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableButton(false);
                createObserver();
                rxfingerPrinter.begin().subscribe(observer);
            }
        });
        fingerprint_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FingerprintUtil.startFingerprintRecognition(ZhiwenAct1.this, null);
            }
        });
    }

    private void createObserver() {
        observer = new DisposableObserver<IdentificationInfo>() {
            @Override
            protected void onStart() {
                if (fingerPrinterView.getState() == FingerPrinterView.STATE_SCANING) {
                    return;
                } else if (fingerPrinterView.getState() == FingerPrinterView.STATE_CORRECT_PWD
                        || fingerPrinterView.getState() == FingerPrinterView.STATE_WRONG_PWD) {
                    fingerPrinterView.setState(FingerPrinterView.STATE_NO_SCANING);
                } else {
                    fingerPrinterView.setState(FingerPrinterView.STATE_SCANING);
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {

            }

            @Override
            public void onNext(IdentificationInfo info) {
                if (info.isSuccessful()) {
                    fingerPrinterView.setState(FingerPrinterView.STATE_CORRECT_PWD);
                    Toast.makeText(ZhiwenAct1.this, "??????????????????", Toast.LENGTH_SHORT).show();
                    enableButton(true);
                } else {
                    FPerException exception = info.getException();
                    if (exception != null) {
                        Toast.makeText(ZhiwenAct1.this, exception.getDisplayMessage(), Toast.LENGTH_SHORT).show();
                    }
                    fingerPrinterView.setState(FingerPrinterView.STATE_WRONG_PWD);
                }
            }
        };
    }

    private void enableButton(boolean enable) {
        mBtnOpen.setClickable(enable);
        mBtnOpen.setEnabled(enable);
    }
}
