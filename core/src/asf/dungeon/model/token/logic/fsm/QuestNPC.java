package asf.dungeon.model.token.logic.fsm;

import asf.dungeon.model.FloorMap;
import asf.dungeon.model.token.Command;
import asf.dungeon.model.token.Token;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Danny on 12/3/2014.
 */
public enum QuestNPC implements State{
        PauseThenMove{
                @Override
                public void begin(FsmLogic fsm, Token token, Command command) {
                        fsm.count = 3;
                        command.setLocation(token.getLocation());
                }

                @Override
                public void update(FsmLogic fsm, Token token, Command command, float delta) {
                        Array<Token> tokensAt = token.getFloorMap().getManhattanNeighborTokens(token.getLocation());
                        for (Token t : tokensAt) {
                                if(t.getInteractor() != null ){
                                        command.setLocation(token.getLocation());
                                        return;
                                }
                        }
                        if(command.getLocation().equals(token.getLocation())){
                                fsm.count-=delta;
                                if(fsm.count <0){
                                        FloorMap floorMap = token.getFloorMap();
                                        int x,y, tries=0;
                                        do{
                                                if(++tries > 20){
                                                        fsm.count = token.dungeon.rand.range(5,10);
                                                        return;
                                                }
                                                x = token.getLocation().x + token.dungeon.rand.range(-2,2);
                                                y = token.getLocation().y + token.dungeon.rand.range(-2,2);
                                                if(fsm.sector != null){
                                                        x = MathUtils.clamp(x, fsm.sector.x1, fsm.sector.x2);
                                                        y = MathUtils.clamp(y, fsm.sector.y1, fsm.sector.y2);
                                                }
                                        }while(floorMap.isLocationBlocked(x,y));
                                        command.setLocation(x,y);
                                        fsm.count = token.dungeon.rand.range(5,10);
                                }
                        }
                }
        };

        @Override
        public void begin(FsmLogic fsm, Token token, Command command) {

        }

        @Override
        public void end(FsmLogic fsm, Token token, Command command) {

        }

        @Override
        public void update(FsmLogic fsm, Token token, Command command, float delta) {

        }
}
