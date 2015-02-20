package asf.dungeon.view;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorType;
import asf.dungeon.model.FxId;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.SfxId;
import asf.dungeon.model.item.BookItem;
import asf.dungeon.model.item.Item;
import asf.dungeon.model.item.KeyItem;
import asf.dungeon.model.item.PotionItem;
import asf.dungeon.model.item.ScrollItem;
import asf.dungeon.model.token.Fountain;
import asf.dungeon.model.token.StatusEffect;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;

/**
 * Created by Danny on 11/22/2014.
 */
public class AssetMappings {
        private final Quaternion[] rotations;
        private final String[] assetLocations;
        private final String[] floorAssetLocations;
        private final String[] wallAssetLocations;
        private final Color[] potionDisplayColors;
        private final FxId[] statusEffectsFxIds;
        private final String[][] soundLocations;

        public AssetMappings() {
                rotations = new Quaternion[8];
                rotations[Direction.North.ordinal()] = new Quaternion().setFromAxisRad(0, 1, 0, 3.1459f); // 180
                rotations[Direction.South.ordinal()] = new Quaternion().setFromAxisRad(0, 1, 0, 0f); // 0
                rotations[Direction.East.ordinal()] = new Quaternion().setFromAxisRad(0, 1, 0, 1.5708f); // 90
                rotations[Direction.West.ordinal()] = new Quaternion().setFromAxisRad(0, 1, 0, 4.71239f); // 270
                rotations[Direction.NorthEast.ordinal()] = new Quaternion().setFromAxisRad(0, 1, 0, 2.35619449f); // 135
                rotations[Direction.NorthWest.ordinal()] = new Quaternion().setFromAxisRad(0, 1, 0, 3.92699082f); // 225
                rotations[Direction.SouthEast.ordinal()] = new Quaternion().setFromAxisRad(0, 1, 0, 0.785398163f); // 45
                rotations[Direction.SouthWest.ordinal()] = new Quaternion().setFromAxisRad(0, 1, 0, 5.49778714f); // 315

                assetLocations = new String[36];
                assetLocations[ModelId.Archer.ordinal()] = "Models/Characters/archer.g3db";
                assetLocations[ModelId.Berzerker.ordinal()] = "Models/Characters/berzerker.g3db";
                assetLocations[ModelId.Cerberus.ordinal()] = "Models/Characters/cerberus.g3db";
                assetLocations[ModelId.Diablous.ordinal()] = "Models/Characters/diablous.g3db";
                assetLocations[ModelId.FemaleMage.ordinal()] = "Models/Characters/female_mage.g3db";
                assetLocations[ModelId.Knight.ordinal()] = "Models/Characters/knight_01.g3db";
                assetLocations[ModelId.Mage.ordinal()] = "Models/Characters/mage.g3db";
                assetLocations[ModelId.Priest.ordinal()] = "Models/Characters/priest.g3db";
                assetLocations[ModelId.Skeleton.ordinal()] = "Models/Characters/Skeleton.g3db";
                assetLocations[ModelId.Goblin.ordinal()] = "Models/Characters/Goblin.g3db";
                assetLocations[ModelId.RockMonster.ordinal()] = "Models/Characters/rockMonster_01.g3db";
                assetLocations[ModelId.Rat.ordinal()] = "Models/Characters/rat.g3db";
                assetLocations[ModelId.TrainingDummy.ordinal()] = "Models/Characters/training_dummy.g3db";
                assetLocations[ModelId.CeramicPitcher.ordinal()] = "Models/Crates/CeramicPitcher.g3db";
                assetLocations[ModelId.Crate.ordinal()] = "Models/Crates/crate_01.g3db";
                assetLocations[ModelId.Barrel.ordinal()] = "Models/Crates/barrel_01.g3db";
                assetLocations[ModelId.Chest.ordinal()] = "Models/Crates/chest_01.g3db";
                assetLocations[ModelId.Fountain.ordinal()] = "Models/Fountain/Fountain.g3db";
                assetLocations[ModelId.Torch.ordinal()] = "Models/Torch/Torch.g3db";
                assetLocations[ModelId.SignPost.ordinal()] = "Models/SignPost/SignPost.g3db";
                assetLocations[ModelId.SpikeTrap.ordinal()] = "Models/SpikeTrap/SpikeTrap.g3db";
                assetLocations[ModelId.Boulder.ordinal()] = "Models/Boulder/Boulder.g3db";
                assetLocations[ModelId.StairsUp.ordinal()] = "Models/Stairs/StairsUp.g3db";
                assetLocations[ModelId.StairsDown.ordinal()] = "Models/Stairs/StairsDown.g3db";
                assetLocations[ModelId.Church.ordinal()] = "Models/Church/Church.g3db";
                assetLocations[ModelId.Potion.ordinal()] = "Models/Loot/Potion/PotionSmall.g3db";
                assetLocations[ModelId.Scroll.ordinal()] = "Models/Loot/Scroll/Scroll.g3db";
                assetLocations[ModelId.Book.ordinal()] = "Models/Loot/Book/Book.g3db";
                assetLocations[ModelId.Key.ordinal()] = "Models/Loot/Key/Key.g3db";
                assetLocations[ModelId.Key2.ordinal()] = "Models/Loot/Key/Key2.g3db";
                assetLocations[ModelId.Key3.ordinal()] = "Models/Loot/Key/Key3.g3db";
                assetLocations[ModelId.SwordLarge.ordinal()] = "Models/Loot/Sword/SwordLarge.g3db";
                assetLocations[ModelId.Sword_01.ordinal()] = "Models/Loot/Sword/BasicSword.g3db";
                assetLocations[ModelId.BowLarge.ordinal()] = "Models/Loot/Bow/BowLarge.g3db";
                assetLocations[ModelId.Bow_01.ordinal()] = "Models/Loot/Bow/bow_01.g3db";
                assetLocations[ModelId.StaffLarge.ordinal()] = "Models/Loot/Staff/StaffLarge.g3db";

                floorAssetLocations = new String[2];
                floorAssetLocations[FloorType.Grassy.ordinal()] = "Textures/Floor/floorGrassyTiles.png";
                floorAssetLocations[FloorType.Dungeon.ordinal()] = "Textures/Floor/floorTilesPressurePlates.png";

                wallAssetLocations = new String[2];
                wallAssetLocations[FloorType.Grassy.ordinal()] = "Textures/Floor/wallGrassyTiles.png";
                wallAssetLocations[FloorType.Dungeon.ordinal()] = "Textures/Floor/wallTiles.png";

                potionDisplayColors = new Color[11];
                potionDisplayColors[PotionItem.Color.Red.ordinal()] = com.badlogic.gdx.graphics.Color.RED;
                potionDisplayColors[PotionItem.Color.Blue.ordinal()] = com.badlogic.gdx.graphics.Color.BLUE;
                potionDisplayColors[PotionItem.Color.Green.ordinal()] = com.badlogic.gdx.graphics.Color.GREEN;
                potionDisplayColors[PotionItem.Color.Yellow.ordinal()] = com.badlogic.gdx.graphics.Color.YELLOW;
                potionDisplayColors[PotionItem.Color.Magenta.ordinal()] = com.badlogic.gdx.graphics.Color.MAGENTA;
                potionDisplayColors[PotionItem.Color.Teal.ordinal()] = Color.TEAL;
                potionDisplayColors[PotionItem.Color.Black.ordinal()] = com.badlogic.gdx.graphics.Color.BLACK;
                potionDisplayColors[PotionItem.Color.Brown.ordinal()] = com.badlogic.gdx.graphics.Color.OLIVE;
                potionDisplayColors[PotionItem.Color.Amber.ordinal()] = com.badlogic.gdx.graphics.Color.ORANGE;
                potionDisplayColors[PotionItem.Color.Silver.ordinal()] = com.badlogic.gdx.graphics.Color.GRAY;

                statusEffectsFxIds = new FxId[14];
                statusEffectsFxIds[StatusEffect.Heal.ordinal()] = FxId.HealAura;
                statusEffectsFxIds[StatusEffect.Poison.ordinal()] = FxId.HealAura;
                statusEffectsFxIds[StatusEffect.Frozen.ordinal()] = FxId.HealAura;
                statusEffectsFxIds[StatusEffect.Burning.ordinal()] = FxId.Burning;
                statusEffectsFxIds[StatusEffect.Paralyze.ordinal()] = FxId.HealAura;
                statusEffectsFxIds[StatusEffect.Invisibility.ordinal()] = FxId.HealAura;
                statusEffectsFxIds[StatusEffect.MindVision.ordinal()] = FxId.HealAura;
                statusEffectsFxIds[StatusEffect.ItemVision.ordinal()] = FxId.HealAura;
                statusEffectsFxIds[StatusEffect.Blind.ordinal()] = FxId.HealAura;
                statusEffectsFxIds[StatusEffect.Might.ordinal()] = FxId.HealAura;
                statusEffectsFxIds[StatusEffect.Speed.ordinal()] = FxId.HealAura;
                statusEffectsFxIds[StatusEffect.Confused.ordinal()] = FxId.HealAura;
                statusEffectsFxIds[StatusEffect.ScaresMonsters.ordinal()] = FxId.HealAura;
                statusEffectsFxIds[StatusEffect.LuresMonsters.ordinal()] = FxId.HealAura;

                soundLocations = new String[3][];
                soundLocations[SfxId.AlertMonster.ordinal()] = new String[]{"Sounds/monster-alert.ogg"};
                soundLocations[SfxId.Hit.ordinal()] = new String[]{"Sounds/goblin-hit.ogg"};
                soundLocations[SfxId.Die.ordinal()] = new String[]{"Sounds/monsterdeath1.ogg","Sounds/monsterdeath2.ogg"};

        }

