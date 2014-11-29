package asf.dungeon;

import asf.dungeon.view.DungeonWorld;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.I18NBundle;


/**
 * Created by danny on 10/19/14.
 */
public class GameOverScreen implements Screen, InputProcessor, EventListener {
        final DungeonApp app;
         DungeonWorld world;
        private Stage stage;

        public GameOverScreen(DungeonApp dungeonApp, DungeonWorld world) {
                app = dungeonApp;
                this.world = world;
        }

        private Window window;
        private Button resumeButton, returnToMainMenuButton;


        @Override
        public void show() {
                stage = app.stage;
                Gdx.input.setInputProcessor(new InputMultiplexer(stage, this));
                Skin skin = app.skin;
                I18NBundle i18n = app.i18n;
                stage.clear();

                Table table = new Table(skin);
                stage.addActor(table);
                table.setFillParent(true);


                window = new Window("GAME OVER",skin);
                table.add(window).minSize(Gdx.graphics.getWidth()*.75f, Gdx.graphics.getHeight()*.75f);

                //window.setFillParent(true);

                window.center();
                window.setMovable(false);
                window.setModal(true);
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
        public void resize(int width, int height) {

        }

        @Override
        public void hide() {

                dispose();
        }

        @Override
        public void render(float delta) {
                if(world != null){
                        // I pause the world here instead of inside of DUngeonWorld itself so that saving and pausing doesnt
                        // happen during the middle of the game loop
                        world.setPaused(true);
                        world = null;
                }
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
                if (!(event instanceof ChangeListener.ChangeEvent)) {
                        return false;
                }

                if (event.getListenerActor() == resumeButton) {
                        app.setAppPaused(false);
                        return true;
                }else if(event.getListenerActor() == returnToMainMenuButton){
                        app.returnToMainMenu();
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
