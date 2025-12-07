package ubi.pdm.fastravel.frontend.routes;
import java.io.Serializable;
public class ThemedRoute implements Serializable {
    private String title;
    private String color;
    private int imageResId;
    private String photoUrl;
    private String distanceText;
    private String timeText;
    private String pathText;

    public ThemedRoute( String title, String color, int imageResId, String photoUrl, String distanceText, String timeText, String pathText){

        this.title = title;
        this.color = color;
        this.imageResId = imageResId;

        this.photoUrl = photoUrl;
        this.distanceText = distanceText;
        this.timeText = timeText;
        this.pathText = pathText;
    }

    public String getPhotoUrl() { return photoUrl; }
    public String getDistanceText() { return distanceText; }
    public String getTimeText() { return timeText; }
    public String getPathText() { return pathText; }

    public String getTitle() { return title; }

    public String getColor() { return color; }
    public int getImageResId() { return imageResId;}
}
