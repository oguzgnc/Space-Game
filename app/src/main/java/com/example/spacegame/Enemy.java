package com.example.spacegame;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import java.util.Random;

public class Enemy {
    public int x, y, width, height;
    public Bitmap enemy;

    public Enemy(Resources res, int screenX, int screenY) {
        enemy = BitmapFactory.decodeResource(res, R.drawable.enemy);
        width = enemy.getWidth();
        height = enemy.getHeight();


        width /= 4;
        height /= 4;
        enemy = Bitmap.createScaledBitmap(enemy, width, height, false);

        Random random = new Random();
        x = random.nextInt(screenX - width);
        y = -height;
    }

    public void move(int speed) {
        y += speed;
    }

    public boolean isOffScreen(int screenY) {
        return y > screenY;
    }

    public Rect getCollisionShape() {
        return new Rect(x, y, x + width, y + height);
    }
}