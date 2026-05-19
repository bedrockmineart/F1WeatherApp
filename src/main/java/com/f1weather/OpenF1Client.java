    package com.f1weather;

    import java.net.URI;
    import java.net.http.HttpClient;
    import java.net.http.HttpRequest;
    import java.net.http.HttpResponse;
    import java.time.Duration;

    public class OpenF1Client {

        private static final String BASE_URL = "https://api.openf1.org/v1/";
        private final HttpClient httpClient;

        public OpenF1Client() {
            this.httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
        }

        public record WeatherData(
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
                        "Weather at %s:\n" +
                        "- Air Temp: %.1f°C\n" +
                        "- Track Temp: %.1f°C\n" +
                        "- Humidity: %.1f%%\n" +
                        "- Pressure: %.1f mbar\n" +
                        "- Wind: %.1f m/s at %d°\n" +
                        "- Rainfall: %s",
                        date, airTemperature, trackTemperature, humidity, pressure, windSpeed, windDirection, (rainfall > 0 ? "Yes" : "No")
                );
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

        // Quick test method
        public static void main(String[] args) {
            OpenF1Client client = new OpenF1Client();
            try {
                System.out.println("Fetching latest weather data from OpenF1...");
                String rawJson = client.getRawWeatherData("latest");

                System.out.println("\n--- Raw JSON Response ---");
                System.out.println(rawJson.substring(0, Math.min(rawJson.length(), 500)) + "... (truncated)");

                System.out.println("\n--- Formatted Output Example ---");
                WeatherData sampleData = new WeatherData( 25.5, 33.2, 55.0, 1013.25, 2.5, 180, 0, "2023-11-62T14:45:00");
                System.out.println( sampleData );

            } catch (Exception e) {
                System.err.println("Error testing OpenF1 API: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
