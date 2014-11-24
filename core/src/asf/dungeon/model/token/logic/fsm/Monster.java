package asf.dungeon.model.token.logic.fsm;

import asf.dungeon.model.Tile;
import asf.dungeon.model.token.Command;
import asf.dungeon.model.token.Token;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Danny on 11/20/2014.
 */
public enum Monster implements State {
        Sleep {
                @Override
                public void begin(FSMLogic fsm, Token token, Command command) {
                        command.setLocation(token.getLocation());
                }

                @Override
                public void update(FSMLogic fsm, Token token, Command command) {
                        Array<Token> tokensInSector;
                        if(fsm.sector != null){
                                tokensInSector= token.getFloorMap().getTokensAt(fsm.sector);
                        }else{
                                tokensInSector= token.getFloorMap().getTokensInExtent(token.getLocation(), token.getDamage().getSightRadius());
                        }

                        for (Token t : tokensInSector) {
                                if (t.getLogic() != null && t.getLogic().getTeam() != fsm.getTeam()) {
                                        if(t.getFogMapping() == null){
                                                // target does not have fogmapping, assume vision is possible
                                                command.setTargetToken(t);
                                                fsm.setState(Chase);
                                                return;
                                        }else{
                                                // target has fogmapping, so use it to ensure vision is possible
                                                boolean canSeeMe = t.getFogMapping().getCurrentFogMap().isVisible(token.getLocation().x, token.getLocation().y);
                                                if(canSeeMe){
                                                        command.setTargetToken(t);
                                                        fsm.setState(Chase);
                                                        return;
                                                }
                                        }

                                }
                        }


                }
        },
        Chase {
                @Override
                public void update(FSMLogic fsm, Token token, Command command) {
                        if (command.getTargetToken() == null || command.getTargetToken().getDamage().isDead()) {
                                fsm.setState(Sleep);
                        }
                }
        },
        Explore{
                @Override
                public void begin(FSMLogic fsm, Token token, Command command) {
                        command.setLocation(token.getLocation());
                }

                @Override
                public void update(FSMLogic fsm, Token token, Command command) {

                        // TODO: incoprorate the "if see player" sort of check from SLEEP

                        if (token.isLocatedAt(token.getCommand().getLocation())) {
                                int x = token.dungeon.rand.random.nextInt(token.getFloorMap().getWidth());
                                int y = token.dungeon.rand.random.nextInt(token.getFloorMap().getHeight());
                                Tile tile = token.getFloorMap().getTile(x, y);
                                if (tile != null && !tile.isBlockMovement() && !tile.isStairs()) {
                                        token.getCommand().setLocation(x,y);

                                }
                        }


                }
        }
        ;

        @Override
        public void begin(FSMLogic fsm, Token token, Command command) {

        }

        @Override
        public void end(FSMLogic fsm, Token token, Command command) {

        }

        @Override
        public void update(FSMLogic fsm, Token token, Command command) {

        }
}
