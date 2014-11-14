package asf.dungeon;

import asf.dungeon.utility.UtMath;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;

/**
 * Created by danny on 10/19/14.
 */
public class LoadingScreen implements Screen {
        final DungeonApp app;
        private Stage stage;

        public LoadingScreen(DungeonApp dungeonApp) {
                app = dungeonApp;
        }




        private Label loadingLabel;

        @Override
        public void show() {
                Gdx.gl.glClearColor(0, 0, 0.2f, 1);

                stage = app.stage;
                Skin skin = app.skin;
                stage.clear();

                Table table = new Table(skin);
                stage.addActor(table);
                table.setFillParent(true);

                loadingLabel = new Label("Loading ...", skin);
                loadingLabel.setFontScale(3);
                table.add(loadingLabel).minSize(300,100).align(Align.left);


        }

        @Override
        public void resize(int width, int height) {

        }

        @Override
        public void hide() {
                //Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
                dispose();
        }

        private float loadingCount = 0;

        @Override
        public void render(float delta) {

                loadingCount += delta;
                float n = UtMath.scalarLimitsInterpolation(loadingCount, 0f, .75f, 0f, 4f);
                if(n < 1){
                        loadingLabel.setText("Loading .");
                }else if(n <2){
                        loadingLabel.setText("Loading ..");
                }else if(n<3){
                        loadingLabel.setText("Loading ...");
                }else if(n<4){
                        loadingLabel.setText("Loading ..");
                }else{
                        loadingCount = 0;
                }


                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
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
}
