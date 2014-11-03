package asf.dungeon.view;

import asf.dungeon.board.CharacterToken;
import asf.dungeon.board.Direction;
import asf.dungeon.board.Token;
import asf.dungeon.board.pathfinder.Pair;
import asf.dungeon.utility.MoreMath;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/**
 * Created by Danny on 11/1/2014.
 */
public class HudSpatial implements Spatial,EventListener, InputProcessor {
        private boolean initialized = false;
        private DungeonWorld world;
        protected CharacterToken localPlayerToken;

        private boolean arrowKeysMove = true; // TODO: this setting should be stored on an app level object so it can be easily changed in the settings
        private boolean showRenderingStasLabel = true;

        private Skin skin;
        private Label label;
        private Button quickSlotButton, avatarButton;
        private Label renderingStats;
        private Touchpad touchPad;
        private Direction currentTouchPadDirection = null;
        private Window avatarWindow;




        @Override
        public void preload(DungeonWorld world) {
                this.world = world;
                skin = new Skin(Gdx.files.internal("Skins/DefaultSkin/uiskin.json"));
        }

        @Override
        public void init(AssetManager assetManager) {
                initialized = true;

                float margin = 15;
                float buttonSize = 100;
                float touchPadSize = 150;

                if(showRenderingStasLabel){
                        renderingStats = new Label("",skin);
                        world.stage.addActor(renderingStats);
                        renderingStats.setBounds(Gdx.graphics.getWidth()-buttonSize-margin,Gdx.graphics.getHeight()-buttonSize-margin,buttonSize,buttonSize);
                        renderingStats.setAlignment(Align.topRight, Align.right);
                }

                label = new Label(" ", skin);
                world.stage.addActor(label);
                label.setBounds(Gdx.graphics.getWidth()*.25f,margin,Gdx.graphics.getWidth()*.5f,buttonSize*.25f);
                label.setAlignment(Align.bottom, Align.center);

                avatarButton = new Button(skin);
                world.stage.addActor(avatarButton);
                avatarButton.setBounds(margin, Gdx.graphics.getHeight()-buttonSize-margin,buttonSize,buttonSize);
                avatarButton.add(new Label("Avatar", skin));
                avatarButton.addCaptureListener(this);


                quickSlotButton = new Button(skin);
                world.stage.addActor(quickSlotButton);
                quickSlotButton.setBounds(Gdx.graphics.getWidth()-buttonSize-margin,margin, buttonSize, buttonSize);
                quickSlotButton.add(new Label("<Empty>", skin));
                quickSlotButton.addCaptureListener(this);


                if(arrowKeysMove){
                        touchPad = new Touchpad(8, skin);
                        touchPad.setBounds(margin, margin, touchPadSize, touchPadSize);
                        world.stage.addActor(touchPad);
                        touchPad.addCaptureListener(this);
                }

                avatarWindow = new Window("Avatar",skin);
                world.stage.addActor(avatarWindow);
                avatarWindow.setModal(true);
                avatarWindow.setMovable(false);
                avatarWindow.setResizable(false);
                avatarWindow.setBounds(Gdx.graphics.getWidth() * .25f * .5f, Gdx.graphics.getHeight() * .25f * .5f, Gdx.graphics.getWidth() * .75f, Gdx.graphics.getHeight() * .75f);
                Button closeButton = new Button(skin);
                closeButton.add(new Label("X",skin));
                closeButton.addCaptureListener(new ChangeListener() {
                        @Override
                        public void changed(ChangeEvent event, Actor actor) {
                                avatarWindow.remove();
                        }
                });
                avatarWindow.add(closeButton);
                avatarWindow.debug();


                Label nameLabel = new Label("Name:", skin);
                TextField nameText = new TextField("", skin);
                Label addressLabel = new Label("Address:", skin);
                TextField addressText = new TextField("", skin);

                Table table = new Table(skin);
                table.setFillParent(true);
                //world.stage.addActor(table);

                table.add(nameLabel);
                table.add(nameText).width(100);
                table.row();
                table.add(addressLabel);
                table.add(addressText).width(100);
                table.debug();      // turn on all debug lines (table, cell, and widget)
                //table.debugTable(); // OR turn on only table lines



        }

        protected void setToken(CharacterToken token){
                localPlayerToken = token;
        }



