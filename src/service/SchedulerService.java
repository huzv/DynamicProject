package service;

import algorithms.Dynamic;
import algorithms.Dynamic.DPResult;
import algorithms.Greedy;
import algorithms.Greedy.GreedyResult;
import javafx.scene.Node;
import model.Task;
import util.ArrayList;
import util.TableRenderer;

public class SchedulerService {

    // Simple data class instead of record
    public static class SchedulerResult {
        public int dpValue;
        public String dpTimeLabel;
        public int greedyValue;
        public String greedyTimeLabel;
        public String logs;
        public Node vizNode;
        
        public SchedulerResult(int dpValue, String dpTimeLabel, 
                               int greedyValue, String greedyTimeLabel,
                               String logs, Node vizNode) {
            this.dpValue = dpValue;
            this.dpTimeLabel = dpTimeLabel;
            this.greedyValue = greedyValue;
            this.greedyTimeLabel = greedyTimeLabel;
            this.logs = logs;
            this.vizNode = vizNode;
        }
    }

    public SchedulerResult execute(ArrayList<Task> tasks, float capacity) {
        // Run DP
        long dpStart = System.nanoTime();
        Dynamic dpSolver = new Dynamic();
        DPResult dpResult = dpSolver.solve(tasks, capacity);
        long dpEnd = System.nanoTime();
        double dpTimeMs = (dpEnd - dpStart) / 1_000_000.0;

        // Run Greedy
        long greedyStart = System.nanoTime();
        Greedy greedySolver = new Greedy();
        GreedyResult greedyResult = greedySolver.solve(tasks, capacity);
        long greedyEnd = System.nanoTime();
        double greedyTimeMs = (greedyEnd - greedyStart) / 1_000_000.0;

        // Format labels
        String dpTimeLabel = String.format("Time Used: %s/%sh",
            formatDuration(dpResult.totalTime), formatDuration(capacity));
        String greedyTimeLabel = String.format("Time Used: %s/%sh",
            formatDuration(greedyResult.totalTime), formatDuration(capacity));

        // Build logs & visualization
        String logs = buildLogs(dpResult, greedyResult, dpTimeMs, greedyTimeMs);
        Node vizNode = TableRenderer.renderDPTable(dpResult.dp, dpResult.take);

        return new SchedulerResult(
            dpResult.totalValue,
            dpTimeLabel,
            greedyResult.totalValue,
            greedyTimeLabel,
            logs,
            vizNode
        );
    }

    private String buildLogs(DPResult dpResult, GreedyResult greedyResult,
                             double dpTimeMs, double greedyTimeMs) {
        StringBuilder sb = new StringBuilder(1024);

        sb.append("=== DYNAMIC PROGRAMMING (OPTIMAL) ===\n");
        sb.append(String.format(" Execution Time: %.3f ms\n", dpTimeMs));
        sb.append(String.format(" Total Value: %d | Time Used: %sh\n\n",
            dpResult.totalValue, formatDuration(dpResult.totalTime)));
        for (Task t : dpResult.chosen) {
            sb.append(String.format(" [] %s (%sh, v:%d)\n",
                t.getName(), formatDuration(t.getDuration()), t.getProductivity()));
        }

        sb.append("\n=== GREEDY SOLUTION ===\n");
        sb.append(String.format(" Execution Time: %.3f ms\n", greedyTimeMs));
        sb.append(String.format(" Total Value: %d | Time Used: %sh\n\n",
            greedyResult.totalValue, formatDuration(greedyResult.totalTime)));
        for (Task t : greedyResult.chosen) {
            sb.append(String.format(" [] %s (%sh, v:%d)\n",
                t.getName(), formatDuration(t.getDuration()), t.getProductivity()));
        }

        sb.append("\n=== COMPARISON ===\n");
        double ratio = dpTimeMs > greedyTimeMs ? dpTimeMs / greedyTimeMs : greedyTimeMs / dpTimeMs;
        String speed = dpTimeMs > greedyTimeMs ? "slower" : "faster";
        sb.append(String.format(" DP is %.2fx %s than Greedy\n", ratio, speed));

        if (dpResult.totalValue > greedyResult.totalValue) {
            sb.append(String.format(" DP found %d more value than Greedy\n",
                dpResult.totalValue - greedyResult.totalValue));
        } else if (dpResult.totalValue == greedyResult.totalValue) {
            sb.append(" Both algorithms found the same optimal value\n");
        }

        return sb.toString();
    }

    private String formatDuration(float d) {
        return d == (long) d ? String.format("%d", (long) d) : String.valueOf(d);
    }
}