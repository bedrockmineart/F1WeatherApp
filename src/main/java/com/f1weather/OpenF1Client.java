package com.f1weather;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class OpenF1Client {

    private static final String BASE_URL = "https://api.openf1.org/v1/";
    private static final String YEAR = "2026"; 
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
            String location,
            String dateStart
    ) {
        @Override
        public String toString() {
            return String.format("%s - %s (%s, Race ID: %s)", name, location, dateStart, raceId);
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
            return new RaceInfo(meetingKey, "Unknown Name", "Unknown Location", "Unknown Date");
        }
        JSONObject meeting = array.getJSONObject(0);
        String name = meeting.optString("meeting_name", "Unknown Name");
        String location = meeting.optString("location", "Unknown Location");
        String dateStart = meeting.optString("date_start", "Unknown Date");
        
        return new RaceInfo(meetingKey, name, location, dateStart);
    }

    public record RaceMeetings(
        List<RaceInfo> races
    ) {

    }

    public RaceMeetings getCurrentYearMeetings() throws Exception {
        String urlString = BASE_URL + "meetings?year=" + YEAR;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to fetch upcoming races: HTTP " + response.statusCode());
        }
        JSONArray array = new JSONArray(response.body());
        List<RaceInfo> races = new java.util.ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonObj = array.getJSONObject(i);
            String raceId = String.valueOf(jsonObj.optInt("meeting_key"));
            if (raceId.equals("0")) {
                raceId = jsonObj.optString("meeting_key", "unknown");
            }
            String name = jsonObj.optString("meeting_name", "Unknown Name");
            String location = jsonObj.optString("location", "Unknown Location");
            String dateStart = jsonObj.optString("date_start", "Unknown Date");
            races.add(new RaceInfo(raceId, name, location, dateStart));
        }
        return new RaceMeetings(races);
    }

    public RaceMeetings getMeetingsAfterDate( RaceMeetings meetings, String date ) {
        List<RaceInfo> upcoming = new java.util.ArrayList<>();
        for (RaceInfo race : meetings.races()) {
            if (race.dateStart() != null && race.dateStart().compareTo(date) > 0) {
                upcoming.add(race);
            } 
        }
        return new RaceMeetings(upcoming);
    }
    
    // Quick test method
    public static void main(String[] args) {
        OpenF1Client client = new OpenF1Client();
        try {
            System.out.println("Fetching current year's meetings from OpenF1...");
            RaceMeetings meetings = client.getCurrentYearMeetings();
            System.out.println("Found " + meetings.races().size() + " meetings in " + YEAR + ":");
            for (int i = 0; i < Math.min(3, meetings.races().size()); i++) { 
                System.out.println(" - " + meetings.races().get(i));
            }
            
            System.out.println("\nTesting getMeetingsAfterDate with May 1st, 2026...");
            RaceMeetings upcomingMeetings = client.getMeetingsAfterDate(meetings, "2026-05-01T00:00:00+00:00");
            System.out.println("Found " + upcomingMeetings.races().size() + " upcoming meetings after May 1st 2026.");
            for (int i = 0; i < Math.min(3, upcomingMeetings.races().size()); i++) { 
                System.out.println(" - " + upcomingMeetings.races().get(i));
            }
            
            System.out.println("\nFetching latest race info from OpenF1...");
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
