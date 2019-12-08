package com.example.administrator.networkgame;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

public class StartActivity extends AppCompatActivity {

    private FrameLayout frameLayout;
    private GameSurface gameSurface;
    private TextView textview;
    private String ip;
    private AlertDialog.Builder alertDialogBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("게임 결과");


        Intent intent = getIntent();
        ip = intent.getStringExtra("ip");

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        frameLayout = findViewById(R.id.frameLayout);
        gameSurface = new GameSurface(this, ip);
        frameLayout.addView(gameSurface);

        gameSurface.setOnChangeCountListener(new GameSurface.OnChangeCountListener() {
            @Override
            public void onChangeCount(int count) {
                TextView textView = (TextView) findViewById(R.id.tv_count);
                textView.setText("공격 가능 : " + count);
            }
        });
        gameSurface.setOnEndPassListener(new GameSurface.OnEndListener() {
            @Override
            public void onEndListener(int notice) {
                if (notice == 10) {
                    alertDialogBuilder
                            .setMessage("당신의 승리")
                            .setCancelable(false)
                            .setPositiveButton("종료",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                            Intent intent =  new Intent(StartActivity.this,MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    })
                            .setNegativeButton("취소",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) { dialog.cancel();
                                        }
                                    });

                } else {
                    alertDialogBuilder
                            .setMessage("당신의 패배")
                            .setCancelable(false)
                            .setPositiveButton("종료",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            Intent intent =  new Intent(StartActivity.this,MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    })
                            .setNegativeButton("취소",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) { dialog.cancel();
                                        }
                                    });

                }
            }
        });


        //this.setContentView(new GameSurface(this));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
            }
        }
    }
}
