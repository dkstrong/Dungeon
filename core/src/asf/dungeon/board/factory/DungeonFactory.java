package asf.dungeon.board.factory;


import asf.dungeon.board.Dungeon;
import asf.dungeon.board.FloorMap;

/**
 * Created by danny on 10/26/14.
 */
public class DungeonFactory {


        public static Dungeon dungeon(Dungeon.Listener listener){

                Dungeon dungeon = new Dungeon(listener, new FloorMapFactory());

                FloorMap floorMap = dungeon.generateFloor(0);




                dungeon.setCurrentFloor(floorMap);

                return dungeon;
        }

}
