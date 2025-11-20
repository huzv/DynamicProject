package algorithms;

import model.Task;
import util.ArrayList;

public class Dynamic {
    public static class DPResult {
        public final int[][] dp;
        public final boolean[][] take;
        public final ArrayList<Task> chosen;
        public final int totalTime;
        public final int totalValue;

        public DPResult(int[][] dp, boolean[][] take, ArrayList<Task> chosen, int totalTime, int totalValue) {
            this.dp = dp;
            this.take = take;
            this.chosen = chosen;
            this.totalTime = totalTime;
            this.totalValue = totalValue;
        }
    }

    public DPResult solve(ArrayList<Task> tasks, int totalHours) {
        if (tasks == null) throw new IllegalArgumentException("tasks null");
        if (totalHours < 0) throw new IllegalArgumentException("totalHours cannot be negative");
        int n = tasks.size();
        int T = totalHours;
        int[][] dp = new int[n + 1][T + 1];
        boolean[][] take = new boolean[n + 1][T + 1];

        for (int i = 1; i <= n; i++) {
            Task task = tasks.get(i - 1);
            int w = task.getDuration();
            int v = task.getProductivity();
            for (int t = 0; t <= T; t++) {
                int notake = dp[i - 1][t];
                int takeVal = (w <= t) ? v + dp[i - 1][t - w] : Integer.MIN_VALUE / 4;
                if (takeVal > notake) {
                    dp[i][t] = takeVal;
                    take[i][t] = true;
                } else {
                    dp[i][t] = notake;
                    take[i][t] = false;
                }
            }
        }

        ArrayList<Task> chosen = new ArrayList<>();
        int totalTime = 0;
        int totalValue = dp[n][T];
        int t = T;
        for (int i = n; i >= 1; i--) {
            if (take[i][t]) {
                Task task = tasks.get(i - 1);
                chosen.add(task);
                totalTime += task.getDuration();
                t -= task.getDuration();
            }
        }
        chosen.reverse();
        return new DPResult(dp, take, chosen, totalTime, totalValue);
    }
}