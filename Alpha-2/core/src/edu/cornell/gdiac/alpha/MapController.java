package edu.cornell.gdiac.alpha;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
//import com.badlogic.gdx.backends.lwjgl.*;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import edu.cornell.gdiac.alpha.objects.PlayerModel;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

import java.util.Map;

public class MapController implements ApplicationListener {

    private OrthographicCamera camera;
    private PlayerModel player;
    private SpriteBatch batch;

    public MapController(PlayerModel pl) {
        player = pl;
        create();
    }

    public void setPlayer (PlayerModel avatar) { player = avatar; }

    @Override
    public void create() {
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.setToOrtho(true,100,100);
        camera.update();

        batch = new SpriteBatch();
    }

    @Override
    public void dispose() {}

    @Override
    public void render() {
        camera.position.set(player.getX(),player.getY(),0);
        batch.setProjectionMatrix(camera.combined);
        //player.draw();
        camera.update();

    }
    @Override
    public void resize(int width, int height) {


    }
    @Override
    public void pause() {}
    @Override
    public void resume() {}
//static final int WORLD_WIDTH = 100;
//    static final int WORLD_HEIGHT = 100;
//
//    private OrthographicCamera cam;
//    private SpriteBatch batch;
//
//    private Sprite mapSprite;
//    private float rotationSpeed;
//
//    @Override
//    public void create() {
//        // Constructs a new OrthographicCamera, using the given viewport width and height
//        // Height is multiplied by aspect ratio.
//        cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//
//        cam.position.set(0,0,0);
//        cam.update();
//
//        batch = new SpriteBatch();
//    }
//
//    @Override
//    public void render() {
//        handleInput();
//        cam.update();
//        batch.setProjectionMatrix(cam.combined);
//
//        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//
//        batch.begin();
//       // mapSprite.draw(batch);
//        batch.end();
//    }
//
//    private void handleInput() {
//        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
//            cam.zoom += 0.02;
//            System.out.println("in");
//        }
//        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
//            cam.zoom -= 0.02;
//            System.out.println("out");
//        }
//        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
//            cam.translate(-3, 0, 0);
//        }
//        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
//            cam.translate(3, 0, 0);
//        }
//        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
//            cam.translate(0, -3, 0);
//        }
//        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
//            cam.translate(0, 3, 0);
//        }
//        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
//            cam.rotate(-rotationSpeed, 0, 0, 1);
//        }
//        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
//            cam.rotate(rotationSpeed, 0, 0, 1);
//        }
//
//        cam.zoom = MathUtils.clamp(cam.zoom, 0.1f, 100/cam.viewportWidth);
//
//        float effectiveViewportWidth = cam.viewportWidth * cam.zoom;
//        float effectiveViewportHeight = cam.viewportHeight * cam.zoom;
//
//        cam.position.x = MathUtils.clamp(cam.position.x, effectiveViewportWidth / 2f, 100 - effectiveViewportWidth / 2f);
//        cam.position.y = MathUtils.clamp(cam.position.y, effectiveViewportHeight / 2f, 100 - effectiveViewportHeight / 2f);
//    }
//
//    @Override
//    public void resize(int width, int height) {
//        cam.viewportWidth = 30f;
//        cam.viewportHeight = 30f * height/width;
//        cam.update();
//    }
//
//    @Override
//    public void resume() {
//    }
//
//    @Override
//    public void dispose() {
//        mapSprite.getTexture().dispose();
//        batch.dispose();
//    }
//
//    @Override
//    public void pause() {
//    }
//
}