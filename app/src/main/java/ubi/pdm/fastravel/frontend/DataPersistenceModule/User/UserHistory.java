package ubi.pdm.fastravel.frontend.DataPersistenceModule.User;

public class UserHistory {
    public int id;
    public String origin;
    public String destiny;
    public String travel_date; // Snake case para match com Rails
    public String created_at;  // Adicionado pelo serializer
    public int user_id;        // Snake case

    public UserHistory() {}

    public UserHistory(String origin, String destiny, String travelDate) {
        this.origin = origin;
        this.destiny = destiny;
        this.travel_date = travelDate;
    }
}