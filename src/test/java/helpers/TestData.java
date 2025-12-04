package helpers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;
public class TestData {
    private static final Random random = new Random();
    public static String generateUniqueLogin() {
        return "test_login_" + LocalDate.now().toString().replaceAll("[^0-9]", "") +
                random.nextInt(1000);
    }
    public static String getFutureDate() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return futureDate.format(formatter);
    }
}