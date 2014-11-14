package asf.dungeon;

import asf.dungeon.model.ModelId;
import asf.dungeon.model.token.Token;
import asf.dungeon.view.DungeonWorld;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;


/**
 * Created by danny on 10/19/14.
 */
public class PlayMenuScreen implements Screen {
        final DungeonApp app;
        private Stage stage;
        private Skin skin;
        private InternalListener internalListener;


        public PlayMenuScreen(DungeonApp dungeonApp) {
                app = dungeonApp;
        }

        private Preferences prefs;
        private Table table;
        private Button knightButton, rogueButton, mageButton, monsterButton, backButton;
        private Label descriptionLabel;
        private HorizontalGroup horizontalGroup;
        private Button loadButton, newGameButton;
        private ButtonGroup buttonGroup;
        @Override
        public void show() {

                stage = app.stage;
                skin = app.skin;
                Gdx.graphics.setContinuousRendering(false);
                Gdx.graphics.requestRendering();
                Gdx.input.setInputProcessor(stage);


                stage.clear();

                internalListener = new InternalListener();

                backButton = new Button(skin);
                stage.addActor(backButton);
                backButton.add("Back");
                backButton.addCaptureListener(internalListener);

                table = new Table(skin);
                stage.addActor(table);
                table.defaults().pad(10);
                table.setFillParent(true);

                //CheckBox cb;
                table.row();

                knightButton = new Button(skin);
                table.add(knightButton).minSize(100, 100);
                knightButton.add("Knight");
                knightButton.addListener(internalListener);

                rogueButton = new Button(skin);
                table.add(rogueButton).minSize(100,100);
                rogueButton.add("Rogue");
                rogueButton.addListener(internalListener);

                mageButton = new Button(skin);
                table.add(mageButton).minSize(100,100);
                mageButton.add("Mage");
                mageButton.addListener(internalListener);

                monsterButton = new Button(skin);
                table.add(monsterButton).minSize(100,100);
                monsterButton.add("Monster");
                monsterButton.addListener(internalListener);



                table.row();

                descriptionLabel = new Label("Description", skin);
                ScrollPane scrollPane = new ScrollPane(descriptionLabel, skin);
                table.add(scrollPane).colspan(4).minSize(400,300);


                table.row();
                horizontalGroup=  new HorizontalGroup();
                table.add(horizontalGroup).colspan(4);
                horizontalGroup.pad(10).space(10);


                loadButton = new Button(skin);
                loadButton.add("Load").minSize(150,100).align(Align.center).getActor().setAlignment(Align.center);
                loadButton.addCaptureListener(internalListener);


                newGameButton = new Button(skin);
                horizontalGroup.addActor(newGameButton);
                newGameButton.add("New Game").minSize(150,100).align(Align.center).getActor().setAlignment(Align.center);
                newGameButton.addCaptureListener(internalListener);



                buttonGroup = new ButtonGroup(knightButton,rogueButton,mageButton,monsterButton);
                buttonGroup.setMaxCheckCount(1);
                buttonGroup.setMinCheckCount(1);
                //buttonGroup.setUncheckLast(true);


                prefs = Gdx.app.getPreferences("Dungeon");
                int selectedHero = prefs.getInteger("selectedHero" ,0);
                if(selectedHero == 1){
                        rogueButton.setChecked(true);
                }else if(selectedHero == 2){
                        mageButton.setChecked(true);
                }else if(selectedHero == 3){
                        monsterButton.setChecked(true);
                }else{
                        knightButton.setChecked(true);
                }
        }

        private void setShowLoadButton(boolean show){
                if(show && loadButton.getParent() == null){
                        horizontalGroup.addActorAt(0, loadButton);
                }else if(!show && loadButton.getParent() != null){
                        loadButton.remove();
                }
        }

        @Override
        public void resize(int width, int height) {
                backButton.setBounds(
                        Gdx.graphics.getWidth() - 110,
                        Gdx.graphics.getHeight() - 110,
                        100, 100);
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

        private class InternalListener extends ChangeListener {


                DungeonWorld.Settings settings;
                private void makeSettings(){
                        settings = (DungeonWorld.Settings)buttonGroup.getChecked().getUserObject();

                        if(settings == null){
                                settings = new DungeonWorld.Settings();
                                if(buttonGroup.getChecked() == knightButton){
                                        settings.playerModel = ModelId.Knight;
                                        prefs.putInteger("selectedHero" ,0);
                                }else if(buttonGroup.getChecked() == rogueButton){
                                        settings.playerModel = ModelId.Archer;
                                        prefs.putInteger("selectedHero" ,1);
                                }else if(buttonGroup.getChecked() == mageButton){
                                        settings.playerModel = ModelId.Mage;
                                        prefs.putInteger("selectedHero" ,2);
                                }else if(buttonGroup.getChecked() == monsterButton){
                                        settings.playerModel = ModelId.Diablous;
                                        prefs.putInteger("selectedHero" ,3);
                                }else{
                                      throw new AssertionError(buttonGroup.getChecked());
                                }
                                settings.loadDungeon();
                                buttonGroup.getChecked().setUserObject(settings);
                        }


                        Token token = settings.getLocalPlayerToken();

                        setShowLoadButton(token != null);

                        descriptionLabel.setText(settings.playerModel.toString());
                }

                @Override
                public void changed(ChangeEvent event, Actor actor) {
                        if(actor == loadButton){
                                prefs.flush();
                                app.loadWorld(settings);
                        }else if(actor == newGameButton){
                                prefs.flush();
                                settings.loadedDungeon = null;
                                app.loadWorld(settings);
                        }else if(actor == backButton){
                                prefs.flush();
                                app.setScreen(new MainMenuScreen(app));
                        }


                        Button heroButton = (Button) actor;
                        if(heroButton.isChecked()){
                                makeSettings();
                        }
                }
        }
}
