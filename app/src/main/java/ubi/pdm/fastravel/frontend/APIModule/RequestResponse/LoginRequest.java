package ubi.pdm.fastravel.frontend.APIModule.RequestResponse;

public class LoginRequest {
    public Auth auth;

    public LoginRequest(String email, String password) {
        this.auth = new Auth(email, password);
    }

    public static class Auth {
        public String email;
        public String password;

        public Auth(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }
}
