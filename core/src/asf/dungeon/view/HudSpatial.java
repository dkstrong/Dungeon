package asf.dungeon.view;

import asf.dungeon.model.Direction;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Tile;
import asf.dungeon.model.item.ArmorItem;
import asf.dungeon.model.item.BookItem;
import asf.dungeon.model.item.ConsumableItem;
import asf.dungeon.model.item.EquipmentItem;
import asf.dungeon.model.item.Item;
import asf.dungeon.model.item.KeyItem;
import asf.dungeon.model.item.PotionItem;
import asf.dungeon.model.item.QuickItem;
import asf.dungeon.model.item.RingItem;
import asf.dungeon.model.item.ScrollItem;
import asf.dungeon.model.item.StackableItem;
import asf.dungeon.model.item.WeaponItem;
import asf.dungeon.model.token.Attack;
import asf.dungeon.model.token.Damage;
import asf.dungeon.model.token.Experience;
import asf.dungeon.model.token.Inventory;
import asf.dungeon.model.token.StatusEffects;
import asf.dungeon.model.token.Token;
import asf.dungeon.utility.UtMath;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
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
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.SnapshotArray;

import java.util.Arrays;


/**
 * Created by Danny on 11/1/2014.
 */
public class HudSpatial implements Spatial, EventListener, InputProcessor, Token.Listener {
        private boolean initialized = false;
        private DungeonWorld world;
        protected Token localPlayerToken;

        // TODO: these settings should be stored on an app level object so it can be easily changed in the settings
        private boolean showRenderingStasLabel = true;

        private Skin skin;
        private Label gameLogLabel, targetInfoLabel;
        private float gameLogDisplayCountdown = Float.NaN;
        private final Array<String> thoughtsMessageQueue = new Array<String>(true, 4, String.class);
        private Button inventoryButton, avatarButton;
        private Button[] quickButtons;
        private ProgressBar healthProgressBar;
        private Label avatarLabel;
        private HorizontalGroup avatarStatusEffectsGroup;
        private Image[] statusEffectImage;
        private Window avatarWindow;
        private Label renderingStats;
        private Direction currentTouchPadDirection = null;
        private Window inventoryWindow;
        private final Array<Button> inventoryButtons = new Array<Button>(true, 20, Button.class);
        private Window itemWindow;
        private Label itemWindowLabel;
        private Button itemWindowUseButton, itemWindowDiscardButton, itemWindowBackButton;
        private final Array<DamageLabel> damageInfoLabels = new Array<DamageLabel>(false, 8, DamageLabel.class);

        private boolean tokenSelectMode = false;
        private ConsumableItem.TargetsTokens tokenSelectForItem;

        private final Vector3 tempVec = new Vector3();

        @Override
        public void preload(DungeonWorld world) {
                this.world = world;
                //skin = new Skin(Gdx.files.internal());

                world.assetManager.load("Skins/BasicSkin/uiskin.json", Skin.class);

                for (String statusEffectIconTextureAssetLocation : world.getAssetMappings().statusEffectIconTextureAssetLocations) {
                        world.assetManager.load(statusEffectIconTextureAssetLocation, Texture.class);
                }

        }

