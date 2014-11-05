package asf.dungeon.board.factory;

import asf.dungeon.board.CharacterToken;
import asf.dungeon.board.CrateToken;
import asf.dungeon.board.Dungeon;
import asf.dungeon.board.FloorMap;
import asf.dungeon.board.logic.LocalPlayerLogicProvider;
import asf.dungeon.board.logic.SimpleLogicProvider;
import com.badlogic.gdx.math.MathUtils;

/**
 * Created by Danny on 11/4/2014.
 */
public interface FloorMapGenerator {

        public FloorMap generate(Dungeon dungeon, int floorIndex);


}
