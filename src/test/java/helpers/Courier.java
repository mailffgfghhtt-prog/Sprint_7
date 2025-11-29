package helpers;

public class Courier {
    private String login;
    private String password;
    private String firstName;

    // Конструктор без параметров
    public Courier() {}

    // Конструктор с тремя параметрами
    public Courier(String login, String password, String firstName) {
        this.login = login;
        this.password = password;
        this.firstName = firstName;
    }

    // Конструктор с двумя параметрами (добавлен)
    public Courier(String login, String password) {
        this.login = login;
        this.password = password;
    }

    // Геттеры и сеттеры
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
}
