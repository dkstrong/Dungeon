package asf.dungeon.model.logic;

import asf.dungeon.model.CharacterToken;
import asf.dungeon.model.Dungeon;

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
