package asf.dungeon.board.factory;

import asf.dungeon.board.CharacterToken;
import asf.dungeon.board.CrateToken;
import asf.dungeon.board.Dungeon;
import asf.dungeon.board.FloorMap;
import asf.dungeon.board.FloorTile;
import asf.dungeon.board.logic.LocalPlayerLogicProvider;
import asf.dungeon.board.logic.SimpleLogicProvider;
import com.badlogic.gdx.math.MathUtils;

/**
 * Created by Danny on 11/4/2014.
 */
public class UtFloorGen {

        protected static void printFloorTile(FloorTile[][] tiles){

                for (int y = tiles[0].length - 1; y >= 0; y--) {
                        for (int x = 0; x < tiles.length; x++) {
                                FloorTile tile = tiles[x][y];
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
                        CharacterToken knightToken = dungeon.newCharacterToken(floorMap,"knight",new LocalPlayerLogicProvider(0,"Player 1"));

                        while(!knightToken.teleportToLocation( MathUtils.random.nextInt(floorMap.getWidth()),MathUtils.random.nextInt(floorMap.getHeight()))){
                        }
                        knightToken.setAttackDamage(3);
                        knightToken.setDeathRemovalCountdown(Float.NaN);
                        //knightToken.setMoveSpeed(50);
                }

                String[] characters;
                if(floorMap.index == 0)
                        characters = new String[]{"priest"};
                else{
                        characters = new String[]{"archer","berzerker","diablous","female_mage","mage","priest"}; // "cerberus"
                }
                //characters = new String[]{};

                for(String characterName : characters){
                        CharacterToken characterToken = dungeon.newCharacterToken(floorMap,characterName, new SimpleLogicProvider());
                        while(!characterToken.teleportToLocation(MathUtils.random.nextInt(floorMap.getWidth()),MathUtils.random.nextInt(floorMap.getHeight()))){
                        }
                }

                // TODO: make sure crates dont spawn on stairs
                String[] crates = new String[]{"CeramicPitcher","CeramicPitcher","CeramicPitcher"};

                for(String crateName : crates){
                        CrateToken crateToken = dungeon.newCrateToken(floorMap,crateName);
                        while(!crateToken.teleportToLocation(MathUtils.random.nextInt(floorMap.getWidth()),MathUtils.random.nextInt(floorMap.getHeight()))){
                        }
                }

        }
}
