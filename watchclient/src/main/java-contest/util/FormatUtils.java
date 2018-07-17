package util;

import java.text.DecimalFormat;

/**
 * Created by lan on 2018/1/18.
 */
public class FormatUtils {
    public static String formatMoney(float money) {
        if (money == 0) {
            return "0";
        } else {
            DecimalFormat df = new DecimalFormat("#0.00");
            return df.format(money);
        }
    }

    public static String formatRank(int rank) {
        if (rank == -1) {
            return "100+";
        } else if (rank > 0 && rank <= 100) {
            return String.valueOf(rank);
        } else {
            return "-";
        }
    }
}
