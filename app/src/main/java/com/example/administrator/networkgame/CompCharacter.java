package com.example.administrator.networkgame;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.Random;

public class CompCharacter extends GameObject {

    private static final int ROW_TOP_TO_BOTTOM = 0;
    private static final int ROW_RIGHT_TO_LEFT = 1;
    private static final int ROW_LEFT_TO_RIGHT = 2;
    private static final int ROW_BOTTOM_TO_TOP = 3;

    // Row index of Image are being used.
    private int rowUsing = ROW_LEFT_TO_RIGHT;

    private int colUsing;

    private Bitmap[] leftToRights;
    private Bitmap[] rightToLefts;
    private Bitmap[] topToBottoms;
    private Bitmap[] bottomToTops;
    private boolean finish = false;

    // Velocity of game character (pixel/millisecond)
    public static final float VELOCITY = 0.5f;

    private int movingVectorX = 10;
    private int movingVectorY = 5;

    private long lastDrawNanoTime = -1;

    private GameSurface gameSurface;

    private Random random;
    private UserCharacter user1;
    private UserCharacter user2;

    public CompCharacter(GameSurface gameSurface, Bitmap image, int x, int y) {
        super(image, 4, 3, x, y);

        this.gameSurface = gameSurface;

        this.topToBottoms = new Bitmap[colCount]; // 3
        this.rightToLefts = new Bitmap[colCount]; // 3
        this.leftToRights = new Bitmap[colCount]; // 3
        this.bottomToTops = new Bitmap[colCount]; // 3


        random = new Random(2000);
        int renX = random.nextInt(getWidth());
        int renY = random.nextInt(getHeight());
        this.movingVectorX = renX;
        this.movingVectorY = renY;


        for (int col = 0; col < this.colCount; col++) {
            this.topToBottoms[col] = this.createSubImageAt(ROW_TOP_TO_BOTTOM, col);
            this.rightToLefts[col] = this.createSubImageAt(ROW_RIGHT_TO_LEFT, col);
            this.leftToRights[col] = this.createSubImageAt(ROW_LEFT_TO_RIGHT, col);
            this.bottomToTops[col] = this.createSubImageAt(ROW_BOTTOM_TO_TOP, col);
        }
    }

    public Bitmap[] getMoveBitmaps() {
        switch (rowUsing) {
            case ROW_BOTTOM_TO_TOP:
                return this.bottomToTops;
            case ROW_LEFT_TO_RIGHT:
                return this.leftToRights;
            case ROW_RIGHT_TO_LEFT:
                return this.rightToLefts;
            case ROW_TOP_TO_BOTTOM:
                return this.topToBottoms;
            default:
                return null;
        }
    }

    public Bitmap getCurrentMoveBitmap() {
        Bitmap[] bitmaps = this.getMoveBitmaps();
        return bitmaps[this.colUsing];
    }


