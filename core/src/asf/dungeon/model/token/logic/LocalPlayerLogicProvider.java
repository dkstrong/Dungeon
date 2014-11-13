package asf.dungeon.model.token.logic;

import asf.dungeon.model.Direction;
import asf.dungeon.model.Dungeon;
import asf.dungeon.model.token.FogMapping;
import asf.dungeon.model.token.Journal;
import asf.dungeon.model.token.Token;

/**
 * Created by danny on 10/26/14.
 */
public class LocalPlayerLogicProvider implements LogicProvider {
        private final int id;
        private String name;
        private Dungeon dungeon;
        private Token token;

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
        public void setToken(Token token) {
                this.token = token;
                this.dungeon = token.dungeon;
                token.add(new FogMapping(token));
                token.add(new Journal());
        }


        @Override
        public boolean teleportToLocation(int x, int y, Direction direction) {
                return true;
        }

        @Override
        public boolean update(float delta) {

                return false;
        }
}
