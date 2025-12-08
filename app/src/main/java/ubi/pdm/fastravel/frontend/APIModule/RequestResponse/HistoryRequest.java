package ubi.pdm.fastravel.frontend.APIModule.RequestResponse;

public class HistoryRequest {
    public HistoryData history;

    public HistoryRequest(String origin, String destiny, String travelDate) {
        this.history = new HistoryData(origin, destiny, travelDate);
    }

    public static class HistoryData {
        public String origin;
        public String destiny;
        public String travel_date;

        public HistoryData(String origin, String destiny, String travelDate) {
            this.origin = origin;
            this.destiny = destiny;
            this.travel_date = travelDate;
        }
    }
}