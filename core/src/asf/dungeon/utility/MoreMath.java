package asf.dungeon.utility;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import java.math.BigDecimal;

/**
 * Created by danny on 10/20/14.
 */
public class MoreMath {

        public static Vector3 quaternionToEuler(Quaternion quaternion, Vector3 store) {
                float sqw = quaternion.w * quaternion.w;
                float sqx = quaternion.x * quaternion.x;
                float sqy = quaternion.y * quaternion.y;
                float sqz = quaternion.z * quaternion.z;
                store.z = (MathUtils.atan2(2.0f * (quaternion.x * quaternion.y + quaternion.z * quaternion.w), (sqx - sqy - sqz + sqw)) * (MathUtils.radDeg));
                store.x = (MathUtils.atan2(2.0f * (quaternion.y * quaternion.z + quaternion.x * quaternion.w), (-sqx - sqy + sqz + sqw)) * (MathUtils.radDeg));
                store.y = (MoreMath.asin(-2.0f * (quaternion.x * quaternion.z - quaternion.y * quaternion.w)) * MathUtils.radDeg);
                return store;

        }

        /**
         * Returns the arc sine of an angle given in radians.<br>
         * Special cases:
         * <ul><li>If fValue is smaller than -1, then the result is -HALF_PI.
         * <li>If the argument is greater than 1, then the result is HALF_PI.</ul>
         * @param fValue The angle, in radians.
         * @return fValue's asin
         * @see java.lang.Math#asin(double)
         */
        public static float asin(float fValue) {
                if (-1.0f < fValue) {
                        if (fValue < 1.0f) {
                                return (float) Math.asin(fValue);
                        }

                        return MathUtils.PI/2f;
                }

                return -MathUtils.PI/2f;
        }

        /**
         *
         * @param input
         * @param match
         * @return input * -1f if the sign of input and match are not the same, otherwise just returns input.
         */
        public static float matchSign(float input, float match) {
                if (match < 0 && input > 0) {
                        return -input;
                } else if (match > 0 && input < 0) {
                        return -input;
                }
                return input;
        }

        public static boolean isBetween(float input, float min, float max) {
                return input >= min && input <= max;
        }

        public static boolean isEven(int n) {
                return n % 2 == 0;
        }

        public static float clamp(float input, float min, float max) {
                return (input < min) ? min : (input > max) ? max : input;
        }

        public static int clamp(int input, int min, int max) {
                return (input < min) ? min : (input > max) ? max : input;
        }

        /**
         * Linear interpolation from startValue to endValue by the given percent.
         * Basically: ((1 - percent) * startValue) + (percent * endValue)
         *
         * @param scale
         *            scale value to use. if 1, use endValue, if 0, use startValue.
         * @param startValue
         *            Begining value. 0% of f
         * @param endValue
         *            ending value. 100% of f
         * @return The interpolated value between startValue and endValue.
         */
        public static float interpolateLinear(float scale, float startValue, float endValue) {
                if (startValue == endValue) {
                        return startValue;
                }
                if (scale <= 0f) {
                        return startValue;
                }
                if (scale >= 1f) {
                        return endValue;
                }
                return ((1f - scale) * startValue) + (scale * endValue);
        }


        /**
         * Linear interpolation from startValue to endValue by the given percent.
         * Basically: ((1 - percent) * startValue) + (percent * endValue)
         *
         * @param scale
         *            scale value to use. if 1, use endValue, if 0, use startValue.
         * @param startValue
         *            Begining value. 0% of f
         * @param endValue
         *            ending value. 100% of f
         * @param store a vector3f to store the result
         * @return The interpolated value between startValue and endValue.
         */
        public static void interpolateLinear(float scale, Vector3 startValue, Vector3 endValue, Vector3 store) {
                store.x = interpolateLinear(scale, startValue.x, endValue.x);
                store.y = interpolateLinear(scale, startValue.y, endValue.y);
                store.z = interpolateLinear(scale, startValue.z, endValue.z);
        }

        public static void normalize(Vector3 vec){
                float length = vec.x * vec.x + vec.y * vec.y + vec.z * vec.z;
                if (length != 1f && length != 0f){
                        length = 1.0f / (float)Math.sqrt(length);
                        vec.x *= length;
                        vec.y *= length;
                        vec.z *= length;
                }

        }


        /**
         * use this value as the "scale" value in interpolation to use hermite acceleration (easily start up, easily slow down at the end)
         *
         * @param t
         * @return
         */
        public static float hermiteT(float t) {
                return (3.0f * t * t) - (2.0f * t * t * t);
        }

        public static float smallest(float val1, float val2) {
                return (val1 < val2 ? val1 : val2);
        }

        public static float smallest(float val1, float val2, float... moreVals) {
                float smallest = smallest(val1, val2);

                for (float m : moreVals) {
                        smallest = smallest(m, smallest);
                }

                return smallest;
        }

        public static int smallest(int val1, int val2) {
                return (val1 < val2 ? val1 : val2);
        }

        public static float largest(float val1, float val2) {
                return (val1 > val2 ? val1 : val2);
        }

        public static float largest(float val1, float val2, float... moreVals) {
                float largest = largest(val1, val2);

                for (float m : moreVals) {
                        largest = largest(m, largest);
                }

                return largest;
        }

        public static int largest(int val1, int val2) {
                return (val1 > val2 ? val1 : val2);
        }

