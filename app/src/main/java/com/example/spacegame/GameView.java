package com.example.spacegame;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;

public class GameView extends SurfaceView implements Runnable {

    private Thread thread;
    private boolean isPlaying;
    private Paint paint;
    private SurfaceHolder surfaceHolder;
    private Background background;
    private Player player;
    private List<Enemy> enemies;
    private List<Bullet> bullets;
    private List<Explosion> explosions;
    private int screenX, screenY;
    private int score;
    private int lives;
    private boolean gameOver;
    private Rect restartButton;
    private GameState gameState;
    private Rect startButton;
    private Bitmap spaceshipBitmap;
    private int buttonRadius = 20;
    private Rect menuButton;
    private int buttonSpacing = 30;
    private int gameOverTextSpacing = 100;
    private int level = 1;
    private int levelThreshold = 120;
    private int baseEnemySpeed = 10;
    private int enemySpeedIncrease = 5;
    private long lastFireTime = 0;
    private long fireInterval = 200;
    private int maxBullets = 10;
    private int bulletIndex = 0;
    private Boss boss;
    private SecondBoss secondBoss;
    private boolean isBossLevel = false;
    private int bossLevelInterval = 6;
    private List<HomingEnemy> homingEnemies = new ArrayList<>();

    private Rect pauseButton;
    private Bitmap pauseButtonBitmap;
    private Bitmap pauseButtonBitmapPaused;
    private boolean isGamePaused = false;


    private SharedPreferences sharedPreferences;
    private List<Integer> highScores;
    private  final String PREF_HIGH_SCORE = "high_scores";


    public GameView(Context context) {
        super(context);

        setFocusable(true);
        setFocusableInTouchMode(true);
        sharedPreferences = context.getSharedPreferences(PREF_HIGH_SCORE, Context.MODE_PRIVATE);
        highScores = new ArrayList<>();
        loadHighScores();

        surfaceHolder = getHolder();
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(60);
        paint.setAntiAlias(true);

        screenX = getResources().getDisplayMetrics().widthPixels;
        screenY = getResources().getDisplayMetrics().heightPixels;

        background = new Background(screenX, screenY, getResources());
        player = new Player(screenX, screenY, getResources());
        enemies = new ArrayList<>();
        bullets = new ArrayList<>();
        for (int i = 0; i < maxBullets; i++) {
            bullets.add(new Bullet(getResources(), 0, 0));
        }
        explosions = new ArrayList<>();
        score = 0;
        lives = 3;
        gameOver = false;
        gameState = GameState.MENU;

        int buttonWidth = 300;
        int buttonHeight = 150;
        int buttonX = screenX / 2 - buttonWidth / 2;
        int buttonY = screenY / 2 + 150;
        startButton = new Rect(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight);

        int restartButtonWidth = 350;
        int restartButtonHeight = 180;
        int restartButtonX = screenX / 2 - restartButtonWidth / 2;
        int restartButtonY = screenY / 2 + 150;
        restartButton = new Rect(restartButtonX, restartButtonY, restartButtonX + restartButtonWidth, restartButtonY + restartButtonHeight);

        int menuButtonWidth = 350;
        int menuButtonHeight = 180;
        int menuButtonX = screenX / 2 - menuButtonWidth / 2;
        int menuButtonY = restartButtonY + restartButtonHeight + buttonSpacing;
        menuButton = new Rect(menuButtonX, menuButtonY, menuButtonX + menuButtonWidth, menuButtonY + menuButtonHeight);


        int pauseButtonSize = 150;
        int pauseButtonTop = 260;
        pauseButton = new Rect(20, pauseButtonTop, 20 + pauseButtonSize, pauseButtonTop + pauseButtonSize);
        pauseButtonBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pause_button);
        pauseButtonBitmap = Bitmap.createScaledBitmap(pauseButtonBitmap, pauseButtonSize, pauseButtonSize, false);
        pauseButtonBitmapPaused = BitmapFactory.decodeResource(getResources(),R.drawable.resume_button);
        pauseButtonBitmapPaused = Bitmap.createScaledBitmap(pauseButtonBitmapPaused,pauseButtonSize,pauseButtonSize,false);

        spaceshipBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.spaceship);
        spaceshipBitmap = Bitmap.createScaledBitmap(spaceshipBitmap, spaceshipBitmap.getWidth() / 2, spaceshipBitmap.getHeight() / 2, false);
    }

    @Override
    public void run() {
        while (isPlaying) {
            if(!isGamePaused) {
                update();
            }
            draw();
            sleep();
        }
    }
    private void loadHighScores(){
        highScores.clear();
        for(int i = 0; i<3;i++){
            int score = sharedPreferences.getInt("high_score_"+i,0);
            highScores.add(score);
        }
    }
    private void saveHighScores(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for(int i =0; i < 3 ;i++){
            editor.putInt("high_score_" + i,highScores.get(i));
        }
        editor.apply();

    }
    private void update() {
        if (gameState == GameState.MENU) {
            return;
        }
        if (gameOver) return;

        background.move();

        int currentEnemySpeed = baseEnemySpeed + (level - 1) * enemySpeedIncrease;

        if (score >= level * levelThreshold) {
            level++;
            if (level % bossLevelInterval == 0) {
                isBossLevel = true;
                if (level == 12) {
                    secondBoss = new SecondBoss(getResources(), screenX, screenY, player);
                    boss = null; // İlk bossu temizle
                }else{
                    if(boss == null){
                        boss = new Boss(getResources(), screenX, screenY,player);
                    } else{
                        boss.increaseDifficulty(level);
                    }

                    secondBoss = null;
                }

                enemies.clear();
                homingEnemies.clear();
            }
        }
        if(isBossLevel){
            if (secondBoss != null) {
                secondBoss.move();
                homingEnemies = secondBoss.getHomingEnemies();
                if (Rect.intersects(secondBoss.getCollisionShape(), player.getCollisionShape())) {
                    lives--;
                    if (lives <= 0) {
                        explosions.add(new Explosion(getResources(), player.x, player.y, player.width, player.height));
                        gameOver = true;
                        isPlaying = false;
                    }
                }
                Iterator<Bullet> bulletIterator = bullets.iterator();
                while (bulletIterator.hasNext()) {
                    Bullet bullet = bulletIterator.next();
                    if (bullet.isActive) {
                        if (Rect.intersects(secondBoss.getCollisionShape(), bullet.getCollisionShape())) {
                            secondBoss.takeDamage();
                            bullet.isActive = false;
                            bullet.x = 0;
                            bullet.y = 0;
                            if(secondBoss.getHealth() <= 0){
                                explosions.add(new Explosion(getResources(), secondBoss.x + secondBoss.width /2 , secondBoss.y + secondBoss.height/2,secondBoss.width, secondBoss.height,true));
                                isBossLevel = false;
                                secondBoss.clearHomingEnemies();
                                secondBoss = null;
                                break;
                            }
                        }
                    }
                }
            }else if(boss != null){
                boss.move();
                homingEnemies = boss.getHomingEnemies();
                if (Rect.intersects(boss.getCollisionShape(), player.getCollisionShape())) {
                    lives--;
                    if (lives <= 0) {
                        explosions.add(new Explosion(getResources(), player.x, player.y, player.width, player.height));
                        gameOver = true;
                        isPlaying = false;
                    }
                }
                Iterator<Bullet> bulletIterator = bullets.iterator();
                while (bulletIterator.hasNext()) {
                    Bullet bullet = bulletIterator.next();
                    if (bullet.isActive) {
                        if (Rect.intersects(boss.getCollisionShape(), bullet.getCollisionShape())) {
                            boss.takeDamage();
                            bullet.isActive = false;
                            bullet.x = 0;
                            bullet.y = 0;
                            if(boss.getHealth() <= 0){
                                explosions.add(new Explosion(getResources(), boss.x + boss.width /2 , boss.y + boss.height/2,boss.width, boss.height,true));
                                isBossLevel = false;
                                boss.clearHomingEnemies();
                                boss = null;
                                break;
                            }
                        }
                    }
                }
            }
        }else{


            Iterator<Enemy> enemyIterator = enemies.iterator();
            while (enemyIterator.hasNext()) {
                Enemy enemy = enemyIterator.next();
                enemy.move(currentEnemySpeed);

                if (Rect.intersects(enemy.getCollisionShape(), player.getCollisionShape())) {
                    lives--;
                    if (lives <= 0) {
                        explosions.add(new Explosion(getResources(), player.x, player.y, player.width, player.height));
                        gameOver = true;
                        isPlaying = false;
                    }
                    enemyIterator.remove();
                    break;
                }

                Iterator<Bullet> bulletIterator = bullets.iterator();
                while (bulletIterator.hasNext()) {
                    Bullet bullet = bulletIterator.next();
                    if (bullet.isActive) {
                        if (Rect.intersects(enemy.getCollisionShape(), bullet.getCollisionShape())) {
                            explosions.add(new Explosion(getResources(), enemy.x, enemy.y, enemy.width, enemy.height));
                            enemyIterator.remove();
                            bullet.isActive = false;
                            bullet.x = 0;
                            bullet.y = 0;
                            score += 30;
                            break;
                        }
                    }
                }

                if (enemy.isOffScreen(screenY)) {
                    enemyIterator.remove();
                }
            }
            if (Math.random() < 0.05) {
                enemies.add(new Enemy(getResources(), screenX, screenY));
            }
        }

        Iterator<HomingEnemy> homingEnemyIterator = homingEnemies.iterator();
        while (homingEnemyIterator.hasNext()) {
            HomingEnemy homingEnemy = homingEnemyIterator.next();
            homingEnemy.move();
            if (homingEnemy.isOffScreen(screenY) || !homingEnemy.isActive) {
                homingEnemyIterator.remove();
                continue;
            }
            if (Rect.intersects(homingEnemy.getCollisionShape(), player.getCollisionShape())) {
                lives--;
                if (lives <= 0) {
                    explosions.add(new Explosion(getResources(), player.x, player.y, player.width, player.height));
                    gameOver = true;
                    isPlaying = false;
                }
                homingEnemyIterator.remove();
                continue;
            }
            Iterator<Bullet> bulletIterator = bullets.iterator();
            while (bulletIterator.hasNext()) {
                Bullet bullet = bulletIterator.next();
                if (bullet.isActive) {
                    if (Rect.intersects(homingEnemy.getCollisionShape(), bullet.getCollisionShape())) {
                        explosions.add(new Explosion(getResources(), homingEnemy.x, homingEnemy.y, homingEnemy.width, homingEnemy.height));
                        homingEnemy.isActive = false;
                        bullet.isActive = false;
                        bullet.x = 0;
                        bullet.y = 0;
                        if(!homingEnemy.isScoreEnabled){
                            score += 30;
                        }
                        break;
                    }
                }
            }
        }




        for (Bullet bullet : bullets) {
            if(bullet.isActive) {
                bullet.move();
                if(bullet.isOffScreen()) {
                    bullet.isActive = false;
                    bullet.x = 0;
                    bullet.y = 0;
                }
            }
        }



        if (player.y < 0) {
            player.y = 0;
        }
        if (player.y > screenY - player.height) {
            player.y = screenY - player.height;
        }

        Iterator<Explosion> explosionIterator = explosions.iterator();
        while (explosionIterator.hasNext()) {
            Explosion explosion = explosionIterator.next();
            explosion.nextFrame();
            if (explosion.isOver()) {
                explosionIterator.remove();
            }
        }
    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            Canvas canvas = surfaceHolder.lockCanvas();

            if (gameState == GameState.MENU) {
                drawMenu(canvas);
            } else {
                canvas.drawBitmap(background.background, background.x, background.y, paint);
                canvas.drawBitmap(background.background, background.x, background.y - background.background.getHeight(), paint);

                if (!gameOver) { // Oyun bitmediyse çiz yada cız
                    if (lives > 0) {
                        canvas.drawBitmap(player.player, player.x, player.y, paint);
                    }
                    if(isBossLevel){
                        if (secondBoss != null) {
                            canvas.drawBitmap(secondBoss.boss, secondBoss.x, secondBoss.y, paint);
                            for(HomingEnemy homingEnemy : homingEnemies){
                                canvas.drawBitmap(homingEnemy.homingEnemy,homingEnemy.x,homingEnemy.y, paint);
                            }
                        } else if(boss != null){
                            canvas.drawBitmap(boss.boss, boss.x, boss.y, paint);
                            for(HomingEnemy homingEnemy : homingEnemies){
                                canvas.drawBitmap(homingEnemy.homingEnemy,homingEnemy.x,homingEnemy.y, paint);
                            }
                        }

                    }else {
                        for (Enemy enemy : enemies) {
                            canvas.drawBitmap(enemy.enemy, enemy.x, enemy.y, paint);
                        }
                    }


                    for (Bullet bullet : bullets) {
                        if(bullet.isActive)
                            canvas.drawBitmap(bullet.bullet, bullet.x, bullet.y, paint);
                    }
                }

                for (Explosion explosion : explosions) {
                    canvas.drawBitmap(explosion.getFrame(), explosion.x, explosion.y, paint);
                }

                if (!gameOver) {
                    drawScore(canvas);
                    drawLives(canvas);
                    drawLevel(canvas);
                    if(isBossLevel){
                        if (secondBoss != null) {
                            drawSecondBossHealth(canvas);
                        } else if (boss != null) {
                            drawBossHealth(canvas);
                        }
                    }
                    if(isGamePaused) {
                        canvas.drawBitmap(pauseButtonBitmapPaused, pauseButton.left, pauseButton.top, paint);
                    }else {
                        canvas.drawBitmap(pauseButtonBitmap, pauseButton.left, pauseButton.top, paint);
                    }
                }



                if (gameOver) {
                    drawGameOver(canvas);
                }
            }

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }
    private void drawSecondBossHealth(Canvas canvas) {
        if(secondBoss != null){
            paint.setTextSize(45); //
            String bossHealthText = "Boss Health: " + secondBoss.getHealth() + " / " + secondBoss.getMaxHealth();
            float textWidth = paint.measureText(bossHealthText);

            int x = screenX - (int)textWidth - 20;
            int y = 80;

            paint.setColor(Color.RED);
            canvas.drawText(bossHealthText, x , y, paint);
            paint.setTextSize(60);
        }

    }
    private void drawBossHealth(Canvas canvas) {
        if(boss != null){
            paint.setTextSize(45);
            String bossHealthText = "Boss Health: " + boss.getHealth() + " / " + boss.getMaxHealth();
            float textWidth = paint.measureText(bossHealthText);

            int x = screenX - (int)textWidth - 20;
            int y = 80;

            paint.setColor(Color.RED);
            canvas.drawText(bossHealthText, x , y, paint);
            paint.setTextSize(60); // Yazı boyutunu geri al.
        }

    }


    private void drawLevel(Canvas canvas) {
        paint.setColor(Color.WHITE);
        canvas.drawText("Level: " + level, 20, 240, paint);
    }


    private void drawMenu(Canvas canvas) {
        canvas.drawBitmap(background.background, background.x, background.y, paint);
        canvas.drawBitmap(background.background, background.x, background.y - background.background.getHeight(), paint);

        int spaceshipX = screenX / 2 - spaceshipBitmap.getWidth() / 2;
        int spaceshipY = screenY / 4 - spaceshipBitmap.getHeight() / 2;
        canvas.drawBitmap(spaceshipBitmap, spaceshipX, spaceshipY, paint);

        paint.setColor(Color.RED);
        RectF rectF = new RectF(startButton);
        canvas.drawRoundRect(rectF, buttonRadius, buttonRadius, paint);
        paint.setColor(Color.WHITE);
        String buttonText = "Start";
        float buttonTextWidth = paint.measureText(buttonText);
        float x = startButton.left + (startButton.width() - buttonTextWidth) / 2;
        float y = startButton.top + startButton.height() / 2 + 20;
        canvas.drawText(buttonText, x, y, paint);

        paint.setTextSize(50);
        int yOffset = screenY / 2 - 100;
        for(int i =0;i < highScores.size();i++){
            String highScoreText = (i + 1) + ". High Score: " + highScores.get(i);
            float textX = screenX / 2 - paint.measureText(highScoreText) / 2;
            canvas.drawText(highScoreText, textX, yOffset + i * 80, paint);
        }
        paint.setTextSize(60);

    }
    private void drawLives(Canvas canvas) {
        paint.setColor(Color.WHITE);
        canvas.drawText("Lives: " + lives, 20, 160, paint);
    }

    private void drawScore(Canvas canvas) {
        paint.setColor(Color.WHITE);
        canvas.drawText("Score: " + score, 20, 80, paint);
    }

    private void drawGameOver(Canvas canvas) {
        paint.setColor(Color.WHITE);
        paint.setTextSize(100);
        String scoreText = "Score: " + score;
        float scoreTextWidth = paint.measureText(scoreText);
        float scoreX = (screenX - scoreTextWidth) / 2;
        float scoreY = screenY / 2 - 100;
        canvas.drawText(scoreText, scoreX, scoreY, paint);
        // Check for new high score
        checkNewHighScore(score);
        String text = "Game Over";
        float textWidth = paint.measureText(text);
        float x = (screenX - textWidth) / 2;
        float y = scoreY + gameOverTextSpacing;
        canvas.drawText(text, x, y, paint);
        paint.setTextSize(60);

        paint.setColor(Color.RED);
        RectF rectF = new RectF(restartButton);
        canvas.drawRoundRect(rectF, buttonRadius, buttonRadius, paint);
        paint.setColor(Color.WHITE);
        String buttonText = "Try Again!";
        float buttonTextWidth = paint.measureText(buttonText);
        x = restartButton.left + (restartButton.width() - buttonTextWidth) / 2;
        y = restartButton.top + restartButton.height() / 2 + 20;
        canvas.drawText(buttonText, x, y, paint);

        paint.setColor(Color.RED);
        RectF menuRectF = new RectF(menuButton);
        canvas.drawRoundRect(menuRectF, buttonRadius, buttonRadius, paint);
        paint.setColor(Color.WHITE);
        String menuText = "Menu";
        float menuTextWidth = paint.measureText(menuText);
        x = menuButton.left + (menuButton.width() - menuTextWidth) / 2;
        y = menuButton.top + menuButton.height() / 2 + 20;
        canvas.drawText(menuText, x, y, paint);
    }

    private void sleep() {
        try {
            Thread.sleep(16);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }

    public void pause() {
        try {
            isPlaying = false;
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        if (gameState == GameState.MENU && event.getAction() == MotionEvent.ACTION_DOWN) {
            if (startButton.contains(x, y)) {
                gameState = GameState.GAME;
                return true;
            }
        } else if (gameOver && event.getAction() == MotionEvent.ACTION_DOWN) {
            if (restartButton.contains(x, y)) {
                restartGame();
                return true;
            } else if (menuButton.contains(x, y)) {
                gameState = GameState.MENU;
                restartGame();
                return true;
            }
        } else if (gameState == GameState.GAME && !gameOver) {

            if(pauseButton.contains(x,y) && event.getAction() == MotionEvent.ACTION_DOWN){
                isGamePaused = !isGamePaused;
                return true;
            }else {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        player.x = (int) event.getX() - player.width / 2;
                        break;
                }

                if (player.x < 0) {
                    player.x = 0;
                }
                if (player.x > screenX - player.width) {
                    player.x = screenX - player.width;
                }
                return true;
            }


        }

        return true;
    }

    private void restartGame() {
        enemies.clear();
        explosions.clear();
        score = 0;
        lives = 3;
        gameOver = false;
        level = 1;
        isBossLevel = false;
        if(boss != null){
            boss.resetDifficulty();
            boss = null;
        }
        if (secondBoss != null) {
            secondBoss.resetDifficulty();
            secondBoss = null;
        }
        homingEnemies.clear();
        for(Bullet bullet : bullets){
            bullet.isActive = false;
            bullet.x = 0;
            bullet.y = 0;
        }
        isPlaying = true;
        isGamePaused = false;
        loadHighScores();
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(this);
            thread.start();
        }
    }

    private void checkNewHighScore(int score){
        if(highScores.size() < 3 || score > highScores.get(highScores.size() -1)) {
            highScores.add(score);
            Collections.sort(highScores, Collections.reverseOrder());
            if(highScores.size() > 3){
                highScores.remove(highScores.size() -1);
            }
            saveHighScores();
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SPACE && gameState == GameState.GAME) {
            fireBullet();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    private void fireBullet() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFireTime >= fireInterval) {

            if(bulletIndex >= maxBullets) {
                bulletIndex = 0;
            }
            Bullet bullet = bullets.get(bulletIndex);
            bullet.x = player.x + player.width / 2;
            bullet.y = player.y;
            bullet.isActive = true;
            bulletIndex++;
            lastFireTime = currentTime;
        }

    }
    }