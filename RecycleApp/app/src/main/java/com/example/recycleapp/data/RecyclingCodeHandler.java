package com.example.recycleapp.data;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.text.similarity.LevenshteinDistance;


public class RecyclingCodeHandler {

    public static class RecyclingCode {
        private final String codeNumber;
        private final String material;
        private final int tfliteClassId;
        private final String materialType;
        private final String description;
        private final String recyclingRecommendations;
        private final List<String> examples;
        private final String notes;
        private final String imagePath;

        public RecyclingCode(String codeNumber, String material, int tfliteClassId, String materialType,
                             String description, String recyclingRecommendations, List<String> examples,
                             String notes, String imagePath) {
            this.codeNumber = codeNumber;
            this.material = material;
            this.tfliteClassId = tfliteClassId;
            this.materialType = materialType;
            this.description = description;
            this.recyclingRecommendations = recyclingRecommendations;
            this.examples = examples;
            this.notes = notes;
            this.imagePath = imagePath;
        }

        public String getCodeNumber() {
            return codeNumber;
        }

        public String getMaterial() {
            return material;
        }

        public int getTfliteClassId() {
            return tfliteClassId;
        }

        public String getMaterialType() {
            return materialType;
        }

        public String getDescription() {
            return description;
        }

        public String getRecyclingRecommendations() {
            return recyclingRecommendations;
        }

        public List<String> getExamples() {
            return examples;
        }

        public String getNotes() {
            return notes;
        }

        public String getImagePath() {
            return imagePath;
        }

        // Преобразует объект в строку, пригодную для поиска.
        public String toSearchableString() {
            return String.format(Locale.getDefault(), "%s %s %s %s %s %s %s",
                    codeNumber, material, materialType, description, recyclingRecommendations,
                    String.join(" ", examples), notes).toLowerCase();
        }
    }

    private final List<RecyclingCode> codes;

    public RecyclingCodeHandler(Context context, String fileName) {
        codes = new ArrayList<>();
        loadData(context, fileName);
    }

    // Загружает данные из JSON-файла в список объектов RecyclingCode.
    private void loadData(Context context, String fileName) {
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String jsonString = new String(buffer, StandardCharsets.UTF_8);

            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray codesArray = jsonObject.getJSONArray("recycling_codes");

            for (int i = 0; i < codesArray.length(); i++) {
                JSONObject codeObject = codesArray.getJSONObject(i);

                String codeNumber = codeObject.getString("code_number");
                String material = codeObject.getString("material");
                int tfliteClassId = codeObject.getInt("tflite_class_id");
                String materialType = codeObject.getString("material_type");
                String description = codeObject.getString("description");
                String recyclingRecommendations = codeObject.getString("recycling_recommendations");
                JSONArray examplesArray = codeObject.getJSONArray("examples");
                List<String> examples = new ArrayList<>();
                for (int j = 0; j < examplesArray.length(); j++) {
                    examples.add(examplesArray.getString(j));
                }
                String notes = codeObject.getString("notes");
                String imagePath = codeObject.getString("image");

                RecyclingCode code = new RecyclingCode(codeNumber, material, tfliteClassId, materialType,
                        description, recyclingRecommendations, examples, notes, imagePath);
                codes.add(code);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Выполняет поиск по текстовому запросу.
    // Если параметр field не указан, ищет по всем полям объекта.
    public List<RecyclingCode> search(String query, String field) {
        List<RecyclingCode> results = new ArrayList<>();
        query = query.toLowerCase();

        for (RecyclingCode code : codes) {
            if (field == null || field.isEmpty()) {
                if (code.toSearchableString().contains(query)) {
                    results.add(code);
                }
            } else {
                String value = getFieldValue(code, field);
                if (value != null && value.toLowerCase().contains(query)) {
                    results.add(code);
                }
            }
        }
        return results;
    }

    // Алгоритм Левенштейна
    public List<RecyclingCode> search(String query, double threshold) {
        List<RecyclingCode> results = new ArrayList<>();
        query = query.toLowerCase();

        // Инициализируем метрику расстояния Левенштейна
        LevenshteinDistance levenshtein = new LevenshteinDistance();

        for (RecyclingCode code : codes) {
            // Список строк для проверки (объединяем информацию для поиска)
            List<String> searchableFields = new ArrayList<>();
            searchableFields.add(code.getCodeNumber());
            searchableFields.add(code.getMaterial());
            searchableFields.add(code.getMaterialType());
            searchableFields.add(code.getDescription());
            searchableFields.add(code.getRecyclingRecommendations());
            searchableFields.addAll(code.getExamples()); // Добавляем примеры
            searchableFields.add(code.getNotes());

            // Проверяем каждое поле на соответствие
            boolean matches = false;
            for (String field : searchableFields) {
                if (field != null) {
                    // Разбиваем поле на отдельные слова
                    String[] words = field.toLowerCase().split("\\W+"); // Разделяем по неалфавитным символам

                    // Проверяем каждое слово
                    for (String word : words) {
                        // Вычисляем расстояние Левенштейна
                        int distance = levenshtein.apply(word, query);

                        // Нормализуем расстояние (0 = полное совпадение, 1 = совсем разные строки)
                        double normalizedDistance = (double) distance / Math.max(word.length(), query.length());

                        // Если совпадает хотя бы одно слово, добавляем объект
                        if (normalizedDistance <= threshold) {
                            matches = true;
                            break;
                        }
                    }

                    if (matches) {
                        break;
                    }
                }
            }

            if (matches) {
                results.add(code);
            }
        }
        return results;
    }



    // Возвращает значение определенного поля объекта RecyclingCode.
    private String getFieldValue(RecyclingCode code, String field) {
        switch (field) {
            case "code_number":
                return code.getCodeNumber();
            case "material":
                return code.getMaterial();
            case "material_type":
                return code.getMaterialType();
            case "description":
                return code.getDescription();
            case "recycling_recommendations":
                return code.getRecyclingRecommendations();
            case "examples":
                return String.join(" ", code.getExamples());
            case "notes":
                return code.getNotes();
            default:
                return null;
        }
    }

    // Ищет объект RecyclingCode по идентификатору tfliteClassId.
    public RecyclingCode getByTfliteClassId(int tfliteClassId) {
        for (RecyclingCode code : codes) {
            if (code.getTfliteClassId() == tfliteClassId) {
                return code;
            }
        }
        return null;
    }

    // Возвращает список RecyclingCode по заданным номерам codeNumber.
    public List<RecyclingCode> getByCodeNumbers(List<String> codeNumbers) {
        List<RecyclingCode> result = new ArrayList<>();
        for (String codeNumber : codeNumbers) {
            for (RecyclingCode code : codes) {
                if (code.getCodeNumber().equals(codeNumber)) {
                    result.add(code);
                    break; // Можно прекратить поиск, так как номера уникальны.
                }
            }
        }
        return result;
    }
}

