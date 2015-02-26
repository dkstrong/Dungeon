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
import asf.dungeon.model.item.WeaponItem;
import asf.dungeon.model.token.Attack;
import asf.dungeon.model.token.CharacterInventory;
import asf.dungeon.model.token.StatusEffect;
import asf.dungeon.model.token.StatusEffects;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.logic.fsm.FsmLogic;
import asf.dungeon.model.token.logic.fsm.State;
import asf.dungeon.model.token.puzzle.CombinationDoorPuzzle;
import asf.dungeon.model.token.quest.Choice;
import asf.dungeon.model.token.quest.Dialouge;
import asf.dungeon.model.token.quest.Quest;
import asf.dungeon.utility.UtMath;
import asf.dungeon.view.token.AbstractTokenSpatial;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.StringBuilder;

import java.util.Arrays;


/**
 * Created by Danny on 11/1/2014.
 */
public class HudSpatial implements Spatial, EventListener, InputProcessor, Token.Listener {
        private boolean initialized = false;
        protected DungeonWorld world;
        public Token localPlayerToken;

        // TODO: these settings should be stored on an app level object so it can be easily changed in the settings
        private boolean showRenderingStasLabel = true;

        private Skin skin;
        // in game hud
        private Label gameLogLabel, targetInfoLabel;
        private float gameLogDisplayCountdown = Float.NaN;
        private float gameOverCountdown = .75f;
        private Button inventoryButton;
        private ItemButtonStack[] quickButtons;
        private ProgressBar healthProgressBar, experienceProgressBar, targetHealthProgressBar;
        private Label avatarInfoLabel;
        private HorizontalGroup avatarStatusEffectsGroup;
        private Container[] statusEffectImage;
        private HorizontalGroup keyIconsGroup;
        private Container[] keyIconImage;
        private Label renderingStats;
        private final Array<DamageLabel> damageInfoLabels = new Array<DamageLabel>(false, 8, DamageLabel.class);
        private final Decal moveCommandDecal = new Decal();
        private final Decal targetCommandDecal = new Decal();
        //inventory hud
        private Table inventoryWindow;
        private Label inputModeLabel;
        private Button inventoryCloseButton;
        private Table backPackTable;
        private final Array<ItemButtonStack> inventoryBackPackButtons = new Array<ItemButtonStack>(true, 16, ItemButtonStack.class);
        private Window itemWindow;
        private ScrollPane itemWindowScrollPane;
        private Label itemWindowLabel;
        private ItemButtonStack itemWindowThumbnailButton;
        private Button itemWindowUseButton, itemWindowDiscardButton, itemWindowBackButton;
        //chat hud
        private Table chatWindow;
        private Image chatImage;
        private ScrollPane chatLabelScrollPane;
        private Label chatTitleLabel, chatLabel;
        private VerticalGroup buttonGroup;
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

        }

        @Override
        public void init(AssetManager assetManager) {
                initialized = true;
                if (localPlayerToken == null) {
                        return;
                }
                skin = world.dungeonApp.skin;

                inventoryButton = new Button(skin);
                world.stage.addActor(inventoryButton);
                inventoryButton.add(new Label("Inventory", skin));
                inventoryButton.addCaptureListener(this);

                healthProgressBar = new ProgressBar(0, localPlayerToken.damage.getMaxHealth(), 1, false, skin, "default");
                healthProgressBar.setValue(localPlayerToken.damage.getHealth());
                healthProgressBar.setAnimateInterpolation(Interpolation.linear);
                healthProgressBar.setAnimateDuration(1f);
                world.stage.addActor(healthProgressBar);

                experienceProgressBar = new ProgressBar(
                        localPlayerToken.experience.getXpAtStartOfLevel(),
                        localPlayerToken.experience.getRequiredXpToLevelUp(),1,false,skin,"yellow");
                experienceProgressBar.setValue(localPlayerToken.experience.getXp());
                experienceProgressBar.setAnimateInterpolation(Interpolation.linear);
                experienceProgressBar.setAnimateDuration(.5f);
                world.stage.addActor(experienceProgressBar);

                targetHealthProgressBar = new ProgressBar(0,10,1,false, skin,"default");
                targetHealthProgressBar.setValue(10);
                targetHealthProgressBar.setAnimateInterpolation(Interpolation.linear);
                targetHealthProgressBar.setAnimateDuration(1f);

                avatarInfoLabel = new Label("Knight\nLevel 1\nXP 25/100", skin);
                avatarInfoLabel.setAlignment(Align.topLeft);
                world.stage.addActor(avatarInfoLabel);

                avatarStatusEffectsGroup = new HorizontalGroup();
                world.stage.addActor(avatarStatusEffectsGroup);
                avatarStatusEffectsGroup.align(Align.bottomLeft);

                StatusEffect[] values = StatusEffects.effectValues;
                statusEffectImage = new Container[values.length];
                for (StatusEffect value : values) {
                        int i = value.ordinal();
                        statusEffectImage[i] = new Container<Actor>(new Image(
                                new TextureRegionDrawable(world.pack.findRegion(world.assetMappings.getHudStatusEffectIcon(value))),
                                Scaling.fit, Align.center
                        ));
                }


                keyIconsGroup = new HorizontalGroup();
                keyIconsGroup.align(Align.topRight);
                keyIconsGroup.reverse();
                world.stage.addActor(keyIconsGroup);

                KeyItem.Type[] keyTypes = KeyItem.typeValues;
                keyIconImage = new Container[keyTypes.length];
                for (KeyItem.Type keyType : keyTypes) {
                        int i = keyType.ordinal();
                        keyIconImage[i] = new Container<Actor>(new Image(
                                new TextureRegionDrawable(world.pack.findRegion(world.assetMappings.getKeyIcon(keyType))),
                                Scaling.fit, Align.center
                        ));
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

                int numQuickSlots = localPlayerToken.inventory.numQuickSlots();
                quickButtons = new ItemButtonStack[numQuickSlots];
                for (int i = 0; i < numQuickSlots; i++) {
                        ItemButtonStack quickButton = new ItemButtonStack(skin, this, null);
                        quickButton.setQuickSlot(true);
                        world.stage.addActor(quickButton);
                        quickButtons[i] = quickButton;
                }


                inventoryWindow = new Table(skin);
                inventoryWindow.setBackground("default-rect");
                //inventoryWindow.setMovable(false);
                inventoryWindow.addCaptureListener(this);
                //inventoryWindow.removeActor(inventoryWindow.getButtonTable());
                backPackTable = new Table(skin);
                //inventoryWindow.debug();
                //world.stage.addActor(inventoryWindow);
                //inventoryWindow.debugAll();
                {

                        inputModeLabel = new Label("Inventory", skin);

                        Container<Label> inputModeContainer = new Container<Label>(inputModeLabel);
                        inputModeContainer.align(Align.bottom);

                        inventoryCloseButton = new Button(skin);
                        inventoryCloseButton.add("Close");
                        inventoryCloseButton.addCaptureListener(this);
                        Container<Button> closeContainer = new Container<Button>(inventoryCloseButton);
                        closeContainer.align(Align.topRight);

                        Stack header = new Stack();
                        header.setTouchable(Touchable.childrenOnly);
                        header.add(inputModeContainer);
                        header.add(closeContainer);
                        inventoryWindow.add(header).fill().padBottom(0).spaceBottom(0);

                        inventoryWindow.row().spaceTop(0).padTop(0).padBottom(10);
                        inventoryWindow.add(backPackTable).fill().expand();
                }

                for (int j = 0; j < 4; j++) {
                        backPackTable.row();
                        for (int i = 0; i < 4; i++) {
                                ItemButtonStack equipmentButton = new ItemButtonStack(skin, this, null);
                                backPackTable.add(equipmentButton);
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
                        itemWindow.row();

                        itemWindowThumbnailButton = new ItemButtonStack(skin, this, null);
                        itemWindowThumbnailButton.setTouchable(Touchable.disabled);

                        itemWindowLabel = new Label("Some information about this item.", skin);
                        itemWindowLabel.setWrap(true);
                        itemWindowLabel.setFillParent(true);
                        itemWindowScrollPane = new ScrollPane(itemWindowLabel, skin);
                        itemWindowScrollPane.setForceScroll(false,false);
                        itemWindowScrollPane.setFadeScrollBars(true);

                        itemWindow.add(itemWindowThumbnailButton).colspan(1);
                        itemWindow.add(itemWindowScrollPane).colspan(2).fill().expand();

                        itemWindow.row();

                        itemWindowUseButton = new Button(skin);
                        itemWindowUseButton.add("Use");
                        itemWindowUseButton.addCaptureListener(this);

                        itemWindowDiscardButton = new Button(skin);
                        itemWindowDiscardButton.add("Discard");
                        itemWindowDiscardButton.addCaptureListener(this);

                        itemWindowBackButton = new Button(skin);
                        itemWindowBackButton.add("Back");
                        itemWindowBackButton.addCaptureListener(this);

                        Table buttonsTable = new Table(skin);
                        buttonsTable.add(itemWindowUseButton);
                        buttonsTable.add(itemWindowDiscardButton);
                        buttonsTable.add(itemWindowBackButton);
                        itemWindow.add(buttonsTable).colspan(3);


                }

                chatWindow = new Table(skin);
                chatWindow.setBackground("default-rect");
                {
                        chatTitleLabel = new Label("Chat Label", skin);
                        chatImage = new Image(new TextureRegionDrawable(), Scaling.fillY, Align.center);

                        Table chatImageTable = new Table(skin);
                        chatImageTable.add(chatTitleLabel);
                        chatImageTable.row();
                        chatImageTable.add(chatImage);

                        chatLabel = new Label("Chat Label", skin);
                        chatLabel.setWrap(true);
                        chatLabelScrollPane = new ScrollPane(chatLabel, skin);

                        chatWindow.row();
                        chatWindow.add(chatImageTable);
                        chatWindow.add(chatLabelScrollPane).fill().expand();

                        buttonGroup = new VerticalGroup();
                        chatWindow.row();
                        chatWindow.add(buttonGroup).colspan(2);

                        for (int i = 0; i < 4; i++) {
                                Button button = new Button(skin);
                                button.addCaptureListener(this);
                                chatChoiceButtons.add(button);
                        }

                }

                resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

                refreshInventoryElements();

                moveCommandDecal.setTextureRegion(world.pack.findRegion("Textures/MoveCommandMarker"));
                moveCommandDecal.setBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                moveCommandDecal.setDimensions(world.floorSpatial.tileDimensions.x, world.floorSpatial.tileDimensions.z);
                moveCommandDecal.setColor(1, 1, 1, 1);
                moveCommandDecal.rotateX(-90);

                targetCommandDecal.setTextureRegion(world.pack.findRegion("Textures/TargetCommandMarker"));
                targetCommandDecal.setBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                targetCommandDecal.setDimensions(world.floorSpatial.tileDimensions.x * 1.1f, world.floorSpatial.tileDimensions.z * 1.1f);
                targetCommandDecal.setColor(1, 1, 1, 1);
                targetCommandDecal.rotateX(-90);

        }

        protected void resize(final int graphicsWidth, final int graphicsHeight) {
                if (inventoryButton == null)
                        return;

                final float margin = 15;
                final float buttonSize = graphicsWidth > graphicsHeight ? 125f : (graphicsWidth-margin*5f) / 4f;


                inventoryButton.setBounds(margin, margin, buttonSize, buttonSize);

                final float progressBarHeight = buttonSize * 0.333f;
                healthProgressBar.setBounds(
                        0,
                        graphicsHeight-progressBarHeight/2f,
                        graphicsWidth,
                        progressBarHeight);

                experienceProgressBar.setBounds(
                        0,
                        -progressBarHeight/2f,
                        graphicsWidth,
                        progressBarHeight);

                avatarInfoLabel.setBounds(
                        margin * 0.5f,
                        graphicsHeight - buttonSize - margin * 0.75f,
                        buttonSize, buttonSize);


                final float effectIconSize = buttonSize * 0.27f;
                avatarStatusEffectsGroup.setBounds(
                        margin,
                        graphicsHeight- buttonSize*0.7f-margin,
                        graphicsWidth,
                        effectIconSize);

                for (Container container : statusEffectImage) {
                        container.pad(0,0, 0, effectIconSize*.25f).prefSize(effectIconSize, effectIconSize).maxSize(effectIconSize,effectIconSize).minSize(effectIconSize,effectIconSize).fill();
                }

                float keyIconSize = buttonSize*.75f;
                keyIconsGroup.setBounds(
                        -margin * 0.5f,
                        graphicsHeight - keyIconSize * 0.85f,
                        graphicsWidth,
                        keyIconSize);

                for (Container container : keyIconImage) {
                        container.prefSize(keyIconSize,keyIconSize).maxSize(keyIconSize,keyIconSize).minSize(keyIconSize,keyIconSize).fill();
                }

                if (renderingStats != null)
                        renderingStats.setBounds(
                                graphicsWidth - buttonSize - margin,
                                graphicsHeight - buttonSize*1.5f - margin,
                                buttonSize, buttonSize);


                gameLogLabel.setBounds(
                        margin,
                        margin + buttonSize,
                        graphicsWidth * .5f,
                        buttonSize);

                targetInfoLabel.setBounds(
                        graphicsWidth * .25f,
                        graphicsHeight - margin - buttonSize*.75f ,
                        graphicsWidth * .5f,
                        buttonSize * .25f);

                final float targetHealthBarWidth = graphicsWidth * .333f;
                final float targetHealthBarHeight = progressBarHeight*0.5f;
                targetHealthProgressBar.setBounds(
                        (graphicsWidth - targetHealthBarWidth)*0.5f,
                        graphicsHeight - margin - buttonSize*.75f -targetHealthBarHeight,
                        targetHealthBarWidth,
                        targetHealthBarHeight
                );

                float invWindowHeight,invWindowButtonSize,invWindowWidth,invWindowCloseButtonSize;
                if (graphicsWidth > graphicsHeight) {
                        invWindowHeight = graphicsHeight * .8f;
                        invWindowButtonSize = invWindowHeight * (1 / 4.75f);
                        invWindowWidth = invWindowButtonSize * 4.4f;
                        invWindowCloseButtonSize = invWindowButtonSize * .5f;
                }else{
                        invWindowWidth = graphicsWidth -30;
                        invWindowButtonSize = invWindowWidth * (1 / 4f);
                        invWindowHeight = invWindowButtonSize * 4.75f;
                        invWindowCloseButtonSize = invWindowButtonSize * .5f;
                }

                inventoryWindow.setBounds(
                        (graphicsWidth - invWindowWidth) * .5f,
                        (graphicsHeight - invWindowHeight) * .5f,
                        invWindowWidth,
                        invWindowHeight);

                Container<Button> closeContainer = ((Container<Button>) inventoryCloseButton.getParent());
                closeContainer.prefSize(invWindowCloseButtonSize, invWindowCloseButtonSize * .5f).
                        pad(invWindowCloseButtonSize * .15f, 0, 0, invWindowCloseButtonSize * .15f);


                for (Cell cell : backPackTable.getCells()) {
                        cell.prefSize(invWindowButtonSize, invWindowButtonSize).minSize(invWindowButtonSize, invWindowButtonSize);
                }

                final float quickXOffset = buttonSize + margin;
                for (int i = 0; i < quickButtons.length; i++) {
                        quickButtons[i].setBounds(graphicsWidth - (quickXOffset * (i + 1)),
                                margin,
                                buttonSize,
                                buttonSize);
                }

                float itemWindowHeight, itemWindowWidth,itemWindowButtonSizeX,itemWindowButtonSizeY;
                if(graphicsWidth > graphicsHeight){
                        itemWindowHeight = graphicsHeight * .5f;
                        itemWindowWidth = itemWindowHeight * 1.75f;
                        itemWindowButtonSizeX = invWindowButtonSize * 1.25f;
                        itemWindowButtonSizeY = invWindowButtonSize * .75f;
                }else{
                        itemWindowWidth = invWindowWidth * .95f;
                        itemWindowHeight = invWindowHeight * .65f;
                        itemWindowButtonSizeX = itemWindowWidth * (1/4f);
                        itemWindowButtonSizeY = itemWindowButtonSizeX * .5f;
                }
                float itemThumbnailSize = invWindowButtonSize * 1.5f;

                itemWindow.setBounds(
                        (graphicsWidth - itemWindowWidth) * .5f,
                        (graphicsHeight - itemWindowHeight) * .5f,
                        itemWindowWidth,
                        itemWindowHeight);

                for (Cell cell : itemWindow.getCells()) {
                        if (cell.getActor() == itemWindowThumbnailButton) {
                                cell.prefSize(itemThumbnailSize, itemThumbnailSize).maxSize(itemThumbnailSize, itemThumbnailSize).minSize(itemThumbnailSize, itemThumbnailSize).
                                        padLeft(20);
                        }else if (cell.getActor() == itemWindowScrollPane) {
                                cell.prefHeight(itemThumbnailSize).maxHeight(itemThumbnailSize).padRight(20);
                        }else if(cell.getActor() instanceof Table){
                                Table buttonsTable = (Table) cell.getActor();
                                buttonsTable.getCells().get(0).prefSize(itemWindowButtonSizeX, itemWindowButtonSizeY).minSize(itemWindowButtonSizeX, itemWindowButtonSizeY);
                                buttonsTable.getCells().get(1).prefSize(itemWindowButtonSizeX, itemWindowButtonSizeY).minSize(itemWindowButtonSizeX, itemWindowButtonSizeY).pad(0,20,0,20);
                                buttonsTable.getCells().get(2).prefSize(itemWindowButtonSizeX, itemWindowButtonSizeY).minSize(itemWindowButtonSizeX, itemWindowButtonSizeY);
                                cell.padBottom(30);
                        }
                }

                resizeChatWindowElements(graphicsWidth, graphicsHeight);

        }

        private void resizeChatWindowElements(int graphicsWidth, int graphicsHeight) {
                if (chatWindow.getParent() == null)
                        return;
                Dialouge dialouge = (Dialouge) chatWindow.getUserObject();
                Choice[] choices = dialouge.getChoices(localPlayerToken.interactor);
                int longestButtonText = 0;
                int buttonI = 0;
                for (Choice choice : choices) {
                        if (choice != null) {
                                longestButtonText = UtMath.largest(longestButtonText, choice.getText().length());
                                Button button = chatChoiceButtons.get(buttonI);
                                button.pad(15, 10, 15, 10);
                                button.clearChildren();
                                button.add(choice.getText());
                                button.setUserObject(choice);
                                if (button.getParent() == null)
                                        buttonGroup.addActor(button);
                                buttonI++;
                        }
                }

                for (; buttonI < 4; buttonI++) {
                        Button button = chatChoiceButtons.get(buttonI);
                        button.setUserObject(null);
                        button.remove();
                }

                chatTitleLabel.setText(localPlayerToken.interactor.chattingWith.name);
                ((TextureRegionDrawable)(chatImage.getDrawable())).setRegion(world.pack.findRegion(world.assetMappings.getInventoryItemTextureAssetLocation(null)));
                chatLabel.setText(dialouge.getMessage(localPlayerToken.interactor));
                float chatWindowWidth = graphicsWidth * UtMath.scalarLimitsInterpolation(longestButtonText, 25f, 75f, .3f, .5f);
                if(chatWindowWidth < 500) chatWindowWidth = 500;
                float chatWindowHeight = chatWindowWidth;
                float chatImageSize = chatWindowWidth *.25f;

                chatWindow.setBounds(
                        (graphicsWidth - chatWindowWidth) * .5f,
                        (graphicsHeight - chatWindowHeight) * .5f,
                        chatWindowWidth,
                        chatWindowHeight);

                for (Cell cell : chatWindow.getCells()) {

                        if (cell.getActor() instanceof Table) {
                                Table chatImageTable = (Table) cell.getActor();
                                chatImageTable.getCells().get(1).prefSize(chatImageSize, chatImageSize).minSize(chatImageSize, chatImageSize).maxSize(chatImageSize,chatImageSize);
                                cell.padTop(20);
                        }else if (cell.getActor() instanceof ScrollPane) {
                                cell.prefSize(chatWindowWidth, chatWindowHeight).maxSize(chatWindowWidth, chatWindowHeight);
                                cell.padRight(20).padTop(20);
                        } else if (cell.getActor() instanceof VerticalGroup) {
                                //cell.prefSize(itemWindowButtonSizeX, itemWindowButtonSizeY).minSize(itemWindowButtonSizeX, itemWindowButtonSizeY);
                                cell.padBottom(20).padTop(20);
                        }
                }

                buttonGroup.space(15);

                chatLabelScrollPane.setScrollX(0);
                chatLabelScrollPane.setScrollY(0);


        }

        private Token lastTargetToken;
        private String getRuntimeMemInfo(){
                final Runtime rt = Runtime.getRuntime();
                final long max = rt.maxMemory();
                final long total = rt.totalMemory();
                final long free = rt.freeMemory();
                final long used = total - free;
                final int availableProcessors = rt.availableProcessors();
                return String.format("%s / %s / %s / %s",used/1024/1024,total/1024/1024,max/1024/1024,availableProcessors);
        }

        @Override
        public void update(float delta) {

                if (renderingStats != null) {
                        renderingStats.getText().setLength(0);
//                        Ray ray = world.cam.getPickRay(Gdx.input.getX(), Gdx.input.getY());
//                        Array<DungeonWorld.RaycastResult> intersectedTokens = world.getTokens(ray, null);
//                        for (DungeonWorld.RaycastResult intersectedToken : intersectedTokens) {
//                                renderingStats.getText().append(intersectedToken.token.name+" : "+intersectedToken.dist2+"\n");
//                        }

                        renderingStats.getText()
                                .append("FPS : ").append(Gdx.graphics.getFramesPerSecond())
                                .append("\nJava Heap : ").append(getRuntimeMemInfo())
                                .append("\nNative Heap : ").append(Gdx.app.getNativeHeap()/ 1024/1024);
                        renderingStats.invalidateHierarchy();
                }

                if (localPlayerToken != null) {
                        // TODO: this might be better to be done with events instead of polling
                        healthProgressBar.setRange(0, localPlayerToken.damage.getMaxHealth());
                        healthProgressBar.setValue(localPlayerToken.damage.getHealth());
                        healthProgressBar.act(delta);

                        experienceProgressBar.setRange(localPlayerToken.experience.getXpAtStartOfLevel(), localPlayerToken.experience.getRequiredXpToLevelUp());
                        experienceProgressBar.setValue(localPlayerToken.experience.getXp());
                        experienceProgressBar.act(delta);

                        StringBuilder sb = avatarInfoLabel.getText();
                        sb.setLength(0);
                        sb.append("Floor ").append(localPlayerToken.floorMap.index+1);
                        avatarInfoLabel.invalidateHierarchy();

                        if (localPlayerToken.damage.isDead()) {
                                targetInfoLabel.setText("GAME OVER!");
                                gameOverCountdown -= delta;
                                targetHealthProgressBar.remove();
                        } else if (tokenSelectMode) {
                                targetInfoLabel.setText("Choose target for " + tokenSelectForItem.getNameFromJournal(localPlayerToken));
                                targetHealthProgressBar.remove();
                        } else {
                                Token targetToken = localPlayerToken.command.getTargetToken();
                                if (targetToken == null) {
                                        targetInfoLabel.setText(null);
                                        targetHealthProgressBar.remove();
                                }else {
                                        targetInfoLabel.setText("Target: " + targetToken.name);
                                        if(targetToken.damage != null){
                                                world.stage.addActor(targetHealthProgressBar);
                                                targetHealthProgressBar.setRange(0,targetToken.damage.getMaxHealth());
                                                targetHealthProgressBar.setValue(targetToken.damage.getHealth());
                                                if(targetToken == lastTargetToken)
                                                        targetHealthProgressBar.act(delta);
                                                else
                                                        targetHealthProgressBar.act(1f);
                                                lastTargetToken = targetToken;
                                        }else{
                                                targetHealthProgressBar.remove();
                                        }

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
                                        label.tokenSpatial.token.move.getFloatLocation(),
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
                if (localPlayerToken != null) {
                        if (localPlayerToken.command.getTargetToken() != null) {
                                moveCommandDecalCount = 0;
                                float x = localPlayerToken.command.getTargetToken().getFloatLocationX();
                                float y = localPlayerToken.command.getTargetToken().getFloatLocationY();
                                targetCommandDecal.setPosition(world.getWorldCoords(x, y, targetCommandDecal.getPosition()));
                                targetCommandDecal.translateY(0.2f);
                                world.decalBatch.add(targetCommandDecal);
                        } else if (moveCommandDecalCount > 0) {
                                moveCommandDecalCount -= delta;
                                world.decalBatch.add(moveCommandDecal);
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

        private void spawnDamageInfoLabel(String text, Token token, Color color) {
                AbstractTokenSpatial tokenSpatial = world.getTokenSpatial(token);
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
                setChatWindowVisible(null);
        }

        public boolean isWindowVisible() {
                if (!isInitialized()) return false;
                return inventoryWindow.getParent() != null || itemWindow.getParent() != null || chatWindow.getParent() != null;
        }

        private void setHudElementsVisible(boolean visible) {
                inventoryButton.setVisible(visible);
                healthProgressBar.setVisible(visible);
                avatarInfoLabel.setVisible(visible);
                avatarStatusEffectsGroup.setVisible(visible);
                keyIconsGroup.setVisible(visible);
                gameLogLabel.setVisible(visible);
                targetInfoLabel.setVisible(visible);
                for (ItemButtonStack quickButton : quickButtons) {
                        quickButton.setVisible(visible && !mapViewMode);
                }
        }

        private void refreshInventoryElements() {
                //Gdx.app.log("HudSpatial", "refresh inventory elements");

                //
                // refresh key display
                for (KeyItem.Type typeValue : KeyItem.typeValues) {
                        if (localPlayerToken.inventory.containsKey(typeValue)) {
                                keyIconsGroup.addActor(keyIconImage[typeValue.ordinal()]);
                        } else {
                                keyIconImage[typeValue.ordinal()].remove();
                        }
                }

                int numQuickSlots = localPlayerToken.inventory.numQuickSlots();
                if (quickButtons.length != numQuickSlots) {
                        // ensure there are enough quick slot elements on the screen (on main hud)
                        int i = quickButtons.length;
                        quickButtons = Arrays.copyOf(quickButtons, numQuickSlots);
                        for (; i < numQuickSlots; i++) {
                                ItemButtonStack quickButton = new ItemButtonStack(skin, this, null);
                                quickButton.setQuickSlot(true);
                                world.stage.addActor(quickButton);
                                quickButtons[i] = quickButton;
                        }

                        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                }
                for (int i = 0; i < numQuickSlots; i++) {
                        quickButtons[i].setItem(localPlayerToken.inventory.getQuickSlot(i));
                }


                // if the inventory window is open, then refresh the inventory window
                if (backPackTable.getParent() != null) {
                        // update the equipped item buttons
                        int buttonI = 0;
                        WeaponItem weaponSlot = localPlayerToken.inventory.getWeaponSlot();
                        if (weaponSlot != null) inventoryBackPackButtons.get(buttonI++).setItem(weaponSlot);
                        ArmorItem armorSlot = localPlayerToken.inventory.getArmorSlot();
                        if (armorSlot != null) inventoryBackPackButtons.get(buttonI++).setItem(armorSlot);
                        RingItem ringSlot = localPlayerToken.inventory.getRingSlot();
                        if (ringSlot != null) inventoryBackPackButtons.get(buttonI++).setItem(ringSlot);
                        for (int i = 0; i < numQuickSlots; i++) {
                                QuickItem quickItem = localPlayerToken.inventory.getQuickSlot(i);
                                if (quickItem != null) inventoryBackPackButtons.get(buttonI++).setItem(quickItem);
                        }
                        //update the backpack item buttons
                        for (int i = 0; i < localPlayerToken.inventory.size(); i++) {
                                Item item = localPlayerToken.inventory.get(i);
                                if (localPlayerToken.inventory.isEquipped(item))
                                        continue;
                                inventoryBackPackButtons.get(buttonI++).setItem(item);
                        }

                        // remaining buttons get set to null
                        for (int i = buttonI; i < inventoryBackPackButtons.size; i++) {
                                inventoryBackPackButtons.get(i).setItem(null);
                        }


                }


        }

        private void setItemWindowContents(Item item) {
                itemWindow.setTitle(item.getNameFromJournal(localPlayerToken));
                itemWindow.setUserObject(item);
                itemWindowThumbnailButton.setItem(item);
                itemWindowScrollPane.setScrollX(0);
                itemWindowScrollPane.setScrollY(0);
                StringBuilder description = itemWindowLabel.getText();
                description.setLength(0);
                description.append(item.getDescriptionFromJournal(localPlayerToken));

                Label useLabel = (Label) itemWindowUseButton.getChildren().get(0);
                Label discardLabel = (Label) itemWindowDiscardButton.getChildren().get(0);

                if (item instanceof EquipmentItem) {
                        if (!localPlayerToken.inventory.canChangeEquipment()) {
                                description.append("\n\nYou can not change Equipment during battle.");
                                useLabel.setText("");
                                discardLabel.setText("");
                                itemWindowUseButton.setDisabled(true);
                                itemWindowDiscardButton.setDisabled(true);
                        } else if (localPlayerToken.inventory.isEquipped(item)) {
                                if (((EquipmentItem) item).cursed) {
                                        // cant unequip cursed item
                                        useLabel.setText("");
                                        discardLabel.setText("");
                                        itemWindowUseButton.setDisabled(true);
                                        itemWindowDiscardButton.setDisabled(true);
                                } else {
                                        useLabel.setText("Unequip");
                                        discardLabel.setText("Throw");
                                        itemWindowUseButton.setDisabled(false);
                                        itemWindowDiscardButton.setDisabled(false);
                                }
                        } else {
                                useLabel.setText("Equip");
                                discardLabel.setText("Throw");
                                itemWindowUseButton.setDisabled(false);
                                itemWindowDiscardButton.setDisabled(false);
                        }
                } else if (item instanceof QuickItem) {
                        if (localPlayerToken.inventory.isEquipped(item)) {
                                useLabel.setText("Unequip");
                        } else {
                                useLabel.setText("Equip");
                        }
                        discardLabel.setText("Throw");
                        itemWindowUseButton.setDisabled(false);
                        itemWindowDiscardButton.setDisabled(false);
                } else if (item instanceof ConsumableItem) {
                        if (item instanceof BookItem) {
                                useLabel.setText("Read");
                        } else {
                                useLabel.setText("Use");
                        }
                        discardLabel.setText("Throw");
                        itemWindowUseButton.setDisabled(false);
                        itemWindowDiscardButton.setDisabled(false);
                } else {
                        useLabel.setText("");
                        discardLabel.setText("Throw");
                        itemWindowUseButton.setDisabled(true);
                        itemWindowDiscardButton.setDisabled(false);
                }

                itemWindowLabel.invalidateHierarchy();
        }

        private void setInventoryWindowVisible(boolean visible) {
                if (!visible && itemSelectMode) {
                        // if in item select mode when closing window, then end item select mode
                        if (itemSelectForItem.isIdentified(localPlayerToken)) {
                                this.setItemSelectMode(null);
                        } else {
                                // if the item select mode is for an unidentifed item, the window cant be
                                // closed and the item must be used.
                                return;
                        }
                }

                setHudElementsVisible(!visible);
                if (visible) {
                        if (inventoryWindow.getParent() == null)
                                world.stage.addActor(inventoryWindow);
                        refreshInventoryElements();
                        world.setPaused(true);
                } else {
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

        private void setChatWindowVisible(Dialouge dialouge) {
                if (tokenSelectMode) {
                        /* actually staying in token select mode might not be an issue..
                        if(!tokenSelectForItem.isIdentified(localPlayerToken)){
                                localPlayerToken.command.consumeItem(tokenSelectForItem);
                        }
                        setTokenSelectMode(null);
                        */
                }

                mouseDownDrag = false;
                setHudElementsVisible(dialouge == null);
                chatWindow.setUserObject(dialouge);
                if (dialouge != null) {
                        if (chatWindow.getParent() == null)
                                world.stage.addActor(chatWindow);
                        resizeChatWindowElements(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                        world.setPaused(true);
                } else {
                        chatWindow.remove();
                        world.setPaused(false);
                }
        }

        public void setMapViewMode(boolean mapViewMode) {
                if (this.mapViewMode == mapViewMode)
                        return;
                this.mapViewMode = mapViewMode;
                //setHudElementsVisible(!this.mapViewMode);
                world.camControl.setChasing(!this.mapViewMode);
                if (this.mapViewMode) {

                } else {
                        world.camControl.setZoom(1);
                }

                for (ItemButtonStack quickButton : quickButtons) {
                        quickButton.setVisible(!mapViewMode);
                }
        }

        public boolean isMapViewMode() {
                return mapViewMode;
        }

        private void setTokenSelectMode(ConsumableItem.TargetsTokens item) {
                tokenSelectForItem = item;
                tokenSelectMode = tokenSelectForItem != null;

                if (tokenSelectMode) {
                        if (tokenSelectForItem.isPrimarilySelfConsume()) {
                                localPlayerToken.command.consumeItem(tokenSelectForItem);
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
                                boolean valid = localPlayerToken.command.consumeItem(itemSelectForItem);
                                itemSelectForItem = null;
                                itemSelectMode = false;
                                if (valid)
                                        this.setInventoryWindowVisible(false);
                                return;
                        }


                        int numValid = 0;
                        SnapshotArray<Actor> backpackChildren = backPackTable.getChildren();
                        for (Actor backpackChild : backpackChildren) {
                                ItemButtonStack button = (ItemButtonStack) backpackChild;
                                Item targetItem = button.getItem();
                                boolean validItem = itemSelectForItem.canConsume(localPlayerToken, targetItem);
                                if (validItem) numValid++;
                                button.setVisible(validItem); // TODO: instead of making button invisible, make it "greyed out"
                                button.setDisabled(!validItem);
                        }

                        inventoryCloseButton.setVisible(itemSelectForItem.isIdentified(localPlayerToken));

                        if (numValid == 0 && !itemSelectForItem.isIdentified(localPlayerToken)) {
                                //no valid items to target, if item is not identified force targeting on itself
                                // because unidentified items can not be cancelled

                                localPlayerToken.command.consumeItem(itemSelectForItem);
                                setItemSelectMode(null);
                        } else {
                                setItemWindowVisible(false);
                                // but keep inventory window open obviously
                        }
                } else {
                        // turn off item select mode
                        SnapshotArray<Actor> backpackChildren = backPackTable.getChildren();
                        for (Actor backpackChild : backpackChildren) {
                                ItemButtonStack button = (ItemButtonStack) backpackChild;
                                button.setVisible(true);
                                button.setDisabled(false);
                        }
                        inventoryCloseButton.setVisible(true);
                        this.setInventoryWindowVisible(false);
                }

                if (itemSelectMode) {
                        inputModeLabel.setText("Choose item to use with " + itemSelectForItem.getNameFromJournal(localPlayerToken) + "     ");
                        if (itemSelectForItem.isIdentified(localPlayerToken))
                                ((Label)inventoryCloseButton.getChildren().get(0)).setText("Cancel");
                        else
                                ((Label)inventoryCloseButton.getChildren().get(0)).setText("Close");
                } else {
                        inputModeLabel.setText("Inventory");
                        ((Label)inventoryCloseButton.getChildren().get(0)).setText("Close");
                }
        }

        @Override
        public void onPathBlocked(Pair nextLocation, Tile nextTile) {
                if (nextTile.isDoor() && nextTile.isDoorLocked()) {
                        if (nextTile.doorSymbol instanceof CombinationDoorPuzzle) {
                                this.appendToGameLog("The door is shut tight.");
                        } else if (nextTile.doorSymbol instanceof KeyItem) {
                                if (localPlayerToken.inventory.containsKey((KeyItem) nextTile.doorSymbol)) {
                                        this.appendToGameLog("Tap on door again to unlock door.");
                                } else {
                                        this.appendToGameLog("You do not have the key to unlock this door.");
                                }
                        } else {
                                throw new AssertionError(nextTile.doorSymbol);
                        }

                }
        }

        @Override
        public void onUseItem(Item item, CharacterInventory.UseItemOutcome out) {
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
                                        this.appendToGameLog("You just teleported " + out.targetToken.name + " to somewhere else on the level.");
                                }

                        }
                } else if (item instanceof BookItem) {
                        BookItem book = (BookItem) item;
                        if (book.getType() == BookItem.Type.MagicMapping) {
                                appendToGameLog("The layout of this floor has become revealed to you.");
                        } else {
                                appendToGameLog("You just read " + item.getNameFromJournal(localPlayerToken));
                        }

                        if (out.targetItem != null || book.getType() == BookItem.Type.ExtraQuickSlot) {
                                // immediatly bring up the inventory window so the user can see the result of using the book on this item
                                this.setInventoryWindowVisible(true);
                        }
                } else if (item instanceof KeyItem) {
                        KeyItem key = (KeyItem) item;
                        appendToGameLog("You unlocked the door with a " + key.getType() + " Key.");
                }

        }

        @Override
        public void onAttack(Token target, Pair targetLocation, boolean ranged) {

        }

        @Override
        public void onAttacked(Token attacker, Token target, Attack.AttackOutcome attackOutcome) {
                if (attackOutcome.dodge) {
                        //System.out.println(String.format("%s dodged attack from %s", target.name, attacker.name));
                        spawnDamageInfoLabel("MISS", target, Color.YELLOW);
                } else {
                        //System.out.println(String.format("%s received %s damage from %s", target.name, damage, attacker.name));
                        spawnDamageInfoLabel(String.valueOf(attackOutcome.damage), target, Color.RED);
                }

        }

        @Override
        public void onFsmStateChange(FsmLogic fsm, State oldState, State newState) {

        }

        @Override
        public void onInventoryChanged() {
                refreshInventoryElements();
        }

        @Override
        public void onStatusEffectChange(StatusEffect effect, float duration) {
                Container statusImage = statusEffectImage[effect.ordinal()];
                if (duration == 0) {
                        statusImage.remove();
                } else if (statusImage.getParent() == null) {
                        avatarStatusEffectsGroup.addActor(statusImage);
                }

        }

        @Override
        public void onLearned(Object journalObject, boolean study) {

                refreshInventoryElements();
                if (journalObject instanceof EquipmentItem) {
                        EquipmentItem item = (EquipmentItem) journalObject;
                        if (study) {
                                this.appendToGameLog(String.format("You've used %s long enough that you now understand its secrets.", item.getNameFromJournal(localPlayerToken)));
                        } else {
                                this.appendToGameLog(String.format("You now understand the secrets of %s.", item.getNameFromJournal(localPlayerToken)));
                        }
                } else if (journalObject instanceof Item) {
                        Item item = (Item) journalObject;
                        if (study)
                                this.appendToGameLog("You have identified " + item.getNameFromJournal(localPlayerToken));
                }


        }

        @Override
        public void onInteract(Quest quest, Dialouge dialouge) {
                this.setChatWindowVisible(dialouge);
        }

        @Override
        public boolean isInitialized() {
                return initialized;
        }

        @Override
        public void dispose() {
                initialized = false;

        }

        private float lastTouchTimer = 0;

        /**
         * called before dungeon.update() to do user input related updates
         *
         * @param delta
         */
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
                        if (localPlayerToken.damage.isDead()) {
                                setTokenSelectMode(null);
                                return;
                        }
                        // if item is not identified and it cant be used, then force it to self consume since it cant be cancelled
                        if (!tokenSelectForItem.isIdentified(localPlayerToken)) {
                                Array<Token> targetableTokens = localPlayerToken.floorMap.getTargetableTokens(localPlayerToken, tokenSelectForItem);
                                if (targetableTokens.size == 0) {
                                        localPlayerToken.command.consumeItem(tokenSelectForItem);
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
                } else if (gameOverCountdown < 0) {
                        world.dungeonApp.setAppGameOver();
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
                } else if (chatWindow.getParent() != null) {
                        float clickX = screenX;
                        float clickY = screenY;
                        if (clickX < chatWindow.getX() || clickY < chatWindow.getY() || clickX > chatWindow.getX() + chatWindow.getWidth() || clickY > chatWindow.getY() + chatWindow.getHeight()) {
                                for (int i = chatChoiceButtons.size - 1; i >= 0; i--) {
                                        Button choiceButton = chatChoiceButtons.get(i);
                                        Object uo = choiceButton.getUserObject();
                                        if (uo instanceof Choice) {
                                                Choice choice = (Choice) uo;
                                                localPlayerToken.command.setChatChoice(choice);
                                                setChatWindowVisible(null);
                                                return true;
                                        }
                                }
                                // This shoudlnt ever really happen though...
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
                Array<DungeonWorld.RaycastResult> intersetedTokens = world.getTokens(ray, null);
                for (DungeonWorld.RaycastResult raycastResult : intersetedTokens) {
                        if (raycastResult.token == localPlayerToken) {
                                localPlayerToken.command.consumeItem(tokenSelectForItem);
                                return true;
                        } else if (tokenSelectForItem.canConsume(localPlayerToken, raycastResult.token)) {
                                localPlayerToken.command.consumeItem(tokenSelectForItem, raycastResult.token);
                                return true;
                        }
                }
                return false;
        }

        private float moveCommandDecalCount = Float.NaN;

        private void setMoveCommandMark(Pair location) {

                moveCommandDecal.setPosition(world.getWorldCoords(location, moveCommandDecal.getPosition()));
                moveCommandDecal.translateY(0.2f);
                moveCommandDecalCount = 2;


        }

        private boolean touchCommand(int screenX, int screenY) {
                if (localPlayerToken.damage.isDead()) return false;
                Ray ray = world.cam.getPickRay(screenX, screenY);
                Token targetToken = world.getToken(ray, localPlayerToken);
                // attempt to target specificaly clicked token
                if (targetToken != null) {
                        localPlayerToken.command.setTargetToken(targetToken);
                        if (localPlayerToken.command.getTargetToken() != null)
                                return true;
                }

                final float distance = -ray.origin.y / ray.direction.y;
                tempWorldCoords.set(ray.direction).scl(distance).add(ray.origin);
                world.getMapCoords(tempWorldCoords, tempMapCoords);
                localPlayerToken.command.setUseKeyOnTile(tempMapCoords);
                if (localPlayerToken.command.isUseKey()) {
                        setMoveCommandMark(tempMapCoords);
                        return true;
                }

                // if no token was clicked then go to the tile that was clicked
                return false;
        }

        private boolean dragCommand(Ray ray) {
                if (localPlayerToken.damage.isDead()) return false;
                final float distance = -ray.origin.y / ray.direction.y;
                tempWorldCoords.set(ray.direction).scl(distance).add(ray.origin);
                world.getMapCoords(tempWorldCoords, tempMapCoords);
                if (localPlayerToken.floorMap.getTile(tempMapCoords) != null) {
                        localPlayerToken.command.setLocation(tempMapCoords);
                        setMoveCommandMark(tempMapCoords);
                        return true;
                }
                return false;
        }

        @Override
        public boolean keyDown(int keycode) {
                /*
                if(localPlayerToken == null) return false;
                int numSlots = -1;
                if(keycode == Input.Keys.NUMPAD_0)numSlots = 0;
                else if(keycode == Input.Keys.NUMPAD_1)numSlots = 1;
                else if(keycode == Input.Keys.NUMPAD_2)numSlots = 2;
                else if(keycode == Input.Keys.NUMPAD_3)numSlots = 3;
                else return false;

                if(numSlots <= localPlayerToken.inventory.numQuickSlots()) return false;

                localPlayerToken.inventory.setNumQuickSlots(numSlots);

                return true;
                */
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

                if (event.getListenerActor() == inventoryButton) {
                        if (!tokenSelectMode)
                                setInventoryWindowVisible(true);
                } else if (event.getListenerActor() == itemWindowUseButton) {
                        Object object = itemWindow.getUserObject();

                        if (object instanceof EquipmentItem) {
                                EquipmentItem item = (EquipmentItem) object;
                                if (localPlayerToken.inventory.isEquipped(item)) {
                                        localPlayerToken.inventory.unequip(item);
                                } else {
                                        boolean valid = localPlayerToken.inventory.equip(item);
                                        if (valid && item.cursed)
                                                appendToGameLog("You grip the " + item.getNameFromJournal(localPlayerToken) + " tightly. You are powerless to remove it.");

                                }
                                setItemWindowVisible(false);
                        } else if (object instanceof QuickItem) {
                                QuickItem item = (QuickItem) object;
                                if (localPlayerToken.inventory.isEquipped(item)) {
                                        localPlayerToken.inventory.unequip(item);
                                } else {
                                        localPlayerToken.inventory.equip(item);
                                }
                                setItemWindowVisible(false);
                        } else if (object instanceof ConsumableItem.TargetsItems) {
                                this.setItemSelectMode((ConsumableItem.TargetsItems) object);
                        } else {
                                throw new AssertionError(object);
                        }


                } else if (event.getListenerActor() == itemWindowDiscardButton) {
                        Item item = (Item) itemWindow.getUserObject();
                        boolean valid = localPlayerToken.inventory.throwItem(item);
                        if (valid)
                                setItemWindowVisible(false);
                } else if (event.getListenerActor() == itemWindowBackButton) {
                        setItemWindowVisible(false);
                } else if (event.getListenerActor() == inventoryCloseButton) {
                        if (itemSelectMode) {
                                setItemSelectMode(null);
                                this.setInventoryWindowVisible(true);
                        }else{
                                setInventoryWindowVisible(false);
                        }
                } else if (event.getListenerActor() instanceof Button) {
                        // an inventory, quick slot, or chat choice button
                        Button button = (Button) event.getListenerActor();
                        Object uo = button.getUserObject();
                        if (uo instanceof ItemButtonStack) {
                                // inventory or quickslot button
                                ItemButtonStack itemButtonStack = (ItemButtonStack) uo;
                                Item item = itemButtonStack.getItem();
                                if (inventoryBackPackButtons.contains(itemButtonStack, true)) {
                                        // inventory screen button
                                        if (item != null) {
                                                if (itemSelectMode) {
                                                        // this item was selected as a target item to be used in consumption
                                                        localPlayerToken.command.consumeItem(itemSelectForItem, item);
                                                        setItemSelectMode(null);
                                                } else {
                                                        // regular browsing selection, show the details window
                                                        setItemWindowContents(item);
                                                        setItemWindowVisible(true);
                                                }
                                        }
                                } else if (item instanceof QuickItem) {
                                        // quick slot button
                                        if (tokenSelectMode) {
                                                if (tokenSelectForItem.isIdentified(localPlayerToken)) {
                                                        // can only cancel identified items
                                                        setTokenSelectMode(null);
                                                }
                                                return false;
                                        } else if (item instanceof ConsumableItem.TargetsTokens) {
                                                setTokenSelectMode((ConsumableItem.TargetsTokens) item);
                                        } else if (item instanceof ConsumableItem) {
                                                localPlayerToken.command.consumeItem((ConsumableItem) item);
                                        } else {
                                                throw new AssertionError(item);
                                        }
                                } else {
                                        // this should mean the item was null, means quickslot had nothing assigned.
                                        // if i add more types of buttons i might want to do quickButtons.contains(button) instead
                                }
                        } else if (chatChoiceButtons.contains(button, true)) {
                                // chat choice button
                                Choice choice = (Choice) event.getListenerActor().getUserObject();
                                localPlayerToken.command.setChatChoice(choice);
                                setChatWindowVisible(null);
                        }

                }
                return false;
        }

        private class DamageLabel {
                Label label;
                float ttl;
                AbstractTokenSpatial tokenSpatial;


        }
}
