package auth.app;

import auth.src.AuthService;

public class AppAuth {
    public static int PORT = 6000;

    public static void main(String[] args) {
        new AuthService(PORT);
    }
}
