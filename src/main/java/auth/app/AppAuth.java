package auth.app;

import auth.src.AuthService;

public class AppAuth {
    public static int PORT = 6000;
    static void main() {
        new AuthService(PORT);
    }
}
