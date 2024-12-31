package com.example.spacegame;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

public class Bullet {
    public int x, y, width, height, speed;
    public Bitmap bullet;
    public boolean isActive = false;


    public Bullet(Resources res, int x, int y) {
        bullet = BitmapFactory.decodeResource(res, R.drawable.bullet);
        width = bullet.getWidth();
        height = bullet.getHeight();

        width /= 25;
        height /= 25;
        bullet = Bitmap.createScaledBitmap(bullet, width, height, false);

        this.x = x;
        this.y = y;
        this.speed = 50;
    }

    public void move() {
        y -= speed;
    }

    public Rect getCollisionShape() {
        return new Rect(x, y, x + width, y + height);
    }

    public boolean isOffScreen() {
        return y < -height;
    }
}