        public static String round(Quaternion quaternion, int decimalPlace) {
                if (quaternion == null) {
                        return "null";
                }
                return "(" + round(quaternion.x, decimalPlace) + ", " + round(quaternion.y, decimalPlace) + ", " + round(quaternion.z, decimalPlace) + ", " + round(quaternion.w, decimalPlace) + ")";
        }

        public static String round(Vector3 vec, int decimalPlace) {
                if (vec == null) {
                        return "null";
                }
                return "(" + round(vec.x, decimalPlace) + ", " + round(vec.y, decimalPlace) + ", " + round(vec.z, decimalPlace) + ")";
        }

        public static BigDecimal round(float d, int decimalPlace) {
                BigDecimal bd = new BigDecimal(Float.toString(d));
                bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
                return bd;
        }

        public static BigDecimal round(double d, int decimalPlace) {
                BigDecimal bd = new BigDecimal(Double.toString(d));
                bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
                return bd;
        }

        public static float firstQuartile(float val1, float val2) {
                return midpoint(val1, midpoint(val1, val2));
        }

        public static float thirdQuartile(float val1, float val2) {
                return midpoint(val2, midpoint(val1, val2));
        }

        public static float midpoint(float val1, float val2) {
                return (val1 + val2) / 2f;
        }

        public static int midpoint(int val1, int val2) {
                return ((val1 + val2) / 2);
        }

        /**
         * Absolute value of the difference of two values
         *
         * @param val1
         * @param val2
         * @return
         */
        public static float range(float val1, float val2) {return MoreMath.abs(val1 - val2);}

        public static float range(Vector3 val1, Vector3 val2){
                float x = val2.x - val1.x;
                float y = val2.y - val1.y;
                float z = val2.z - val1.z;
                float len = (float)Math.sqrt(x * x + y * y + z * z);
                return len;

        }

        public static float abs(float value){
                if(value < 0){
                        return -value;
                }
                return value;
        }

        public static float scalarLimitsInterpolation(float input, float oldMin, float oldMax, float newMin, float newMax) {
                float percent = MoreMath.percentOfScalar(MoreMath.clamp(input, oldMin, oldMax), oldMin, oldMax);
                return MoreMath.scalarOfPercent(percent, newMin, newMax);
        }

        public static int scalarLimitsInterpolation(float input, float oldMin, float oldMax, int newMin, int newMax) {
                float percent = MoreMath.percentOfScalar(MoreMath.clamp(input, oldMin, oldMax), oldMin, oldMax);
                return MoreMath.scalarOfPercent(percent, newMin, newMax);
        }

        public static int scalarLimitsInterpolation(int input, int oldMin, int oldMax, int newMin, int newMax) {
                float percent = MoreMath.percentOfScalar(MoreMath.clamp(input, oldMin, oldMax), oldMin, oldMax);
                return MoreMath.scalarOfPercent(percent, newMin, newMax);
        }

        public static float scalarLimitsExtrapolation(float input, float oldMin, float oldMax, float newMin, float newMax) {
                float percent = MoreMath.percentOfScalar(input, oldMin, oldMax);
                return MoreMath.scalarOfPercent(percent, newMin, newMax);
        }

        public static int scalarLimitsExtrapolation(float input, float oldMin, float oldMax, int newMin, int newMax) {
                float percent = MoreMath.percentOfScalar(input, oldMin, oldMax);
                return MoreMath.scalarOfPercent(percent, newMin, newMax);
        }

        public static int scalarLimitsExtrapolation(int input, int oldMin, int oldMax, int newMin, int newMax) {
                float percent = MoreMath.percentOfScalar(input, oldMin, oldMax);
                return MoreMath.scalarOfPercent(percent, newMin, newMax);
        }

        /**
         * Takes a float with a min and max value and makes it between 0 and 1 with the same percentage value. reverse of scalarOfPercent()
         *
         *
         * @param input val between oldMin and oldMax
         * @param oldMin
         * @param oldMax
         * @return value between 0 and 1
         */
        public static float percentOfScalar(float input, float oldMin, float oldMax) {
                return (input - oldMin) / (oldMax - oldMin);
        }

        /**
         * Take a float between 0 and 1 and scale it to be between newMin and newMax, reverse of percentOfScalar()
         *
         *
         * @param input val between 0 and 1
         * @param newMin
         * @param newMax
         * @return value between newMin and newMax
         */
        public static float scalarOfPercent(float input, float newMin, float newMax) {
                return (newMin + (input * (newMax - newMin)));
        }

        public static int scalarOfPercent(float input, int newMin, int newMax) {
                return (int) (Math.round((newMin + (MoreMath.abs(input) * (newMax - newMin)))) * MoreMath.sign(input));
        }

        /**
         * Returns 1 if the number is positive, -1 if the number is negative, and 0 otherwise
         * @param iValue The integer to examine.
         * @return The integer's sign.
         */
        public static int sign(int iValue) {
                if (iValue > 0) {
                        return 1;
                }
                if (iValue < 0) {
                        return -1;
                }
                return 0;
        }


        /**
         * Returns 1 if the number is positive, -1 if the number is negative, and 0 otherwise
         * @param fValue The float to examine.
         * @return The float's sign.
         */
        public static float sign(float fValue) {
                return Math.signum(fValue);
        }

        public static float randomFloat(float min, float max) {
                return MathUtils.random(min, max); //return (min + (FastMath.rand.nextFloat() * (max - min)));
        }


        public static float randomSign() {

                return (MathUtils.randomBoolean() ? 1f : -1f);
        }


        private MoreMath() {
        }
}
