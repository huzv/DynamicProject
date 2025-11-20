package algorithms;

import model.Task;
import util.ArrayList;
import java.util.Comparator;

public class Greedy {
    public static class GreedyResult {
        public final ArrayList<Task> chosen;
        public final int totalTime;
        public final int totalValue;

        public GreedyResult(ArrayList<Task> chosen, int totalTime, int totalValue) {
            this.chosen = chosen;
            this.totalTime = totalTime;
            this.totalValue = totalValue;
        }
    }

    public GreedyResult solve(ArrayList<Task> tasks, int totalHours) {
        if (tasks == null) throw new IllegalArgumentException("tasks null");
        if (totalHours < 0) throw new IllegalArgumentException("totalHours cannot be negative");
        ArrayList<Task> sorted = new ArrayList<>(tasks);
        sorted.sort(Comparator.comparingDouble(t -> -((double) t.getProductivity() / t.getDuration())));
        ArrayList<Task> chosen = new ArrayList<>();
        int time = 0;
        int value = 0;
        for (Task task : sorted) {
            if (time + task.getDuration() <= totalHours) {
                chosen.add(task);
                time += task.getDuration();
                value += task.getProductivity();  // Fixed: was task.getDuration()
            }
        }
        return new GreedyResult(chosen, time, value);
    }
}