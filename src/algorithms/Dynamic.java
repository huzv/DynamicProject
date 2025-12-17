package algorithms;

import model.Task;
import util.ArrayList;

public class Dynamic {
    
    public static class DPResult {
        public final int[][] dp;
        public final boolean[][] take;
        public final ArrayList<Task> chosen;
        public final float totalTime;
        public final int totalValue;

        public DPResult(int[][] dp, boolean[][] take, ArrayList<Task> chosen, float totalTime, int totalValue) {
            this.dp = dp;
            this.take = take;
            this.chosen = chosen;
            this.totalTime = totalTime;
            this.totalValue = totalValue;
        }
    }

    public DPResult solve(ArrayList<Task> tasks, float totalHours) {
        if (tasks == null) throw new IllegalArgumentException("Tasks cannot be null");

        int n = tasks.size();
        
        // Since durations are in 0.5 increments, we scale everything by 2.
        // 1.5 hours becomes 3 units. Total capacity is scaled similarly.
        int capacityUnits = (int) Math.round(totalHours * 2);
        
        // dp[i][w] stores the max productivity using the first 'i' items with capacity 'w'.
        int[][] dp = new int[n + 1][capacityUnits + 1];
        
        // The 'take' table allows us to reconstruct the solution later.
        // If take[i][w] is true, it means item 'i' was included in the optimal solution for capacity 'w'.
        boolean[][] take = new boolean[n + 1][capacityUnits + 1];
        
        for (int i = 1; i <= n; i++) {
            Task task = tasks.get(i - 1);
            int weight = task.getDurationUnits(); 
            int value = task.getProductivity();
            
            for (int w = 0; w <= capacityUnits; w++) {
                // Don't include the current task. Value is same as previous row.
                int valueWithout = dp[i - 1][w];
                
                // Include the current task (only if it fits).
                // Value is current task's value + best value achievable with remaining capacity.
                int valueWith = -1;
                if (weight <= w) {
                    valueWith = value + dp[i - 1][w - weight];
                }
                
                // Decision: Maximize productivity
                if (valueWith > valueWithout) {
                    dp[i][w] = valueWith;
                    take[i][w] = true;
                } else {
                    dp[i][w] = valueWithout;
                    take[i][w] = false;
                }
            }
        }

        // Trace back through the 'take' table to find which tasks were actually chosen.
        ArrayList<Task> chosen = new ArrayList<>();
        float actualTime = 0;
        int currentCapacity = capacityUnits;
        
        for (int i = n; i > 0; i--) {
            if (take[i][currentCapacity]) {
                Task task = tasks.get(i - 1);
                chosen.add(task);
                actualTime += task.getDuration();
                currentCapacity -= task.getDurationUnits();
            }
        }

        // We reverse to match natural reading order.
        chosen.reverse();
        
        return new DPResult(dp, take, chosen, actualTime, dp[n][capacityUnits]);
    }
}