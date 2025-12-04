package ubi.pdm.fastravel.frontend.routes;

public class ThemedRoute {
    private int id;
    private String title;
    private String icon;
    private String color;
    private String description;
    private String details;
    private String duration;
    private int stops;
    private int imageResId; // Novo campo para a imagem
    private String imageUrl; // Opcional: para imagens da internet

    public ThemedRoute(int id, String title, String icon, String color,
                       String description, String details, String duration,
                       int stops, int imageResId) {
        this.id = id;
        this.title = title;
        this.icon = icon;
        this.color = color;
        this.description = description;
        this.details = details;
        this.duration = duration;
        this.stops = stops;
        this.imageResId = imageResId;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getIcon() { return icon; }
    public String getColor() { return color; }
    public String getDescription() { return description; }
    public String getDetails() { return details; }
    public String getDuration() { return duration; }
    public int getStops() { return stops; }
    public int getImageResId() { return imageResId; }
    public String getImageUrl() { return imageUrl; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
