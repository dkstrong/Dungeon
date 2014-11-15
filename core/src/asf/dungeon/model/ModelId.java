package asf.dungeon.model;

/**
 * Model id is used to tell the view classes what sort of thing should be viewed
 * doesnt necesarily need to be an assetlocaton. This should be the only
 * only "cross over" of view related information being held in the dungeon model.
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
        Potion("Models/Loot/Potion/PotionSmall.g3db"),
        Key("Models/Loot/Key/Key.g3db");
        //HealthPotion("Models/Loot/loot_health_potion.g3db");

        public transient final String assetLocation;

        ModelId(String assetLocation) {
                this.assetLocation = assetLocation;
        }
}
