/*
 * Code taken from Modern Industrialization
 * https://github.com/AztechMC/Modern-Industrialization/blob/1.20.1/src/main/java/aztech/modern_industrialization/util/TextHelper.java
 * Thanks!
 */

package net.natte.tankstorage.client.helpers;

public class TextHelper {

    public record Amount(String digit, String unit) {
    }

    public record MaxedAmount(String digit, String maxDigit, String unit) {
    }

    public static final String[] units = new String[] { "k", "M", "G", "T", "P", "E" };
    public static final long[] nums = new long[] {
            1000L, 1000_000L, 1000_000_000L, 1000_000_000_000L, 1000_000_000_000_000L, 1000_000_000_000_000_000L };

    public static String getAmount(double amount, long num) {
        double fract = amount / num;
        if (fract < 10) {
            return String.format("%.3f", fract);
        } else if (fract < 100) {
            return String.format("%.2f", fract);
        } else {
            return String.format("%.1f", fract);
        }
    }

    public static MaxedAmount getMaxedAmount(double amount, double max) {
        if (max < 100000) {
            return new MaxedAmount(getAmount(amount, 1), getAmount(max, 1), "");
        } else {
            int i = 0;
            while (max / nums[i] >= 1000) {
                i++;
            }
            return new MaxedAmount(getAmount(amount, nums[i]), getAmount(max, nums[i]), units[i]);
        }
    }
}
