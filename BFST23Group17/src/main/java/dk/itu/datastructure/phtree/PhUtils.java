package dk.itu.datastructure.phtree;

import java.util.Arrays;

public class PhUtils {

    public static boolean[] floatToBits(float val) {
        long sl = toSortableLong(val < 0 ? -val : val);
        return longToBits(sl, val < 0);
    }

    public static boolean[] longToBits(long val, boolean isNeg) {
        boolean[] bits = new boolean[32];
        int count = 0;
        while (val > 1) {
            bits[31-count] = val%2 == 1;
            val = val/2;
            count++;
        }
        bits[31-count] = val == 1;

        if (isNeg) bits[0] = true;

        return bits;

    }

    public static long toSortableLong(float value) {
        int r =  Float.floatToRawIntBits(value);
        return (r >= 0) ? r : r ^ 0x7FFFFFFF;
    }

    public static float toFloat(long value) {
        return (float) Double.longBitsToDouble(value >= 0 ? value : value ^ 0x7FFFFFFFFFFFFFFFL);
    }

    // TODO: Optimize speed of function - consider alternatives
    public static boolean[] boundToHyperbox(float l, float b, float r, float t) {
        boolean[] bits = new boolean[128];

        boolean[] lx = floatToBits(l);
        boolean[] by = floatToBits(b);
        boolean[] rx = floatToBits(r);
        boolean[] ty = floatToBits(t);

        for (int i = 0 ; i<32 ; i++) {
            bits[4*i] = lx[i];
            bits[4*i+1] = by[i];
            bits[4*i+2] = rx[i];
            bits[4*i+3] = ty[i];
        }
        return bits;
    }

    public static float bitsToFloat(boolean[] bits) {
        int bitsAsInt = 0;
        for (int i = 0; i < 32; i++) {
            bitsAsInt = (bitsAsInt << 1) | (bits[i] ? 1 : 0);
        }
        return Float.intBitsToFloat(bitsAsInt);
    }

    public static int bitsToInt(boolean[] bits) {
        int ret = 0;
        for (int i = 0 ; i<bits.length ; i++) {
            ret += (bits[i] ? 1 : 0)*Math.pow(2, bits.length-(i+1));
        }
        return ret;
    }

    public static int bitsToInt(boolean bit0, boolean bit1, boolean bit2, boolean bit3) {
        return (bit0 ? 1 : 0)*8 + (bit1 ? 1 : 0)*4 + (bit2 ? 1 : 0)*2 + (bit3 ? 1 : 0);
    }

    public static boolean[] intToBits(int i) {
        boolean[] bits = new boolean[4];
        String[] bs = Integer.toBinaryString(i).split("");
        for (int in = 0 ; in<bs.length ; in++) {
            bits[in+(4-bs.length)] = bs[in].equals("1");
        }
        return bits;
    }

    public static boolean compareEqualBits(boolean[] bits1, boolean[] bits2) {
        if (bits1.length != bits2.length) return false;
        for (int i = 0 ; i < bits1.length ; i++) {
            if (bits1[i] != bits2[i]) return false;
        }
        return true;
    }

    public static boolean compareEqualBits(boolean[] bits1, boolean[] bits2, int index) {
        for (int i = 0 ; i < 4 ; i++) {
            if (bits1[index*4+i] != bits2[index*4+i]) return false;
        }
        return true;
    }

    public static boolean compareUnequalBits(boolean[] bits1, boolean[] bits2) {
        return !compareEqualBits(bits1, bits2);
    }

    public static boolean compareUnequalBits(boolean[] bits1, boolean[] bits2, int index) {
        return !compareEqualBits(bits1, bits2, index);
    }

    /**
     * Compares float bits
     * @param f1 Float 1
     * @param f2 Float 2
     * @return negative if f1 < f2 - 0 if f1==f2 - positive if f1 > f2
     */
    public static int compareFloatBits(boolean[] f1, boolean[] f2) {
        if (f1.length > 32 || f2.length > 32) throw new IllegalArgumentException("Float bit arrays must be > 32 bits");
        float f1f = bitsToFloat(f1), f2f = bitsToFloat(f2);
        return (int) ((f1f-f2f)*1000000.0f);
    }

    public static int compareFloats(float f1, float f2) {
        return (int) ((f1-f2)*100000000.0f);
    }

