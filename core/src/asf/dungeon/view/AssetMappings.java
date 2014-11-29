package asf.dungeon.view;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FxId;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.item.BookItem;
import asf.dungeon.model.item.PotionItem;
import asf.dungeon.model.item.ScrollItem;
import asf.dungeon.model.token.StatusEffects;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Quaternion;

/**
 * Created by Danny on 11/22/2014.
 */
public class AssetMappings {
        protected final Quaternion[] rotations;
        protected final String[] assetLocations;
        protected final Color[] potionDisplayColors;
        protected final String[] scrollTextureAssetLocations;
        protected final String[] bookTextureAssetLocations;
        protected final FxId[] statusEffectsFxIds;
        protected final String[] statusEffectIconTextureAssetLocations;

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

                assetLocations = new String[15];
                assetLocations[ModelId.Archer.ordinal()] = "Models/Characters/archer.g3db";
                assetLocations[ModelId.Berzerker.ordinal()] = "Models/Characters/berzerker.g3db";
                assetLocations[ModelId.Cerberus.ordinal()] = "Models/Characters/cerberus.g3db";
                assetLocations[ModelId.Diablous.ordinal()] = "Models/Characters/diablous.g3db";
                assetLocations[ModelId.FemaleMage.ordinal()] = "Models/Characters/female_mage.g3db";
                assetLocations[ModelId.Knight.ordinal()] = "Models/Characters/knight.g3db";
                assetLocations[ModelId.Mage.ordinal()] = "Models/Characters/mage.g3db";
                assetLocations[ModelId.Priest.ordinal()] = "Models/Characters/priest.g3db";
                assetLocations[ModelId.CeramicPitcher.ordinal()] = "Models/Crates/CeramicPitcher.g3db";
                assetLocations[ModelId.Potion.ordinal()] = "Models/Loot/Potion/PotionSmall.g3db";
                assetLocations[ModelId.Scroll.ordinal()] = "Models/Loot/Scroll/Scroll.g3db";
                assetLocations[ModelId.Key.ordinal()] = "Models/Loot/Key/Key.g3db";
                assetLocations[ModelId.Key2.ordinal()] = "Models/Loot/Key/Key2.g3db";
                assetLocations[ModelId.Key3.ordinal()] = "Models/Loot/Key/Key3.g3db";
                assetLocations[ModelId.Sword.ordinal()] = "Models/Loot/Sword/SwordLarge.g3db";

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

                scrollTextureAssetLocations = new String[8];
                scrollTextureAssetLocations[ScrollItem.Symbol.Air.ordinal()] = "Models/Loot/Scroll/parchment_air.png";
                scrollTextureAssetLocations[ScrollItem.Symbol.Earth.ordinal()] = "Models/Loot/Scroll/parchment_earth.png";
                scrollTextureAssetLocations[ScrollItem.Symbol.Fire.ordinal()] = "Models/Loot/Scroll/parchment_fire.png";
                scrollTextureAssetLocations[ScrollItem.Symbol.Light.ordinal()] = "Models/Loot/Scroll/parchment_light.png";
                scrollTextureAssetLocations[ScrollItem.Symbol.Metal.ordinal()] = "Models/Loot/Scroll/parchment_metal.png";
                scrollTextureAssetLocations[ScrollItem.Symbol.Shadow.ordinal()] = "Models/Loot/Scroll/parchment_shadow.png";
                scrollTextureAssetLocations[ScrollItem.Symbol.Water.ordinal()] = "Models/Loot/Scroll/parchment_water.png";
                scrollTextureAssetLocations[ScrollItem.Symbol.Wood.ordinal()] = "Models/Loot/Scroll/parchment_wood.png";

