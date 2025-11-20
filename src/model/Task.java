package model;
public class Task {
    private String name;
    private int duration;
    private int productivity;

    public Task(String name, int duration, int productivity) {
        this.name = name;
        this.duration = duration;
        this.productivity = productivity;
    }
    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getProductivity() {
        return productivity;
    }

    public void setProductivity(int productivity) {
        this.productivity = productivity;
    }

    @Override
    public String toString() {
        return name + ", " + duration + ", " + productivity;
    }
}
