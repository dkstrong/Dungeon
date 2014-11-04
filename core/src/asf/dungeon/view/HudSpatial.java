package asf.dungeon.view;

import asf.dungeon.board.CharacterToken;
import asf.dungeon.board.Direction;
import asf.dungeon.board.Token;
import asf.dungeon.board.Pair;
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
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/**
 * Created by Danny on 11/1/2014.
 */
public class HudSpatial implements Spatial, EventListener, InputProcessor {
        private boolean initialized = false;
        private DungeonWorld world;
        protected CharacterToken localPlayerToken;

        // TODO: these settings should be stored on an app level object so it can be easily changed in the settings
        private boolean arrowKeysMove = false;
        private boolean showRenderingStasLabel = true;

        private Skin skin;
        private Label label;
        private Button inventoryButton, quickSlotButton, avatarButton;
        private Label avatarLabel;
        private Label renderingStats;
        private Touchpad touchPad;
        private Direction currentTouchPadDirection = null;
        private Window avatarWindow;


        @Override
        public void preload(DungeonWorld world) {
                this.world = world;
                skin = new Skin(Gdx.files.internal("Skins/DefaultSkin/uiskin.json"));
        }

        public void resize(int width, int height) {
                if(avatarButton == null)
                        return;

                float margin = 15;

                float buttonSize = 100;
                float touchPadSize = 150;

                avatarButton.setBounds(margin, Gdx.graphics.getHeight() - buttonSize - margin, buttonSize, buttonSize);
                avatarLabel.setBounds(margin + buttonSize, Gdx.graphics.getHeight() - buttonSize - margin, buttonSize, buttonSize);
                if(renderingStats != null)
                        renderingStats.setBounds(Gdx.graphics.getWidth() - buttonSize - margin, Gdx.graphics.getHeight() - buttonSize - margin, buttonSize, buttonSize);

                label.setBounds(Gdx.graphics.getWidth() * .25f, margin, Gdx.graphics.getWidth() * .5f, buttonSize * .25f);

                quickSlotButton.setBounds(Gdx.graphics.getWidth()-buttonSize-margin, margin, buttonSize, buttonSize);
                inventoryButton.setBounds(Gdx.graphics.getWidth()-buttonSize-margin-buttonSize-margin, margin, buttonSize, buttonSize);

                if(touchPad != null)
                        touchPad.setBounds(margin, margin, touchPadSize, touchPadSize);

                float windowHeight = Gdx.graphics.getHeight() - margin-margin;
                float windowWidth = windowHeight*.75f;
                float windowButtonSize = windowWidth * .25f;
                float windowCloseButtonSize = windowButtonSize*.5f;
                avatarWindow.setBounds(
                        (Gdx.graphics.getWidth() - windowWidth) * .5f,
                        ((Gdx.graphics.getHeight()-windowHeight) *.5f),
                        windowWidth,
                        windowHeight);

                for (Cell cell : avatarWindow.getCells()) {
                        if(String.valueOf(cell.getActor().getUserObject()).equals("Close Inventory")){
                                cell.prefSize(windowCloseButtonSize,windowCloseButtonSize*.5f).pad(windowCloseButtonSize * .35f, 0, windowCloseButtonSize * .15f, 0);
                        }else{
                                cell.prefSize(windowButtonSize,windowButtonSize);
                        }

                }


        }
        @Override
        public void init(AssetManager assetManager) {
                initialized = true;

                avatarButton = new Button(skin);
                world.stage.addActor(avatarButton);

                avatarButton.add(new Label("Avatar", skin));
                avatarButton.addCaptureListener(this);

                avatarLabel = new Label("Knight\nLevel 1\nXP 25/100", skin);
                world.stage.addActor(avatarLabel);

                avatarLabel.setAlignment(Align.topLeft);

                if (showRenderingStasLabel) {
                        renderingStats = new Label("", skin);
                        world.stage.addActor(renderingStats);
                        renderingStats.setAlignment(Align.topRight);
                }

                label = new Label(" ", skin);
                label.setAlignment(Align.center);
                world.stage.addActor(label);

                quickSlotButton = new Button(skin);
                world.stage.addActor(quickSlotButton);
                quickSlotButton.add(new Label("<Empty>", skin));
                quickSlotButton.addCaptureListener(this);

                inventoryButton = new Button(skin);
                world.stage.addActor(inventoryButton);
                inventoryButton.add(new Label("Inventory", skin));
                inventoryButton.addCaptureListener(this);



                if (arrowKeysMove) {
                        touchPad = new Touchpad(8, skin);

                        world.stage.addActor(touchPad);
                        touchPad.addCaptureListener(this);
                }

                avatarWindow = new Window("Inventory", skin);
                avatarWindow.setMovable(false);
                avatarWindow.setModal(true);
                avatarWindow.addCaptureListener(this);
                //world.stage.addActor(avatarWindow);
                float windowHeight = Gdx.graphics.getHeight() - 15-15;
                float windowWidth = windowHeight*.75f;
                float windowButtonSize = windowWidth * .25f;
                float windowCloseButtonSize = windowButtonSize*.5f;
                avatarWindow.setBounds(
                        (Gdx.graphics.getWidth() - windowWidth) * .5f,
                        ((Gdx.graphics.getHeight()-windowHeight) *.5f),
                        windowWidth,
                        windowHeight);

                //avatarWindow.debug();
                {

                        Table inventoryTable = avatarWindow;

                        inventoryTable.defaults().prefSize(windowButtonSize, windowButtonSize);

                        inventoryTable.row().pad(windowButtonSize * .1f, 0, windowButtonSize * .15f, 0);
                        Button equippedWeaponButton = new Button(skin);
                        equippedWeaponButton.add("Weapon");
                        inventoryTable.add(equippedWeaponButton);

                        Button armorButton = new Button(skin);
                        armorButton.add("Armor");
                        inventoryTable.add(armorButton);

                        Button ringButton = new Button(skin);
                        ringButton.add("Ring");
                        inventoryTable.add(ringButton);

                        Button offhandButton = new Button(skin);
                        offhandButton.add("Quick");
                        inventoryTable.add(offhandButton);


                        for (int y = 0; y < 4; y++) {
                                inventoryTable.row();
                                for (int x = 0; x < 4; x++) {
                                        Button invButton = new Button(skin);
                                        invButton.setDisabled(true);
                                        invButton.add("");

                                        inventoryTable.add(invButton);
                                }
                        }


                        inventoryTable.row();
                        Button closeButton = new Button(skin);
                        closeButton.add("Close Inventory");
                        closeButton.setUserObject("Close Inventory");
                        closeButton.addCaptureListener(new ChangeListener() {
                                @Override
                                public void changed(ChangeEvent event, Actor actor) {
                                        forceInventoryClosed();
                                }
                        });
                        inventoryTable.add(closeButton).colspan(4).prefSize(windowCloseButtonSize,windowCloseButtonSize*.5f).pad(windowCloseButtonSize * .35f, 0, windowCloseButtonSize * .15f, 0);


                }


                resize(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());

        }

