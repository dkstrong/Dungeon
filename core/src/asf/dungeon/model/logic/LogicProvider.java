package asf.dungeon.model.logic;


import asf.dungeon.model.CharacterToken;

/**
 * Created by danny on 10/23/14.
 */
public interface LogicProvider {

        public void setToken(CharacterToken token);

        public void updateLogic(float delta);
}
