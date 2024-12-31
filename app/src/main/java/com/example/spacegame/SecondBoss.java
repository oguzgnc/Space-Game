package com.example.spacegame;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SecondBoss {
    public int x, y, width, height, speed;
    public Bitmap boss;
    private int health = 30;
    private int maxHealth = 30;
    private int baseMaxHealth = 30;
    private int screenX, screenY;
    Random random = new Random();
    private long lastFireTime = 0;
    private long fireInterval = 700;
    private int baseFireInterval = 700;
    private List<HomingEnemy> homingEnemies = new ArrayList<>();
    private Resources res;
    private Player player;
    private int homingEnemySpeed = 15;
    private int baseHomingEnemySpeed = 15;
    private int difficultyLevel = 1;


    public SecondBoss(Resources res,int screenX , int screenY, Player player) {
        this.screenX = screenX;
        this.screenY = screenY;
        this.res = res;
        this.player = player;
        boss = BitmapFactory.decodeResource(res, R.drawable.second_boss);
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

    public void increaseDifficulty(){
        difficultyLevel++;
        baseMaxHealth *= 2;
        maxHealth = baseMaxHealth;
        health = maxHealth;
        baseHomingEnemySpeed *= 2;
        homingEnemySpeed = baseHomingEnemySpeed;
    }
    public void resetDifficulty(){
        difficultyLevel = 1;
        maxHealth = baseMaxHealth;
        health = maxHealth;
        fireInterval = baseFireInterval;
        homingEnemySpeed = baseHomingEnemySpeed;
    }

    public void move() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFireTime >= fireInterval) {
            fireHomingEnemy();
            lastFireTime = currentTime;
        }
    }

    private void fireHomingEnemy(){
        homingEnemies.add(new HomingEnemy(res, screenX, screenY, player,x,y,width,height, true, homingEnemySpeed,2));
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

    public Rect getCollisionShape() {
        return new Rect(x, y, x + width, y + height);
    }
}