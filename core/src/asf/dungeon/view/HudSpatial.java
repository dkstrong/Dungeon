package asf.dungeon.view;

import asf.dungeon.model.CharacterToken;
import asf.dungeon.model.Direction;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Token;
import asf.dungeon.model.Item;
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
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;

/**
 * Created by Danny on 11/1/2014.
 */
public class HudSpatial implements Spatial, EventListener, InputProcessor, CharacterToken.Listener {
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
        private Window inventoryWindow;
        private final Array<Button> inventoryButtons = new Array<Button>(true, 16, Button.class);
        private Window itemWindow;
        private Label itemWindowLabel;
        private Button itemWindowUseButton, itemWindowDiscardButton, itemWindowBackButton;


        @Override
        public void preload(DungeonWorld world) {
                this.world = world;
                skin = new Skin(Gdx.files.internal("Skins/DefaultSkin/uiskin.json"));
        }

        public void resize(int width, int height) {
                if (avatarButton == null)
                        return;

                float margin = 15;

                float buttonSize = 100;
                float touchPadSize = 150;

                avatarButton.setBounds(margin, Gdx.graphics.getHeight() - buttonSize - margin, buttonSize, buttonSize);
                avatarLabel.setBounds(margin + buttonSize, Gdx.graphics.getHeight() - buttonSize - margin, buttonSize, buttonSize);
                if (renderingStats != null)
                        renderingStats.setBounds(Gdx.graphics.getWidth() - buttonSize - margin, Gdx.graphics.getHeight() - buttonSize - margin, buttonSize, buttonSize);

                label.setBounds(Gdx.graphics.getWidth() * .25f, margin, Gdx.graphics.getWidth() * .5f, buttonSize * .25f);

                quickSlotButton.setBounds(Gdx.graphics.getWidth() - buttonSize - margin, margin, buttonSize, buttonSize);
                inventoryButton.setBounds(Gdx.graphics.getWidth() - buttonSize - margin - buttonSize - margin, margin, buttonSize, buttonSize);

                if (touchPad != null)
                        touchPad.setBounds(margin, margin, touchPadSize, touchPadSize);

                float windowHeight = Gdx.graphics.getHeight() - margin - margin;
                float windowWidth = windowHeight * .75f;
                float windowButtonSize = windowWidth * .25f;
                float windowCloseButtonSize = windowButtonSize * .5f;
                inventoryWindow.setBounds(
                        (Gdx.graphics.getWidth() - windowWidth) * .5f,
                        ((Gdx.graphics.getHeight() - windowHeight) * .5f),
                        windowWidth,
                        windowHeight);

                for (Cell cell : inventoryWindow.getCells()) {
                        if (String.valueOf(cell.getActor().getUserObject()).equals("Close Inventory")) {
                                cell.prefSize(windowCloseButtonSize, windowCloseButtonSize * .5f).pad(windowCloseButtonSize * .35f, 0, windowCloseButtonSize * .15f, 0);
                        } else {
                                cell.prefSize(windowButtonSize, windowButtonSize);
                        }

                }

                float itemWindowHeight = Gdx.graphics.getHeight() * .5f;
                float itemWindowWidth = itemWindowHeight * 1.75f;
                float itemWindowButtonSize = itemWindowWidth * .25f;
                float itemWindowCloseButtonSize = itemWindowButtonSize * .5f;

                itemWindow.setBounds(
                        (Gdx.graphics.getWidth() - itemWindowWidth) * .5f,
                        (Gdx.graphics.getHeight() - itemWindowHeight) * .5f,
                        itemWindowWidth,
                        itemWindowHeight);


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
                setButtonContents(quickSlotButton, null);

                inventoryButton = new Button(skin);
                world.stage.addActor(inventoryButton);
                inventoryButton.add(new Label("Inventory", skin));
                inventoryButton.addCaptureListener(this);

                if (arrowKeysMove) {
                        touchPad = new Touchpad(8, skin);

                        world.stage.addActor(touchPad);
                        touchPad.addCaptureListener(this);
                }


                inventoryWindow = new Window("Inventory", skin);
                inventoryWindow.setMovable(false);
                inventoryWindow.setModal(true);
                inventoryWindow.addCaptureListener(this);
                inventoryWindow.removeActor(inventoryWindow.getButtonTable());
                //world.stage.addActor(inventoryWindow);
                float windowHeight = Gdx.graphics.getHeight() - 15 - 15;
                float windowWidth = windowHeight * .75f;
                float windowButtonSize = windowWidth * .25f;
                float windowCloseButtonSize = windowButtonSize * .5f;
                inventoryWindow.setBounds(
                        (Gdx.graphics.getWidth() - windowWidth) * .5f,
                        ((Gdx.graphics.getHeight() - windowHeight) * .5f),
                        windowWidth,
                        windowHeight);
                //inventoryWindow.debug();
                {

                        Table inventoryTable = inventoryWindow;

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
                                        invButton.addCaptureListener(this);
                                        setButtonContents(invButton, null);
                                        inventoryTable.add(invButton);
                                        inventoryButtons.add(invButton);
                                }
                        }