    // Created with assistance of AI recourses
    public static int compareFloats(boolean[] b, boolean[] a) {
        int signA = a[0] ? -1 : 1; // get sign of a (0 is positive, 1 is negative)
        int signB = b[0] ? -1 : 1; // get sign of b

        // if signs are different, return the sign of the difference
        if (signA != signB) {
            return signA - signB;
        }

        // extract exponent and mantissa from a and b
        int expA = 0, expB = 0;
        for (int i = 1; i <= 8; i++) {
            expA = (expA << 1) | (a[i] ? 1 : 0);
            expB = (expB << 1) | (b[i] ? 1 : 0);
        }
        expA -= 127; // subtract bias to get actual exponent
        expB -= 127;

        int mantA = 0, mantB = 0;
        for (int i = 9; i <= 31; i++) {
            mantA = (mantA << 1) | (a[i] ? 1 : 0);
            mantB = (mantB << 1) | (b[i] ? 1 : 0);
        }
        mantA |= (1 << 23); // add implied bit to mantissa
        mantB |= (1 << 23);

        // handle special cases
        if (expA == 128) {
            if (mantA == 0) {
                return signA == 1 ? 1 : -1; // a is NaN
            } else {
                return signA == 1 ? -1 : 1; // a is infinity
            }
        } else if (expB == 128) {
            if (mantB == 0) {
                return signB == 1 ? -1 : 1; // b is NaN
            } else {
                return signB == 1 ? 1 : -1; // b is infinity
            }
        } else if (expA == -127 && mantA == 1) {
            if (expB == -127 && mantB == 1) {
                return 0; // both a and b are zero
            } else {
                return signA == 1 ? -1 : 1; // a is subnormal, b is not
            }
        } else if (expB == -127 && mantB == 1) {
            return signB == 1 ? 1 : -1; // b is subnormal, a is not
        }

        // calculate the actual value of a and b
        double valueA = Math.pow(-1, signA) * (1 + mantA / Math.pow(2, 23)) * Math.pow(2, expA);
        double valueB = Math.pow(-1, signB) * (1 + mantB / Math.pow(2, 23)) * Math.pow(2, expB);

        // compare the values and return the result
        if (valueA < valueB) {
            return -1;
        } else if (valueA > valueB) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * How floats work:
     * bit 0
     * Sign bit
     * 0 if positive, 1 if negative
     *
     * bits 1 -> 8
     * Exponent bits/byte
     * Exponent is -126 by default
     * Depending on value from exponent byte, calculation is:
     * -126 + (exp byte val) - 1
     *
     * bits 9 -> 31
     * Normal 22 bits value where bit is 2^-(bit pos)
     * Gets multiplied by exponent
     */

    /**
     * Returns the smallest possible float, given an initial n bits
     * @param bits float in bits to complete
     * @return completed smallest possible float
     */
    public static boolean[] smallestPossibleFloat(boolean[] bits) {
        boolean[] ret = new boolean[32];
        if (bits.length == 0) {
            // bit 0 => true (negative)
            // exp bits => false
            // val bits => true
            ret[0] = true;
            Arrays.fill(ret, 1, 9, false);
            Arrays.fill(ret, 9, 32, true);
        } else if (bits.length == 1) {
            ret[0] = bits[0];
            // bit 0 => input
            // exp bits => false
            // val bits => true
            Arrays.fill(ret, 1, 9, false);
            Arrays.fill(ret, 9, 32, true);
        } else {
            // Exp bits false if no val
            // Val bits true if no val
            ret = Arrays.copyOf(bits, 32);
            if (bits.length-1 < 8) {
                // Has exp bits to fill
                Arrays.fill(ret, bits.length, 9, false);
                // Exp is negative
//                Arrays.fill(ret, 9, 32, (-126 + bitsToInt(Arrays.copyOfRange(ret, 1, 9)) - 1) < 0);
                Arrays.fill(ret, 9, 32, false);

            } else if (bits.length-1 < 32) {
                // Has val bits to fill
//                Arrays.fill(ret, bits.length, 32, (-126 + bitsToInt(Arrays.copyOfRange(ret, 1, 9)) - 1) < 0);
                Arrays.fill(ret, bits.length, 32, false);
            }
        }
        return ret;
    }

    public static boolean[] biggestPossibleFloat(boolean[] bits) {
        boolean[] ret = new boolean[32];

        // Biggest value is sign false - rest true but last exp bit (index 8 - 9th bit)

        if (bits.length == 0) {
            ret[0] = false;
            Arrays.fill(ret, 1, 32, true);
            ret[8] = false;
        } else if (bits.length == 1) {
            ret[0] = bits[0];
            if (bits[0]) {
                Arrays.fill(ret, 1, 32, false);
            } else {
                Arrays.fill(ret, 1, 32, true);
                ret[8] = false;
            }
        } else {
            ret = Arrays.copyOf(bits, 32);
            if (bits.length-1 < 8) {
                // Has exp bits to fill
                Arrays.fill(ret, bits.length, 9, true);
                ret[8] = !ret[1];

//                Arrays.fill(ret, 9, 32, (-126 + bitsToInt(Arrays.copyOfRange(ret, 1, 9)) - 1) >= 0);
                Arrays.fill(ret, 9, 32, true);
            } else if (bits.length-1 < 32) {
                // Has val bits to fill
//                Arrays.fill(ret, bits.length, 32, (-126 + bitsToInt(Arrays.copyOfRange(ret, 1, 9)) - 1) >= 0);
                Arrays.fill(ret, bits.length, 32, true);
            }
        }

        return ret;
    }
}
