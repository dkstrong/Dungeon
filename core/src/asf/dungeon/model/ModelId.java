package asf.dungeon.model;

import asf.dungeon.model.token.Decor;

public enum ModelId {
        Archer,
        Berzerker,
        Cerberus,
        Diablous,
        FemaleMage,
        Knight,
        Mage,
        Priest,
        Skeleton,
        Goblin,
        RockMonster,
        Rat,
        TrainingDummy,
        CeramicPitcher,
        Crate,
        Barrel,
        Chest,
        Foliage,
        Tree,
        Fountain,
        Torch,
        SignPost,
        SpikeTrap,
        Boulder,
        Bench(new Decor.Mask(new Pair(1,0))),
        Chair,
        Table1,
        Table2(new Decor.Mask(new Pair(1,0))),
        StairsUp,
        StairsDown,
        /**
         * 3 wide, 4 long, its origin is at 1,0 instead of 0,0 so Decor.Mask doesnt work with Church...
         */
        Church,
        ChurchDoor,
        Potion,
        Scroll,
        Book,
        Key,
        Key2,
        Key3,
        SwordLarge,
        Sword_01,
        BowLarge,
        Bow_01,
        StaffLarge;

        public final transient Decor.Mask decorMask;

        ModelId() {
                decorMask = null;
        }

        ModelId(Decor.Mask decorMask) {
                this.decorMask = decorMask;
        }
}
