package asf.dungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;


/**
 * Created by danny on 10/19/14.
 */
public class MainMenuScreen implements Screen {
        final DungeonApp app;
        private Stage stage;
        private InternalListener internalListener;


        public MainMenuScreen(DungeonApp dungeonApp) {
                app = dungeonApp;
        }

        private Button playButton, leaderBoardsButton, settingsButton, aboutButton, quitButton;
        @Override
        public void show() {
                stage = app.stage;
                Gdx.graphics.setContinuousRendering(false);
                Gdx.graphics.requestRendering();
                Gdx.input.setInputProcessor(stage);
                Skin skin = app.skin;

                stage.clear();

                internalListener = new InternalListener();

                Table table = new Table(skin);
                stage.addActor(table);

                table.setFillParent(true);

                playButton = new Button(skin);
                table.add(playButton).minSize(100,100);
                playButton.add("Play");
                playButton.addCaptureListener(internalListener);

                leaderBoardsButton = new Button(skin);
                table.add(leaderBoardsButton).minSize(100,100);
                leaderBoardsButton.add("Leaderboards");

                settingsButton = new Button(skin);
                table.add(settingsButton).minSize(100,100);
                settingsButton.add("Settings");

                aboutButton = new Button(skin);
                table.add(aboutButton).minSize(100,100);
                aboutButton.add("About");

                quitButton = new Button(skin);
                stage.addActor(quitButton);
                quitButton.add("Quit");

                quitButton.addCaptureListener(internalListener);

        }

        @Override
        public void resize(int width, int height) {
                quitButton.setBounds(
                        Gdx.graphics.getWidth() - 110,
                        Gdx.graphics.getHeight() - 110,
                        100,100);
        }

        @Override
        public void hide() {
                dispose();
        }

        @Override
        public void render(float delta) {
                Gdx.gl.glClearColor(0, 0, 0.2f, 1);
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
                //Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

                app.stage.draw();


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

        private class InternalListener extends ClickListener {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                        if(event.getListenerActor() == playButton){
                                app.setScreen(new PlayMenuScreen(app));

                        }else if(event.getListenerActor() == quitButton){
                                app.exitApp();
                        }
                }
        }
}
