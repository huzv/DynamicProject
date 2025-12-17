package algorithms;

import model.Task;
import util.ArrayList;

public class Greedy {
    
    public static class GreedyResult {
        public final ArrayList<Task> chosen;
        public final float totalTime;
        public final int totalValue;

        public GreedyResult(ArrayList<Task> chosen, float totalTime, int totalValue) {
            this.chosen = chosen;
            this.totalTime = totalTime;
            this.totalValue = totalValue;
        }
    }

    public GreedyResult solve(ArrayList<Task> tasks, float totalHours) {
        if (tasks == null || totalHours < 0) {
            throw new IllegalArgumentException("Invalid input");
        }
        
        int n = tasks.size();
        
        // Optimization 1: Indirect Sorting
        // Instead of sorting the heavy Task list directly (which changes UI order),
        // we sort an array of integers representing the indices. 
        // Swapping ints is much faster than swapping object references.
        int[] indices = new int[n];
        
        // Optimization 2: Pre-compute Ratios (Memoization)
        // Division is expensive. Calculating (value / duration) inside the sort comparator
        // would result in O(N log N) divisions. By pre-calculating, we reduce this to O(N).
        double[] ratios = new double[n];
        
        for (int i = 0; i < n; i++) {
            indices[i] = i;
            Task t = tasks.get(i);
            float duration = t.getDuration();
            // Avoid division by zero; treat 0-duration as infinite value
            ratios[i] = (duration == 0) ? Double.MAX_VALUE : (double) t.getProductivity() / duration;
        }
        
        // Run custom QuickSort on the indices based on the pre-computed ratios
        quickSort(indices, ratios, 0, n - 1);
        
        ArrayList<Task> chosen = new ArrayList<>();
        float usedTime = 0;
        int totalValue = 0;
        
        // Iterate through the sorted indices (Best Ratio -> Worst Ratio)
        for (int i = 0; i < n; i++) {
            int taskIndex = indices[i];
            Task task = tasks.get(taskIndex);
            float duration = task.getDuration();
            
            if (usedTime + duration <= totalHours) {
                chosen.add(task);
                usedTime += duration;
                totalValue += task.getProductivity();
            }
        }
        
        return new GreedyResult(chosen, usedTime, totalValue);
    }

    /**
     * Standard QuickSort implementation.
     * Time Complexity: O(N log N) average.
     * We sort 'indices' but compare values from 'ratios'.
     */
    private void quickSort(int[] indices, double[] ratios, int low, int high) {
        if (low < high) {
            int partitionIndex = partition(indices, ratios, low, high);
            quickSort(indices, ratios, low, partitionIndex - 1);
            quickSort(indices, ratios, partitionIndex + 1, high);
        }
    }

    /**
     * Partitions the array for QuickSort.
     * Orders Descending (Highest Ratio first).
     */
    private int partition(int[] indices, double[] ratios, int low, int high) {
        // Pivot selection: High element
        double pivotValue = ratios[indices[high]];
        int i = (low - 1);

        for (int j = low; j < high; j++) {
            // Check if current element's ratio is GREATER than pivot (Descending order)
            if (ratios[indices[j]] > pivotValue) {
                i++;
                swap(indices, i, j);
            }
        }
        swap(indices, i + 1, high);
        return i + 1;
    }

    private void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}