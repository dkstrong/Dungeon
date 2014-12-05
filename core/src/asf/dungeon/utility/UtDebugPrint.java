package asf.dungeon.utility;

import com.badlogic.gdx.math.Vector3;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Danny on 12/3/2014.
 */
public class UtDebugPrint {

        public interface Debuggable{
                public List<String> toDebugInfo();
        }

        public static void print(List<String> out){
                for (String s : out) {
                        System.out.println(s);
                }
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

        public static List<String> getDebugInfo(Object o) {
                if(o instanceof Debuggable){
                        Debuggable dbg = (Debuggable) o;
                        return dbg.toDebugInfo();
                }
                return object(o);
        }

        public static List<String> object(Object o){
                List<String> out = out();

                format(out, o.getClass().getSimpleName());

                try {
                        Field[] fields = o.getClass().getDeclaredFields();
                        for (Field field : fields) {
                                field.setAccessible(true);
                                format(out, "%-15s %15s %s",field.getType().getSimpleName(),field.getName()+":", field.get(o));
                        }
                }catch (IllegalAccessException e) {
                        format(out, e.getMessage());
                }

                return out;
        }


}