        @Override
        public void init(AssetManager assetManager) {
                initialized = true;
                if (localPlayerToken == null) {
                        return;
                }
                skin = world.assetManager.get("Skins/BasicSkin/uiskin.json", Skin.class);

                avatarButton = new Button(skin);
                world.stage.addActor(avatarButton);

                avatarButton.add(new Label("Avatar", skin));
                avatarButton.addCaptureListener(this);

                Damage damage = localPlayerToken.get(Damage.class);
                healthProgressBar = new ProgressBar(0, damage.getMaxHealth(), 1, false, skin, "default");
                healthProgressBar.setValue(damage.getHealth());
                healthProgressBar.setAnimateInterpolation(Interpolation.linear);
                healthProgressBar.setAnimateDuration(1f);
                world.stage.addActor(healthProgressBar);

                avatarLabel = new Label("Knight\nLevel 1\nXP 25/100", skin);
                world.stage.addActor(avatarLabel);
                avatarLabel.setAlignment(Align.topLeft);

                avatarStatusEffectsGroup = new HorizontalGroup();
                world.stage.addActor(avatarStatusEffectsGroup);
                avatarStatusEffectsGroup.align(Align.bottomLeft);


                statusEffectImage = new Image[world.getAssetMappings().statusEffectIconTextureAssetLocations.length];
                for (int i = 0; i < statusEffectImage.length; i++) {
                        statusEffectImage[i] = new Image(world.assetManager.get(world.getAssetMappings().statusEffectIconTextureAssetLocations[i], Texture.class));
                        //statusEffectImage[i].setScaling(Scaling.fit);
                        statusEffectImage[i].setScaling(Scaling.fillY);
                        statusEffectImage[i].setAlign(Align.bottomLeft);
                        //avatarStatusEffectsGroup.addActor(statusEffectImage[i]);
                }


                if (showRenderingStasLabel) {
                        renderingStats = new Label("", skin);
                        world.stage.addActor(renderingStats);
                        renderingStats.setAlignment(Align.topRight);
                }

                gameLogLabel = new Label(null, skin);
                gameLogLabel.setAlignment(Align.bottomLeft, Align.bottomLeft);
                //gameLogLabel.debug();


                targetInfoLabel = new Label(null, skin);
                targetInfoLabel.setAlignment(Align.center);
                world.stage.addActor(targetInfoLabel);

                quickButtons = new Button[localPlayerToken.getInventory().numQuickSlots()];
                for (int i = 0; i < quickButtons.length; i++) {
                        Button quickButton = new Button(skin);
                        world.stage.addActor(quickButton);
                        quickButton.add(new Label("<Empty>", skin));
                        quickButton.addCaptureListener(internalGestureListener);
                        setButtonContents(quickButton, null);
                        quickButtons[i] = quickButton;
                }


                inventoryButton = new Button(skin);
                world.stage.addActor(inventoryButton);
                inventoryButton.add(new Label("Inventory", skin));
                inventoryButton.addCaptureListener(this);

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

                        inventoryWindow.defaults().prefSize(windowButtonSize, windowButtonSize);

                        inventoryWindow.row().pad(windowButtonSize * .1f, 0, windowButtonSize * .15f, 0);
                        for (int i = 0; i < 4; i++) {
                                Button equipmentButton = new Button(skin);
                                equipmentButton.addCaptureListener(this);
                                inventoryWindow.add(equipmentButton);
                                inventoryButtons.add(equipmentButton);
                        }

                        for (int y = 0; y < 4; y++) {
                                inventoryWindow.row();
                                for (int x = 0; x < 4; x++) {
                                        Button invButton = new Button(skin);
                                        invButton.addCaptureListener(this);

                                        setButtonContents(invButton, null);
                                        inventoryWindow.add(invButton);
                                        inventoryButtons.add(invButton);
                                }
                        }

                        inventoryWindow.row();
                        Button closeButton = new Button(skin);
                        closeButton.add("Close Inventory");
                        closeButton.setUserObject("Close Inventory");
                        closeButton.addCaptureListener(new ChangeListener() {
                                @Override
                                public void changed(ChangeEvent event, Actor actor) {
                                        setInventoryWindowVisible(false);
                                }
                        });
                        inventoryWindow.add(closeButton).colspan(4).prefSize(windowCloseButtonSize, windowCloseButtonSize * .5f).pad(windowCloseButtonSize * .35f, 0, windowCloseButtonSize * .15f, 0);

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

                refreshInventoryElements();

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

                inventoryButton.setBounds(
                        margin + buttonSize, Gdx.graphics.getHeight() - buttonSize - margin - buttonSize * .333f,
                        buttonSize, buttonSize);

                avatarStatusEffectsGroup.setBounds(
                        margin + buttonSize,
                        Gdx.graphics.getHeight() - buttonSize - margin,
                        buttonSize * .333f,
                        buttonSize * .333f);


                if (renderingStats != null)
                        renderingStats.setBounds(Gdx.graphics.getWidth() - buttonSize - margin, Gdx.graphics.getHeight() - buttonSize - margin, buttonSize, buttonSize);


                gameLogLabel.setBounds(
                        margin,
                        margin,
                        Gdx.graphics.getWidth() * .5f,
                        buttonSize * 1.25f);

                targetInfoLabel.setBounds(
                        Gdx.graphics.getWidth() * .25f,
                        Gdx.graphics.getHeight() - margin - buttonSize * .5f,
                        Gdx.graphics.getWidth() * .5f,
                        buttonSize * .25f);

                float quickXOffset = buttonSize + margin;
                for (int i = 0; i < quickButtons.length; i++) {
                        quickButtons[i].setBounds(Gdx.graphics.getWidth() - (quickXOffset * (i + 1)),
                                margin,
                                buttonSize,
                                buttonSize);
                }


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



        protected void setToken(Token token) {
                localPlayerToken = token;
                if (this.isInitialized()) {
                        this.init(world.assetManager);
                }
        }

        @Override
        public void update(float delta) {

                if (renderingStats != null) {
                        renderingStats.setText("FPS : " + Gdx.graphics.getFramesPerSecond());
                }

                if (localPlayerToken != null) {
                        healthProgressBar.setRange(0, localPlayerToken.getDamage().getMaxHealth());
                        healthProgressBar.setValue(localPlayerToken.getDamage().getHealth());
                        healthProgressBar.act(delta);

                        String s = String.format("%s / %s\n Level %s (XP: %s / %s)",
                                localPlayerToken.getDamage().getHealth(),
                                localPlayerToken.getDamage().getMaxHealth(),
                                localPlayerToken.getExperience().getLevel(),
                                localPlayerToken.getExperience().getXp(),
                                localPlayerToken.getExperience().getRequiredXpToLevelUp());
                        avatarLabel.setText(s);

                        if (tokenSelectMode) {
                                targetInfoLabel.setText("Choose target for " + tokenSelectForItem.getNameFromJournal(localPlayerToken));
                        } else {
                                Token targetToken = localPlayerToken.getCommand().getTargetToken();
                                if (targetToken == null)
                                        targetInfoLabel.setText(null);
                                else {
                                        targetInfoLabel.setText("Target: " + targetToken.getName());
                                }
                        }

                }

                gameLogDisplayCountdown -= delta;
                if (gameLogDisplayCountdown < 0) {
                        gameLogLabel.remove();
                        gameLogDisplayCountdown = Float.NaN;
                }


                for (DamageLabel label : damageInfoLabels) {
                        if (label.tokenSpatial != null) {
                                label.ttl -= delta;
                                if (!label.tokenSpatial.isInitialized() || label.ttl <= 0 || label.tokenSpatial.visU <= 0) {
                                        label.tokenSpatial = null;
                                        label.ttl = 0;
                                        label.label.remove();
                                        continue;
                                }

                                world.getScreenCoords(
                                        label.tokenSpatial.getToken().getMove().getLocationFloatX(),
                                        label.tokenSpatial.getToken().getMove().getLocationFloatY(),
                                        tempVec);


                                if (label.label.getText().toString().toLowerCase().contains("d")) {
                                        tempVec.x -= 15;
                                } else {
                                        tempVec.x -= 2;
                                }

                                tempVec.y += 80 + Interpolation.linear.apply(0, 35, 1 - (label.ttl / 1.5f));
                                label.label.setPosition(tempVec.x, tempVec.y);
                                label.label.getColor().a = label.ttl / 1.5f;

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
                                        Damage damage = localPlayerToken.get(Damage.class);
                                        Experience experience = localPlayerToken.get(Experience.class);
                                        sb.append(String.format("HP: %s / %s\n", damage.getHealth(), damage.getMaxHealth()));

                                        String vit = experience.getVitalityBase() + (experience.getVitalityMod() > 0 ? " + " + experience.getVitalityMod() : "");
                                        String str = experience.getStrengthBase() + (experience.getStrengthMod() > 0 ? " + " + experience.getStrengthMod() : "");
                                        String agi = experience.getAgilityBase() + (experience.getAgilityMod() > 0 ? " + " + experience.getAgilityMod() : "");
                                        String lck = experience.getLuckBase() + (experience.getLuckMod() > 0 ? " + " + experience.getLuckMod() : "");

                                        sb.append(String.format("Vitality: %s \n", vit));
                                        sb.append(String.format("Strength: %s \n", str));
                                        sb.append(String.format("Agility: %s \n", agi));
                                        sb.append(String.format("Luck: %s \n", lck));


                                        label.setText(sb);
                                }
                        }


                }


        }

