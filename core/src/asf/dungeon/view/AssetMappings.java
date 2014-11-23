package asf.dungeon.view;

import asf.dungeon.model.FxId;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.item.PotionItem;
import asf.dungeon.model.token.StatusEffects;
import com.badlogic.gdx.graphics.Color;

/**
 * Created by Danny on 11/22/2014.
 */
public class AssetMappings {
        protected final String[] assetLocations;
        protected final String[] potionTextureAssetLocations;
        protected final Color[] potionDisplayColors;
        protected final FxId[] statusEffectsFxIds;
        protected final String[] statusEffectIconTextureAssetLocations;

        public AssetMappings() {
                assetLocations = new String[14];
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
                assetLocations[ModelId.Key.ordinal()] = "Models/Loot/Key/Key.g3db";
                assetLocations[ModelId.Key2.ordinal()] = "Models/Loot/Key/Key2.g3db";
                assetLocations[ModelId.Key3.ordinal()] = "Models/Loot/Key/Key3.g3db";
                assetLocations[ModelId.Sword.ordinal()] = "Models/Loot/Sword/SwordLarge.g3db";

                potionTextureAssetLocations = new String[12];
                potionTextureAssetLocations[PotionItem.Color.LightBlue.ordinal()] = "Models/Loot/Potion/potion_silver_blue.png";
                potionTextureAssetLocations[PotionItem.Color.Red.ordinal()] = "Models/Loot/Potion/potion_silver_red.png";
                potionTextureAssetLocations[PotionItem.Color.Blue.ordinal()] = "Models/Loot/Potion/potion_silver_blue.png";
                potionTextureAssetLocations[PotionItem.Color.Green.ordinal()] = "Models/Loot/Potion/potion_silver_green.png";
                potionTextureAssetLocations[PotionItem.Color.Yellow.ordinal()] = "Models/Loot/Potion/potion_silver_blue.png";
                potionTextureAssetLocations[PotionItem.Color.Magenta.ordinal()] = "Models/Loot/Potion/potion_silver_blue.png";
                potionTextureAssetLocations[PotionItem.Color.Black.ordinal()] = "Models/Loot/Potion/potion_silver_blue.png";
                potionTextureAssetLocations[PotionItem.Color.Brown.ordinal()] = "Models/Loot/Potion/potion_silver_blue.png";
                potionTextureAssetLocations[PotionItem.Color.Amber.ordinal()] = "Models/Loot/Potion/potion_silver_blue.png";
                potionTextureAssetLocations[PotionItem.Color.White.ordinal()] = "Models/Loot/Potion/potion_silver_blue.png";
                potionTextureAssetLocations[PotionItem.Color.Silver.ordinal()] = "Models/Loot/Potion/potion_silver_blue.png";
                potionTextureAssetLocations[PotionItem.Color.Purple.ordinal()] = "Models/Loot/Potion/potion_silver_blue.png";

                potionDisplayColors = new Color[12];
                potionDisplayColors[PotionItem.Color.LightBlue.ordinal()] = com.badlogic.gdx.graphics.Color.TEAL;
                potionDisplayColors[PotionItem.Color.Red.ordinal()] = com.badlogic.gdx.graphics.Color.RED;
                potionDisplayColors[PotionItem.Color.Blue.ordinal()] = com.badlogic.gdx.graphics.Color.BLUE;
                potionDisplayColors[PotionItem.Color.Green.ordinal()] = com.badlogic.gdx.graphics.Color.GREEN;
                potionDisplayColors[PotionItem.Color.Yellow.ordinal()] = com.badlogic.gdx.graphics.Color.YELLOW;
                potionDisplayColors[PotionItem.Color.Magenta.ordinal()] = com.badlogic.gdx.graphics.Color.MAGENTA;
                potionDisplayColors[PotionItem.Color.Black.ordinal()] = com.badlogic.gdx.graphics.Color.BLACK;
                potionDisplayColors[PotionItem.Color.Brown.ordinal()] = com.badlogic.gdx.graphics.Color.OLIVE;
                potionDisplayColors[PotionItem.Color.Amber.ordinal()] = com.badlogic.gdx.graphics.Color.ORANGE;
                potionDisplayColors[PotionItem.Color.White.ordinal()] = com.badlogic.gdx.graphics.Color.WHITE;
                potionDisplayColors[PotionItem.Color.Silver.ordinal()] = com.badlogic.gdx.graphics.Color.GRAY;
                potionDisplayColors[PotionItem.Color.Purple.ordinal()] = com.badlogic.gdx.graphics.Color.PURPLE;

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

        protected String getAssetLocation(ModelId modelId) {
                return assetLocations[modelId.ordinal()];
        }

        protected String getPotionTextureAssetLocation(PotionItem potionItem) {
                return potionTextureAssetLocations[potionItem.getColor().ordinal()];
        }

        protected Color getPotionColor(PotionItem potionItem) {
                return potionDisplayColors[potionItem.getColor().ordinal()];
        }

        protected FxId getStatusEffectFxId(StatusEffects.Effect effect) {
                return statusEffectsFxIds[effect.ordinal()];
        }
}
