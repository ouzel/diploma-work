package com.example.recycleapp.data.model;
import java.util.List;
import java.util.Map;

public class RecyclingPoint {
    private int id;
    private String name;
    private List<String> materials;
    private String address;
    private float latitude;
    private float longitude;
    private Map<String, String> schedule;
    private String description;

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getMaterials() { return materials; }
    public void setMaterials(List<String> materials) { this.materials = materials; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public float getLatitude() { return latitude; }
    public void setLatitude(float latitude) { this.latitude = latitude; }

    public float getLongitude() { return longitude; }
    public void setLongitude(float longitude) { this.longitude = longitude; }

    public Map<String, String> getSchedule() { return schedule; }
    public void setSchedule(Map<String, String> schedule) { this.schedule = schedule; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    /**
     * Возвращает график работы в читаемом формате.
     */
    public String getFormattedSchedule() {
        if (schedule == null || schedule.isEmpty()) {
            return "График работы не указан.";
        }

        StringBuilder formattedSchedule = new StringBuilder();
        for (Map.Entry<String, String> entry : schedule.entrySet()) {
            formattedSchedule
                    .append(entry.getKey()) // День недели
                    .append(": ")
                    .append(entry.getValue()) // Часы работы
                    .append("\n"); // Перевод строки
        }

        return formattedSchedule.toString().trim(); // Убираем лишний перевод строки в конце
    }
}
