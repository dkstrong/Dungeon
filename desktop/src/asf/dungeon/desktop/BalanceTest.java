package asf.dungeon.desktop;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Tile;
import asf.dungeon.model.item.EquipmentItem;
import asf.dungeon.model.item.Item;
import asf.dungeon.model.token.Attack;
import asf.dungeon.model.token.StatusEffects;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.DungeonLoader;
import asf.dungeon.utility.UtMath;
import asf.dungeon.view.DungeonWorld;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Danny on 11/16/2014.
 */
public class BalanceTest implements Dungeon.Listener, Token.Listener {
        public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException{
                BalanceTest test = new BalanceTest();
        }


        private final int numSimulations;
        private final boolean printOnAttackInfo;
        private final DungeonWorld.Settings settings;
        Dungeon dungeon;
        Token playerToken;
        Token monsterToken;
        boolean hasShownTokenStats = false;

        List<Simulation> simulations = new ArrayList<Simulation>();
        Simulation simulation;

        private static class Simulation {
                int killCount = 0;
                int attackCount = 0;
                int dodgeCount =0;
                int criticalCount =0;
        }

        public BalanceTest() throws NoSuchFieldException, IllegalAccessException{
                numSimulations = 1500;
                settings = new DungeonWorld.Settings();
                settings.playerModel = ModelId.Knight;
                settings.balanceTest = true;
                //settings.random =  new Random(1);

                printOnAttackInfo = numSimulations <= 5;
                for (int k = 0; k < numSimulations; k++) {
                        doSimulation();
                }
                printSessionStats();
        }

        private void printSessionStats() throws NoSuchFieldException, IllegalAccessException {
                println();
                printLabel("Session Stats");

                if(numSimulations > 1000){
                        for (Field field : Simulation.class.getDeclaredFields()) {
                                println("Avg "+splitUpper(field.getName()) + UtMath.round(getAverage(field),2) + " stdDev: " + UtMath.round(getStdDev(field),2));
                        }
                        println();
                }

                float avgKill = getAverage(Simulation.class.getDeclaredField("killCount"));
                float avgAttack = getAverage(Simulation.class.getDeclaredField("attackCount"));
                float avgDodge = getAverage(Simulation.class.getDeclaredField("dodgeCount"));
                float avgCrit = getAverage(Simulation.class.getDeclaredField("criticalCount"));
                printStat("Average Kills", avgKill);
                printStat("Attacks per kill", avgAttack / avgKill);
                printStat("Dodges per kill", avgDodge / avgKill);
                printStat("Criticals per kill", avgCrit / avgKill);


        }



        private void doSimulation() {
                if (hasShownTokenStats) {
                        if (printOnAttackInfo) println();
                        if (printOnAttackInfo) printLabel("Begin Simulation");
                }
                simulation = new Simulation();
                simulations.add(simulation);



                dungeon = DungeonLoader.createDungeon(settings);
                dungeon.setListener(this);
                float delta = 1 / 30f;
                while (!playerToken.getDamage().isDead()) {
                        dungeon.update(delta);
                }

                if (printOnAttackInfo) printSimStats(simulation);
        }

        @Override
        public void onAttacked(Token attacker, Token target, Attack.AttackOutcome attackOutcome) {
                if (!hasShownTokenStats) {
                        hasShownTokenStats = true;
                        printTokenInfo(playerToken, monsterToken);

                        println();
                        printLabel("Begin Simulation");
                }

                if (attackOutcome.dodge) {
                        if (printOnAttackInfo) {
                                println(String.format("%s (%s / %s) attacked %s (%s / %s), but he dodged it! ",
                                        attacker.getName(), attacker.getDamage().getHealth(), attacker.getDamage().getMaxHealth(),
                                        target.getName(), target.getDamage().getHealth(), target.getDamage().getMaxHealth()));
                        }
                } else {
                        if (printOnAttackInfo) {
                                println(String.format("%s (%s / %s) attacked %s (%s / %s) for %s damage %s",
                                        attacker.getName(), attacker.getDamage().getHealth(), attacker.getDamage().getMaxHealth(),
                                        target.getName(), target.getDamage().getHealth(), target.getDamage().getMaxHealth(),
                                        attackOutcome.damage, attackOutcome.critical ? "CRITICAL HIT!" : ""));

                        }

                }

                if (attacker == playerToken) {
                        simulation.attackCount++;
                        if(attackOutcome.critical)
                                simulation.criticalCount++;
                }

                if(target == playerToken){
                        if(attackOutcome.dodge)
                                simulation.dodgeCount++;
                }

                if (target.getDamage().isDead()) {
                        if (printOnAttackInfo) println(String.format("%s is dead", target.getName()));
                        if (target.getLogic().getTeam() == 1)
                                simulation.killCount++;
                }
        }

        private static void printLabel(String label) {

                System.out.printf("%30s\n", "----" + label + "----");

        }

        private static void printInfo(String label, Object o1, Object o2) {
                if (o1 instanceof Float) {
                        o1 = UtMath.round((Float) o1, 2);
                }

                if (o2 instanceof Float) {
                        o2 = UtMath.round((Float) o2, 2);
                }

                System.out.printf("%20s\t%-10s %s\n", label, o1, o2);
        }

        void printSimStats(Simulation sim) {
                try {
                        Field[] declaredFields = sim.getClass().getDeclaredFields();
                        for (Field declaredField : declaredFields) {
                                printStat(declaredField.getName(), String.valueOf(declaredField.get(sim)));
                        }
                } catch (IllegalAccessException e) {
                        e.printStackTrace();
                }

        }

