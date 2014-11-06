package asf.dungeon.model;

/**
 * Created by Danny on 11/5/2014.
 */
public enum ModelId {
        Archer("Models/Characters/archer.g3db"),
        Berzerker("Models/Characters/berzerker.g3db"),
        Cerberus("Models/Characters/cerberus.g3db"),
        Diablous("Models/Characters/diablous.g3db"),
        FemaleMage("Models/Characters/female_mage.g3db"),
        Knight("Models/Characters/knight.g3db"),
        Mage("Models/Characters/mage.g3db"),
        Priest("Models/Characters/priest.g3db"),
        CeramicPitcher("Models/Crates/CeramicPitcher.g3db"),
        HealthPotion("Models/Loot/loot_health_potion.g3db");

        public final String assetLocation;

        ModelId(String assetLocation) {
                this.assetLocation = assetLocation;
        }
}
