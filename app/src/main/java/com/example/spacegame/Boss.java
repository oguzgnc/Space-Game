package com.example.spacegame;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Boss {
    public int x, y, width, height, speed;
    public Bitmap boss;
    private int health = 20;
    private int maxHealth = 20;
    private int baseMaxHealth = 20;
    private int screenX, screenY;
    Random random = new Random();
    private long lastFireTime = 0;
    private long fireInterval = 1000;
    private int baseFireInterval = 1000;
    private List<HomingEnemy> homingEnemies = new ArrayList<>();
    private Resources res;
    private Player player;
    private int homingEnemySpeed = 12;
    private int baseHomingEnemySpeed = 12;
    private int difficultyLevel = 1;
    private int bossType = 1;


    public Boss(Resources res,int screenX , int screenY, Player player) {
        this.screenX = screenX;
        this.screenY = screenY;
        this.res = res;
        this.player = player;
        boss = BitmapFactory.decodeResource(res, R.drawable.boss);
        width = boss.getWidth();
        height = boss.getHeight();

        width /= 4;
        height /= 4;
        boss = Bitmap.createScaledBitmap(boss, width, height, false);
        x = screenX/2 - width/2;
        y = 100;
        speed = 0;
        this.baseMaxHealth = maxHealth;
        this.baseHomingEnemySpeed = homingEnemySpeed;
    }
    public void increaseDifficulty(int level){
        difficultyLevel++;
        if(level == 12){
            this.bossType = 2;
            baseMaxHealth *= 3;
            baseHomingEnemySpeed *= 3;
        }
        else {
            baseMaxHealth *= 2;
            baseHomingEnemySpeed *= 2;

        }
        maxHealth = baseMaxHealth;
        health = maxHealth;
        homingEnemySpeed = baseHomingEnemySpeed;

    }


    public void resetDifficulty(){
        difficultyLevel = 1;
        maxHealth = baseMaxHealth;
        health = maxHealth;
        fireInterval = baseFireInterval;
        homingEnemySpeed = baseHomingEnemySpeed;
        bossType = 1;
    }


    public void move() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFireTime >= fireInterval) {
            fireHomingEnemy();
            lastFireTime = currentTime;
        }
    }
    private void fireHomingEnemy(){
        homingEnemies.add(new HomingEnemy(res, screenX, screenY, player,x,y,width,height, true, homingEnemySpeed));
    }

    public List<HomingEnemy> getHomingEnemies(){
        return homingEnemies;
    }

    public void clearHomingEnemies(){
        homingEnemies.clear();
    }


    public void takeDamage() {
        health--;
    }

    public int getHealth() {
        return health;
    }
    public int getMaxHealth(){
        return maxHealth;
    }
    public int getDifficultyLevel(){
        return difficultyLevel;
    }
    public int getBossType(){
        return bossType;
    }

    public Rect getCollisionShape() {
        return new Rect(x, y, x + width, y + height);
    }
}