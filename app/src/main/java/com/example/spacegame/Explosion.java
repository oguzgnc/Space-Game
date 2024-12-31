package com.example.spacegame;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Explosion {
    public int x, y, frame, width, height;
    public Bitmap[] explosion;
    private int offsetX, offsetY;
    private int frameDelay;
    private int delayCounter;
    private boolean isBossExplosion;
    public Explosion(Resources res, int x, int y, int objectWidth, int objectHeight, boolean isBossExplosion) {
        this.frame = 0;
        this.isBossExplosion = isBossExplosion;
        explosion = new Bitmap[5];

        for (int i = 0; i < 5; i++) {
            int resourceId;
            if(isBossExplosion){
                resourceId = res.getIdentifier("boss_explosion_" + (i + 1), "drawable", "com.example.spacegame");
            }
            else {
                resourceId = res.getIdentifier("explosion_" + (i + 1), "drawable", "com.example.spacegame");
            }


            explosion[i] = BitmapFactory.decodeResource(res, resourceId);
            width = explosion[i].getWidth();
            height = explosion[i].getHeight();

            width /= 4;
            height /= 4;
            explosion[i] = Bitmap.createScaledBitmap(explosion[i], width, height, false);
        }
        this.x = x;
        this.y = y;

        offsetX = (objectWidth - width) / 2;
        offsetY = (objectHeight - height) / 2;
        this.x -= offsetX;
        this.y -= offsetY;

        frameDelay = 2;
        delayCounter = 0;
    }
    public Explosion(Resources res, int x, int y, int objectWidth, int objectHeight) {
        this(res, x,y,objectWidth,objectHeight,false);
    }
    public void nextFrame() {
        delayCounter++;
        if (delayCounter >= frameDelay) {
            frame++;
            delayCounter = 0;
        }
    }


    public Bitmap getFrame() {
        return explosion[frame];
    }

    public boolean isOver() {
        return frame == 5;
    }
}