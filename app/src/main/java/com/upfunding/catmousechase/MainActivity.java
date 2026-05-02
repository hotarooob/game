package com.upfunding.catmousechase;

import android.app.Activity;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import java.util.Random;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(new CatChaseView(this));
    }

    @Override
    protected void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    static class CatChaseView extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Random random = new Random();
        private final ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_MUSIC, 35);

        private int width, height;
        private float catX, catY, targetX, targetY;
        private float catRadius;
        private Mouse[] mice;
        private int score;
        private float timeLeft;
        private boolean started;
        private boolean gameOver;
        private long lastTimeNanos;

        private final int backgroundTop = Color.rgb(255, 238, 218);
        private final int backgroundBottom = Color.rgb(230, 249, 232);
        private final int catColor = Color.rgb(247, 166, 166);
        private final int catEarColor = Color.rgb(242, 140, 140);
        private final int mouseColor = Color.rgb(133, 133, 142);
        private final int cheeseColor = Color.rgb(255, 213, 91);
        private final int uiText = Color.rgb(55, 55, 65);

        CatChaseView(Activity activity) {
            super(activity);
            setFocusable(true);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            width = w;
            height = h;
            resetGame();
        }

        private void resetGame() {
            catRadius = Math.max(34f, width * 0.075f);
            catX = width * 0.5f;
            catY = height * 0.72f;
            targetX = catX;
            targetY = catY;
            score = 0;
            timeLeft = 60f;
            started = false;
            gameOver = false;
            lastTimeNanos = 0;

            int mouseCount = 4;
            mice = new Mouse[mouseCount];
            for (int i = 0; i < mouseCount; i++) {
                mice[i] = new Mouse();
                resetMouse(mice[i], true);
            }
            invalidate();
        }

        private void startGame() {
            started = true;
            gameOver = false;
            lastTimeNanos = 0;
        }

        private void resetMouse(Mouse m, boolean anywhere) {
            float margin = catRadius * 1.8f;
            if (anywhere) {
                m.x = margin + random.nextFloat() * Math.max(1, width - margin * 2);
                m.y = margin * 2 + random.nextFloat() * Math.max(1, height - margin * 4);
            } else {
                int side = random.nextInt(4);
                if (side == 0) { m.x = -margin; m.y = random.nextFloat() * height; }
                else if (side == 1) { m.x = width + margin; m.y = random.nextFloat() * height; }
                else if (side == 2) { m.x = random.nextFloat() * width; m.y = -margin; }
                else { m.x = random.nextFloat() * width; m.y = height + margin; }
            }
            m.radius = Math.max(22f, width * 0.047f);
            m.speed = Math.max(125f, width * (0.30f + random.nextFloat() * 0.18f));
            float angle = random.nextFloat() * (float) Math.PI * 2f;
            m.vx = (float) Math.cos(angle) * m.speed;
            m.vy = (float) Math.sin(angle) * m.speed;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            long now = System.nanoTime();
            float dt = 0f;
            if (lastTimeNanos != 0) {
                dt = Math.min(0.033f, (now - lastTimeNanos) / 1_000_000_000f);
            }
            lastTimeNanos = now;

            drawBackground(canvas);
            if (started && !gameOver) {
                update(dt);
            }
            drawCheeseDots(canvas);
            if (mice != null) {
                for (Mouse m : mice) drawMouse(canvas, m);
            }
            drawCat(canvas, catX, catY, catRadius);
            drawUi(canvas);

            if (!started) drawStartOverlay(canvas);
            if (gameOver) drawGameOverOverlay(canvas);

            postInvalidateOnAnimation();
        }

        private void update(float dt) {
            timeLeft -= dt;
            if (timeLeft <= 0) {
                timeLeft = 0;
                gameOver = true;
                started = false;
                tone.startTone(ToneGenerator.TONE_PROP_NACK, 180);
                return;
            }

            float catSpeed = Math.max(430f, width * 0.86f);
            float dx = targetX - catX;
            float dy = targetY - catY;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist > 3f) {
                float move = Math.min(dist, catSpeed * dt);
                catX += dx / dist * move;
                catY += dy / dist * move;
            }
            catX = clamp(catX, catRadius, width - catRadius);
            catY = clamp(catY, catRadius + 25f, height - catRadius);

            for (Mouse m : mice) {
                float ax = m.x - catX;
                float ay = m.y - catY;
                float d = (float) Math.sqrt(ax * ax + ay * ay) + 0.001f;
                if (d < width * 0.55f) {
                    m.vx += ax / d * m.speed * 2.4f * dt;
                    m.vy += ay / d * m.speed * 2.4f * dt;
                }

                float speedNow = (float) Math.sqrt(m.vx * m.vx + m.vy * m.vy);
                float maxSpeed = m.speed * 1.35f;
                if (speedNow > maxSpeed) {
                    m.vx = m.vx / speedNow * maxSpeed;
                    m.vy = m.vy / speedNow * maxSpeed;
                }

                m.x += m.vx * dt;
                m.y += m.vy * dt;

                if (m.x < m.radius) { m.x = m.radius; m.vx = Math.abs(m.vx); }
                if (m.x > width - m.radius) { m.x = width - m.radius; m.vx = -Math.abs(m.vx); }
                if (m.y < m.radius + 50f) { m.y = m.radius + 50f; m.vy = Math.abs(m.vy); }
                if (m.y > height - m.radius) { m.y = height - m.radius; m.vy = -Math.abs(m.vy); }

                float catchDistance = catRadius * 0.82f + m.radius * 0.80f;
                float cdx = m.x - catX;
                float cdy = m.y - catY;
                if (cdx * cdx + cdy * cdy < catchDistance * catchDistance) {
                    score++;
                    tone.startTone(ToneGenerator.TONE_PROP_BEEP, 65);
                    resetMouse(m, false);
                }
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                if (gameOver) {
                    resetGame();
                    startGame();
                    targetX = event.getX();
                    targetY = event.getY();
                    return true;
                }
                if (!started) startGame();
            }
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                targetX = event.getX();
                targetY = event.getY();
                return true;
            }
            return true;
        }

        private void drawBackground(Canvas c) {
            p.setShader(new android.graphics.LinearGradient(0, 0, 0, height, backgroundTop, backgroundBottom, Shader.TileMode.CLAMP));
            c.drawRect(0, 0, width, height, p);
            p.setShader(null);

            p.setColor(Color.argb(45, 255, 255, 255));
            for (int i = 0; i < 9; i++) {
                float x = (width / 8f) * i;
                c.drawCircle(x, height * 0.16f + (i % 3) * 42f, width * 0.12f, p);
            }
        }

        private void drawCheeseDots(Canvas c) {
            p.setColor(Color.argb(60, 255, 195, 69));
            for (int i = 0; i < 18; i++) {
                float x = ((i * 67) % Math.max(1, width));
                float y = 95 + ((i * 131) % Math.max(1, height - 145));
                c.drawCircle(x, y, 5 + (i % 3) * 2, p);
            }
        }

        private void drawCat(Canvas c, float x, float y, float r) {
            p.setShader(new RadialGradient(x - r * .35f, y - r * .35f, r * 1.3f, Color.rgb(255, 200, 200), catColor, Shader.TileMode.CLAMP));
            c.drawCircle(x, y, r, p);
            p.setShader(null);

            Path leftEar = new Path();
            leftEar.moveTo(x - r * .78f, y - r * .55f);
            leftEar.lineTo(x - r * 1.05f, y - r * 1.25f);
            leftEar.lineTo(x - r * .30f, y - r * .90f);
            leftEar.close();
            Path rightEar = new Path();
            rightEar.moveTo(x + r * .78f, y - r * .55f);
            rightEar.lineTo(x + r * 1.05f, y - r * 1.25f);
            rightEar.lineTo(x + r * .30f, y - r * .90f);
            rightEar.close();
            p.setColor(catEarColor);
            c.drawPath(leftEar, p);
            c.drawPath(rightEar, p);

            p.setColor(Color.rgb(45, 45, 55));
            c.drawCircle(x - r * .34f, y - r * .12f, r * .10f, p);
            c.drawCircle(x + r * .34f, y - r * .12f, r * .10f, p);

            p.setColor(Color.WHITE);
            c.drawOval(x - r * .23f, y + r * .10f, x + r * .23f, y + r * .36f, p);
            p.setColor(Color.rgb(42, 42, 48));
            Path nose = new Path();
            nose.moveTo(x, y + r * .12f);
            nose.lineTo(x - r * .10f, y + r * .24f);
            nose.lineTo(x + r * .10f, y + r * .24f);
            nose.close();
            c.drawPath(nose, p);

            p.setStrokeWidth(Math.max(3f, r * .045f));
            p.setStyle(Paint.Style.STROKE);
            p.setColor(Color.argb(180, 70, 70, 80));
            for (int s = -1; s <= 1; s += 2) {
                c.drawLine(x + s * r * .24f, y + r * .21f, x + s * r * .72f, y + r * .08f, p);
                c.drawLine(x + s * r * .24f, y + r * .28f, x + s * r * .72f, y + r * .31f, p);
            }
            p.setStyle(Paint.Style.FILL);
        }

        private void drawMouse(Canvas c, Mouse m) {
            p.setColor(Color.argb(45, 0, 0, 0));
            c.drawOval(m.x - m.radius * .9f, m.y + m.radius * .45f, m.x + m.radius * .9f, m.y + m.radius * .78f, p);
            p.setColor(mouseColor);
            c.drawOval(m.x - m.radius * .85f, m.y - m.radius * .58f, m.x + m.radius * .85f, m.y + m.radius * .58f, p);
            p.setColor(Color.rgb(170, 170, 178));
            c.drawCircle(m.x - m.radius * .55f, m.y - m.radius * .48f, m.radius * .34f, p);
            c.drawCircle(m.x + m.radius * .55f, m.y - m.radius * .48f, m.radius * .34f, p);
            p.setColor(Color.rgb(35, 35, 40));
            c.drawCircle(m.x - m.radius * .23f, m.y - m.radius * .06f, m.radius * .07f, p);
            c.drawCircle(m.x + m.radius * .23f, m.y - m.radius * .06f, m.radius * .07f, p);
            p.setColor(Color.rgb(245, 190, 195));
            c.drawCircle(m.x, m.y + m.radius * .15f, m.radius * .08f, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(2f, m.radius * .07f));
            p.setColor(Color.rgb(100, 100, 108));
            c.drawArc(m.x - m.radius * 1.35f, m.y - m.radius * .15f, m.x - m.radius * .1f, m.y + m.radius * 1.05f, 5, 115, false, p);
            p.setStyle(Paint.Style.FILL);
        }

        private void drawUi(Canvas c) {
            p.setShader(null);
            p.setColor(Color.argb(170, 255, 255, 255));
            c.drawRoundRect(18, 18, width - 18, 76, 28, 28, p);
            p.setColor(uiText);
            p.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
            p.setTextSize(Math.max(28f, width * 0.055f));
            p.setTextAlign(Paint.Align.LEFT);
            c.drawText("Score: " + score, 38, 58, p);
            p.setTextAlign(Paint.Align.RIGHT);
            c.drawText("Time: " + (int) Math.ceil(timeLeft), width - 38, 58, p);
            p.setTextAlign(Paint.Align.LEFT);
        }

        private void drawStartOverlay(Canvas c) {
            p.setColor(Color.argb(158, 255, 255, 255));
            c.drawRoundRect(width * .08f, height * .30f, width * .92f, height * .62f, 44, 44, p);
            p.setColor(uiText);
            p.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
            p.setTextAlign(Paint.Align.CENTER);
            p.setTextSize(Math.max(34f, width * .077f));
            c.drawText("قطة المطاردة", width / 2f, height * .40f, p);
            p.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
            p.setTextSize(Math.max(22f, width * .047f));
            c.drawText("المس الشاشة وخلّي القطة تلحق الفئران", width / 2f, height * .48f, p);
            p.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
            p.setTextSize(Math.max(24f, width * .052f));
            c.drawText("اضغط للبدء", width / 2f, height * .56f, p);
            p.setTextAlign(Paint.Align.LEFT);
        }

        private void drawGameOverOverlay(Canvas c) {
            p.setColor(Color.argb(185, 255, 255, 255));
            c.drawRoundRect(width * .08f, height * .31f, width * .92f, height * .65f, 44, 44, p);
            p.setColor(uiText);
            p.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
            p.setTextAlign(Paint.Align.CENTER);
            p.setTextSize(Math.max(32f, width * .074f));
            c.drawText("انتهى الوقت!", width / 2f, height * .42f, p);
            p.setTextSize(Math.max(26f, width * .06f));
            c.drawText("نتيجتك: " + score, width / 2f, height * .51f, p);
            p.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
            p.setTextSize(Math.max(22f, width * .048f));
            c.drawText("اضغط لإعادة اللعب", width / 2f, height * .59f, p);
            p.setTextAlign(Paint.Align.LEFT);
        }

        private float clamp(float v, float min, float max) {
            return Math.max(min, Math.min(max, v));
        }

        static class Mouse {
            float x, y, vx, vy, speed, radius;
        }
    }
}
