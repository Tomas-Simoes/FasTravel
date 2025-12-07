package ubi.pdm.fastravel.frontend.HistoryModule;

public class HistoryEntry {
    private int id;
    private String date;
    private String duration;
    private String startTime;
    private String endTime;
    private String origin;
    private String destination;
    private int mainModeIcon; // Drawable ID (e.g., R.drawable.ic_bus)

    // Constructor
    public HistoryEntry(int id, String date, String duration, String startTime, String endTime, String origin, String destination, int mainModeIcon) {
        this.id = id;
        this.date = date;
        this.duration = duration;
        this.startTime = startTime;
        this.endTime = endTime;
        this.origin = origin;
        this.destination = destination;
        this.mainModeIcon = mainModeIcon;
    }

    // Getters
    public int getId() { return id; }
    public String getDate() { return date; }
    public String getDuration() { return duration; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public int getMainModeIcon() { return mainModeIcon; }
}
