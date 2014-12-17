package asf.dungeon.desktop;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.DungeonLoader;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.token.Token;
import asf.dungeon.utility.UtDebugPrint;
import asf.dungeon.view.DungeonWorld;

import java.util.Random;

/**
 * Created by Danny on 11/16/2014.
 */
public class FloorGenTest implements Dungeon.Listener {
        public static void main(String[] args) {
                FloorGenTest test = new FloorGenTest();
        }

        private final DungeonWorld.Settings settings;
        Dungeon dungeon;

        public FloorGenTest() {

                settings = new DungeonWorld.Settings();
                settings.playerModel = ModelId.Knight;
                settings.random =  new Random(3);  // 2

                dungeon = DungeonLoader.createDungeon(settings);
                dungeon.setListener(this);

        }

        @Override
        public void onNewPlayerToken(Token playerToken) {

        }

        @Override
        public void onFloorMapChanged(FloorMap newFloorMap) {
                UtDebugPrint.print(newFloorMap.toTileStrings());
        }

        @Override
        public void onTokenAdded(Token token) {


        }

        @Override
        public void onTokenRemoved(Token token) {

        }
}
