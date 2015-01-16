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
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.I18NBundle;


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
        private Button resumeButton, returnToMainMenuButton;
        private ImageButton soundButton;
        @Override
        public void show() {
                //app.dungeonWorld.hudSpatial.
                stage = app.stage;
                Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));
                Skin skin = app.skin;
                I18NBundle i18n = app.i18n;
                stage.clear();

                soundButton = new ImageButton(skin);
                ImageButton.ImageButtonStyle style = soundButton.getStyle();
                style.imageUp = new TextureRegionDrawable(app.pack.findRegion("Volume"));
                style.imageChecked = new TextureRegionDrawable(app.pack.findRegion("VolumeMuted"));
                stage.addActor(soundButton);
                soundButton.addCaptureListener(this);
                soundButton.setChecked(!app.music.isMusicEnabled());


                window = new Window(i18n.get("paused"),skin);
                stage.addActor(window);

                //window.setFillParent(true);

                window.center();
                window.setMovable(false);
                window.setModal(false);
                window.addCaptureListener(this);
                window.removeActor(window.getButtonTable());

                window.row();
                resumeButton = new Button(skin);
                resumeButton.add(i18n.get("resume"));
                resumeButton.addCaptureListener(this);
                window.add(resumeButton).minSize(150,100);

                window.row();
                returnToMainMenuButton = new Button(skin);
                returnToMainMenuButton.add("Return to Main Menu");
                returnToMainMenuButton.addCaptureListener(this);
                        window.add(returnToMainMenuButton).minSize(150,100);


        }

        @Override
        public void resize(int graphicsWidth, int graphicsHeight) {
                float width = graphicsWidth * 0.7f;
                float height = graphicsHeight * 0.65f;
                window.setBounds(
                        (graphicsWidth-width)*0.5f,
                        (graphicsHeight-height)*0.5f,
                        width,
                        height);
                soundButton.setBounds(
                        graphicsWidth - 110,
                        graphicsHeight - 110,
                        100,100);
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
                                                app.setAppPaused(false);
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
                        app.setAppPaused(false);
                        return true;
                }else if(event.getListenerActor() == returnToMainMenuButton){
                        app.returnToMainMenu();
                }else if(event.getListenerActor() == soundButton){
                        app.music.setMusicEnabled(!soundButton.isChecked());
                        app.prefs.putBoolean("musicEnabled",app.music.isMusicEnabled() );
                        app.prefs.flush();
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
                        app.setAppPaused(false);
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
