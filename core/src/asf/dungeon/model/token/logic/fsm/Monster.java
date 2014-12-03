package asf.dungeon.model.token.logic.fsm;

import asf.dungeon.model.FloorMap;
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
                public void update(FSMLogic fsm, Token token, Command command, float delta) {
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
                public void update(FSMLogic fsm, Token token, Command command, float delta) {
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
                public void update(FSMLogic fsm, Token token, Command command, float delta) {

                        // TODO: incoprorate the "if see player" sort of check from SLEEP

                        if (token.isLocatedAt(token.getCommand().getLocation())) {
                                FloorMap floorMap = token.getFloorMap();
                                int x,y, tries=0;
                                do{
                                        if(++tries > 20){
                                                return;
                                        }
                                        x = token.dungeon.rand.random.nextInt(floorMap.getWidth());
                                        y = token.dungeon.rand.random.nextInt(floorMap.getHeight());
                                }while(floorMap.getTile(x,y) == null || !floorMap.getTile(x,y).isFloor() || floorMap.hasTokensAt(x,y));
                                command.setLocation(x,y);
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
        public void update(FSMLogic fsm, Token token, Command command, float delta) {

        }
}