        @Override
        public void update(float delta) {

                label.setText("Knight : " + localPlayerToken.getHealth());

                if(renderingStats != null){
                        renderingStats.setText("FPS : " + Gdx.graphics.getFramesPerSecond());
                }

        }

        @Override
        public void render(float delta) {

        }

        @Override
        public boolean isInitialized() {
                return initialized;
        }

        @Override
        public void dispose() {
                initialized = false;
                if (skin != null)
                        skin.dispose();

        }

        private final Vector3 tempWorldCoords = new Vector3();
        private final Pair tempMapCoords = new Pair();

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Ray ray = world.cam.getPickRay(screenX, screenY);
                if(arrowKeysMove){
                        Token targetToken = world.getToken(ray, localPlayerToken);
                        if(targetToken != null){
                                localPlayerToken.setMoveTokenTarget(targetToken);
                                world.selectionMark.mark(targetToken.getLocation());
                                return true;
                        }
                }else{
                        final float distance = -ray.origin.y / ray.direction.y;
                        tempWorldCoords.set(ray.direction).scl(distance).add(ray.origin);
                        world.getMapCoords(tempWorldCoords, tempMapCoords);
                        if(localPlayerToken.getFloorMap().getTile(tempMapCoords) != null){
                                localPlayerToken.setMoveTarget(tempMapCoords);
                                world.selectionMark.mark(tempMapCoords);
                                return true;
                        }
                }
                return false;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
                Ray ray = world.cam.getPickRay(screenX, screenY);
                if(arrowKeysMove){
                        Token targetToken = world.getToken(ray, localPlayerToken);
                        if(targetToken != null){
                                localPlayerToken.setMoveTokenTarget(targetToken);
                                world.selectionMark.mark(targetToken.getLocation());
                                return true;
                        }
                }else{
                        final float distance = -ray.origin.y / ray.direction.y;
                        tempWorldCoords.set(ray.direction).scl(distance).add(ray.origin);
                        world.getMapCoords(tempWorldCoords, tempMapCoords);
                        if(localPlayerToken.getFloorMap().getTile(tempMapCoords) != null){
                                localPlayerToken.setMoveTarget(tempMapCoords);
                                world.selectionMark.mark(tempMapCoords);
                                return true;
                        }
                }
                return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                return false;
        }

        @Override
        public boolean keyDown(int keycode) {
                if(keycode == Input.Keys.ESCAPE){
                        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)){
                                world.dungeonApp.exitApp();
                                return true;
                        }
                }
                Direction dir = Direction.getDirection(keycode);
                if(dir != null){
                        localPlayerToken.setMoveDir(dir);
                        return true;
                }
                return false;
        }

        @Override
        public boolean keyUp(int keycode) {
                if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
                        world.dungeonApp.setWorldPaused(true);
                        return true;
                }

                Direction dir = Direction.getDirection(keycode);
                if(dir == localPlayerToken.getMoveDir()){
                        localPlayerToken.setMoveDir(null);
                        return true;
                }
                return false;
        }

        @Override
        public boolean keyTyped(char character) {
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

        /**
         * handles events triggered by the touchPad and converts them in to moveDirections
         * for the local player token
         * @param event
         * @return
         */
        @Override
        public boolean handle(Event event) {
                if(!(event instanceof ChangeListener.ChangeEvent)){
                        return false;
                }

                if (event.getListenerActor() == touchPad) {
                        Direction newDir = calcTouchPadDirection();
                        if(newDir != currentTouchPadDirection){
                                currentTouchPadDirection = newDir;
                                localPlayerToken.setMoveDir(newDir);
                        }
                        return true;
                }else if(event.getListenerActor() == avatarButton){
                        //Gdx.app.log("handle",event+"");
                        world.stage.addActor(avatarWindow);
                        return true;
                }
                return false;
        }

        private Direction calcTouchPadDirection(){
                if(!touchPad.isTouched()){
                        return null;
                }else
                if(MoreMath.abs(touchPad.getKnobPercentX()  ) > MoreMath.abs(touchPad.getKnobPercentY())){
                        if(touchPad.getKnobPercentX() > 0){
                                return Direction.East;
                        }else{
                                return Direction.West;
                        }
                }else{
                        if(touchPad.getKnobPercentY() > 0){
                                return Direction.North;
                        }else{
                                return Direction.South;
                        }
                }
        }


}
