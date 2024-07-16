/*
 * Code taken from Modern Industrialization
 * https://github.com/AztechMC/Modern-Industrialization/blob/1.20.1/src/main/java/aztech/modern_industrialization/util/TextHelper.java
 * Thanks!
 */

package net.natte.tankstorage.client.helpers;

public class TextHelper {

    public record MaxedAmount(String digit, String maxDigit, String unit) {
    }

    public static final String[] units = new String[]{"k", "M", "G", "T", "P", "E"};

    private static String getAmount(double frac) {
        if (frac < 10)
            return String.format("%.3f", frac);
        else if (frac < 100)
            return String.format("%.2f", frac);
        else
            return String.format("%.1f", frac);
    }

    public static MaxedAmount getMaxedAmount(double amount, double max) {
        if (max < 100_000) {
            return new MaxedAmount(getAmount(amount), getAmount(max), "");
        } else {
            int i = 0;
            double maxAdj = max;
            double numAdj = amount;
            while (maxAdj >= 1000) {
                i++;
                maxAdj /= 1000;
                numAdj /= 1000;
            }
            return new MaxedAmount(getAmount(numAdj), getAmount(maxAdj), units[i]);
        }
    }
}
