package ubi.pdm.fastravel.frontend.APIModule.RequestResponse;

public class RegisterRequest {
    public User user;

    public RegisterRequest(String name, String email, String password) {
        this.user = new User(name, email, password);
    }

    public static class User {
        public String name;
        public String email;
        public String password;

        public User(String name, String email, String password) {
            this.name = name;
            this.email = email;
            this.password = password;
        }
    }
}