    public void update() {
        this.colUsing++;
        if (colUsing >= this.colCount) {
            this.colUsing = 0;
        }
        // Current time in nanoseconds
        long now = System.nanoTime();

        // Never once did draw.
        if (lastDrawNanoTime == -1) {
            lastDrawNanoTime = now;
        }
        // Change nanoseconds to milliseconds (1 nanosecond = 1000000 milliseconds).
        int deltaTime = (int) ((now - lastDrawNanoTime) / 1000000);

        // Distance moves
        float distance = VELOCITY * deltaTime;

        double movingVectorLength = Math.sqrt(movingVectorX * movingVectorX + movingVectorY * movingVectorY);

        // Calculate the new position of the game character.
        this.x = x + (int) (distance * movingVectorX / movingVectorLength);
        this.y = y + (int) (distance * movingVectorY / movingVectorLength);


        // When the game's character touches the edge of the screen, then change direction

        if (this.x < 0) {
            this.x = 0;
            if (user2 != null) {
                if ((Math.pow((user1.getX() - x), 2) + Math.pow(user1.getY() - y, 2)) > (Math.pow((user2.getX() - x), 2) + Math.pow(user2.getY() - y, 2))) {
                    setMovingVector(this.user1.getX() - this.x, this.user1.getY() - this.y);
                } else {
                    setMovingVector(this.user2.getX() - this.x, this.user2.getY() - this.y);
                }
            } else {
                setMovingVector(this.user1.getX() - this.x, this.user1.getY() - this.y);
            }

        } else if (this.x > this.gameSurface.getWidth() - width) {
            this.x = this.gameSurface.getWidth() - width;
            if (user2 != null) {
                if ((Math.pow((user1.getX() - x), 2) + Math.pow(user1.getY() - y, 2)) > (Math.pow((user2.getX() - x), 2) + Math.pow(user2.getY() - y, 2))) {
                    setMovingVector(this.user1.getX() - this.x, this.user1.getY() - this.y);
                } else {

                    setMovingVector(this.user2.getX() - this.x, this.user2.getY() - this.y);
                }
            } else {
                setMovingVector(this.user1.getX() - this.x, this.user1.getY() - this.y);
            }
        }


        if (this.y < 0) {
            this.y = 0;
            if (user2 != null) {
                if ((Math.pow((user1.getX() - x), 2) + Math.pow(user1.getY() - y, 2)) > (Math.pow((user2.getX() - x), 2) + Math.pow(user2.getY() - y, 2))) {
                    setMovingVector(this.user1.getX() - this.x, this.user1.getY() - this.y);
                } else {

                    setMovingVector(this.user2.getX() - this.x, this.user2.getY() - this.y);
                }
            }else{
                setMovingVector(this.user1.getX() - this.x, this.user1.getY() - this.y);
            }
        } else if (this.y > this.gameSurface.getHeight() - height) {
            this.y = this.gameSurface.getHeight() - height;
            if (user2 != null) {
                if ((Math.pow((user1.getX() - x), 2) + Math.pow(user1.getY() - y, 2)) > (Math.pow((user2.getX() - x), 2) + Math.pow(user2.getY() - y, 2))) {
                    setMovingVector(this.user1.getX() - this.x, this.user1.getY() - this.y);
                } else {

                    setMovingVector(this.user2.getX() - this.x, this.user2.getY() - this.y);
                }
            }else{
                setMovingVector(this.user1.getX() - this.x, this.user1.getY() - this.y);
            }
        }

        // rowUsing
        if (movingVectorX > 0) {
            if (movingVectorY > 0 && Math.abs(movingVectorX) < Math.abs(movingVectorY)) {
                this.rowUsing = ROW_TOP_TO_BOTTOM;
            } else if (movingVectorY < 0 && Math.abs(movingVectorX) < Math.abs(movingVectorY)) {
                this.rowUsing = ROW_BOTTOM_TO_TOP;
            } else {
                this.rowUsing = ROW_LEFT_TO_RIGHT;
            }
        } else {
            if (movingVectorY > 0 && Math.abs(movingVectorX) < Math.abs(movingVectorY)) {
                this.rowUsing = ROW_TOP_TO_BOTTOM;
            } else if (movingVectorY < 0 && Math.abs(movingVectorX) < Math.abs(movingVectorY)) {
                this.rowUsing = ROW_BOTTOM_TO_TOP;
            } else {
                this.rowUsing = ROW_RIGHT_TO_LEFT;
            }
        }
    }

    public void draw(Canvas canvas) {
        if (!finish) {
            Bitmap bitmap = this.getCurrentMoveBitmap();
            canvas.drawBitmap(bitmap, x, y, null);
            // Last draw time.
            this.lastDrawNanoTime = System.nanoTime();
        }
    }

    public void setMovingVector(int movingVectorX, int movingVectorY) {
        this.movingVectorX = movingVectorX;
        this.movingVectorY = movingVectorY;
    }

    public void setFinish(Boolean finish) {
        this.finish = finish;
    }

    public void setUser1(UserCharacter user1) {
        this.user1 = user1;
    }

    public void setUser2(UserCharacter user2) {
        this.user2 = user2;
    }

}
