package asf.dungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Created by danny on 10/19/14.
 */
public class MainMenuScreen implements Screen {
        final DungeonApp game;
        public SpriteBatch batch;
        public BitmapFont font;

        OrthographicCamera camera;

        public MainMenuScreen(DungeonApp dungeonApp) {
                game = dungeonApp;

                batch = new SpriteBatch();
                font = new BitmapFont();

                camera = new OrthographicCamera();
                camera.setToOrtho(false, 800, 480);
        }

        @Override
        public void render(float delta) {
                Gdx.gl.glClearColor(0, 0, 0.2f, 1);
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

                camera.update();
                batch.setProjectionMatrix(camera.combined);

                batch.begin();
                font.draw(batch, "Welcome to Dungeon Game!!! ", 100, 150);
                font.draw(batch, "Tap anywhere to begin!", 100, 100);
                batch.end();

                if (Gdx.input.isTouched()) {
                        game.loadWorld();
                        dispose();
                }
        }

        @Override
        public void resize(int width, int height) {

        }

        @Override
        public void show() {
                Gdx.graphics.setContinuousRendering(false);
                Gdx.graphics.requestRendering();

        }

        @Override
        public void hide() {

        }

        @Override
        public void pause() {

        }

        @Override
        public void resume() {

        }

        @Override
        public void dispose() {
                batch.dispose();
                font.dispose();
        }
}
