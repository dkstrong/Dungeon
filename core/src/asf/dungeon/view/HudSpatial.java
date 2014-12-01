package asf.dungeon.view;

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
import asf.dungeon.model.token.Interact;
import asf.dungeon.model.token.InteractChat;
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
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
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
        // in game hud
        private Label gameLogLabel, targetInfoLabel;
        private float gameLogDisplayCountdown = Float.NaN;
        private float gameOverCountdown = .75f;
        private Button avatarButton;
        private Button[] quickButtons;
        private ProgressBar healthProgressBar;
        private Label avatarLabel;
        private HorizontalGroup avatarStatusEffectsGroup;
        private Image[] statusEffectImage;
        private Label renderingStats;
        private final Array<DamageLabel> damageInfoLabels = new Array<DamageLabel>(false, 8, DamageLabel.class);
        //inventory hud
        private Table inventoryWindow;
        private Image inventoryWindowBackgroundImage;
        private HorizontalGroup inputModeHorizontalGroup;
        private Label inputModeLabel;
        private Button inputModeCancelButton;
        private Table equipmentTable, backPackTable;
        private final Array<Button> inventoryEquipmentButtons = new Array<Button>(true, 6, Button.class);
        private final Array<Button> inventoryBackPackButtons = new Array<Button>(true, 16, Button.class);
        private Window itemWindow;
        private Label itemWindowLabel;
        private Button itemWindowUseButton, itemWindowDiscardButton, itemWindowBackButton;
        //chat hud
        private Table chatWindow;
        private Label chatLabel;
        private HorizontalGroup buttonGroup;
        private final Array<Button> chatChoiceButtons = new Array<Button>(true, 4, Button.class);
        // input mode
        private boolean tokenSelectMode = false;
        private ConsumableItem.TargetsTokens tokenSelectForItem;
        private boolean itemSelectMode = false;
        private ConsumableItem.TargetsItems itemSelectForItem;
        private boolean mapViewMode = false;
        // temp
        private final Vector3 tempVec = new Vector3();

        protected void setToken(Token token) {
                localPlayerToken = token;
                if (localPlayerToken != null) {
                        if (this.isInitialized())
                                this.init(world.assetManager);
                        if (!mapViewMode)
                                world.camControl.setChaseTarget(world.getTokenSpatial(localPlayerToken));
                } else {
                        if (this.isInitialized()) {
                                this.setHudElementsVisible(false);
                                this.setInventoryWindowVisible(false);
                        }
                        world.camControl.setChaseTarget(null);
                }

        }

        @Override
        public void preload(DungeonWorld world) {
                this.world = world;
                //skin = new Skin(Gdx.files.internal());

                world.assetManager.load("Skins/BasicSkin/uiskin.json", Skin.class);

                for (String statusEffectIconTextureAssetLocation : world.assetMappings.statusEffectIconTextureAssetLocations) {
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
                avatarButton.add(new Label("Inventory", skin));
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


                statusEffectImage = new Image[world.assetMappings.statusEffectIconTextureAssetLocations.length];
                for (int i = 0; i < statusEffectImage.length; i++) {
                        statusEffectImage[i] = new Image(world.assetManager.get(world.assetMappings.statusEffectIconTextureAssetLocations[i], Texture.class));
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
                gameLogLabel.setAlignment(Align.topLeft, Align.topLeft);
                //gameLogLabel.debug();


                targetInfoLabel = new Label(null, skin);
                targetInfoLabel.setAlignment(Align.center);
                world.stage.addActor(targetInfoLabel);

                int numQuickSlots = localPlayerToken.getInventory().numQuickSlots();
                quickButtons = new Button[numQuickSlots];
                for (int i = 0; i < numQuickSlots; i++) {
                        Button quickButton = new Button(skin);
                        world.stage.addActor(quickButton);
                        quickButton.add(new Label("<Empty>", skin));
                        quickButton.addCaptureListener(this);
                        setButtonContents(quickButton, null);
                        quickButtons[i] = quickButton;
                }


                inventoryWindowBackgroundImage = new Image(skin, "default-rect");



                inventoryWindow = new Table(skin);
                //inventoryWindow.setMovable(false);
                inventoryWindow.addCaptureListener(this);
                //inventoryWindow.removeActor(inventoryWindow.getButtonTable());
                equipmentTable = new Table(skin);
                backPackTable = new Table(skin);
                //inventoryWindow.debug();
                //world.stage.addActor(inventoryWindow);
                //inventoryWindow.debugAll();
                {

                        inputModeLabel = new Label("Inventory and Stats", skin);

                        inputModeCancelButton = new Button(skin);
                        inputModeCancelButton.add(new Label("Cancel", skin));
                        inputModeCancelButton.addCaptureListener(this);

                        inputModeHorizontalGroup = new HorizontalGroup();
                        inputModeHorizontalGroup.addActor(inputModeLabel);
                        inventoryWindow.add(inputModeHorizontalGroup).colspan(3);

                        inventoryWindow.row();
                        inventoryWindow.add(equipmentTable).fill().expand();
                        inventoryWindow.add(backPackTable).fill().expand();

                        Label descriptionLabel = new Label("Hero Stats", skin);
                        descriptionLabel.setWrap(true);
                        ScrollPane scrollPane = new ScrollPane(descriptionLabel, skin);
                        scrollPane.setUserObject("Hero Stats");
                        inventoryWindow.add(scrollPane).fill().expand();


                        inventoryWindow.row();
                        Button closeButton = new Button(skin);
                        closeButton.add("Close");

                        closeButton.setUserObject("Close");
                        closeButton.addCaptureListener(new ChangeListener() {
                                @Override
                                public void changed(ChangeEvent event, Actor actor) {
                                        setInventoryWindowVisible(false);
                                }
                        });
                        inventoryWindow.add(closeButton).colspan(3);
                }


                for (int j = 0; j < 4; j++) {
                        equipmentTable.row();
                        for (int i = 0; i < 2; i++) {
                                if ((j == 0 || j == 2) && i == 1) {
                                        continue;
                                } else if (inventoryEquipmentButtons.size - 3 >= numQuickSlots) {
                                        continue;
                                }
                                Button equipmentButton = new Button(skin);
                                setButtonContents(equipmentButton, null);
                                equipmentButton.addCaptureListener(this);
                                inventoryEquipmentButtons.add(equipmentButton);
                                Cell<Button> cell = equipmentTable.add(equipmentButton);
                                if (j == 2) {
                                        cell.padTop(20);
                                }
                                if (j == 0 || j == 2 || (j == 3 && numQuickSlots == 2)) {
                                        cell.colspan(2);
                                } else {

                                }
                        }
                }

                for (int j = 0; j < 4; j++) {
                        backPackTable.row();
                        for (int i = 0; i < 4; i++) {
                                Button equipmentButton = new Button(skin);
                                setButtonContents(equipmentButton, null);
                                equipmentButton.addCaptureListener(this);
                                Cell<Button> cell = backPackTable.add(equipmentButton);
                                inventoryBackPackButtons.add(equipmentButton);

                        }
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
                        itemWindow.add(itemWindowUseButton);

                        itemWindowDiscardButton = new Button(skin);
                        itemWindowDiscardButton.add("Discard");
                        itemWindowDiscardButton.addCaptureListener(this);
                        itemWindow.add(itemWindowDiscardButton);

                        itemWindowBackButton = new Button(skin);
                        itemWindowBackButton.add("Back");
                        itemWindowBackButton.addCaptureListener(this);
                        itemWindow.add(itemWindowBackButton);
                }

                chatWindow = new Table(skin);
                {
                        chatWindow.row();
                        chatLabel = new Label("Chat Label", skin);
                        chatLabel.setWrap(true);
                        ScrollPane scrollPane = new ScrollPane(chatLabel, skin);
                        chatWindow.add(scrollPane).fill().expand().colspan(3);
                        chatWindow.row();

                        buttonGroup = new HorizontalGroup();
                        chatWindow.add(buttonGroup).colspan(3);
                        for(int i=0; i<4; i++){
                                Button button = new Button(skin);
                                button.addCaptureListener(this);
                                button.setUserObject(i);
                                chatChoiceButtons.add(button);
                        }

                }


                resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

                refreshInventoryElements();

        }

        protected void resize(int width, int height) {
                if (avatarButton == null)
                        return;

                float margin = 15;
                float buttonSize = 100;

                avatarButton.setBounds(margin, margin, buttonSize, buttonSize);

                healthProgressBar.setBounds(
                        margin + buttonSize,
                        margin + buttonSize - buttonSize * .27f,
                        buttonSize * 1.5f,
                        buttonSize * .333f);

                avatarLabel.setBounds(
                        margin + buttonSize,
                        margin - buttonSize * .233f,
                        buttonSize, buttonSize);


                avatarStatusEffectsGroup.setBounds(
                        margin + buttonSize + margin * .25f,
                        margin,
                        buttonSize * .333f,
                        buttonSize * .333f);


                if (renderingStats != null)
                        renderingStats.setBounds(Gdx.graphics.getWidth() - buttonSize - margin, Gdx.graphics.getHeight() - buttonSize - margin, buttonSize, buttonSize);


                gameLogLabel.setBounds(
                        margin,
                        Gdx.graphics.getHeight() - margin - buttonSize,
                        Gdx.graphics.getWidth() * .5f,
                        buttonSize);

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


                float windowWidth = Gdx.graphics.getWidth() * .85f;
                float windowHeight = Gdx.graphics.getHeight() - 15 - 15;
                float windowButtonSize = windowHeight * (1 / 5f);
                float windowCloseButtonSize = windowButtonSize * .5f;
                inventoryWindow.setBounds(
                        (Gdx.graphics.getWidth() - windowWidth) * .5f,
                        ((Gdx.graphics.getHeight() - windowHeight) * .5f),
                        windowWidth,
                        windowHeight);

                inventoryWindowBackgroundImage.setBounds(
                        (Gdx.graphics.getWidth() - windowWidth) * .5f,
                        ((Gdx.graphics.getHeight() - windowHeight) * .5f),
                        windowWidth,
                        windowHeight);


                for (Cell cell : inventoryWindow.getCells()) {
                        String uoVal = String.valueOf(cell.getActor().getUserObject());
                        if (uoVal.equals("Close")) {
                                cell.prefSize(windowCloseButtonSize, windowCloseButtonSize * .5f).pad(windowCloseButtonSize * .35f, 0, windowCloseButtonSize * .15f, 0);
                        } else {
                                cell.prefSize(windowButtonSize, windowButtonSize);
                        }

                }

                for (Cell cell : equipmentTable.getCells()) {
                        cell.prefSize(windowButtonSize, windowButtonSize).minSize(windowButtonSize, windowButtonSize);
                }

                for (Cell cell : backPackTable.getCells()) {
                        cell.prefSize(windowButtonSize, windowButtonSize).minSize(windowButtonSize, windowButtonSize);
                }

                float itemWindowHeight = Gdx.graphics.getHeight() * .5f;
                float itemWindowWidth = itemWindowHeight * 1.75f;
                float itemWindowButtonSizeX = windowButtonSize * 1.25f;
                float itemWindowButtonSizeY = windowButtonSize * .75f;

                itemWindow.setBounds(
                        (Gdx.graphics.getWidth() - itemWindowWidth) * .5f,
                        (Gdx.graphics.getHeight() - itemWindowHeight) * .5f,
                        itemWindowWidth,
                        itemWindowHeight);

                for (Cell cell : itemWindow.getCells()) {
                        if (cell.getActor() instanceof Button) {
                                cell.prefSize(itemWindowButtonSizeX, itemWindowButtonSizeY).minSize(itemWindowButtonSizeX, itemWindowButtonSizeY);
                        }
                }


                float chatWindowWidth = Gdx.graphics.getWidth()*.5f;
                float chatWindowHeight = chatWindowWidth;

                chatWindow.setBounds(
                        (Gdx.graphics.getWidth() - chatWindowWidth) * .5f,
                        (Gdx.graphics.getHeight() - chatWindowHeight) * .5f,
                        chatWindowWidth,
                        chatWindowHeight);

                for (Cell cell : chatWindow.getCells()) {
                        String uoVal = String.valueOf(cell.getActor().getUserObject());
                        if (cell.getActor() instanceof Button) {
                                cell.prefSize(itemWindowButtonSizeX, itemWindowButtonSizeY).minSize(itemWindowButtonSizeX, itemWindowButtonSizeY);
                        }
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

                        if (localPlayerToken.getDamage().isDead()) {
                                targetInfoLabel.setText("GAME OVER!");
                                gameOverCountdown-=delta;
                        } else if (tokenSelectMode) {
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

        protected void closeAllWindows() {
                setInventoryWindowVisible(false);
                setChatWindowVisible(false);
        }

        public boolean isWindowVisible() {
                if (!isInitialized()) return false;
                return inventoryWindow.getParent() != null || itemWindow.getParent() != null || chatWindow.getParent() != null;
        }

        private void setHudElementsVisible(boolean visible) {
                avatarButton.setVisible(visible);
                healthProgressBar.setVisible(visible);
                avatarLabel.setVisible(visible);
                avatarStatusEffectsGroup.setVisible(visible);
                gameLogLabel.setVisible(visible);
                targetInfoLabel.setVisible(visible);
                for (Button quickButton : quickButtons) {
                        quickButton.setVisible(visible && !mapViewMode);
                }
        }

        private Item getItem(Button button) {
                if (button.getUserObject() instanceof Item)
                        return (Item) button.getUserObject();
                return null;
        }

        private Button getButton(Item item) {
                SnapshotArray<Actor> children = backPackTable.getChildren();
                for (Actor child : children) {
                        if (child.getUserObject() == item && child instanceof Button) {
                                return (Button) child;
                        }
                }

                children = equipmentTable.getChildren();
                for (Actor child : children) {
                        if (child.getUserObject() == item && child instanceof Button) {
                                return (Button) child;
                        }
                }
                return null;
        }

        private void refreshInventoryElements() {
                Gdx.app.log("HudSpatial", "refresh inventory elements");
                // refresh hero stats
                SnapshotArray<Actor> children = inventoryWindow.getChildren();
                Label heroStatsLabel = null;
                for (Actor child : children) {
                        String uoName = String.valueOf(child.getUserObject());
                        if (uoName.equals("Hero Stats")) {
                                ScrollPane scrollPane = (ScrollPane) child;
                                heroStatsLabel = (Label) scrollPane.getChildren().get(0);
                                break;
                        }
                }

                StringBuilder sb = new StringBuilder();
                Damage damage = localPlayerToken.get(Damage.class);
                Experience experience = localPlayerToken.get(Experience.class);
                sb.append(String.format("%s\nLevel %s\n\n", localPlayerToken.getName(), localPlayerToken.getExperience().getLevel()));
                sb.append(String.format("HP: %s / %s\n\n", damage.getHealth(), damage.getMaxHealth()));

                String vit = experience.getVitalityBase() + (experience.getVitalityMod() > 0 ? " + " + experience.getVitalityMod() : "");
                String str = experience.getStrengthBase() + (experience.getStrengthMod() > 0 ? " + " + experience.getStrengthMod() : "");
                String agi = experience.getAgilityBase() + (experience.getAgilityMod() > 0 ? " + " + experience.getAgilityMod() : "");
                String inte = experience.getIntelligenceBase() + (experience.getIntelligenceMod() > 0 ? " + " + experience.getIntelligenceMod() : "");
                String lck = experience.getLuckBase() + (experience.getLuckMod() > 0 ? " + " + experience.getLuckMod() : "");

                sb.append(String.format("Vit: %s \n", vit));
                sb.append(String.format("Str: %s \n", str));
                sb.append(String.format("Agi: %s \n", agi));
                sb.append(String.format("Int: %s \n", inte));
                sb.append(String.format("Luck: %s \n", lck));

                sb.append("\n");

                StatusEffects statusEffets = localPlayerToken.get(StatusEffects.class);
                if (statusEffets.hasStatusEffect(StatusEffects.Effect.Paralyze)) {
                        sb.append("You are paralyzed\n");
                }

                if (statusEffets.hasStatusEffect(StatusEffects.Effect.Poison)) {
                        sb.append("You are poisoned\n");
                }

                heroStatsLabel.setText(sb);


                //
                // refresh inventory


                int numQuickSlots = localPlayerToken.getInventory().numQuickSlots();
                // ensure there are enough hud elements for all the quick slots
                if (quickButtons.length != numQuickSlots) {
                        // resize the quick buttons array, and populate their contents
                        quickButtons = Arrays.copyOf(quickButtons, numQuickSlots);
                        float quickXOffset = quickButtons[0].getX() + Gdx.graphics.getWidth();
                        float quickYOffset = quickButtons[0].getY();
                        float quickButtonSize = quickButtons[0].getWidth();
                        for (int i = 0; i < numQuickSlots; i++) {
                                if (quickButtons[i] == null) {
                                        Button quickButton = new Button(skin);
                                        world.stage.addActor(quickButton);
                                        quickButton.addCaptureListener(this);
                                        quickButtons[i] = quickButton;
                                        quickButtons[i].setBounds(Gdx.graphics.getWidth() - (quickXOffset * (i + 1)), quickYOffset, quickButtonSize, quickButtonSize);

                                        Button equipmentButton = new Button(skin);
                                        equipmentButton.addCaptureListener(this);
                                        inventoryEquipmentButtons.add(equipmentButton);
                                        equipmentTable.add(equipmentButton);

                                        // ensure there is top padding for quick item 1 on the inventory window
                                        if (inventoryEquipmentButtons.size >= 4) {
                                                Cell q1 = equipmentTable.getCell(inventoryEquipmentButtons.get(3));
                                                q1.padTop(20);
                                        }
                                        // ensure that quick item 2 is properly aligned based on if there is a 3rd quick item or not
                                        if (inventoryEquipmentButtons.size >= 5) {
                                                Cell q2 = equipmentTable.getCell(inventoryEquipmentButtons.get(4));
                                                q2.colspan(inventoryEquipmentButtons.size == 5 ? 2 : 1);
                                        }

                                }
                                QuickItem quickItem = localPlayerToken.getInventory().getQuickSlot(i);
                                setButtonContents(quickButtons[i], quickItem);
                        }
                } else {
                        // has the correct number of quick button elements, just need to repopulate their contents
                        for (int i = 0; i < numQuickSlots; i++) {
                                QuickItem quickItem = localPlayerToken.getInventory().getQuickSlot(i);
                                setButtonContents(quickButtons[i], quickItem);
                        }
                }


                // if the inventory window is open, then refresh the inventory window
                if (backPackTable.getParent() != null) {
                        WeaponItem weaponSlot = localPlayerToken.getInventory().getWeaponSlot();
                        setButtonContents(inventoryEquipmentButtons.get(0), weaponSlot);
                        ArmorItem armorSlot = localPlayerToken.getInventory().getArmorSlot();
                        setButtonContents(inventoryEquipmentButtons.get(1), armorSlot);
                        RingItem ringSlot = localPlayerToken.getInventory().getRingSlot();
                        setButtonContents(inventoryEquipmentButtons.get(2), ringSlot);

                        for (int i = 0; i < numQuickSlots; i++) {
                                QuickItem quick = localPlayerToken.getInventory().getQuickSlot(i);
                                setButtonContents(inventoryEquipmentButtons.get(i + 3), quick);
                        }

                        //Gdx.app.log("HudSpatial", "refresh inventory window");
                        int buttonI = 0;
                        for (int i = 0; i < localPlayerToken.getInventory().size(); i++) {
                                Item item = localPlayerToken.getInventory().get(i);
                                if (localPlayerToken.getInventory().isEquipped(item))
                                        continue;
                                setButtonContents(inventoryBackPackButtons.get(buttonI), item);
                                buttonI++;
                        }

                        for (int i = buttonI; i < inventoryBackPackButtons.size; i++) {
                                setButtonContents(inventoryBackPackButtons.get(i), null);
                        }


                }


        }

        private void setButtonContents(Button button, Item item) {
                if (item == null) {
                        if (button.getUserObject() != null) {
                                button.clearChildren();
                                button.add("");

                        }
                        //button.setDisabled(true);
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
                                                        Gdx.app.log("HudSpatial", "update button contents: " + item + " charges: " + ((StackableItem) item).getCharges());
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
                // TODO: when unable to equip/unequp/discard items due to curse or attack timer
                // there should be an error dialog that pops up with the relevant message
                // instead of just disabling the button

                if (item instanceof EquipmentItem || item instanceof QuickItem) {
                        if (!localPlayerToken.getInventory().canChangeEquipment() || (item instanceof EquipmentItem && ((EquipmentItem) item).isCursed())) {
                                useLabel.setText("");
                                discardLabel.setText("");
                                itemWindowUseButton.setDisabled(true);
                                itemWindowDiscardButton.setDisabled(true);
                        } else if (localPlayerToken.getInventory().isEquipped(item)) {
                                useLabel.setText("Unequip");
                        } else {
                                useLabel.setText("Equip");
                        }
                } else if (item instanceof ConsumableItem) {
                        if (item instanceof BookItem) {
                                useLabel.setText("Read");
                        } else {
                                useLabel.setText("Use");
                        }

                }


        }

        private void setInventoryWindowVisible(boolean visible) {

                setHudElementsVisible(!visible);
                if (visible) {

                        if (inventoryWindow.getParent() == null) {
                                world.stage.addActor(inventoryWindowBackgroundImage);
                                world.stage.addActor(inventoryWindow);
                        }

                        refreshInventoryElements();
                        world.setPaused(true);
                } else {
                        inventoryWindowBackgroundImage.remove();
                        inventoryWindow.remove();
                        itemWindow.remove();
                        world.setPaused(false);
                }
        }

        private void setItemWindowVisible(boolean visible) {

                if (visible) {
                        if (inventoryWindow.getParent() == null)
                                world.stage.addActor(inventoryWindow);
                        if (itemWindow.getParent() == null)
                                world.stage.addActor(itemWindow);
                        world.setPaused(true);
                } else {
                        itemWindow.remove();
                        world.setPaused(false);
                }
        }

        private void setChatWindowContents(InteractChat chat){
                chatLabel.setText(chat.getMessage(localPlayerToken.getInteractor()));
                String[] choices = chat.getChoices(localPlayerToken.getInteractor());
                for(int i=0; i < 4; i++){
                        Button button = chatChoiceButtons.get(i);
                        if(i < choices.length && choices[i] != null){
                                button.clearChildren();
                                button.add(choices[i]);
                                if(button.getParent() == null)
                                        buttonGroup.addActor(button);
                        }else{
                                button.remove();
                        }
                }
        }

        private void setChatWindowVisible(boolean visible){
                if(tokenSelectMode){
                        /* actually staying in token select mode might not be an issue..
                        if(!tokenSelectForItem.isIdentified(localPlayerToken)){
                                localPlayerToken.getCommand().consumeItem(tokenSelectForItem);
                        }
                        setTokenSelectMode(null);
                        */
                }
                mouseDownDrag = false;
                setHudElementsVisible(!visible);
                if(visible){
                        if(chatWindow.getParent() == null)
                                world.stage.addActor(chatWindow);
                        world.setPaused(true);
                }else{
                        chatWindow.remove();
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
                        BookItem book = (BookItem) item;
                        if (book.getType() == BookItem.Type.MagicMapping) {
                                appendToGameLog("The layout of this floor has become revealed to you.");
                        } else {
                                appendToGameLog("You just read " + item.getName());
                        }

                        if (out.targetItem != null) {
                                // immediatly bring up the inventory window so the user can see the result of using the book on this item
                                this.setInventoryWindowVisible(true);
                        }
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
                        if (study)
                                this.appendToGameLog("You have identified " + identifiedItem.getName());
                }


        }

        @Override
        public void onInteract(Interact interact) {
                if(interact instanceof InteractChat){
                        this.setChatWindowVisible(true);
                        this.setChatWindowContents((InteractChat) interact);
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

        protected void setMapViewMode(boolean mapViewMode) {
                if (this.mapViewMode == mapViewMode)
                        return;
                this.mapViewMode = mapViewMode;
                //setHudElementsVisible(!this.mapViewMode);
                world.camControl.setChasing(!this.mapViewMode);
                if (this.mapViewMode) {

                } else {
                        world.camControl.setZoom(1);
                }

                for (Button quickButton : quickButtons) {
                        quickButton.setVisible(!mapViewMode);
                }
        }

        protected boolean isMapViewMode() {
                return mapViewMode;
        }

        private void setTokenSelectMode(ConsumableItem.TargetsTokens item) {
                tokenSelectForItem = item;
                tokenSelectMode = tokenSelectForItem != null;

                if (tokenSelectMode) {
                        if (tokenSelectForItem.isPrimarilySelfConsume()) {
                                localPlayerToken.getCommand().consumeItem(tokenSelectForItem);
                                tokenSelectMode = false;
                                tokenSelectForItem = null;
                        } else {
                                if (!tokenSelectForItem.isIdentified(localPlayerToken)) {
                                        // if not identified, then using the scroll can not be cancelled, dont change text
                                } else {
                                        // TODO: change text to "cancel"
                                }
                        }
                } else {

                }
        }

        private void setItemSelectMode(ConsumableItem.TargetsItems item) {
                itemSelectForItem = item;

                itemSelectMode = itemSelectForItem != null;

                if (itemSelectMode) {
                        if (item.isPrimarilySelfConsume()) {
                                boolean valid = localPlayerToken.getCommand().consumeItem(itemSelectForItem);
                                itemSelectForItem = null;
                                itemSelectMode = false;
                                if (valid)
                                        this.setInventoryWindowVisible(false);
                                return;
                        }


                        int numValid = 0;
                        SnapshotArray<Actor> equipmentChildren = equipmentTable.getChildren();
                        SnapshotArray<Actor> backpackChildren = backPackTable.getChildren();
                        int i = 0;
                        int n = equipmentChildren.size;
                        boolean p = true;
                        // I call this the frankenloop, it iterates over both equipment and backpack buttons to check if they can be used with the user selected item
                        while (i < n || p) {
                                Actor actor = p ? equipmentChildren.get(i) : backpackChildren.get(i);
                                if (actor instanceof Button) {
                                        Button button = (Button) actor;
                                        Object uo = button.getUserObject();
                                        boolean validItem;
                                        if (uo instanceof Item) {
                                                Item targetItem = (Item) uo;
                                                validItem = itemSelectForItem.canConsume(localPlayerToken, targetItem);
                                        } else {
                                                validItem = false;
                                        }
                                        if (validItem) numValid++;
                                        button.setVisible(validItem);
                                        button.setDisabled(!validItem);
                                }
                                i++;
                                if (i >= n && p) {
                                        n = backpackChildren.size;
                                        i = 0;
                                        p = false;
                                }

                        }

                        SnapshotArray<Actor> avatarWindowChildren = inventoryWindow.getChildren();
                        for (Actor avatarWindowChild : avatarWindowChildren) {
                                String uo = String.valueOf(avatarWindowChild.getUserObject());
                                if (uo.equals("Close")) {

                                        avatarWindowChild.setVisible(false);
                                }
                        }


                        if (numValid == 0 && !itemSelectForItem.isIdentified(localPlayerToken)) {
                                //no valid items to target, if item is not identified force targeting on itself
                                // because unidentified items can not be cancelled

                                localPlayerToken.getCommand().consumeItem(itemSelectForItem);
                                setItemSelectMode(null);
                        } else {
                                setItemWindowVisible(false);
                                // but keep inventory window open obviously
                        }
                } else {
                        // turn off item select mode
                        SnapshotArray<Actor> equipmentChildren = equipmentTable.getChildren();
                        SnapshotArray<Actor> backpackChildren = backPackTable.getChildren();
                        int i = 0;
                        int n = equipmentChildren.size;
                        boolean p = true;
                        // the return of the frrrannkeeenloooop
                        while (i < n || p) {
                                Actor actor = p ? equipmentChildren.get(i) : backpackChildren.get(i);
                                if (actor instanceof Button) {
                                        Button button = (Button) actor;
                                        button.setVisible(true);
                                        button.setDisabled(false);
                                }
                                i++;
                                if (i >= n && p) {
                                        n = backpackChildren.size;
                                        i = 0;
                                        p = false;
                                }

                        }

                        SnapshotArray<Actor> avatarWindowChildren = inventoryWindow.getChildren();
                        for (Actor avatarWindowChild : avatarWindowChildren) {
                                String uo = String.valueOf(avatarWindowChild.getUserObject());
                                if (uo.equals("Close")) {

                                        avatarWindowChild.setVisible(true);
                                }
                        }
                        this.setInventoryWindowVisible(false);
                }

                if (itemSelectMode) {
                        inputModeLabel.setText("Choose item to use with " + itemSelectForItem.getNameFromJournal(localPlayerToken) + "     ");
                        if (itemSelectForItem.isIdentified(localPlayerToken)) {
                                if (inputModeCancelButton.getParent() == null)
                                        inputModeHorizontalGroup.addActor(inputModeCancelButton);
                        } else
                                inputModeCancelButton.remove();
                } else {
                        inputModeLabel.setText("Inventory and Stats");
                        inputModeCancelButton.remove();
                }
        }

        /**
         * called before dungeon.update() to do user input related updates
         *
         * @param delta
         */
        private float lastTouchTimer = 0;

        protected void updateInput(float delta) {
                if (mapViewMode) {
                        // end map view mode by double tapping
                        if (Gdx.input.justTouched()) {
                                if (lastTouchTimer < 0.54f) {
                                        setMapViewMode(false);
                                } else {
                                        lastTouchTimer = 0;
                                }
                        }
                        lastTouchTimer += delta;
                } else if (tokenSelectMode) {
                        if (localPlayerToken.getDamage().isDead()) {
                                setTokenSelectMode(null);
                                return;
                        }
                        // if item is not identified and it cant be used, then force it to self consume since it cant be cancelled
                        if (!tokenSelectForItem.isIdentified(localPlayerToken)) {
                                Array<Token> targetableTokens = localPlayerToken.getFloorMap().getTargetableTokens(localPlayerToken, tokenSelectForItem);
                                if (targetableTokens.size == 0) {
                                        localPlayerToken.getCommand().consumeItem(tokenSelectForItem);
                                        setTokenSelectMode(null);
                                }
                        }
                } else if (itemSelectMode) {


                } else if (mouseDownDrag) {
                        Ray ray = world.cam.getPickRay(Gdx.input.getX(), Gdx.input.getY());
                        dragCommand(ray);
                }
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {

                if (world.isPaused()) return false;
                if (mapViewMode) {
                        return true;
                } else if (tokenSelectMode) {
                        boolean hasTarget = tokenSelectCommand(screenX, screenY);
                        if (hasTarget) {
                                tokenSelectMode = false;
                                tokenSelectForItem = null;
                        }
                }else if(gameOverCountdown < 0){
                        world.dungeonApp.setAppGameOver();
                } else{
                        boolean hasTarget = touchCommand(screenX, screenY);
                        if (!hasTarget) {
                                mouseDownDrag = true;
                        }
                }


                return true;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (world.isPaused()) return false;
                if (mapViewMode) {
                        world.camControl.drag(screenX, screenY);
                        return true;
                } else if (mouseDownDrag) {
                        return true;
                }
                return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (inventoryWindow.getParent() != null) {
                        float clickX = screenX;
                        float clickY = screenY;
                        if (clickX < inventoryWindow.getX() || clickY < inventoryWindow.getY() || clickX > inventoryWindow.getX() + inventoryWindow.getWidth() || clickY > inventoryWindow.getY() + inventoryWindow.getHeight()) {
                                setInventoryWindowVisible(false);
                                return true;
                        }
                }else if(chatWindow.getParent() != null){
                        float clickX = screenX;
                        float clickY = screenY;
                        if (clickX < chatWindow.getX() || clickY < chatWindow.getY() || clickX > chatWindow.getX() + chatWindow.getWidth() || clickY > chatWindow.getY() + chatWindow.getHeight()) {
                                setChatWindowVisible(false);
                                return true;
                        }
                }
                if (world.isPaused()) return false;
                if (mapViewMode) {
                        world.camControl.drag(-1, -1);
                        return true;
                } else if (mouseDownDrag) {
                        mouseDownDrag = false;
                        return true;
                }
                return false;
        }

        @Override
        public boolean scrolled(int amount) {
                if (world.isPaused()) return false;
                if (!tokenSelectMode && !itemSelectMode) {
                        world.camControl.setZoom(world.camControl.getZoom() - amount * .1f);
                        setMapViewMode(world.camControl.getZoom() < 1);
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
                if (localPlayerToken.getDamage().isDead()) return false;
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
                if (localPlayerToken.getDamage().isDead()) return false;
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
                        if (!tokenSelectMode && !itemSelectMode && event instanceof InputEvent) {
                                InputEvent inputEvent = (InputEvent) event;
                                if (inputEvent.getType() == InputEvent.Type.touchDown) { // ideally this would be on touchUp, but soemthing with how modal windows work prevents me from using touchUp
                                        float clickX = inputEvent.getStageX();
                                        float clickY = inputEvent.getStageY();
                                        if (clickX < itemWindow.getX() || clickY < itemWindow.getY() || clickX > itemWindow.getX() + itemWindow.getWidth() || clickY > itemWindow.getY() + itemWindow.getHeight()) {
                                                setItemWindowVisible(false);
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
                        if (!tokenSelectMode)
                                setInventoryWindowVisible(true);
                } else if (event.getListenerActor() == itemWindowUseButton) {
                        Object object = itemWindow.getUserObject();

                        if (object instanceof EquipmentItem) {
                                EquipmentItem item = (EquipmentItem) object;
                                if (localPlayerToken.getInventory().isEquipped(item)) {
                                        localPlayerToken.getInventory().unequip(item);
                                } else {
                                        boolean valid = localPlayerToken.getInventory().equip(item);
                                        if (valid && item.isCursed())
                                                appendToGameLog("You grip the " + item.getNameFromJournal(localPlayerToken) + " tightly. You are powerless to remove it.");

                                }
                                setItemWindowVisible(false);
                        } else if (object instanceof QuickItem) {
                                QuickItem item = (QuickItem) object;
                                if (localPlayerToken.getInventory().isEquipped(item)) {
                                        localPlayerToken.getInventory().unequip(item);
                                } else {
                                        localPlayerToken.getInventory().equip(item);
                                }
                                setItemWindowVisible(false);
                        } else if (object instanceof ConsumableItem.TargetsItems) {
                                this.setItemSelectMode((ConsumableItem.TargetsItems) object);
                        } else {
                                throw new AssertionError(object);
                        }


                } else if (event.getListenerActor() == itemWindowDiscardButton) {
                        Item item = (Item) itemWindow.getUserObject();
                        boolean valid = localPlayerToken.getInventory().discard(item);
                        if (valid)
                                setItemWindowVisible(false);
                } else if (event.getListenerActor() == itemWindowBackButton) {
                        setItemWindowVisible(false);
                } else if (event.getListenerActor() == inputModeCancelButton) {
                        if (itemSelectMode) {
                                setItemSelectMode(null);
                                this.setInventoryWindowVisible(true);
                        }
                }else if (event.getListenerActor() instanceof Button) {
                        // either an chat choice button, inventory screen button, or quick slot button
                        Button button = (Button) event.getListenerActor();
                        Object uo = event.getListenerActor().getUserObject();

                        if(chatChoiceButtons.contains(button, true)){
                                Gdx.app.log("HudSpatial","handle: "+ uo);
                                // chat choice button
                                InteractChat chat = (InteractChat)chatWindow.getUserObject();
                                Integer choiceIndex = (Integer)event.getListenerActor().getUserObject();
                                localPlayerToken.getCommand().setChatChoice(choiceIndex);
                                setChatWindowVisible(false);
                        }else if (inventoryEquipmentButtons.contains(button, true) || inventoryBackPackButtons.contains(button, true)) {
                                // inventory screen button
                                if (uo instanceof Item) {
                                        if (!itemSelectMode) {
                                                setItemWindowContents((Item) uo);
                                                setItemWindowVisible(true);
                                        } else {
                                                localPlayerToken.getCommand().consumeItem(itemSelectForItem, (Item) uo);
                                                setItemSelectMode(null);

                                        }
                                }
                        } else {
                                // quick slot button press
                                if (uo instanceof QuickItem) {
                                        if (tokenSelectMode) {
                                                if (tokenSelectForItem.isIdentified(localPlayerToken)) {
                                                        // can only cancel identified items
                                                        setTokenSelectMode(null);
                                                }
                                                return false;
                                        } else if (uo instanceof ConsumableItem.TargetsTokens) {
                                                setTokenSelectMode((ConsumableItem.TargetsTokens) uo);
                                        } else if (uo instanceof ConsumableItem) {
                                                localPlayerToken.getCommand().consumeItem((ConsumableItem) uo);
                                        } else {
                                                throw new AssertionError(uo);
                                        }
                                } else if (uo == null) {
                                        setInventoryWindowVisible(true);
                                }
                        }

                }
                return false;
        }

        private class DamageLabel {
                Label label;
                float ttl;
                TokenSpatial tokenSpatial;


        }
}
