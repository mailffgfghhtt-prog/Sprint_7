package helpers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
public class TestData {
    public static String generateUniqueLogin() {
        return "courier_" + System.currentTimeMillis();
    }
    public static String generateUniqueTrack() {
        return String.valueOf(System.currentTimeMillis() % 1000000);
    }
    public static String getFutureDate() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        return futureDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}