                        inventoryTable.row();
                        Button closeButton = new Button(skin);
                        closeButton.add("Close Inventory");
                        closeButton.setUserObject("Close Inventory");
                        closeButton.addCaptureListener(new ChangeListener() {
                                @Override
                                public void changed(ChangeEvent event, Actor actor) {
                                        setInventoryWindowVisible(false);
                                }
                        });
                        inventoryTable.add(closeButton).colspan(4).prefSize(windowCloseButtonSize, windowCloseButtonSize * .5f).pad(windowCloseButtonSize * .35f, 0, windowCloseButtonSize * .15f, 0);


                }


                itemWindow = new Window("Item", skin);
                itemWindow.setModal(true);
                itemWindow.setMovable(false);
                itemWindow.addCaptureListener(this);
                itemWindow.removeActor(itemWindow.getButtonTable());
                //world.stage.addActor(itemWindow);
                itemWindow.debugAll();
                {

                        itemWindow.defaults().space(6);
                        itemWindow.row().space(20);
                        itemWindowLabel = new Label("Some information about this item.", skin);
                        itemWindow.add(itemWindowLabel).colspan(3);


                        itemWindow.row().spaceTop(60);

                        itemWindowUseButton = new Button(skin);
                        itemWindowUseButton.add("Use");
                        itemWindowUseButton.addCaptureListener(this);
                        itemWindow.add(itemWindowUseButton);

                        itemWindowDiscardButton = new Button(skin);
                        itemWindowDiscardButton.add("Discard");
                        itemWindowDiscardButton.addCaptureListener(this);
                        itemWindow.add(itemWindowDiscardButton).align(Align.bottom);

                        itemWindowBackButton = new Button(skin);
                        itemWindowBackButton.add("Back");
                        itemWindowBackButton.addCaptureListener(this);
                        itemWindow.add(itemWindowBackButton).align(Align.bottom);


                }


                resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        }

        protected void setToken(CharacterToken token) {
                localPlayerToken = token;
        }

        private void setButtonContents(Button button, Item item) {
                if (item == null) {
                        if (button.getUserObject() != null) {
                                button.clearChildren();
                                if (button == quickSlotButton) {
                                        button.add("<Empty>");
                                } else {
                                        button.add("");
                                }

                        }
                        button.setDisabled(true);
                        button.setUserObject(null);
                } else {
                        if (button.getUserObject() == null) {
                                button.clearChildren();
                                Label nameLabel = new Label(item.getName(), skin);
                                nameLabel.setUserObject("Name");
                                button.add(nameLabel);
                        } else {
                                SnapshotArray<Actor> children = button.getChildren();
                                for (int i = 0; i < children.size; i++) {
                                        Actor actor = children.get(i);
                                        if (String.valueOf(actor.getUserObject()).equals("Name")) {
                                                Label label = (Label) actor;
                                                label.setText(item.getName());
                                        }

                                }
                        }
                        button.setDisabled(false);
                        button.setUserObject(item);


                }

        }

        private void setItemWindowContents(Item item) {
                itemWindow.setTitle(item.getName());
                itemWindowLabel.setText("This item is a " + item.getModelId().name());

                itemWindow.setUserObject(item);

        }

        @Override
        public void update(float delta) {

                label.setText("Knight : " + localPlayerToken.getHealth());

                if (renderingStats != null) {
                        renderingStats.setText("FPS : " + Gdx.graphics.getFramesPerSecond());
                }


        }

        private Item getItem(Button button) {
                if (button.getUserObject() instanceof Item)
                        return (Item) button.getUserObject();
                return null;
        }

        @Override
        public void onInventoryAdd(Item item) {
                if(getItem(quickSlotButton) == null)
                        setButtonContents(quickSlotButton, item);

                if (inventoryWindow.getParent() != null) {
                        refreshInventoryWindow();
                }


        }

        @Override
        public void onInventoryRemove(Item item) {
                if (inventoryWindow.getParent() != null) {
                        refreshInventoryWindow();
                }
        }

        @Override
        public void onConsumeItem(Item item) {

        }

        private void refreshInventoryWindow() {
                Item quickItem = getItem(quickSlotButton);
                int buttonI=0;
                for (int i = 0; i < localPlayerToken.getInventory().size; i++) {
                        Item item = localPlayerToken.getInventory().get(i);
                        if(quickItem == item){
                                continue;
                        }
                        setButtonContents(inventoryButtons.get(buttonI), item);
                        buttonI++;
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

        protected void setInventoryWindowVisible(boolean visible) {
                if (visible) {
                        if (inventoryWindow.getParent() == null)
                                world.stage.addActor(inventoryWindow);
                        refreshInventoryWindow();
                } else {
                        inventoryWindow.remove();
                        itemWindow.remove();
                        inventoryWindow.setModal(true);
                }
        }

        private void setItemDialogVisible(boolean visible) {
                if (visible) {
                        if (inventoryWindow.getParent() == null)
                                world.stage.addActor(inventoryWindow);
                        inventoryWindow.setModal(false);
                        if (itemWindow.getParent() == null)
                                world.stage.addActor(itemWindow);

                } else {
                        itemWindow.remove();
                        inventoryWindow.setModal(true);
                }
        }

        private final Vector3 tempWorldCoords = new Vector3();
        private final Pair tempMapCoords = new Pair();

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Ray ray = world.cam.getPickRay(screenX, screenY);
                Token targetToken = world.getToken(ray, localPlayerToken);
                if (targetToken != null) {
                        localPlayerToken.setContinousMoveToken(targetToken);
                        if (localPlayerToken.getContinuousMoveToken() != null) {
                                world.selectionMark.mark(targetToken.getLocation());
                                return true;
                        }

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
                        localPlayerToken.setContinousMoveToken(targetToken);
                        if (localPlayerToken.getContinuousMoveToken() != null) {
                                world.selectionMark.mark(targetToken.getLocation());
                                return true;
                        }
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
                if (event.getTarget() == itemWindow) {
                        if (event instanceof InputEvent) {
                                InputEvent inputEvent = (InputEvent) event;
                                if (inputEvent.getType() == InputEvent.Type.touchDown) {
                                        float clickX = inputEvent.getStageX();
                                        float clickY = inputEvent.getStageY();
                                        if (clickX < itemWindow.getX() || clickY < itemWindow.getY() || clickX > itemWindow.getX() + itemWindow.getWidth() || clickY > itemWindow.getY() + itemWindow.getHeight()) {
                                                setItemDialogVisible(false);
                                                return true;
                                        }
                                }
                        }
                        return false;
                }

                if (event.getTarget() == inventoryWindow) {
                        if (event instanceof InputEvent) {
                                InputEvent inputEvent = (InputEvent) event;
                                if (inputEvent.getType() == InputEvent.Type.touchDown) {
                                        float clickX = inputEvent.getStageX();
                                        float clickY = inputEvent.getStageY();
                                        if (clickX < inventoryWindow.getX() || clickY < inventoryWindow.getY() || clickX > inventoryWindow.getX() + inventoryWindow.getWidth() || clickY > inventoryWindow.getY() + inventoryWindow.getHeight()) {
                                                //Gdx.app.log("handle", "clickX " +clickX+ " type:  "+inputEvent.getType());
                                                setInventoryWindowVisible(false);
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
                        setInventoryWindowVisible(false);
                        Direction newDir = calcTouchPadDirection();
                        if (newDir != currentTouchPadDirection) {
                                currentTouchPadDirection = newDir;
                                localPlayerToken.setMoveDir(newDir);
                        }
                        return true;
                } else if (event.getListenerActor() == avatarButton) {
                        return true;
                } else if (event.getListenerActor() == quickSlotButton) {

                } else if (event.getListenerActor() == inventoryButton) {
                        if (inventoryWindow.getParent() == null) {
                                setInventoryWindowVisible(true);
                        }
                } else if (event.getListenerActor() == itemWindowUseButton) {
                        Item item = (Item) itemWindow.getUserObject();
                        boolean valid = localPlayerToken.useItem(item);
                        if (valid)
                                setInventoryWindowVisible(false);
                } else if (event.getListenerActor() == itemWindowDiscardButton) {
                        Item item = (Item) itemWindow.getUserObject();
                        boolean valid = localPlayerToken.discardItem(item);
                        if (valid)
                                setItemDialogVisible(false);
                } else if (event.getListenerActor() == itemWindowBackButton) {
                        setItemDialogVisible(false);
                } else if (event.getListenerActor() instanceof Button) {
                        Object userObject = event.getListenerActor().getUserObject();
                        if (userObject instanceof Item) {
                                setItemWindowContents((Item) userObject);
                                setItemDialogVisible(true);
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