        public void preload3dModels(AssetManager assetManager){
                for (String assetLocation : assetLocations) {
                        if(assetLocation == null) continue;
                        assetManager.load(assetLocation, Model.class);
                }
        }

        public static FileHandle getUserMonsterLocation(){
                if(Gdx.app.getType() != Application.ApplicationType.Desktop)
                        return null;

                FileHandle fileHandle = Gdx.files.local("Models");
                FileHandle[] list = fileHandle.list(".g3db");
                if(list.length > 0)
                        return list[list.length-1];
                return null;

        }

        public String getAssetLocation(ModelId modelId) {
                return assetLocations[modelId.ordinal()];
        }

        public String getFloorAssetLocation(FloorType floorType){
                return floorAssetLocations[floorType.ordinal()];
        }

        public String getWallAssetLocation(FloorType floorType){
                return wallAssetLocations[floorType.ordinal()];
        }

        public String[][] getSoundLocations(){
                return soundLocations;
        }


        public String[] getSoundLocations(SfxId sfx){
                return soundLocations[sfx.ordinal()];
        }

        public String getRandomSoundLocation(SfxId sfx){
                String[] locations = soundLocations[sfx.ordinal()];
                return locations[MathUtils.random.nextInt(locations.length)];
        }

        public Quaternion getRotation(Direction dir){
                return rotations[dir.ordinal()];
        }



