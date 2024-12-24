package com.example.codeann01;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.graphics.Color;
import android.text.Html;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity"; // Для логирования
    private TextView todayHolidayTextView;
    private TextView upcomingHolidaysTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        todayHolidayTextView = findViewById(R.id.todayHolidayTextView);
        upcomingHolidaysTextView = findViewById(R.id.upcomingHolidaysTextView);

        // Чтение файла и отображение данных
        Map<String, String> holidays = loadHolidaysFromFile();

        if (holidays.isEmpty()) {
            todayHolidayTextView.setText("Ошибка: файл с праздниками пуст или отсутствует.");
            upcomingHolidaysTextView.setText("");
        } else {
            displayHolidays(holidays);
        }
    }

    private Map<String, String> loadHolidaysFromFile() {
        Map<String, String> holidays = new HashMap<>();
        try {
            InputStream inputStream = getAssets().open("holidays.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            int lineNumber = 0; // Для отслеживания строк
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] parts = line.split(" ", 2);
                if (parts.length == 2) {
                    String date = parts[0].trim();  // Убираем лишние пробелы
                    String holiday = parts[1].trim();

                    // Проверяем формат даты MM-dd
                    if (date.matches("\\d{2}-\\d{2}")) {
                        holidays.put(date, holiday);
                    } else {
                        Log.e(TAG, "Неверный формат даты в строке " + lineNumber + ": " + date);
                    }
                } else {
                    Log.e(TAG, "Неверный формат строки " + lineNumber + ": " + line);
                }
            }
            reader.close();
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при чтении файла с праздниками: ", e);
        }
        return holidays;
    }

    private void displayHolidays(Map<String, String> holidays) {
        // Получаем текущую дату с устройства
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        // Проверяем текущую дату
        String todayKey = today.format(formatter);
        Log.d(TAG, "Текущая дата (формат MM-dd): " + todayKey);

        // Находим праздник на сегодня
        String todayHoliday = holidays.getOrDefault(todayKey, "Нет праздника на сегодня");

        // Находим три ближайших праздника
        List<String> upcomingHolidays = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            LocalDate nextDate = today.plusDays(i);
            String nextKey = nextDate.format(formatter);
            if (holidays.containsKey(nextKey)) {
                upcomingHolidays.add(holidays.get(nextKey));
            }
        }

        // Обновляем интерфейс
        todayHolidayTextView.setText("Сегодня: " + todayHoliday);

        if (upcomingHolidays.isEmpty()) {
            upcomingHolidaysTextView.setText("Ближайшие праздники не найдены.");
        } else {
            StringBuilder upcomingText = new StringBuilder();
            for (int i = 0; i < upcomingHolidays.size(); i++) {
                String holiday = upcomingHolidays.get(i);
                String dayLabel = getDayLabel(i + 1); // Определяем метку: Завтра, Послезавтра и т.д.
                int color = getColorForIndex(i); // Цвет текста
                String styledText = "<font color='" + color + "'>" + dayLabel + ": " + holiday + "</font>";
                upcomingText.append(styledText).append("<br><br>"); // Разделение
            }
            upcomingHolidaysTextView.setText(Html.fromHtml(upcomingText.toString().trim(), Html.FROM_HTML_MODE_LEGACY));
        }
    }

    /**
     * Метод для определения цвета текста.
     * Чем ближе праздник, тем ярче текст.
     */
    private int getColorForIndex(int index) {
        switch (index) {
            case 0:
                return Color.parseColor("#FEF4C0");
            case 1:
                return Color.parseColor("#FDB10B");
            case 2:
                return Color.parseColor("#FE8535");
            default:
                return Color.parseColor("#FFFFFF"); // Дефолт
        }
    }

    /**
     * Метод для определения метки дня (Завтра, Послезавтра и т.д.)
     */
    private String getDayLabel(int dayOffset) {
        switch (dayOffset) {
            case 1:
                return "Завтра";
            case 2:
                return "Послезавтра";
            case 3:
                return "Через два дня";
            default:
                return "";
        }
    }
}
