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
                        if(fsm.sector == null || fsm.sector.contains(token.getLocation())) {
                                // monster haas no sector, or already is in sector, just sit still
                                command.setLocation(token.getLocation());
                        }else{
                                // move to a point in sector
                                FloorMap floorMap = token.getFloorMap();
                                int x,y, tries=0;
                                do{
                                        if(++tries > 20){
                                                x = token.getLocation().x;
                                                y = token.getLocation().y;
                                                return;
                                        }
                                        x = fsm.sector.getRandomX(fsm.rand);
                                        y = fsm.sector.getRandomY(fsm.rand);
                                }while(floorMap.getTile(x,y) == null || !floorMap.getTile(x,y).isFloor() || floorMap.hasTokensAt(x,y));
                                command.setLocation(x,y);
                        }
                }

                @Override
                public void update(FSMLogic fsm, Token token, Command command, float delta) {
                        Array<Token> tokensInSector;
                        if(fsm.sector != null && fsm.sector.contains(token.getLocation())){
                                tokensInSector= token.getFloorMap().getTokensAt(fsm.sector);
                        }else{
                                tokensInSector= token.getFloorMap().getTokensInExtent(token.getLocation(), token.getDamage().getSightRadius());
                        }

                        chaseToken(fsm, token, command, tokensInSector);

                }


        },
        Chase {
                @Override
                public void begin(FSMLogic fsm, Token token, Command command) {
                        fsm.target = token.getCommand().getTargetToken();
                }

                @Override
                public void update(FSMLogic fsm, Token token, Command command, float delta) {
                        if (fsm.target.getDamage().isDead()) {
                                fsm.setState(Sleep);
                                return;
                        }

                        // if you have a ranged weapon, then the monster should try to stay at maximum range
                        // while on attack cooldown.
                        if(token.getAttack().isOnAttackCooldown() && token.getInventory().getWeaponSlot() != null && token.getInventory().getWeaponSlot().isRanged()){

                                //token.getAttack().getAttackRange()
                                int distance = fsm.target.getLocation().distance(token.getLocation());

                                if(distance == token.getAttack().getAttackRange()){
                                        // sit still, good location
                                        command.setLocation(token.getLocation());
                                }else{
                                        // TODO: move to the closest location whose distance == attack range

                                }

                        }else{
                                command.setTargetToken(fsm.target);
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
                        if (token.isLocatedAt(token.getCommand().getLocation())) {
                                // Wander aimlessly
                                FloorMap floorMap = token.getFloorMap();
                                int x,y, tries=0;
                                do{
                                        if(++tries > 20){
                                                x = token.getLocation().x;
                                                y = token.getLocation().y;
                                                return;
                                        }
                                        x = token.dungeon.rand.random.nextInt(floorMap.getWidth());
                                        y = token.dungeon.rand.random.nextInt(floorMap.getHeight());
                                }while(floorMap.getTile(x,y) == null || !floorMap.getTile(x,y).isFloor() || floorMap.hasTokensAt(x,y));
                                command.setLocation(x,y);
                        }else{
                                // If see a valid target, then switch to chase state
                                Array<Token>  tokensInSector= token.getFloorMap().getTokensInExtent(token.getLocation(), token.getDamage().getSightRadius());
                                chaseToken(fsm, token, command, tokensInSector);
                        }


                }
        };

        @Override
        public void begin(FSMLogic fsm, Token token, Command command) {

        }

        @Override
        public void end(FSMLogic fsm, Token token, Command command) {

        }

        @Override
        public void update(FSMLogic fsm, Token token, Command command, float delta) {

        }

        private static void chaseToken(FSMLogic fsm, Token token, Command command, Array<Token> tokensInSector ){
                for (Token t : tokensInSector) {
                        if (t.getLogic() != null && t.getLogic().getTeam() != fsm.getTeam() && t.getDamage() != null && t.getDamage().isAttackable()) {
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
}
