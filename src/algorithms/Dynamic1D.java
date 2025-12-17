package algorithms;

import model.Task;
import util.ArrayList;

public class Dynamic {
    
    public static class DPResult {
        public final int[] dp;
        public final ArrayList<Task> chosen;
        public final float totalTime;
        public final int totalValue;

        public DPResult(int[] dp, ArrayList<Task> chosen, float totalTime, int totalValue) {
            this.dp = dp;
            this.chosen = chosen;
            this.totalTime = totalTime;
            this.totalValue = totalValue;
        }
    }

    public DPResult solve(ArrayList<Task> tasks, float totalHours) {
        if (tasks == null) throw new IllegalArgumentException("Tasks cannot be null");

        int n = tasks.size();
        if (n == 0) {
            return new DPResult(new int[1], new ArrayList<>(), 0, 0);
        }
        
        int capacityUnits = (int) Math.round(totalHours * 2);
        
        // Single 1D array - O(W) space instead of O(n×W)
        int[] dp = new int[capacityUnits + 1];
        
        // Track which item was last added at each capacity - O(W) space
        int[] lastItemAt = new int[capacityUnits + 1];
        for (int i = 0; i <= capacityUnits; i++) {
            lastItemAt[i] = -1;
        }
        
        // Build DP - iterate backwards through capacity to ensure 0/1 property
        for (int i = 0; i < n; i++) {
            Task task = tasks.get(i);
            int weight = task.getDurationUnits();
            int value = task.getProductivity();
            
            for (int w = capacityUnits; w >= weight; w--) {
                int newValue = dp[w - weight] + value;
                if (newValue > dp[w]) {
                    dp[w] = newValue;
                    lastItemAt[w] = i;
                }
            }
        }
        
        // Reconstruct: check each item's contribution
        ArrayList<Task> chosen = new ArrayList<>();
        float actualTime = 0;
        int remainingCapacity = capacityUnits;
        int remainingValue = dp[capacityUnits];
        
        // Iterate backwards, checking if each item contributes to optimal
        for (int i = n - 1; i >= 0 && remainingValue > 0; i--) {
            Task task = tasks.get(i);
            int weight = task.getDurationUnits();
            int value = task.getProductivity();
            
            if (remainingCapacity >= weight && lastItemAt[remainingCapacity] == i) {
                chosen.add(task);
                actualTime += task.getDuration();
                remainingCapacity -= weight;
                remainingValue -= value;
            }
        }
        
        chosen.reverse();
        
        return new DPResult(dp, chosen, actualTime, dp[capacityUnits]);
    }
}        
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