                bookTextureAssetLocations = new String[10];
                bookTextureAssetLocations[BookItem.Symbol.Alpha.ordinal()] = "Models/Loot/Book/Book.png";
                bookTextureAssetLocations[BookItem.Symbol.Beta.ordinal()] = "Models/Loot/Book/Book.png";
                bookTextureAssetLocations[BookItem.Symbol.Gamma.ordinal()] = "Models/Loot/Book/Book.png";
                bookTextureAssetLocations[BookItem.Symbol.Delta.ordinal()] = "Models/Loot/Book/Book.png";
                bookTextureAssetLocations[BookItem.Symbol.Epsilon.ordinal()] = "Models/Loot/Book/Book.png";
                bookTextureAssetLocations[BookItem.Symbol.Zeta.ordinal()] = "Models/Loot/Book/Book.png";
                bookTextureAssetLocations[BookItem.Symbol.Eta.ordinal()] = "Models/Loot/Book/Book.png";
                bookTextureAssetLocations[BookItem.Symbol.Theta.ordinal()] = "Models/Loot/Book/Book.png";
                bookTextureAssetLocations[BookItem.Symbol.Iota.ordinal()] = "Models/Loot/Book/Book.png";
                bookTextureAssetLocations[BookItem.Symbol.Kappa.ordinal()] = "Models/Loot/Book/Book.png";

                statusEffectsFxIds = new FxId[6];
                statusEffectsFxIds[StatusEffects.Effect.Heal.ordinal()] = FxId.HealAura;
                statusEffectsFxIds[StatusEffects.Effect.Poison.ordinal()] = FxId.HealAura;
                statusEffectsFxIds[StatusEffects.Effect.Paralyze.ordinal()] = FxId.HealAura;
                statusEffectsFxIds[StatusEffects.Effect.Invisibility.ordinal()] = FxId.HealAura;
                statusEffectsFxIds[StatusEffects.Effect.MindVision.ordinal()] = FxId.HealAura;
                statusEffectsFxIds[StatusEffects.Effect.Speed.ordinal()] = FxId.HealAura;

                statusEffectIconTextureAssetLocations = new String[6];
                statusEffectIconTextureAssetLocations[StatusEffects.Effect.Heal.ordinal()] = "Interface/Hud/health.png";
                statusEffectIconTextureAssetLocations[StatusEffects.Effect.Poison.ordinal()] = "Interface/Hud/health.png";
                statusEffectIconTextureAssetLocations[StatusEffects.Effect.Paralyze.ordinal()] = "Interface/Hud/health.png";
                statusEffectIconTextureAssetLocations[StatusEffects.Effect.Invisibility.ordinal()] = "Interface/Hud/health.png";
                statusEffectIconTextureAssetLocations[StatusEffects.Effect.MindVision.ordinal()] = "Interface/Hud/health.png";
                statusEffectIconTextureAssetLocations[StatusEffects.Effect.Speed.ordinal()] = "Interface/Hud/health.png";
        }

        protected Quaternion getRotation(Direction dir){
                return rotations[dir.ordinal()];
        }

        protected String getAssetLocation(ModelId modelId) {
                return assetLocations[modelId.ordinal()];
        }

        protected String getPotionTextureAssetLocation(PotionItem potionItem) {
                return "Models/Loot/Potion/Potion-"+potionItem.getColor().name()+".png";
        }

        protected Color getPotionColor(PotionItem potionItem) {
                return potionDisplayColors[potionItem.getColor().ordinal()];
        }

        protected String getScrollTextureAssetLocation(ScrollItem scrollItem){
                return "Models/Loot/Scroll/parchment_"+scrollItem.getSymbol().name().toLowerCase()+".png";

                //return scrollTextureAssetLocations[scrollItem.getSymbol().ordinal()];
        }

        protected String getBookTextureAssetLocation(BookItem bookItem){
                return bookTextureAssetLocations[bookItem.getSymbol().ordinal()];
        }

        protected FxId getStatusEffectFxId(StatusEffects.Effect effect) {
                return statusEffectsFxIds[effect.ordinal()];
        }
}
