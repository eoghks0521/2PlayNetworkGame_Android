package com.example.administrator.networkgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.os.Handler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class GameSurface extends SurfaceView implements SurfaceHolder.Callback {


    private String msg;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String str;
    private byte[] bb;
    private byte[] bb2;
    private String str2;
    private byte[] b2 = new byte[64];

    private Bitmap bg;

    private boolean fireOk = false;

    private int VectX = 5, VectY = 10;
    private int passVecX = 0, passVecY = 0;
    private int RvX = 0, RvY = 0;
    private String ip; //= "172.30.1.30";//"10.0.2.2";
    private int port = 9999;

    private int xpos,ypos;

    private Handler handler;

    private GestureDetector gestureDetector;



    private GameThread gameThread;

    private final List<FireBall> fireBallList = new ArrayList<FireBall>();
    private final List<CompCharacter> compCharacterList = new ArrayList<CompCharacter>();
    private final List<UserCharacter> userCharacterList = new ArrayList<UserCharacter>();
    private final List<Explosion> explosionList = new ArrayList<Explosion>();


    private UserCharacter user1;
    private CompCharacter[] comp;
    private UserCharacter user2;
    private int fireballCount = 100;

    public interface OnChangeCountListener {
        public void onChangeCount(int count);
    }
    public interface OnEndListener {
        public void onEndListener(int notice);
    }


    OnChangeCountListener onChangeBallsListener = null;
    OnEndListener onEndPassListener = null;

    public void setOnChangeCountListener(OnChangeCountListener listener) {
        onChangeBallsListener = listener;
    }
    public void setOnEndPassListener(OnEndListener listener) {
        onEndPassListener = listener;
    }


    public GameSurface(Context context, String ip) {
        super(context);

        // Make Game Surface focusable so it can handle events.
        this.setFocusable(true);

        // Sét callback.
        this.getHolder().addCallback(this);

        gestureDetector = new GestureDetector(context, new GestureListener());
        this.ip = ip;
    }

    public void update() {

        if(user1 != null) {
            this.user1.update();
        }
        if (user2 != null) {
            this.user2.update();
            if (passVecX != RvX && passVecY != RvY) {
                user2.setMovingVector(RvX, RvY);
                passVecX = RvX;
                passVecY = RvY;
            }
            user2.setPos(xpos,ypos);
        }


        Iterator<FireBall> it_fire = this.fireBallList.iterator();
        Iterator<CompCharacter> it_comp = this.compCharacterList.iterator();
        Iterator<UserCharacter> it_user = this.userCharacterList.iterator();

        while (it_user.hasNext()) {
            UserCharacter userX = it_user.next();
            while (it_fire.hasNext()) {
                FireBall fireBall = it_fire.next();

                if (fireBall.isFinish()) {
                    // If explosion finish, Remove the current element from the iterator & list.
                    it_fire.remove();
                    continue;
                }
                if (userX.getX() < fireBall.getX() && fireBall.getX() < userX.getX() + userX.getWidth()
                        && userX.getY() < fireBall.getY() && fireBall.getY() < userX.getY() + userX.getHeight()) {

                    if(fireBall.getFireLife()<18)continue;

                    Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.explosion);
                    if(userX == user2) {
                        user2 = null;
                        Explosion explosion = new Explosion(this, bitmap, userX.getX(), userX.getY());
                        this.explosionList.add(explosion);
                        it_fire.remove();

                        if (onEndPassListener != null) {
                            int notice = 20;
                            onEndPassListener.onEndListener(notice);
                        }

                    }else {
                        user1 = null;
                        Explosion explosion = new Explosion(this, bitmap, userX.getX(), userX.getY());
                        this.explosionList.add(explosion);
                        it_fire.remove();

                        if (onEndPassListener != null) {
                            int notice = 10;
                            onEndPassListener.onEndListener(notice);
                        }

                    }
                    break;
                }
            }
            it_fire = this.fireBallList.iterator();
        }
        while (it_comp.hasNext()) {

            CompCharacter compX = it_comp.next();

            while (it_fire.hasNext()) {

                FireBall fireBall = it_fire.next();

                if (fireBall.isFinish()) {
                    // If explosion finish, Remove the current element from the iterator & list.
                    it_fire.remove();
                    continue;
                }

                if (compX.getX() < fireBall.getX() && fireBall.getX() < compX.getX() + compX.getWidth()
                        && compX.getY() < fireBall.getY() && fireBall.getY() < compX.getY() + compX.getHeight()) {
                    // Remove the current element from the iterator and the list.
                    it_comp.remove();
                    compX.setFinish(true);
                    // Create Explosion object.
                    Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.explosion);
                    Explosion explosion = new Explosion(this, bitmap, compX.getX(), compX.getY());

                    this.explosionList.add(explosion);
                    it_fire.remove();
                    break;
                }
            }
            it_fire = this.fireBallList.iterator();
        }

        for (CompCharacter compCharacter : this.compCharacterList) {

            if(user1 != null) compCharacter.setUser1(user1);
            if (user2 != null) compCharacter.setUser2(user2);
            compCharacter.update();
        }
        for (Explosion explosion : this.explosionList) {
            explosion.update();
        }
        for (FireBall fireBall : this.fireBallList) {
            fireBall.update();
        }

        Iterator<Explosion> iterator4 = this.explosionList.iterator();
        while (iterator4.hasNext()) {
            Explosion explosion = iterator4.next();

            if (explosion.isFinish()) {
                // If explosion finish, Remove the current element from the iterator & list.
                iterator4.remove();
                continue;
            }
        }
        if (dos != null) {
            try {
                if (fireOk) {
                    dos.write(bb2);
                    dos.flush();
                    fireOk = false;
                    Log.d("PackKwon2","전송완료");
                } else {

                    int code = 200;
                    str2 = Integer.toString(user1.getX()) + " " + Integer.toString(user1.getY())
                            + " " + Integer.toString(VectX) + " " + Integer.toString(VectY) + " " + Integer.toString(code) + " ";
                    String s = String.format("%-64s", str2);
                    bb = s.getBytes();
                    dos.write(bb);
                    dos.flush();
                }
            } catch (Exception e) {

            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int x = (int) event.getX();
                int y = (int) event.getY();

                int movingVectorX = x - this.user1.getX();
                int movingVectorY = y - this.user1.getY();

                VectX = movingVectorX;
                VectY = movingVectorY;

                this.user1.setMovingVector(movingVectorX, movingVectorY);
                break;

        }

        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawBitmap(bg,0, 0, null);

        if(user1 != null) {
            this.user1.draw(canvas);
        }
        if (user2 != null) {
            this.user2.draw(canvas);
        }

        for (CompCharacter comp : this.compCharacterList) {
            comp.draw(canvas);
        }
        for (FireBall fireBall : this.fireBallList) {
            fireBall.draw(canvas);
        }

        for (Explosion explosion : this.explosionList) {
            explosion.draw(canvas);
        }


    }

    // Implements method of SurfaceHolder.Callback

    public void createmonster(){
        Bitmap compBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.monster);
        this.comp = new CompCharacter[30];

        Random r2 = new Random(1000);
        for (int i = 0; i < 30; i++) {
            this.comp[i] = new CompCharacter(this, compBitmap, r2.nextInt(getWidth()), r2.nextInt(getHeight()));
            this.compCharacterList.add(comp[i]);
        }
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        handler = new Handler();


        Bitmap userBitmap1 = BitmapFactory.decodeResource(this.getResources(), R.drawable.cowboy);

        Bitmap mBitmap = BitmapFactory.decodeResource(this.getResources(),R.drawable.ace);
        bg = Bitmap.createScaledBitmap(mBitmap, getWidth(), getHeight(), true);

        Random r = new Random();
        int xx = r.nextInt(getWidth());
        int yy =  r.nextInt(getHeight());
        this.user1 = new UserCharacter(this, userBitmap1,xx ,yy);
        Log.d("QRQRQRQR","====="+ xx + " " + yy);

        this.userCharacterList.add(user1);
        // Socket making thread
        new Thread(new Runnable() {
            boolean create = true;
            int aaa;
            int bbb;
            public void run() {
                try {
                    setSocket(ip, port);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while (true) {
                    try {
                        dis.read(b2,0,64);

                        if (b2 != null) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    msg = new String(b2);
                                    msg = msg.trim();

                                    Scanner scanner = new Scanner(msg);

                                    for (int i = 0; i < 4; i++) {
                                        scanner.next();
                                    }
                                    int a = scanner.nextInt();
                                    Log.d("GuSCANNER","====="+a);
                                    if (a == 200) {
                                        Scanner scanner2 = new Scanner(msg);
                                        if (create) {
                                            Bitmap user2Bit = BitmapFactory.decodeResource(GameSurface.this.getResources(), R.drawable.chibi2);
                                            aaa = scanner2.nextInt();
                                            bbb = scanner2.nextInt();
                                            user2 = new UserCharacter(GameSurface.this, user2Bit, aaa, bbb);
                                            Log.d("KAKAKAKA","====="+ aaa + " " + bbb);
                                            GameSurface.this.userCharacterList.add(user2);
                                            Log.w("ace3", "상대편 생성");
                                            createmonster();
                                            fireballCount=100;
                                            create = false;
                                        } else {
                                            xpos = scanner2.nextInt();
                                            ypos = scanner2.nextInt();
                                            RvX = scanner2.nextInt();
                                            RvY = scanner2.nextInt();
                                        }
                                    } else if(a==100){
                                        Log.d("PackKwon4","생성완료");
                                        Scanner scanner2 = new Scanner(msg);
                                        Bitmap bitmap = BitmapFactory.decodeResource(GameSurface.this.getResources(), R.drawable.fireball);
                                        FireBall fire = new FireBall(GameSurface.this, bitmap, scanner2.nextInt(), scanner2.nextInt());

                                        GameSurface.this.fireBallList.add(fire);

                                        fire.setMovingVector(scanner2.nextInt(), scanner2.nextInt());
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();


        this.gameThread = new GameThread(this, holder);
        this.gameThread.setRunning(true);
        this.gameThread.start();
    }

    // Implements method of SurfaceHolder.Callback
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }


    // Implements method of SurfaceHolder.Callback
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        while (retry) {
            try {
                this.gameThread.setRunning(false);

                // Parent thread must wait until the end of GameThread.
                this.gameThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retry = true;
        }
    }

    public void setSocket(String ip, int port) throws IOException {
        try {
            socket = new Socket(ip, port);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        // event when double tap occurs
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (fireballCount > 0 && user1 != null) {
                int x2 = (int) e.getX();
                int y2 = (int) e.getY();

                int movingVectorX2 = x2 - GameSurface.this.user1.getX();
                int movingVectorY2 = y2 - GameSurface.this.user1.getY();

                Bitmap bitmap = BitmapFactory.decodeResource(GameSurface.this.getResources(), R.drawable.fireball);
                FireBall fire = new FireBall(GameSurface.this, bitmap, user1.getX(), user1.getY());



                GameSurface.this.fireBallList.add(fire);

                fire.setMovingVector(movingVectorX2, movingVectorY2);
                fireballCount--;

                if (onChangeBallsListener != null) {
                    onChangeBallsListener.onChangeCount(fireballCount);
                }

                int code = 100;
                int dumy = 12345;

                str = Integer.toString(user1.getX()) + " " + Integer.toString(user1.getY()) + " "
                        + Integer.toString(movingVectorX2) + " " + Integer.toString(movingVectorX2) + " " + Integer.toString(code) + " "+ Integer.toString(dumy);
                String s = String.format("%-64s", str);
                bb2 = s.getBytes();
                fireOk = true;

            }
            return true;
        }
    }
}