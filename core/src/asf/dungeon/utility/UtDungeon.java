package asf.dungeon.utility;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.MasterJournal;
import asf.dungeon.model.token.Token;
import com.badlogic.gdx.math.Vector3;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Danny on 12/3/2014.
 */
public class UtDungeon {

        public interface Debuggable{
                public List<String> toDebugInfo();
        }

        public static void format(List<String> out, String s, Object... vals) {
                for (int i = 0; i < vals.length; i++) {
                        if (vals[i] instanceof Vector3) {
                                vals[i] = UtMath.round((Vector3) vals[i], 2);
                        }else if(vals[i] instanceof Float){
                                Float f = (Float) vals[i];
                                if(!Float.isNaN(f))
                                        vals[i] = UtMath.round(f, 2);
                        }
                }

                out.add(String.format(s, vals));

        }

        public static List<String> out() {
                return new LinkedList<String>();
        }

        public static List<String> dungeon(Dungeon dungeon) {
                List<String> out = out();
                format(out, "Dungeon");

                format(out, "Num Floors: %s", dungeon.numFloormaps());
                format(out, "Current Floor: %s", dungeon.getCurrentFloopMap().index);

                format(out, "Local Player Token: %s", dungeon.getLocalPlayerToken());

                return out;
        }

        public static List<String> masterJournal(Dungeon dungeon) {
                List<String> out = out();
                MasterJournal mj = dungeon.getMasterJournal();

                format(out, "Master Journal");
                format(out, "List journal contents here");
                return out;
        }

        public static List<String> floorMap(FloorMap floorMap) {
                List<String> out = out();

                format(out, "FloorMap " + floorMap.index);
                format(out, "List Floor map info here");
                return out;
        }

        public static List<String> token(Token token) {
                List<String> out = out();

                format(out, "Token");
                format(out, "Id: %s", token.getId());
                format(out, "Name: %s", token.getName());
                format(out, "Model Id: %s", token.getModelId());
                format(out, "Location: %s", token.getLocation());
                format(out, "Direction: %s", token.getDirection());
                format(out, "Blocks Pathing: %s", token.isBlocksPathing());

                return out;
        }

        public static List<String> object(Object o) {
                if(o instanceof Debuggable){
                        Debuggable dbg = (Debuggable) o;
                        return dbg.toDebugInfo();
                }

                List<String> out = out();

                format(out, o.getClass().getSimpleName());

                try {
                        Field[] fields = o.getClass().getDeclaredFields();
                        for (Field field : fields) {
                                field.setAccessible(true);
                                format(out, "%s: %s (%s)",field.getName(), field.get(o), field.getType());
                        }
                }catch (IllegalAccessException e) {
                        format(out, e.getMessage());
                }





                return out;
        }


}
