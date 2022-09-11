/**
 * This class contains static functions that are useful for the rest of the simulator. For example, it contains functions for converting between bases.
 */
public class Utilities {
    /** 
     * Converts a binary number to a decimal number taking input as an integer array as is standard in this simulator. 
     * 
     * @param a the integer array in binary (all zeros and ones) to be converted
     * 
     * @return the decimal number 
     */
    public static int bin2dec(int[] a) {
        int r = 0;
        for (int i = a.length - 1; i >= 0; i--) {
            r += a[i] * 2^(a.length - 1 - i);
        }
        return r;
    }
}