        private void refreshInventoryElements() {
                //Gdx.app.log("HudSpatial", "refresh inventory");

                int numQuickSlots = localPlayerToken.getInventory().numQuickSlots();
                // ensure there are enough hud elements for all the quick slots
                if(quickButtons.length != numQuickSlots){
                        // resize the quick buttons array, and populate their contents
                        quickButtons = Arrays.copyOf(quickButtons, numQuickSlots);
                        float quickXOffset = quickButtons[0].getX() + Gdx.graphics.getWidth();
                        float quickYOffset = quickButtons[0].getY();
                        float quickButtonSize = quickButtons[0].getWidth();
                        for (int i = 0; i < numQuickSlots; i++) {
                                if(quickButtons[i] == null){
                                        Button quickButton = new Button(skin);
                                        world.stage.addActor(quickButton);
                                        quickButton.addCaptureListener(internalGestureListener);
                                        quickButtons[i] = quickButton;
                                        quickButtons[i].setBounds(Gdx.graphics.getWidth() - (quickXOffset * (i + 1)), quickYOffset,quickButtonSize,quickButtonSize);
                                }
                                QuickItem quickItem = localPlayerToken.getInventory().getQuickSlot(i);
                                setButtonContents(quickButtons[i], quickItem);
                        }
                }else{
                        // has the correct number of quick button elements, just need to repopulate their contents
                        for (int i = 0; i < numQuickSlots; i++) {
                                QuickItem quickItem = localPlayerToken.getInventory().getQuickSlot(i);
                                setButtonContents(quickButtons[i], quickItem);
                        }
                }





                // if the inventory window is open, then refresh the inventory window
                if (inventoryWindow.getParent() != null) {
                        WeaponItem weaponSlot = localPlayerToken.getInventory().getWeaponSlot();
                        setButtonContents(inventoryButtons.get(0), weaponSlot);
                        ArmorItem armorSlot = localPlayerToken.getInventory().getArmorSlot();
                        setButtonContents(inventoryButtons.get(1), armorSlot);
                        RingItem ringSlot = localPlayerToken.getInventory().getRingSlot();
                        setButtonContents(inventoryButtons.get(2), ringSlot);


                        //Gdx.app.log("HudSpatial", "refresh inventory window");
                        int buttonI = 4;
                        for (int i = 0; i < localPlayerToken.getInventory().size(); i++) {
                                Item item = localPlayerToken.getInventory().get(i);
                                if (localPlayerToken.getInventory().isEquipped(item))
                                        continue;
                                boolean hasItem = false;
                                for (int j = buttonI - 1; j >= 0; j--) {
                                        if (inventoryButtons.get(j).getUserObject() != null && inventoryButtons.get(j).getUserObject().equals(item)) {
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
                                button.add("");

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

                                Label quantityLabel;
                                if (item instanceof StackableItem) {
                                        StackableItem stackableItem = (StackableItem) item;
                                        quantityLabel = new Label("x" + stackableItem.getCharges(), skin);
                                } else {
                                        quantityLabel = new Label(" ", skin);
                                }
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
                                                if (item instanceof StackableItem) {
                                                        StackableItem stackableItem = (StackableItem) item;
                                                        label.setText("x" + stackableItem.getCharges());
                                                } else {
                                                        label.setText(" ");
                                                }
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


                Label useLabel = (Label) itemWindowUseButton.getChildren().get(0);
                Label discardLabel = (Label) itemWindowDiscardButton.getChildren().get(0);
                discardLabel.setText("Discard");
                itemWindowUseButton.setDisabled(false);
                itemWindowDiscardButton.setDisabled(false);
                if (item instanceof ConsumableItem) {
                        useLabel.setText("Use");
                } else if (item instanceof EquipmentItem) {
                        // TODO: when unable to equip/unequp/discard items due to curse or attack timer
                        // there should be an error dialog that pops up with the relevant message
                        // instead of just disabling the button
                        if (localPlayerToken.getInventory().isEquipped(item)) {
                                boolean cursed = ((EquipmentItem) item).isCursed();
                                if (!cursed && localPlayerToken.getInventory().canChangeEquipment()) {
                                        useLabel.setText("Unequip");
                                } else {
                                        useLabel.setText("");
                                        discardLabel.setText("");
                                        itemWindowUseButton.setDisabled(true);
                                        itemWindowDiscardButton.setDisabled(true);
                                }
                        } else {
                                if (localPlayerToken.getInventory().canChangeEquipment()) {
                                        useLabel.setText("Equip");
                                } else {
                                        useLabel.setText("");
                                        itemWindowUseButton.setDisabled(true);
                                }
                        }

                }


        }

        private void appendToGameLog(String text) {

                String baseText = String.valueOf(gameLogLabel.getText());

                String[] split = baseText.split("\n");
                if (split.length > 4) {
                        baseText = "";
                        for (int i = split.length - 4; i < split.length; i++) {
                                baseText += split[i] + "\n";
                        }
                } else {
                        baseText += "\n";
                }

                gameLogLabel.setText(baseText + text);

                if (Float.isNaN(gameLogDisplayCountdown)) gameLogDisplayCountdown = 0;
                gameLogDisplayCountdown += UtMath.scalarLimitsInterpolation(text.length(), 30f, 80f, 5f, 8f);
                if (gameLogDisplayCountdown > 16) gameLogDisplayCountdown = 16;

                if (gameLogLabel.getParent() == null)
                        world.stage.addActor(gameLogLabel);

        }

        protected void closeAllWindows() {
                setAvatarWindowVisible(false);
                setInventoryWindowVisible(false);
        }

        public boolean isWindowVisible() {
                if (!isInitialized()) return false;
                return avatarWindow.getParent() != null || inventoryWindow.getParent() != null || itemWindow.getParent() != null;
        }

        private void setAvatarWindowVisible(boolean visible) {
                if (visible) {
                        setInventoryWindowVisible(false);
                        if (avatarWindow.getParent() == null)
                                world.stage.addActor(avatarWindow);
                        refreshAvatarWindowElements();
                        world.setPaused(true);
                } else {
                        avatarWindow.remove();
                        world.setPaused(false);
                }
        }

        private void setInventoryWindowVisible(boolean visible) {
                if (visible) {
                        setAvatarWindowVisible(false);
                        if (inventoryWindow.getParent() == null)
                                world.stage.addActor(inventoryWindow);
                        refreshInventoryElements();
                        world.setPaused(true);
                } else {
                        inventoryWindow.remove();
                        itemWindow.remove();
                        inventoryWindow.setModal(true);
                        world.setPaused(false);
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
                        world.setPaused(true);
                } else {
                        itemWindow.remove();
                        inventoryWindow.setModal(true);
                        world.setPaused(false);
                }
        }

        @Override
        public void onPathBlocked(Pair nextLocation, Tile nextTile) {
                if (nextTile.isDoor() && nextTile.isDoorLocked()) {
                        KeyItem key = localPlayerToken.getInventory().getKeyItem(nextTile.getKeyType());
                        if (key != null) {
                                this.appendToGameLog("Tap on door again to unlock door.");
                        } else {
                                this.appendToGameLog("You do not have the key to unlock this door.");
                        }
                }
        }

        @Override
        public void onUseItem(Item item, Inventory.Character.UseItemOutcome out) {
                if (!out.didSomething) {
                        this.appendToGameLog("A bright light flashed, but nothing happened.");
                        return;
                }
                if (item instanceof PotionItem) {
                        this.appendToGameLog("You just drank " + item.getNameFromJournal(localPlayerToken));
                } else if (item instanceof ScrollItem) {
                        ScrollItem scroll = (ScrollItem) item;
                        if (scroll.getType() == ScrollItem.Type.Lightning) {
                                spawnDamageInfoLabel(String.valueOf(out.damage), out.targetToken, Color.RED);
                        } else if (scroll.getType() == ScrollItem.Type.Teleportation) {
                                if (out.targetToken == localPlayerToken) {
                                        this.appendToGameLog("You just teleported to somewhere else on the level.");
                                } else {
                                        this.appendToGameLog("You just teleported " + out.targetToken.getName() + " to somewhere else on the level.");
                                }

                        }
                } else if (item instanceof BookItem) {

                }

        }

        @Override
        public void onAttack(Token target, Pair targetLocation, boolean ranged) {

        }

        @Override
        public void onAttacked(Token attacker, Token target, Attack.AttackOutcome attackOutcome) {
                if (attackOutcome.dodge) {
                        //System.out.println(String.format("%s dodged attack from %s", target.getName(), attacker.getName()));
                        spawnDamageInfoLabel("MISS", target, Color.YELLOW);
                } else {
                        //System.out.println(String.format("%s received %s damage from %s", target.getName(), damage, attacker.getName()));
                        spawnDamageInfoLabel(String.valueOf(attackOutcome.damage), target, Color.RED);
                }

        }

        private void spawnDamageInfoLabel(String text, Token token, Color color) {
                TokenSpatial tokenSpatial = world.getTokenSpatial(token);
                if (tokenSpatial.visU <= 0)
                        return;
                DamageLabel label = null;
                for (DamageLabel l : damageInfoLabels) {
                        if (l.tokenSpatial == null) {
                                label = l;
                                break;
                        }
                }
                if (label == null) {
                        label = new DamageLabel();
                        damageInfoLabels.add(label);
                        label.label = new Label("", skin);
                        label.label.setFontScale(1.75f);
                }
                world.stage.addActor(label.label);
                label.label.setText(text);
                label.ttl = 1.5f;
                label.tokenSpatial = tokenSpatial;
                label.label.setColor(color);
        }

        @Override
        public void onInventoryChanged() {
                refreshInventoryElements();
        }

        @Override
        public void onStatusEffectChange(StatusEffects.Effect effect, float duration) {
                Image statusImage = statusEffectImage[effect.ordinal()];
                if (duration == 0) {
                        statusImage.remove();
                } else if (statusImage.getParent() == null) {
                        avatarStatusEffectsGroup.addActor(statusImage);
                }

        }

        @Override
        public void onLearned(Item identifiedItem, boolean study) {

                refreshInventoryElements();
                if (identifiedItem instanceof EquipmentItem) {
                        EquipmentItem item = (EquipmentItem) identifiedItem;
                        if (study) {
                                this.appendToGameLog(String.format("You've used %s long enough that you now understand its secrets.", item.getName()));
                        } else {
                                this.appendToGameLog(String.format("You now understand the secrets of %s.", item.getName()));
                        }
                } else {
                        this.appendToGameLog("You have identified " + identifiedItem.getName());
                }


        }

        @Override
        public boolean isInitialized() {
                return initialized;
        }

        @Override
        public void dispose() {
                initialized = false;

        }

        /**
         * called before dungeon.update() to do user input related updates
         *
         * @param delta
         */
        protected void updateInput(float delta) {
                if (tokenSelectMode) {
                        // do this check to prevent getting stuck in a state of not being able to cancel
                        // the quick slot, but not being able ot move because stuck in targeting mode
                        if (!tokenSelectForItem.isIdentified(localPlayerToken)) {

                                Array<Token> targetableTokens = localPlayerToken.getFloorMap().getTargetableTokens(localPlayerToken, tokenSelectForItem);
                                Gdx.app.log("HudSpatial", "not identified, " + targetableTokens);
                                if (targetableTokens.size == 0) {
                                        tokenSelectMode = false;
                                        tokenSelectForItem = null;
                                        localPlayerToken.getCommand().consumeItem(tokenSelectForItem);
                                } else if (targetableTokens.size == 1) {
                                        if (targetableTokens.get(0) == localPlayerToken) {
                                                localPlayerToken.getCommand().consumeItem(tokenSelectForItem);
                                                tokenSelectMode = false;
                                                tokenSelectForItem = null;

                                        }
                                }
                        }
                } else if (mouseDownDrag) {
                        Ray ray = world.cam.getPickRay(Gdx.input.getX(), Gdx.input.getY());
                        dragCommand(ray);
                }
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (tokenSelectMode) {
                        boolean hasTarget = tokenSelectCommand(screenX, screenY);
                        if (hasTarget) {
                                tokenSelectMode = false;
                                tokenSelectForItem = null;
                        }
                } else {
                        boolean hasTarget = touchCommand(screenX, screenY);
                        if (!hasTarget) {
                                mouseDownDrag = true;
                        }
                }


                return true;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (mouseDownDrag) {
                        return true;
                }
                return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (mouseDownDrag) {
                        mouseDownDrag = false;
                        return true;
                }
                return false;
        }

        private boolean mouseDownDrag = false;
        private final Vector3 tempWorldCoords = new Vector3();
        private final Pair tempMapCoords = new Pair();

        private boolean tokenSelectCommand(int screenX, int screenY) {
                Ray ray = world.cam.getPickRay(screenX, screenY);
                Token targetToken = world.getToken(ray, null);

                if (targetToken == null)
                        return false;

                if (targetToken == localPlayerToken) {
                        localPlayerToken.getCommand().consumeItem(tokenSelectForItem);
                } else if (tokenSelectForItem.canConsume(localPlayerToken, targetToken)) {
                        localPlayerToken.getCommand().consumeItem(tokenSelectForItem, targetToken);
                }

                return true;
        }

        private boolean touchCommand(int screenX, int screenY) {
                Ray ray = world.cam.getPickRay(screenX, screenY);
                Token targetToken = world.getToken(ray, localPlayerToken);
                // attempt to target specificaly clicked token
                if (targetToken != null) {
                        localPlayerToken.getCommand().setTargetToken(targetToken);
                        if (localPlayerToken.getCommand().getTargetToken() != null) {
                                world.selectionMark.mark(targetToken.getLocation());
                                return true;
                        }

                }

                final float distance = -ray.origin.y / ray.direction.y;
                tempWorldCoords.set(ray.direction).scl(distance).add(ray.origin);
                world.getMapCoords(tempWorldCoords, tempMapCoords);
                localPlayerToken.getCommand().setUseKeyOnTile(tempMapCoords);
                if (localPlayerToken.getCommand().isUseKey()) {
                        world.selectionMark.mark(tempMapCoords);
                        return true;
                }

                // if no token was clicked then go to the tile that was clicked
                return false;
        }

        private boolean dragCommand(Ray ray) {
                final float distance = -ray.origin.y / ray.direction.y;
                tempWorldCoords.set(ray.direction).scl(distance).add(ray.origin);
                world.getMapCoords(tempWorldCoords, tempMapCoords);
                if (localPlayerToken.getFloorMap().getTile(tempMapCoords) != null) {
                        localPlayerToken.getCommand().setLocation(tempMapCoords);
                        world.selectionMark.mark(tempMapCoords);
                        return true;
                }
                return false;
        }

        @Override
        public boolean keyDown(int keycode) {
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

                if (event.getListenerActor() == avatarButton) {
                        setAvatarWindowVisible(true);
                } else if (event.getListenerActor() == inventoryButton) {
                        setInventoryWindowVisible(true);
                } else if (event.getListenerActor() == itemWindowUseButton) {
                        Object object = itemWindow.getUserObject();

                        if (object instanceof ConsumableItem) {
                                boolean valid = localPlayerToken.getCommand().consumeItem((ConsumableItem) object);
                                if (valid)
                                        setInventoryWindowVisible(false);
                        } else if (object instanceof EquipmentItem) {
                                EquipmentItem item = (EquipmentItem) object;
                                if (localPlayerToken.getInventory().isEquipped(item)) {
                                        localPlayerToken.getInventory().unequip(item);
                                } else {
                                        boolean valid = localPlayerToken.getInventory().equip(item);
                                        if (valid && item.isCursed())
                                                appendToGameLog("You grip the " + item.getNameFromJournal(localPlayerToken) + " tightly. You are powerless to remove it.");

                                }
                                setItemDialogVisible(false);
                        } else if (object instanceof QuickItem) {
                                QuickItem item = (QuickItem) object;
                                if (localPlayerToken.getInventory().isEquipped(item)) {
                                        localPlayerToken.getInventory().unequip(item);
                                } else {
                                        localPlayerToken.getInventory().equip(item);
                                }
                        }


                } else if (event.getListenerActor() == itemWindowDiscardButton) {
                        Item item = (Item) itemWindow.getUserObject();
                        boolean valid = localPlayerToken.getInventory().discard(item);
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
                        Object uo = event.getListenerActor().getUserObject();
                        if(uo instanceof QuickItem){
                                if (tokenSelectMode) {
                                        if (tokenSelectForItem.isIdentified(localPlayerToken)) {
                                                // can only cancel identified items
                                                tokenSelectForItem = null;
                                                tokenSelectMode = false;
                                        }
                                        return;
                                }
                                if (uo instanceof ConsumableItem.TargetsTokens) {
                                        tokenSelectMode = true;
                                        tokenSelectForItem = (ConsumableItem.TargetsTokens) uo;
                                        Gdx.app.log("HudSpatial", "start targeting mode");
                                        // if item is not identified and can not be used on any targets, then automatically cast on self
                                        if (!tokenSelectForItem.isIdentified(localPlayerToken)) {
                                                // if not identified, then using the scroll can not be cancelled
                                        } else {
                                                // TODO: change text to "cancel"
                                        }

                                } else if (uo instanceof ConsumableItem) {
                                        localPlayerToken.getCommand().consumeItem((ConsumableItem)uo);
                                } else {
                                        throw new AssertionError(uo);
                                }
                        }
                }

                @Override
                public boolean longPress(Actor actor, float x, float y) {

                        return false;
                }
        };

        private class DamageLabel {
                Label label;
                float ttl;
                TokenSpatial tokenSpatial;


        }
}
