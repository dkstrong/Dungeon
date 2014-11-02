package asf.dungeon.board.logic;

import asf.dungeon.board.CharacterToken;
import asf.dungeon.board.Direction;
import asf.dungeon.board.Dungeon;
import asf.dungeon.board.Token;
import asf.dungeon.board.pathfinder.Pair;
import asf.dungeon.view.DungeonWorld;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

/**
 * Created by danny on 10/26/14.
 */
public class LocalPlayerLogicProvider implements LogicProvider {
        private final int id;
        private String name;
        private Dungeon dungeon;
        private CharacterToken token;

        public LocalPlayerLogicProvider(int id, String name) {
                this.id = id;
                this.name = name;
        }

        public int getId() {
                return id;
        }

        public String getName() {
                return name;
        }

        @Override
        public void setToken(CharacterToken token) {
                this.token = token;
                this.dungeon = token.dungeon;
                token.setFogMappingEnabled(true);
        }

        @Override
        public void updateLogic(float delta) {


        }


}
