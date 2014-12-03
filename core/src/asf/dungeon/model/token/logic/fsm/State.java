package asf.dungeon.model.token.logic.fsm;

import asf.dungeon.model.token.Command;
import asf.dungeon.model.token.Token;

/**
 * Created by Danny on 11/20/2014.
 */
public interface State {

        public void begin(FSMLogic fsm, Token token, Command command);

        public void end(FSMLogic fsm, Token token, Command command);

        public void update(FSMLogic fsm, Token token, Command command, float delta);

}
