package com.inDrive.plugin.navigation.graphhopper.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeocodeRequest {
    private String query;

    private String locale;

    private int limit;

    private String provider;

    public String toString() {
        StringBuilder builder = new StringBuilder("");

        if (query != null && query.trim().length() > 0)
            builder.append(String.format("q=%s", query.replace(" ", "%20")));

        if (locale != null && locale.trim().length() > 0)
            builder.append(String.format("&locale=%s", locale));

        if (limit > 0)
            builder.append(String.format("&limit=%d", limit));

        if (provider != null && provider.trim().length() > 0)
            builder.append(String.format("&provider=%s", provider));

        return builder.toString();
    }
}
