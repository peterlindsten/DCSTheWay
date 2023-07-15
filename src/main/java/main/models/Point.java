package main.models;

public record Point(String latitude,
                    String longitude,
                    String elevation,
                    Hemisphere latitudeHemisphere,
                    Hemisphere longitudeHemisphere) {
}
