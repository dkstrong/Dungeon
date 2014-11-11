package asf.dungeon.model.factory;

import asf.dungeon.model.CharacterToken;
import asf.dungeon.model.CrateToken;
import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.PotionItem;
import asf.dungeon.model.Tile;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.logic.LocalPlayerLogicProvider;
import asf.dungeon.model.logic.SimpleLogicProvider;
import com.badlogic.gdx.math.MathUtils;

/**
 * Created by Danny on 11/4/2014.
 */
public class UtFloorGen {

        protected static void printFloorTile(Tile[][] tiles){

                for (int y = tiles[0].length - 1; y >= 0; y--) {
                        for (int x = 0; x < tiles.length; x++) {
                                Tile tile = tiles[x][y];
                                if(tile == null)
                                        System.out.print(" ");
                                else
                                        System.out.print(tile);
                        }
                        System.out.println();
                }




        }

        protected static void spawnTokens(Dungeon dungeon, FloorMap floorMap){
                if(floorMap.index == 0){
                        boolean rangedHero = true;//MathUtils.random.nextBoolean();

                        CharacterToken knightToken = dungeon.newCharacterToken(floorMap,"Player 1", rangedHero ? ModelId.Archer : ModelId.Knight, new LocalPlayerLogicProvider(0,"Player 1"));

                        while(!knightToken.teleportToLocation( MathUtils.random.nextInt(floorMap.getWidth()),MathUtils.random.nextInt(floorMap.getHeight()))){
                        }
                        knightToken.setAttackDamage(3);
                        knightToken.setAbleRangedAttack(rangedHero);
                        knightToken.setDeathRemovalCountdown(Float.NaN);
                        knightToken.addItem(new PotionItem(dungeon, PotionItem.Type.Health));
                        knightToken.addItem(new PotionItem(dungeon, PotionItem.Type.Health));
                        //knightToken.setMoveSpeed(50);
                }

                ModelId[] characters;
                if(floorMap.index == 0)
                        characters = new ModelId[]{}; //destLoc
                else{
                        characters = new ModelId[]{ModelId.Archer,ModelId.Berzerker,ModelId.Diablous,ModelId.FemaleMage,ModelId.Mage,ModelId.Priest}; // "cerberus"
                }
                //characters = new String[]{};

                for(ModelId modelId : characters){
                        CharacterToken characterToken = dungeon.newCharacterToken(floorMap,modelId.name(),modelId, new SimpleLogicProvider());
                        while(!characterToken.teleportToLocation(MathUtils.random.nextInt(floorMap.getWidth()),MathUtils.random.nextInt(floorMap.getHeight())) || floorMap.getTile(characterToken.getLocation()).isStairs() ){
                        }
                }

                // TODO: make sure crates dont spawn on stairs
                ModelId[] crates = new ModelId[]{ModelId.CeramicPitcher,ModelId.CeramicPitcher,ModelId.CeramicPitcher,ModelId.CeramicPitcher,ModelId.CeramicPitcher};

                for(ModelId modelId : crates){
                        CrateToken crateToken = dungeon.newCrateToken(floorMap,modelId.name(), modelId, new PotionItem(dungeon, PotionItem.Type.Health));
                        while(!crateToken.teleportToLocation(MathUtils.random.nextInt(floorMap.getWidth()),MathUtils.random.nextInt(floorMap.getHeight())) || floorMap.getTile(crateToken.getLocation()).isStairs() ){
                        }

                }

        }
}
