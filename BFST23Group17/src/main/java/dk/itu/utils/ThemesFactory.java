package dk.itu.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import dk.itu.App;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ThemesFactory {

    private static final Map<String, String[]> colors = new HashMap<>();

    private static Theme theme;

    private static int colorMode = 0;

    public static Theme getTheme() {
        if (colors.isEmpty()) {
            try {
                    Map<String, String[]> colorsMap = JsonMapper.builder()
                            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                            .configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true)
                            .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true)
                            .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)
                            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                            .build()
                            .readValue(App.class.getResourceAsStream("/styles/colors.json"), new TypeReference<>() {
                            });
                    for (Map.Entry<String, String[]> entry : colorsMap.entrySet()) {
                        colors.put(entry.getKey(), entry.getValue());
                    }

            } catch (IOException e) {//
                throw new RuntimeException("Can't load themes class");
            }


        }

        if (theme == null) {
            try {
                theme = JsonMapper.builder()
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        .configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true)
                        .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true)
                        .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)
                        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                        .build()
                        .readValue(App.class.getResourceAsStream("/styles/styles.json"), Theme.class);
            } catch (IOException e) {
                throw new RuntimeException("Can't load themes class");
            }
        }

        return theme;
    }


    public static Map<String, String[]> getColors() {
        return colors;
    }

    public static int getColorMode() {
        return colorMode;
    }
    public static void setColorMode(int colorMode) {
        ThemesFactory.colorMode = colorMode;
    }
}
