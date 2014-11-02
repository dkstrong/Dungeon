package asf.dungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Created by danny on 10/19/14.
 */
public class PauseScreen implements Screen, InputProcessor{
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


        }

        @Override
        public void resize(int width, int height) {

        }

        @Override
        public void show() {
                Gdx.input.setInputProcessor(this);
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

        @Override
        public boolean keyDown(int keycode) {
                if(keycode == Input.Keys.ESCAPE){
                        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)){
                                game.exitApp();
                                return true;
                        }
                }
                return false;
        }

        @Override
        public boolean keyUp(int keycode) {
                if(keycode == Input.Keys.ESCAPE){
                        game.setWorldPaused(false);
                        return true;
                }
                return false;
        }

        @Override
        public boolean keyTyped(char character) {
                return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                game.setWorldPaused(false);
                return true;

        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
                return false;
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
                return false;
        }

        @Override
        public boolean scrolled(int amount) {
                return false;
        }
}
