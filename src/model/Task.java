package model;

public class Task {
    private String name;
    // Optimization: Store duration as half-hour units (short)
    // 1.5 hours -> 3 units. Max duration: ~16,000 hours (plenty)
    private short durationUnits; 
    // Optimization: Store productivity as short (assuming < 32,767)
    private short productivity;

    public Task(String name, float duration, int productivity) {
        this.name = name;
        setDuration(duration);
        setProductivity(productivity);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public float getDuration() { 
        // Convert internal unit back to float for UI/Interface compatibility
        return durationUnits / 2.0f; 
    }
    
    public void setDuration(float duration) { 
        // Store as half-hour units
        this.durationUnits = (short) Math.round(duration * 2); 
    }
    
    // Internal optimization accessor
    public short getDurationUnits() { return durationUnits; }

    public int getProductivity() { return productivity; }
    
    public void setProductivity(int productivity) { 
        this.productivity = (short) productivity; 
    }

    @Override
    public String toString() {
        // Optimization: Pre-calculate capacity for StringBuilder
        return new StringBuilder(name.length() + 10)
            .append(name).append(", ")
            .append(getDuration()).append(", ")
            .append(productivity).toString();
    }
}