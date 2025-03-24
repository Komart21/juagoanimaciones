package com.mpes;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.Input;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture spriteSheet;
    private Texture background;
    private TextureRegion[][] spriteFrames;
    private Animation<TextureRegion> walkUp, walkDown, walkLeft, walkRight;
    private float stateTime;
    private float bgPosx, bgPosy;

    private final float SPEED = 200f;

    private Rectangle up, down, left, right;
    private final int IDLE = 0, UP = 1, DOWN = 2, LEFT = 3, RIGHT = 4;

    // Almacenar la última dirección para usarla con el joystick
    private int lastDirection = DOWN;

    @Override
    public void create() {
        batch = new SpriteBatch();
        spriteSheet = new Texture(Gdx.files.internal("Sprite.png"));
        background = new Texture(Gdx.files.internal("fondo.jpg"));
        background.setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.MirroredRepeat);

        // Dividimos el sprite en cuadros de 32x32 (ajusta esto si es diferente)
        spriteFrames = TextureRegion.split(spriteSheet, spriteSheet.getWidth() / 4, spriteSheet.getHeight() / 4);

        // Creamos animaciones para cada dirección (Nota: intercambiamos filas 0 y 1)
        walkDown = new Animation<>(0.1f, spriteFrames[0]);
        walkUp = new Animation<>(0.1f, spriteFrames[1]);
        walkLeft = new Animation<>(0.1f, spriteFrames[2]);
        walkRight = new Animation<>(0.1f, spriteFrames[3]);

        stateTime = 0f;
        bgPosx = 0;
        bgPosy = 0;

        // Definimos las regiones para el joystick virtual
        up = new Rectangle(0, Gdx.graphics.getHeight() * 2 / 3, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() / 3);
        down = new Rectangle(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() / 3);
        left = new Rectangle(0, 0, Gdx.graphics.getWidth() / 3, Gdx.graphics.getHeight());
        right = new Rectangle(Gdx.graphics.getWidth() * 2 / 3, 0, Gdx.graphics.getWidth() / 3, Gdx.graphics.getHeight());
    }


    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        // Actualizamos el tiempo para las animaciones
        stateTime += Gdx.graphics.getDeltaTime();

        // Movimiento del fondo controlado por las teclas o el joystick virtual
        handleInput();

        // Actualizamos la porción del fondo que se ve en pantalla
        TextureRegion bgRegion = new TextureRegion(background);
        bgRegion.setRegion((int) bgPosx, (int) bgPosy, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Coordenadas fijas del personaje (siempre en el centro de la pantalla)
        float playerX = (Gdx.graphics.getWidth() - spriteSheet.getWidth() / 4) / 2;
        float playerY = (Gdx.graphics.getHeight() - spriteSheet.getHeight() / 4) / 2;

        // Dibujamos el fondo y el personaje
        batch.begin();
        batch.draw(bgRegion, 0, 0);
        batch.draw(getCurrentFrame(), playerX, playerY);
        batch.end();
    }

    private void handleInput() {
        float delta = Gdx.graphics.getDeltaTime();
        int direction = virtualJoystickControl(); // Control por joystick virtual
        boolean moving = false;

        // Control del teclado
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || direction == UP) {
            bgPosy -= SPEED * delta;
            lastDirection = UP;
            moving = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || direction == DOWN) {
            bgPosy += SPEED * delta;
            lastDirection = DOWN;
            moving = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || direction == LEFT) {
            bgPosx -= SPEED * delta;
            lastDirection = LEFT;
            moving = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || direction == RIGHT) {
            bgPosx += SPEED * delta;
            lastDirection = RIGHT;
            moving = true;
        }

        // Si el jugador no se está moviendo, mantenemos la última dirección
        if (!moving) {
            stateTime = 0; // Reiniciar la animación cuando el jugador esté quieto
        }
    }

    private TextureRegion getCurrentFrame() {
        // Si el jugador se está moviendo, mostramos la animación de movimiento
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || lastDirection == UP) {
            return walkUp.getKeyFrame(stateTime, true);
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || lastDirection == DOWN) {
            return walkDown.getKeyFrame(stateTime, true);
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || lastDirection == LEFT) {
            return walkLeft.getKeyFrame(stateTime, true);
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || lastDirection == RIGHT) {
            return walkRight.getKeyFrame(stateTime, true);
        }

        // Si el jugador está quieto, mantenemos la animación en la última dirección
        switch (lastDirection) {
            case UP:
                return walkUp.getKeyFrame(0, true);
            case DOWN:
                return walkDown.getKeyFrame(0, true);
            case LEFT:
                return walkLeft.getKeyFrame(0, true);
            case RIGHT:
                return walkRight.getKeyFrame(0, true);
            default:
                return walkDown.getKeyFrame(0, true);
        }
    }


    // Implementación del joystick virtual
    protected int virtualJoystickControl() {
        for (int i = 0; i < 10; i++) {
            if (Gdx.input.isTouched(i)) {
                Vector3 touchPos = new Vector3();
                touchPos.set(Gdx.input.getX(i), Gdx.input.getY(i), 0);
                touchPos = new Vector3(Gdx.input.getX(i), Gdx.graphics.getHeight() - Gdx.input.getY(i), 0); // Ajustamos las coordenadas para que coincidan con la pantalla

                if (up.contains(touchPos.x, touchPos.y)) {
                    return UP;
                } else if (down.contains(touchPos.x, touchPos.y)) {
                    return DOWN;
                } else if (left.contains(touchPos.x, touchPos.y)) {
                    return LEFT;
                } else if (right.contains(touchPos.x, touchPos.y)) {
                    return RIGHT;
                }
            }
        }
        return IDLE;
    }

    @Override
    public void dispose() {
        batch.dispose();
        spriteSheet.dispose();
        background.dispose();
    }
}
