package asf.dungeon.view;

import asf.dungeon.model.item.Item;
import asf.dungeon.model.item.StackableItem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.SnapshotArray;

/**
 * Created by Danny on 12/2/2014.
 */
public class ItemButtonStack extends Stack implements DungeonWorld.LoadedNotifyable {

        private Skin skin;
        private HudSpatial hud;
        private Item item;
        private Button button;
        private Image itemImage;
        private Table labelsTable;
        private boolean isQuickSlot = false;

        public ItemButtonStack(Skin skin, HudSpatial hud, Item item) {
                super();
                this.skin = skin;
                this.hud = hud;
                setTouchable(Touchable.childrenOnly);
                button = new Button(skin);
                button.addCaptureListener(hud);
                button.setUserObject(this);
                setItem( item);
        }

        @Override
        public boolean onLoaded() {
                String assetLocation = hud.world.assetMappings.getInventoryItemTextureAssetLocation(item);
                if(!hud.world.assetManager.isLoaded(assetLocation, Texture.class)){
                        Gdx.app.error("ItemButton","onLoaded()... but not this item's texture.");
                        return false;
                }

                clearChildren();
                add(button);
                add(getImage());
                add(getTable());

                Texture itemTex = hud.world.assetManager.get(assetLocation, Texture.class);
                TextureRegion tr = new TextureRegion(itemTex);
                itemImage.setDrawable(new TextureRegionDrawable(tr));

                return true;
        }

        public void setItem( Item item){
                this.item = item;
                if(item == null){
                        clearChildren();
                        add(button);
                        if(isQuickSlot){
                                add(new Label("<Empty>", skin));
                        }

                }else{
                        String assetLocation = hud.world.assetMappings.getInventoryItemTextureAssetLocation(item);
                        if(!hud.world.assetManager.isLoaded(assetLocation, Texture.class)){
                                hud.world.assetManager.load(assetLocation, Texture.class);
                                hud.world.notifyOnLoaded(this);
                                clearChildren();
                                add(button);
                                add(getTable());
                        }else{
                                onLoaded();
                        }



                        SnapshotArray<Actor> children = labelsTable.getChildren();
                        for (int i = 0; i < children.size; i++) {
                                Actor actor = children.get(i);
                                String uoName = String.valueOf(actor.getUserObject());
                                if (uoName.equals("Name")) {
                                        Label label = (Label) actor;
                                        label.setText(item.getNameFromJournal(hud.localPlayerToken));
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

        }

        private Image getImage(){
                if(itemImage != null)
                        return itemImage;
                itemImage = new Image();
                itemImage.setTouchable(Touchable.disabled);
                return itemImage;
        }

        private Table getTable(){
                if(labelsTable != null)
                        return labelsTable;

                        labelsTable = new Table(skin);
                labelsTable.setTouchable(Touchable.disabled);
                labelsTable.clearChildren();
                labelsTable.defaults().pad(0, 0, 0, 0).space(0, 0, 0, 0);
                labelsTable.row().expand();
                Label reqLabel = new Label(" ", skin);
                reqLabel.setUserObject("Req");
                labelsTable.add(reqLabel).align(Align.topLeft).pad(5, 5, 0, 0);

                Label valLabel = new Label(" ", skin);
                valLabel.setUserObject("Val");
                labelsTable.add(valLabel).align(Align.topRight).pad(5, 0, 0, 5);

                labelsTable.row().expand();
                Label nameLabel = new Label(" ", skin);
                nameLabel.setUserObject("Name");
                labelsTable.add(nameLabel).colspan(2).center();

                labelsTable.row().expand();
                Label quantityLabel = new Label(" ", skin);
                quantityLabel.setUserObject("Quantity");
                labelsTable.add(quantityLabel).align(Align.bottomRight).colspan(2).pad(0, 0, 3, 5);
                return labelsTable;
        }

        public Item getItem(){
                return item;
        }

        public boolean isQuickSlot() {
                return isQuickSlot;
        }

        public void setQuickSlot(boolean isQuickSlot) {
                this.isQuickSlot = isQuickSlot;
        }


}