        private static void printStat(String label, float stat) {
                System.out.println(label + ": " + UtMath.round(stat, 2));
        }

        private static void printStat(String label, int stat) {
                System.out.println(label + ": " + stat);
        }

        private static void printStat(String label, String stat) {
                System.out.println(label + ": " + stat);
        }

        private static void println(String text) {
                System.out.println(text);
        }

        private static void println() {
                System.out.println();
        }

        private static void printTokenInfo(Token token1, Token token2) {
                printInfo("Name",
                        token1.getName(),
                        token2.getName());
                printInfo("Level",
                        token1.getExperience().getLevel(),
                        token2.getExperience().getLevel());


                printInfo("Vitality", // increases health, increases magic damage and defense
                        token1.getExperience().getVitalityBase()+(token1.getExperience().getVitalityMod() > 0 ? " + "+token1.getExperience().getVitalityMod() : ""),
                        token2.getExperience().getVitalityBase()+(token2.getExperience().getVitalityMod() > 0 ? " + "+token2.getExperience().getVitalityMod() : ""));

                printInfo("Strength", // increases physic damage and defense
                        token1.getExperience().getStrengthBase()+(token1.getExperience().getStrengthMod() > 0 ? " + "+token1.getExperience().getStrengthMod() : ""),
                        token2.getExperience().getStrengthBase()+(token2.getExperience().getStrengthMod() > 0 ? " + "+token2.getExperience().getStrengthMod() : ""));

                printInfo("Agility", // increases move speed, rate of attack, and chance to dodge
                        token1.getExperience().getAgilityBase()+(token1.getExperience().getAgilityMod() > 0 ? " + "+token1.getExperience().getAgilityMod() : ""),
                        token2.getExperience().getAgilityBase()+(token2.getExperience().getAgilityMod() > 0 ? " + "+token2.getExperience().getAgilityMod() : ""));

                printInfo("Luck", // increases chance of critical hit
                        token1.getExperience().getLuckBase()+(token1.getExperience().getLuckMod() > 0 ? " + "+token1.getExperience().getLuckMod() : ""),
                        token2.getExperience().getLuckBase()+(token2.getExperience().getLuckMod() > 0 ? " + "+token2.getExperience().getLuckMod() : ""));

                println();
                printInfo("Weapon",
                        token1.getInventory().getWeaponSlot(),
                        token2.getInventory().getWeaponSlot());
                printInfo("Armor",
                        token1.getInventory().getArmorSlot(),
                        token2.getInventory().getArmorSlot());
                printInfo("Ring",
                        token1.getInventory().getRingSlot(),
                        token2.getInventory().getRingSlot());
                println();
                printLabel("Vitality");
                printInfo("HP",
                        token1.getDamage().getMaxHealth(),
                        token2.getDamage().getMaxHealth());
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
                printLabel("Luck");
                printInfo("Critical Hit",
                        token1.getAttack().getCriticalHitChance(),
                        token2.getAttack().getCriticalHitChance());
        }



        private float getAverage(Field field) throws NoSuchFieldException, IllegalAccessException{

                float count = 0;
                for (Simulation sim : simulations) {
                        Object fieldVal = field.get(sim);
                        if (fieldVal instanceof Integer) {
                                Integer intVal = (Integer) fieldVal;
                                count += intVal;
                        } else {
                                throw new AssertionError(fieldVal);
                        }
                }
                float mean = count / simulations.size();
                return mean;
        }

        private float getStdDev(Field field) throws NoSuchFieldException, IllegalAccessException{
                float mean = getAverage(field);
                float sumDifSqr = 0;

                for (Simulation sim : simulations) {
                        Object fieldVal = field.get(sim);
                        if (fieldVal instanceof Integer) {
                                Integer intVal = (Integer) fieldVal;
                                float difference = intVal - mean;
                                float differenceSqr = difference * difference;
                                sumDifSqr += differenceSqr;

                        } else {
                                throw new AssertionError(fieldVal);
                        }
                }
                float variance = sumDifSqr / simulations.size();
                float stdDev = (float) Math.sqrt(variance);
                return stdDev;
        }

        private String splitUpper(String input){
                String[] split = input.split("(?=\\p{Upper})");
                String out="";
                for (String s : split) {
                        s = s.substring(0,1).toUpperCase()+s.substring(1).toLowerCase();
                        out+=s+" ";
                }
                return out;

        }

        @Override
        public void onPathBlocked(Pair nextLocation, Tile nextTile) {

        }

        @Override
        public void onAttack(Token target, Pair targetLocation, boolean ranged) {

        }


        @Override
        public void onInventoryChanged() {

        }


        @Override
        public void onUseItem(Item item) {

        }

        @Override
        public void onStatusEffectChange(StatusEffects.Effect effect, float duration) {

        }

        @Override
        public void onLearnedThroughStudy(EquipmentItem item) {

        }

        @Override
        public void onNewPlayerToken(Token playerToken) {

        }

        @Override
        public void onFloorMapChanged(FloorMap newFloorMap) {

        }

        @Override
        public void onTokenAdded(Token token) {
                token.setListener(this);

                if (token.getLogic() == null)
                        return;

                if (token.getLogic().getTeam() == 0) {
                        playerToken = token;
                } else if (token.getLogic().getTeam() == 1) {
                        monsterToken = token;
                }

        }

        @Override
        public void onTokenRemoved(Token token) {

        }
}