        protected void setToken(CharacterToken token) {
                localPlayerToken = token;
        }


        @Override
        public void update(float delta) {

                label.setText("Knight : " + localPlayerToken.getHealth());

                if (renderingStats != null) {
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

        protected void forceInventoryClosed() {
                avatarWindow.remove();
        }

        private final Vector3 tempWorldCoords = new Vector3();
        private final Pair tempMapCoords = new Pair();

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Ray ray = world.cam.getPickRay(screenX, screenY);
                Token targetToken = world.getToken(ray, localPlayerToken);
                if (targetToken != null) {
                        localPlayerToken.setMoveTokenTarget(targetToken);
                        world.selectionMark.mark(targetToken.getLocation());
                        return true;
                }

                if (!arrowKeysMove) {
                        final float distance = -ray.origin.y / ray.direction.y;
                        tempWorldCoords.set(ray.direction).scl(distance).add(ray.origin);
                        world.getMapCoords(tempWorldCoords, tempMapCoords);
                        if (localPlayerToken.getFloorMap().getTile(tempMapCoords) != null) {
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
                Token targetToken = world.getToken(ray, localPlayerToken);
                if (targetToken != null) {
                        localPlayerToken.setMoveTokenTarget(targetToken);
                        world.selectionMark.mark(targetToken.getLocation());
                        return true;
                }

                if (!arrowKeysMove) {
                        final float distance = -ray.origin.y / ray.direction.y;
                        tempWorldCoords.set(ray.direction).scl(distance).add(ray.origin);
                        world.getMapCoords(tempWorldCoords, tempMapCoords);
                        if (localPlayerToken.getFloorMap().getTile(tempMapCoords) != null) {
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
                if (keycode == Input.Keys.ESCAPE) {
                        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                                world.dungeonApp.exitApp();
                                return true;
                        }
                }
                Direction dir = Direction.getDirection(keycode);
                if (dir != null) {
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
                if (dir == localPlayerToken.getMoveDir()) {
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
         *
         * @param event
         * @return
         */
        @Override
        public boolean handle(Event event) {
                if (event.getTarget() == avatarWindow) {
                        if (event instanceof InputEvent) {
                                InputEvent inputEvent = (InputEvent) event;
                                if(inputEvent.getType()== InputEvent.Type.touchDown){
                                        float clickX = inputEvent.getStageX();
                                        float clickY = inputEvent.getStageY();
                                        if(clickX < avatarWindow.getX() || clickY < avatarWindow.getY() || clickX > avatarWindow.getX()+avatarWindow.getWidth() || clickY > avatarWindow.getY()+avatarWindow.getHeight()){
                                                //Gdx.app.log("handle", "clickX " +clickX+ " type:  "+inputEvent.getType());
                                                forceInventoryClosed();
                                                return true;
                                        }
                                }

                        }
                        return false;
                }
                if (!(event instanceof ChangeListener.ChangeEvent)) {
                        return false;
                }

                if (event.getListenerActor() == touchPad) {
                        forceInventoryClosed();
                        Direction newDir = calcTouchPadDirection();
                        if (newDir != currentTouchPadDirection) {
                                currentTouchPadDirection = newDir;
                                localPlayerToken.setMoveDir(newDir);
                        }
                        return true;
                } else if (event.getListenerActor() == avatarButton) {
                        return true;
                } else if (event.getListenerActor() == quickSlotButton) {

                } else if(event.getListenerActor() == inventoryButton){
                        if (avatarWindow.getParent() == null) {
                                world.stage.addActor(avatarWindow);
                        }
                }
                return false;
        }

        private Direction calcTouchPadDirection() {
                if (!touchPad.isTouched()) {
                        return null;
                } else if (MoreMath.abs(touchPad.getKnobPercentX()) > MoreMath.abs(touchPad.getKnobPercentY())) {
                        if (touchPad.getKnobPercentX() > 0) {
                                return Direction.East;
                        } else {
                                return Direction.West;
                        }
                } else {
                        if (touchPad.getKnobPercentY() > 0) {
                                return Direction.North;
                        } else {
                                return Direction.South;
                        }
                }
        }


}
