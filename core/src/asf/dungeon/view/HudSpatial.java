package asf.dungeon.view;

import asf.dungeon.model.CharacterToken;
import asf.dungeon.model.DamageableToken;
import asf.dungeon.model.Direction;
import asf.dungeon.model.Item;
import asf.dungeon.model.Pair;
import asf.dungeon.model.StatusEffect;
import asf.dungeon.model.Token;
import asf.dungeon.utility.MoreMath;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
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
        private Label characterActionInfoLabel, targetInfoLabel;
        private float centerLabelCountdown = Float.NaN;
        private Button inventoryButton, quickSlotButton, avatarButton;
        private ProgressBar healthProgressBar;
        private Label avatarLabel;
        private HorizontalGroup avatarStatusEffectsGroup;
        private Image[] statusEffectImage;
        private Window avatarWindow;
        private Label renderingStats;
        private Touchpad touchPad;
        private Direction currentTouchPadDirection = null;
        private Window inventoryWindow;
        private final Array<Button> inventoryButtons = new Array<Button>(true, 16, Button.class);
        private Window itemWindow;
        private Label itemWindowLabel;
        private Button itemWindowUseButton, itemWindowDiscardButton, itemWindowBackButton;
        private final Array<DamageLabel> damageInfoLabels = new Array<DamageLabel>(false, 8, DamageLabel.class);

        private final Vector3 tempVec = new Vector3();

        @Override
        public void preload(DungeonWorld world) {
                this.world = world;
                skin = new Skin(Gdx.files.internal("Skins/BasicSkin/uiskin.json"));
        }


        @Override
        public void init(AssetManager assetManager) {
                initialized = true;

                avatarButton = new Button(skin);
                world.stage.addActor(avatarButton);

                avatarButton.add(new Label("Avatar", skin));
                avatarButton.addCaptureListener(this);

                healthProgressBar = new ProgressBar(0, localPlayerToken.getMaxHealth(), 1, false, skin, "default");
                healthProgressBar.setValue(localPlayerToken.getHealth());
                healthProgressBar.setAnimateInterpolation(Interpolation.linear);
                healthProgressBar.setAnimateDuration(1f);
                world.stage.addActor(healthProgressBar);

                avatarLabel = new Label("Knight\nLevel 1\nXP 25/100", skin);
                world.stage.addActor(avatarLabel);
                avatarLabel.setAlignment(Align.topLeft);

                avatarStatusEffectsGroup = new HorizontalGroup();
                world.stage.addActor(avatarStatusEffectsGroup);
                avatarStatusEffectsGroup.align(Align.bottomLeft);

                StatusEffect[] statusEffectValues = StatusEffect.values();
                statusEffectImage = new Image[statusEffectValues.length];
                for (int i = 0; i < statusEffectValues.length; i++) {
                        statusEffectImage[i] = new Image(new Texture(Gdx.files.internal("Interface/Hud/health.png")));
                        //statusEffectImage[i].setScaling(Scaling.fit);
                        statusEffectImage[i].setScaling(Scaling.fillY);
                        statusEffectImage[i].setAlign(Align.bottomLeft);
                        //avatarStatusEffectsGroup.addActor(statusEffectImage[i]);
                };


                if (showRenderingStasLabel) {
                        renderingStats = new Label("", skin);
                        world.stage.addActor(renderingStats);
                        renderingStats.setAlignment(Align.topRight);
                }

                characterActionInfoLabel = new Label(null, skin);
                characterActionInfoLabel.setAlignment(Align.center);


                targetInfoLabel = new Label(null, skin);
                targetInfoLabel.setAlignment(Align.center);
                world.stage.addActor(targetInfoLabel);

                quickSlotButton = new Button(skin);
                world.stage.addActor(quickSlotButton);
                quickSlotButton.add(new Label("<Empty>", skin));
                quickSlotButton.addCaptureListener(internalGestureListener);
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

                avatarWindow = new Window("Avatar", skin);
                avatarWindow.setMovable(false);
                avatarWindow.setModal(true);
                avatarWindow.addCaptureListener(this);
                avatarWindow.removeActor(avatarWindow.getButtonTable());

                //avatarWindow.debugAll();
                {
                        avatarWindow.row();
                        Label nameLabel = new Label("Name", skin);
                        nameLabel.setUserObject("nameLabel");
                        avatarWindow.add(nameLabel).colspan(4).expand();

                        avatarWindow.row();
                        Label statsLabel = new Label("Stats", skin);
                        statsLabel.setUserObject("statsLabel");
                        avatarWindow.add(statsLabel).colspan(4).expand();


                        avatarWindow.row();
                        Button closeButton = new Button(skin);
                        closeButton.add("Close");
                        closeButton.setUserObject("Close");
                        closeButton.addCaptureListener(new ChangeListener() {
                                @Override
                                public void changed(ChangeEvent event, Actor actor) {
                                        setAvatarWindowVisible(false);
                                }
                        });
                        avatarWindow.add(closeButton).colspan(4);
                }

                //world.stage.addActor(avatarWindow);
                //this.refreshAvatarWindowElements();

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
                //itemWindow.debugAll();
                {

                        itemWindow.defaults().space(6);
                        itemWindow.row().space(20);
                        itemWindowLabel = new Label("Some information about this item.", skin);
                        itemWindow.add(itemWindowLabel).colspan(3);


                        itemWindow.row().spaceTop(60);

                        itemWindowUseButton = new Button(skin);
                        itemWindowUseButton.add("Use");
                        itemWindowUseButton.addCaptureListener(this);
                        itemWindow.add(itemWindowUseButton).minSize(windowButtonSize * 1.25f, windowButtonSize * .75f);

                        itemWindowDiscardButton = new Button(skin);
                        itemWindowDiscardButton.add("Discard");
                        itemWindowDiscardButton.addCaptureListener(this);
                        itemWindow.add(itemWindowDiscardButton).minSize(windowButtonSize * 1.25f, windowButtonSize * .75f);

                        itemWindowBackButton = new Button(skin);
                        itemWindowBackButton.add("Back");
                        itemWindowBackButton.addCaptureListener(this);
                        itemWindow.add(itemWindowBackButton).minSize(windowButtonSize * 1.25f, windowButtonSize * .75f);


                }

                resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        }

        protected void resize(int width, int height) {
                if (avatarButton == null)
                        return;

                float margin = 15;

                float buttonSize = 100;
                float touchPadSize = 150;

                avatarButton.setBounds(margin, Gdx.graphics.getHeight() - buttonSize - margin, buttonSize, buttonSize);

                healthProgressBar.setBounds(margin + buttonSize, Gdx.graphics.getHeight() - margin - buttonSize * .333f,
                        buttonSize * 1.5f,
                        buttonSize * .333f);

                avatarLabel.setBounds(
                        margin + buttonSize, Gdx.graphics.getHeight() - buttonSize - margin - buttonSize * .333f,
                        buttonSize, buttonSize);

                avatarStatusEffectsGroup.setBounds(
                        margin + buttonSize,
                        Gdx.graphics.getHeight() - buttonSize - margin,
                        buttonSize * .333f,
                        buttonSize * .333f);


                if (renderingStats != null)
                        renderingStats.setBounds(Gdx.graphics.getWidth() - buttonSize - margin, Gdx.graphics.getHeight() - buttonSize - margin, buttonSize, buttonSize);

                characterActionInfoLabel.setBounds(
                        Gdx.graphics.getWidth() * .25f,
                        (Gdx.graphics.getHeight() - (buttonSize * .25f)) * .5f,
                        Gdx.graphics.getWidth() * .5f,
                        buttonSize * .25f);

                targetInfoLabel.setBounds(
                        Gdx.graphics.getWidth() * .25f,
                        Gdx.graphics.getHeight() - margin - buttonSize * .5f,
                        Gdx.graphics.getWidth() * .5f,
                        buttonSize * .25f);

                quickSlotButton.setBounds(Gdx.graphics.getWidth() - buttonSize - margin, margin, buttonSize, buttonSize);
                inventoryButton.setBounds(margin, margin, buttonSize, buttonSize);

                if (touchPad != null)
                        touchPad.setBounds(margin, margin, touchPadSize, touchPadSize);

                float avatarWindowHeight = Gdx.graphics.getHeight() - margin - margin;
                float avatarWindowWidth = avatarWindowHeight * .75f;
                float avatarWindowButtonSize = avatarWindowWidth * .25f;
                float avatarWindowCloseButtonSize = avatarWindowButtonSize * .5f;
                avatarWindow.setBounds(
                        (Gdx.graphics.getWidth() - avatarWindowWidth) * .5f,
                        ((Gdx.graphics.getHeight() - avatarWindowHeight) * .5f),
                        avatarWindowWidth,
                        avatarWindowHeight);

                for (Cell cell : avatarWindow.getCells()) {
                        if (String.valueOf(cell.getActor().getUserObject()).equals("Close")) {
                                cell.prefSize(avatarWindowCloseButtonSize, avatarWindowCloseButtonSize * .5f).pad(avatarWindowCloseButtonSize * .35f, 0, avatarWindowCloseButtonSize * .15f, 0);
                        } else {
                                cell.prefSize(avatarWindowButtonSize, avatarWindowButtonSize);
                        }

                }

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



        protected void setToken(CharacterToken token) {
                localPlayerToken = token;
        }


        @Override
        public void update(float delta) {


                healthProgressBar.setRange(0, localPlayerToken.getMaxHealth());
                healthProgressBar.setValue(localPlayerToken.getHealth());
                healthProgressBar.act(delta);

                String s = String.format("%s / %s\n%s / %s",localPlayerToken.getHealth(),localPlayerToken.getMaxHealth(),localPlayerToken.getAttackCoolDown(),localPlayerToken.getAttackCooldownDuration());
                avatarLabel.setText(s);



                Token targetToken = localPlayerToken.getContinuousMoveToken();
                if (targetToken == null)
                        targetInfoLabel.setText(null);
                else {
                        targetInfoLabel.setText("Target: " + targetToken.getName());
                }


                if (renderingStats != null) {
                        renderingStats.setText("FPS : " + Gdx.graphics.getFramesPerSecond());
                }

                centerLabelCountdown -= delta;
                if (centerLabelCountdown < 0) {
                        characterActionInfoLabel.remove();
                        centerLabelCountdown = Float.NaN;
                }

                for (DamageLabel label : damageInfoLabels) {
                        if(label.tokenSpatial != null){
                                label.ttl -= delta;
                                if(!label.tokenSpatial.isInitialized() || label.ttl <=0 || label.tokenSpatial.visU <=0){
                                        label.tokenSpatial = null;
                                        label.ttl = 0;
                                        label.label.remove();
                                        continue;
                                }

                                world.getScreenCoords(
                                        label.tokenSpatial.token.getLocationFloatX(),
                                        label.tokenSpatial.token.getLocationFloatY(),
                                        tempVec);


                                if(label.label.getText().toString().toLowerCase().contains("d")){
                                        tempVec.x-= 15;
                                }else{
                                        tempVec.x-= 2;
                                }

                                tempVec.y+= 80 + Interpolation.linear.apply(0,35,1-(label.ttl/1.5f));
                                label.label.setPosition(tempVec.x,tempVec.y);
                                label.label.getColor().a = label.ttl/1.5f;

                        }
                }


        }


        @Override
        public void render(float delta) {

        }

        private Item getItem(Button button) {
                if (button.getUserObject() instanceof Item)
                        return (Item) button.getUserObject();
                return null;
        }


        private void refreshAvatarWindowElements() {
                if (avatarWindow.getParent() != null) {

                        SnapshotArray<Actor> children = avatarWindow.getChildren();
                        for (Actor child : children) {
                                String uoName = String.valueOf(child.getUserObject());
                                if (uoName.equals("nameLabel")) {
                                        Label label = (Label) child;
                                        label.setText(localPlayerToken.getName());
                                } else if (uoName.equals("statsLabel")) {
                                        Label label = (Label) child;
                                        StringBuilder sb = new StringBuilder();
                                        sb.append(String.format("HP: %s / %s\n", localPlayerToken.getHealth(), localPlayerToken.getMaxHealth()));
                                        sb.append(String.format("Strength: %s \n", localPlayerToken.getStrengthRating()));
                                        sb.append(String.format("Speed: %s \n", localPlayerToken.getSpeedRating()));
                                        sb.append(String.format("Defense: %s \n", localPlayerToken.getDefenseRating()));


                                        label.setText(sb);
                                }
                        }


                }


        }

        private void refreshInventoryElements() {
                Gdx.app.log("HudSpatial", "refresh quick button");
                Item quickItem = getItem(quickSlotButton);
                setButtonContents(quickSlotButton, quickItem); // refresh the display

                if (inventoryWindow.getParent() != null) {
                        Gdx.app.log("HudSpatial", "refresh inventory window");
                        int buttonI = 0;
                        for (int i = 0; i < localPlayerToken.getInventory().size; i++) {
                                Item item = localPlayerToken.getInventory().get(i);
                                if (item.equals(quickItem))
                                        continue;
                                boolean hasItem = false;
                                for (int j = buttonI - 1; j >= 0; j--) {
                                        if (inventoryButtons.get(j).getUserObject().equals(item)) {
                                                hasItem = true;
                                                break;
                                        }
                                }
                                if (hasItem)
                                        continue;
                                setButtonContents(inventoryButtons.get(buttonI), item);
                                buttonI++;
                        }

                        for (int i = buttonI; i < inventoryButtons.size; i++) {
                                setButtonContents(inventoryButtons.get(i), null);
                        }


                }


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
                                //button.debugAll();

                                button.clearChildren();
                                button.defaults().pad(0, 0, 0, 0).space(0, 0, 0, 0);
                                button.row().expand();
                                Label reqLabel = new Label(" ", skin);
                                reqLabel.setUserObject("Req");
                                button.add(reqLabel).align(Align.topLeft).pad(5, 5, 0, 0);

                                Label valLabel = new Label(" ", skin);
                                valLabel.setUserObject("Val");
                                button.add(valLabel).align(Align.topRight).pad(5, 0, 0, 5);

                                button.row().expand();
                                Label nameLabel = new Label(item.getNameFromJournal(localPlayerToken), skin);
                                nameLabel.setUserObject("Name");
                                button.add(nameLabel).colspan(2).center();

                                button.row().expand();

                                int quantity = localPlayerToken.getQuantity(item);
                                Label quantityLabel = new Label(quantity > 1 ? "x" + quantity : "", skin);
                                quantityLabel.setUserObject("Quantity");
                                button.add(quantityLabel).align(Align.bottomRight).colspan(2).pad(0, 0, 3, 5);


                        } else {
                                SnapshotArray<Actor> children = button.getChildren();
                                for (int i = 0; i < children.size; i++) {
                                        Actor actor = children.get(i);
                                        String uoName = String.valueOf(actor.getUserObject());
                                        if (uoName.equals("Name")) {
                                                Label label = (Label) actor;
                                                label.setText(item.getNameFromJournal(localPlayerToken));
                                        } else if (uoName.equals("Quantity")) {
                                                Label label = (Label) actor;
                                                int quantity = localPlayerToken.getQuantity(item);
                                                label.setText(quantity > 1 ? "x" + quantity : "");
                                        }

                                }
                        }
                        button.setDisabled(false);
                        button.setUserObject(item);


                }

        }

        private void setItemWindowContents(Item item) {
                itemWindow.setTitle(item.getNameFromJournal(localPlayerToken));
                itemWindowLabel.setText(item.getDescriptionFromJournal(localPlayerToken));

                itemWindow.setUserObject(item);

        }

        private void setCenterLabelText(String text) {
                characterActionInfoLabel.setText(text);
                centerLabelCountdown = 5;
                world.stage.addActor(characterActionInfoLabel);
        }

        protected void closeAllWindows() {
                setAvatarWindowVisible(false);
                setInventoryWindowVisible(false);
        }

        private void setAvatarWindowVisible(boolean visible) {
                if (visible) {
                        setInventoryWindowVisible(false);
                        if (avatarWindow.getParent() == null)
                                world.stage.addActor(avatarWindow);
                        refreshAvatarWindowElements();
                } else {
                        avatarWindow.remove();
                }
        }

        private void setInventoryWindowVisible(boolean visible) {
                if (visible) {
                        setAvatarWindowVisible(false);
                        if (inventoryWindow.getParent() == null)
                                world.stage.addActor(inventoryWindow);
                        refreshInventoryElements();
                } else {
                        inventoryWindow.remove();
                        itemWindow.remove();
                        inventoryWindow.setModal(true);
                }
        }

        private void setItemDialogVisible(boolean visible) {
                if (visible) {
                        setAvatarWindowVisible(false);
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

        @Override
        public void onAttack(DamageableToken target, boolean ranged) {

        }

        @Override
        public void onAttacked(CharacterToken attacker, CharacterToken target, int damage, boolean dodge) {
                if (dodge) {
                        System.out.println(String.format("%s dodged attack from %s", target.getName(), attacker.getName()));
                        spawnDamageInfoLabel("dodge", target, Color.YELLOW);
                } else {
                        System.out.println(String.format("%s received %s damage from %s", target.getName(), damage, attacker.getName()));
                        spawnDamageInfoLabel(String.valueOf(damage), target, Color.RED);
                }

        }

        private void spawnDamageInfoLabel(String text, Token token, Color color) {
                TokenSpatial tokenSpatial = world.getTokenSpatial(token);
                if(tokenSpatial.visU <=0)
                        return;
                DamageLabel label = null;
                for (DamageLabel l : damageInfoLabels) {
                        if (l.tokenSpatial == null) {
                                label = l;
                                break;
                        }
                }
                if (label == null){
                        label = new DamageLabel();
                        damageInfoLabels.add(label);
                        label.label = new Label("",skin);
                        label.label.setFontScale(1.75f);
                }
                world.stage.addActor(label.label);
                label.label.setText(text);
                label.ttl = 1.5f;
                label.tokenSpatial = tokenSpatial;
                label.label.setColor(color);
        }

        @Override
        public void onInventoryAdd(Item item) {
                if (getItem(quickSlotButton) == null)
                        setButtonContents(quickSlotButton, item);

                refreshInventoryElements();
        }

        @Override
        public void onInventoryRemove(Item item) {
                if (getItem(quickSlotButton) == item) {
                        // change the quick slot to another item that .equals it (eg to another health potion) otherwise set to null
                        Item setItem = null;
                        for (Item i : localPlayerToken.getInventory()) {
                                if (i.equals(item)) {
                                        setItem = i;
                                        break;
                                }
                        }
                        ;

                        setButtonContents(quickSlotButton, setItem);
                }

                refreshInventoryElements();
        }

        @Override
        public void onConsumeItem(Item.Consumable item) {
                // refreshInventoryElements();  // onInventoryRemove will be called right after, no point in doing this twice
                this.setCenterLabelText("You just drank " + item.getNameFromJournal(localPlayerToken));
        }

        @Override
        public void onStatusEffectChange(StatusEffect effect, float duration) {
                Image statusImage = statusEffectImage[effect.ordinal()];
                if (duration == 0) {
                        statusImage.remove();
                } else if (statusImage.getParent() == null) {
                        avatarStatusEffectsGroup.addActor(statusImage);
                }

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

                if (event.getTarget() == avatarWindow) {
                        if (event instanceof InputEvent) {
                                InputEvent inputEvent = (InputEvent) event;
                                if (inputEvent.getType() == InputEvent.Type.touchDown) {
                                        float clickX = inputEvent.getStageX();
                                        float clickY = inputEvent.getStageY();
                                        if (clickX < avatarWindow.getX() || clickY < avatarWindow.getY() || clickX > avatarWindow.getX() + avatarWindow.getWidth() || clickY > avatarWindow.getY() + avatarWindow.getHeight()) {
                                                setAvatarWindowVisible(false);
                                                return true;
                                        }
                                }
                        }
                        return false;
                }

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
                        setAvatarWindowVisible(true);
                } else if (event.getListenerActor() == inventoryButton) {
                        setInventoryWindowVisible(true);
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
                        // inventory screen button
                        Object userObject = event.getListenerActor().getUserObject();
                        if (userObject instanceof Item) {
                                setItemWindowContents((Item) userObject);
                                setItemDialogVisible(true);
                        }
                }
                return false;
        }

        private final ActorGestureListener internalGestureListener = new ActorGestureListener() {
                @Override
                public void tap(InputEvent event, float x, float y, int count, int button) {
                        if (event.getListenerActor() == quickSlotButton) {
                                Item item = getItem(quickSlotButton);
                                if (item == null) {
                                        // TODO: show screen to set quick slot
                                } else {
                                        if (item instanceof Item.Consumable) {
                                                localPlayerToken.useItem(item);
                                        } else {
                                                throw new AssertionError("TODO: need to handle item of type " + item.getClass());
                                        }
                                }
                        }
                }

                @Override
                public boolean longPress(Actor actor, float x, float y) {
                        System.out.println("long");
                        return false;
                }
        };

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

        private class DamageLabel{
                Label label;
                float ttl;
                TokenSpatial tokenSpatial;


        }
}
