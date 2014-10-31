package asf.dungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Created by danny on 10/19/14.
 */
public class PauseScreen implements Screen {
        final DungeonApp game;
        public SpriteBatch batch;
        public BitmapFont font;

        OrthographicCamera camera;

        public PauseScreen(DungeonApp dungeonApp) {
                game = dungeonApp;

                batch = new SpriteBatch();
                font = new BitmapFont();

                camera = new OrthographicCamera();
                camera.setToOrtho(false, 800, 480);
                camera.update();
        }

        @Override
        public void render(float delta) {
                //Gdx.gl.glClearColor(0, 0, 0.2f, 1);
                //Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


                batch.setProjectionMatrix(camera.combined);

                batch.begin();
                font.draw(batch, "Paused", 100, 150);
                font.draw(batch, "Tap to continue.", 100, 100);
                batch.end();

                if (Gdx.input.isTouched()) {
                        game.setWorldPaused(false);
                }else if(Gdx.input.isKeyPressed(Input.Keys.ESCAPE)){
                        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)){
                                game.exitApp();
                        }else{
                                game.setWorldPaused(false);
                        }
                }
        }

        @Override
        public void resize(int width, int height) {

        }

        @Override
        public void show() {

        }

        @Override
        public void hide() {
                //Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
                dispose();
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