        public String getHudStatusEffectIcon(StatusEffect effect){
                return "Interface/Hud/health";
        }

        public String getKeyIcon(KeyItem.Type keyType){
                return "Interface/Hud/Key2Hud";
        }

        public String getInventoryItemTextureAssetLocation(Item item){
                return "Interface/Loot/Potion/Potion-Amber-Transparent";
        }

        public String getPotionTextureAssetLocation(PotionItem potionItem) {
                return "Models/Loot/Potion/Potion-"+potionItem.getColor().name()+".png";
        }

        public Color getPotionColor(PotionItem potionItem) {
                return potionDisplayColors[potionItem.getColor().ordinal()];
        }

        public String getScrollTextureAssetLocation(ScrollItem scrollItem){
                return "Models/Loot/Scroll/parchment_"+scrollItem.getSymbol().name().toLowerCase()+".png";
        }

        public String getFountainTextureAssetLocation(Fountain fountain){
                if(fountain.isConsumed()) return getEmptyFountainTextureAssetLocation(fountain);
                return "Models/Fountain/Fountain-"+fountain.getFountainColor().name()+".png";
        }

        public String getEmptyFountainTextureAssetLocation(Fountain fountain){
                return "Models/Fountain/Fountain.png";
        }

        public String getBookTextureAssetLocation(BookItem bookItem){
                return "Models/Loot/Book/Book.png";
        }



        public FxId getStatusEffectFxId(StatusEffect effect) {
                return statusEffectsFxIds[effect.ordinal()];
        }
}
