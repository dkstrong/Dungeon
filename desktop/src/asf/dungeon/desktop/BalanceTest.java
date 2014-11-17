package asf.dungeon.desktop;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Tile;
import asf.dungeon.model.item.Item;
import asf.dungeon.model.token.StatusEffects;
import asf.dungeon.model.token.Token;
import asf.dungeon.utility.DungeonLoader;
import asf.dungeon.utility.UtMath;
import asf.dungeon.view.DungeonWorld;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Danny on 11/16/2014.
 */
public class BalanceTest implements Dungeon.Listener, Token.Listener {
        public static void main(String[] args) {
                BalanceTest test = new BalanceTest();
        }

        private static final boolean printOnAttackInfo = false;
        private static final int numSimulations = 25;
        Dungeon dungeon;
        Token playerToken;
        Token monsterToken;
        boolean hasShownTokenStats = false;

        List<Simulation> simulations = new ArrayList<Simulation>();
        Simulation simulation;

        private class Simulation{
                int killCount = 0;
                int attackCount=0;

                void printInfo(){
                        println("Monsters Defeated: " + simulation.killCount);
                }
        }

        public BalanceTest() {

                for(int k=0; k<numSimulations; k++){
                        doSimulation();
                }

                println();
                printLabel("Session Stats");

                float killCount=0, attackCount=0;
                for (Simulation sim : simulations) {
                        killCount+=sim.killCount;
                        attackCount+=sim.attackCount;
                }
                killCount/=numSimulations;
                attackCount/=numSimulations;

                println("Avg kill count: "+killCount);
                println("Avg attack count: "+attackCount);


        }

        private void doSimulation() {
                if (hasShownTokenStats) {
                        if(printOnAttackInfo)println();
                        if(printOnAttackInfo)printLabel("Begin Simulation");
                }
                simulation = new Simulation();
                simulations.add(simulation);

                DungeonWorld.Settings settings = new DungeonWorld.Settings();
                settings.playerModel = ModelId.Knight;
                settings.balanceTest = true;

                dungeon = DungeonLoader.createDungeon(settings);
                dungeon.setListener(this);
                float delta = 1/30f;
                while (!playerToken.getDamage().isDead()) {
                        dungeon.update(delta);
                }

                if(printOnAttackInfo) simulation.printInfo();
        }

        @Override
        public void onAttacked(Token attacker, Token target, int damage, boolean dodge) {

                if (dodge) {
                        if(printOnAttackInfo) println(String.format("%s (%s / %s) attacked %s (%s / %s), but he dodged it! ", attacker.getName(), attacker.getDamage().getHealth(), attacker.getDamage().getMaxHealth(), target.getName(), target.getDamage().getHealth(), target.getDamage().getMaxHealth()));
                } else {
                        if(printOnAttackInfo) println(String.format("%s (%s / %s) attacked %s (%s / %s) for %s damage ", attacker.getName(), attacker.getDamage().getHealth(), attacker.getDamage().getMaxHealth(), target.getName(), target.getDamage().getHealth(), target.getDamage().getMaxHealth(), damage));
                }

                if(attacker == playerToken){
                        simulation.attackCount++;
                }

                if (target.getDamage().isDead()) {
                        if(printOnAttackInfo) println(String.format("%s is dead", target.getName()));
                        if (target.getLogic().getTeam() == 1)
                                simulation.killCount++;
                }
        }

        private static void printLabel(String label) {
                System.out.println(String.format("\t%20s----", "----" + label));
        }

        private static void printInfo(String label, Object o1, Object o2) {
                if (o1 instanceof Float) {
                        o1 = UtMath.round((Float) o1, 2);
                }

                if (o2 instanceof Float) {
                        o2 = UtMath.round((Float) o2, 2);
                }

                System.out.print(String.format("%20s\t%s\t%s\n", label, o1, o2));
        }

        private static void println(String text) {
                System.out.println(text);
        }

        private static void println() {
                System.out.println();
        }

        private static void printTokenInfo(Token token1, Token token2) {
                printInfo("Name",
                        "Player",
                        token2.getName());
                printInfo("Level",
                        token1.getExperience().getLevel(),
                        token2.getExperience().getLevel());
                printInfo("Vitality", // increases health, increases magic damage and defense
                        token1.getExperience().getVitality(),
                        token2.getExperience().getVitality());

                printInfo("Strength", // increases physic damage and defense
                        token1.getExperience().getStrength(),
                        token2.getExperience().getStrength());

                printInfo("Agility", // increases move speed, rate of attack, and chance to dodge
                        token1.getExperience().getAgility(),
                        token2.getExperience().getAgility());

                println();
                printLabel("Vitality");
                printInfo("HP",
                        token1.getDamage().getHealth(),
                        token2.getDamage().getHealth());
                printLabel("Strength");
                printInfo("Attack Duration",
                        token1.getAttack().getAttackDuration(),
                        token2.getAttack().getAttackDuration());
                printInfo("Attack Range",
                        token1.getAttack().getAttackRange(),
                        token2.getAttack().getAttackRange());
                printLabel("Agility");
                printInfo("Move Speed",
                        token1.getMove().getMoveSpeed(),
                        token2.getMove().getMoveSpeed());
                printInfo("Attack Cooldown",
                        token1.getAttack().getAttackCooldownDuration(),
                        token2.getAttack().getAttackCooldownDuration());
                printInfo("Projectile Speed",
                        token1.getAttack().getProjectileSpeed(),
                        token2.getAttack().getProjectileSpeed());

        }


        @Override
        public void onPathBlocked(Pair nextLocation, Tile nextTile) {

        }

        @Override
        public void onAttack(Token target, Pair targetLocation, boolean ranged) {

        }


        @Override
        public void onInventoryAdd(Item item) {

        }

        @Override
        public void onInventoryRemove(Item item) {

        }

        @Override
        public void onUseItem(Item item) {

        }

        @Override
        public void onStatusEffectChange(StatusEffects.Effect effect, float duration) {

        }

        @Override
        public void onFloorMapChanged(FloorMap newFloorMap) {

        }

        @Override
        public void onTokenAdded(Token token) {
                token.setListener(this);

                if(token.getLogic() == null)
                        return;

                if (token.getLogic().getTeam() == 0) {
                        playerToken = token;
                } else if (token.getLogic().getTeam() == 1) {
                        monsterToken = token;
                        if (!hasShownTokenStats) {
                                hasShownTokenStats = true;
                                printTokenInfo(playerToken, monsterToken);

                                println();
                                printLabel("Begin Simulation");
                        }
                }

        }

        @Override
        public void onTokenRemoved(Token token) {

        }
}
