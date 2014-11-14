package asf.dungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;


/**
 * Created by danny on 10/19/14.
 */
public class PauseScreen implements Screen, InputProcessor, EventListener {
        final DungeonApp app;
        private Stage stage;

        public PauseScreen(DungeonApp dungeonApp) {
                app = dungeonApp;
        }

        private Window window;
        private Button resumeButton;
        @Override
        public void show() {
                stage = app.stage;
                Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));
                Skin skin = app.skin;
                stage.clear();

                Table table = new Table(skin);
                stage.addActor(table);
                table.setFillParent(true);

                window = new Window("Paused",skin);
                table.add(window).minSize(Gdx.graphics.getWidth()*.75f, Gdx.graphics.getHeight()*.75f);

                //window.setFillParent(true);

                window.center();
                window.setMovable(false);
                window.setModal(true);
                window.addCaptureListener(this);
                window.removeActor(window.getButtonTable());

                window.row();
                resumeButton = new Button(skin);
                resumeButton.add("Resume");
                resumeButton.addCaptureListener(this);
                window.add(resumeButton).minSize(150,100);


        }

        @Override
        public void resize(int width, int height) {

        }

        @Override
        public void hide() {

                dispose();
        }

        @Override
        public void render(float delta) {
                //Gdx.gl.glClearColor(0, 0, 0.2f, 1);
                //Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

                stage.draw();


        }

        @Override
        public void pause() {

        }

        @Override
        public void resume() {

        }

        @Override
        public void dispose() {

        }

        @Override
        public boolean handle(Event event) {
                if (event.getTarget() == window) {
                        if (event instanceof InputEvent) {
                                InputEvent inputEvent = (InputEvent) event;
                                if (inputEvent.getType() == InputEvent.Type.touchDown) {
                                        float clickX = inputEvent.getStageX();
                                        float clickY = inputEvent.getStageY();
                                        if (clickX < window.getX() || clickY < window.getY() || clickX > window.getX() + window.getWidth() || clickY > window.getY() + window.getHeight()) {
                                                app.setWorldPaused(false);
                                                return true;
                                        }
                                }
                        }
                        return false;
                }

                if (!(event instanceof ChangeListener.ChangeEvent)) {
                        return false;
                }

                if (event.getListenerActor() == resumeButton) {
                        app.setWorldPaused(false);
                        return true;
                }

                return false;
        }

        @Override
        public boolean keyDown(int keycode) {
                if(keycode == Input.Keys.ESCAPE){
                        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)){
                                app.exitApp();
                                return true;
                        }
                }
                return false;
        }

        @Override
        public boolean keyUp(int keycode) {
                if(keycode == Input.Keys.ESCAPE){
                        app.setWorldPaused(false);
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
                return false;

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
