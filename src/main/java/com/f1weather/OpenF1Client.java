package com.f1weather;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.json.JSONArray;
import org.json.JSONObject;

public class OpenF1Client {

    private static final String BASE_URL = "https://api.openf1.org/v1/";
    private final HttpClient httpClient;

    public OpenF1Client() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public record WeatherData(
            String raceId,
            double airTemperature,
            double trackTemperature,
            double humidity,
            double pressure,
            double windSpeed,
            int windDirection,
            int rainfall,
            String date
    ) {
        @Override
        public String toString() {
            return String.format(
                    "Weather at %s (Race ID: %s):\n" +
                    "- Air Temp: %.1f°C\n" +
                    "- Track Temp: %.1f°C\n" +
                    "- Humidity: %.1f%%\n" +
                    "- Pressure: %.1f mbar\n" +
                    "- Wind: %.1f m/s at %d°\n" +
                    "- Rainfall: %s",
                    date, raceId, airTemperature, trackTemperature, humidity, pressure, windSpeed, windDirection, (rainfall > 0 ? "Yes" : "No")
            );
        }
    }

    public record RaceInfo(
            String raceId,
            String name,
            String location
    ) {
        @Override
        public String toString() {
            return String.format("%s - %s (Race ID: %s)", name, location, raceId);
        }
    }

    /**
     *
     * @param sessionKey 
     * @return 
     * @throws Exception 
     */
    public String getRawWeatherData(String sessionKey) throws Exception {
        String urlString = BASE_URL + "weather?session_key=" + sessionKey;
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to fetch weather data: HTTP " + response.statusCode());
        }
        
        return response.body();
    }
    
    public WeatherData parseWeatherData(String rawJson, String raceId) {
        JSONArray array = new JSONArray(rawJson);
        if (array.isEmpty()) {
            return null;
        }
        JSONObject obj = array.getJSONObject(array.length() - 1);
        return new WeatherData(
                raceId,
                obj.optDouble("air_temperature", 0.0),
                obj.optDouble("track_temperature", 0.0),
                obj.optDouble("humidity", 0.0),
                obj.optDouble("pressure", 0.0),
                obj.optDouble("wind_speed", 0.0),
                obj.optInt("wind_direction", 0),
                obj.optInt("rainfall", 0),
                obj.optString("date", "")
        );
    }

    public RaceInfo getRaceInfo(String meetingKey) throws Exception {
        String urlString = BASE_URL + "meetings?meeting_key=" + meetingKey;
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to fetch race info: HTTP " + response.statusCode());
        }
        
        JSONArray array = new JSONArray(response.body());
        if (array.isEmpty()) {
            return new RaceInfo(meetingKey, "Unknown Name", "Unknown Location");
        }
        JSONObject meeting = array.getJSONObject(0);
        String name = meeting.optString("meeting_name", "Unknown Name");
        String location = meeting.optString("location", "Unknown Location");
        
        return new RaceInfo(meetingKey, name, location);
    }
    
    // Quick test method
    public static void main(String[] args) {
        OpenF1Client client = new OpenF1Client();
        try {
            System.out.println("Fetching latest race info from OpenF1...");
            RaceInfo raceInfo = client.getRaceInfo("latest");
            System.out.println("Race Info: " + raceInfo);

            System.out.println("\nFetching latest weather data from OpenF1...");
            String rawJson = client.getRawWeatherData("latest");
            
            System.out.println("\n--- Raw JSON Response ---");
            System.out.println(rawJson.substring(0, Math.min(rawJson.length(), 500)) + "... (truncated)");

            RaceInfo testRaceInfo = client.getRaceInfo("latest");
            System.out.println( testRaceInfo.toString() );
            
            System.out.println("\n--- Parsed Weather Data ---");
            WeatherData parsedData = client.parseWeatherData(rawJson, "latest");
            System.out.println(parsedData != null ? parsedData : "No weather data found.");
            
        } catch (Exception e) {
            System.err.println("Error testing OpenF1 API: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
