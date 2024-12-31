package com.example.spacegame;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

public class HomingEnemy {
    public int x, y, width, height, speed;
    public Bitmap homingEnemy;
    private int screenX, screenY;
    private Player player;
    public boolean isActive = true;
    public boolean isScoreEnabled = false;
    private int enemyType = 1;

    public HomingEnemy(Resources res, int screenX, int screenY, Player player, int bossX, int bossY , int bossWidth, int bossHeight, boolean isScoreEnabled, int speed,int enemyType) {
        this.screenX = screenX;
        this.screenY = screenY;
        this.player = player;
        this.isScoreEnabled = isScoreEnabled;
        this.enemyType = enemyType;
        int resourceId = R.drawable.homing_enemy;

        if (enemyType == 2) {
            resourceId = R.drawable.second_homing_enemy;
        }


        homingEnemy = BitmapFactory.decodeResource(res, resourceId);
        width = homingEnemy.getWidth();
        height = homingEnemy.getHeight();

        width /= 4;
        height /= 4;
        homingEnemy = Bitmap.createScaledBitmap(homingEnemy, width, height, false);
        x = bossX + bossWidth / 2 - width/2;
        y = bossY + bossHeight / 2 - height/2;
        this.speed = speed;
    }
    public HomingEnemy(Resources res, int screenX, int screenY, Player player, int bossX, int bossY , int bossWidth, int bossHeight,boolean isScoreEnabled, int speed) {
        this(res,screenX,screenY,player,bossX,bossY,bossWidth,bossHeight,isScoreEnabled,speed,1);
    }
    public HomingEnemy(Resources res, int screenX, int screenY, Player player) {
        this(res,screenX,screenY,player,0,0,0,0,false,12);
    }


    public void move() {
        if(player == null) return;
        double angle = Math.atan2(player.y + player.height/2 - y , player.x + player.width /2 - x );
        x += speed * Math.cos(angle);
        y += speed * Math.sin(angle);
    }

    public boolean isOffScreen(int screenY) {
        return y > screenY;
    }

    public Rect getCollisionShape() {
        return new Rect(x, y, x + width, y + height);
    }
    public int getEnemyType() {
        return enemyType;
    }
}