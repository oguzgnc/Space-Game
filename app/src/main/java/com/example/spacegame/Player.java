package com.example.spacegame;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

public class Player {
    public int x, y, width, height;
    public Bitmap player;

    public Player(int screenX, int screenY, Resources res) {
        player = BitmapFactory.decodeResource(res, R.drawable.spaceship);
        width = player.getWidth();
        height = player.getHeight();


        width /= 5;
        height /= 5;
        player = Bitmap.createScaledBitmap(player, width, height, false);


        x = screenX / 2 - width / 2;
        y = screenY - height - 20;
    }

    public Rect getCollisionShape() {
        return new Rect(x, y, x + width, y + height);
    }
}