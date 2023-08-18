package dk.itu.utils;

import com.fasterxml.jackson.annotation.*;
import dk.itu.view.components.DebugDetailsComponent;
import javafx.scene.paint.Color;

import java.io.Serializable;
import java.util.*;
import java.security.SecureRandom;
@JsonIgnoreProperties(ignoreUnknown = true)
public class Theme {
    @JsonProperty
    public Map<Integer, Float> bounds;
    @JsonProperty
    public Map<String, Map<String, ColorDescription>> theme;
    private int minLayer = 1, maxLayer = 21;


    public static final String DEFAULT = "default";

    public ColorDescription getColorDescription(Map<String, String> tags) {

        if (tags == null) return getColorDescription(DEFAULT, DEFAULT);

        List<ColorDescription> colorDescriptionList = new ArrayList<>();

        for (Map.Entry<String, String> entry : tags.entrySet()) {
            if (theme.containsKey(entry.getKey())) {
                if (theme.get(entry.getKey()).containsKey(entry.getValue())) {
                    colorDescriptionList.add(getColorDescription(entry.getKey(), entry.getValue()));
                } else {
                    colorDescriptionList.add(getColorDescription(entry.getKey(), DEFAULT));
                }
            }
        }

        colorDescriptionList.removeIf(Objects::isNull);

        if (colorDescriptionList.isEmpty()) return getColorDescription(DEFAULT, DEFAULT);

        return colorDescriptionList.stream().parallel().min(Comparator.comparingInt(ColorDescription::getPriority)).get();
    }

    public ColorDescription getColorDescription(String _topCategory, String _subCategory) {
        Map<String, ColorDescription> subCatMap = theme.containsKey(_topCategory) ? theme.get(_topCategory) : theme.get(DEFAULT);
        return subCatMap.containsKey(_subCategory) ? subCatMap.get(_subCategory) : subCatMap.get(DEFAULT);
    }

    public float getLayerBounds(int layer) {
        if (bounds.containsKey(layer)) return bounds.get(layer);
        return Float.MAX_VALUE;
    }

    public int getMinLayer() {
        return minLayer;
    }

    public int getMaxLayer() {
        return maxLayer;
    }


    // use and update this color palette to see or update the colors we use:
    // https://davidmathlogic.com/colorblind/#%23000000-%23E67E22-%23CD6133-%23F5DCBA-%23FFF1BA-%23FCD6A4-%2334ACE0-%230092DA-%23AAD3DF-%237781F7-%232C3E50-%2371aba4-%23F3E4DD-%23EEEEEE-%23BDC3C7-%23999999-%23CDC3BB-%23D9D0C9-%23E0DFDF-%23F2EFE9-%237F8C8D-%23C6C5C5-%23B8B8B8-%23D5C5C4-%23AC39AC-%2394769B-%23F5C7C2-%23EBDBE8-%23FA8173-%23C8D7AB-%23B8D4A7-%23D6D99F-%23A9CBAF-%23ADD19E-%23CDEBB0-%23EEF0D5-%23DFFCE2-%23C7C7B4-%23AAE0CB-%23DEF6C0-%23188920-%23C8FACC-%239DDAA2-%23A2AD95-%23A9F5AE-%23F8FABF-%23A88936-%23C77400-%23E74C3C-%23FFFFFF-%23FFFFE5
    public static class ColorDescription implements Serializable {
        private String[] color = null, fillColor = null, borderColor = null, innerFillColor = null, iconColor = null;
        private Integer layer = 1, priority = Integer.MAX_VALUE;
        private boolean dashed = false, visible = false, shouldFill = true, isRoad = false;
        private Double width = 1.0;
        private String icon = null;
        private Map<Float, Integer> nodeSkip = null;

        // for use with randomcolor modes
        private Color[] designatedColor;
        boolean[] hasDesignatedColor;
        //

        @JsonProperty("color")
        private void setColor(String colorString) {
            if (ThemesFactory.getColors().containsKey(colorString)) {
                color = ThemesFactory.getColors().get(colorString.intern());
            } else {
                color = ThemesFactory.getColors().get("default");
            }
        }

        @JsonProperty("fillColor")
        private void setFillColor(String _fillColor) {
            if (ThemesFactory.getColors().containsKey(_fillColor)) {
                fillColor = ThemesFactory.getColors().get(_fillColor.intern());
            } else {
                fillColor = ThemesFactory.getColors().get("default");
            }
        }

        @JsonProperty("borderColor")
        private void setBorderColor(String _borderColor) {
            if (ThemesFactory.getColors().containsKey(_borderColor)) {
                borderColor = ThemesFactory.getColors().get(_borderColor.intern());
            } else {
                borderColor = ThemesFactory.getColors().get("default");
            }
        }

        @JsonProperty("innerFillColor")
        private void setInnerFillColor(String _innerFillColor) {
            if (ThemesFactory.getColors().containsKey(_innerFillColor)) {
                innerFillColor = ThemesFactory.getColors().get(_innerFillColor.intern());
            } else {
                innerFillColor = ThemesFactory.getColors().get("default");
            }
        }

        @JsonProperty("iconColor")
        private void setIconColor(String _iconColor) {
            if (ThemesFactory.getColors().containsKey(_iconColor)) {
                iconColor = ThemesFactory.getColors().get(_iconColor.intern());
            } else {
                iconColor = ThemesFactory.getColors().get("default");
            }
        }

        @JsonProperty("layer")
        private void setLayer(Integer _layer) {
            layer = _layer;
        }

        @JsonProperty("shouldFill")
        private void setShouldFill(boolean _shouldFill) {
            shouldFill = _shouldFill;
        }

        @JsonProperty("priority")
        private void setPriority(Integer _priority) {
            priority = _priority;
        }

        @JsonProperty("dashed")
        private void setDashed(Boolean _dashed) {
            dashed = _dashed;
        }

        @JsonProperty("isRoad")
        private void setIsRoad(Boolean _isRoad) {
            isRoad = _isRoad;
        }

        @JsonProperty("icon")
        private void setIcon(String _icon) {
            icon = _icon.intern();
        }

        @JsonProperty("visible")
        private void setVisible(boolean _visible) {
            visible = _visible;
        }

        @JsonProperty("width")
        private void setWidth(Double _width) {
            width = _width;
        }

        @JsonProperty("nodeSkip")
        private void setNodeSkip(Map<Float, Integer> _nodeSkip) {
            nodeSkip = _nodeSkip;
        }

        public String getFillColor() {
            // checks if experimental features are enabled
            if (checkExperimentalBool()) {
                return setRandomColor(ThemesFactory.getColorMode(), 0);
            }
            if (fillColor != null) {
                return Objects.requireNonNullElse(fillColor[ThemesFactory.getColorMode()], "#000000");
                //return Objects.requireNonNullElse(fillColor[ThemesFactory.getColorMode()], Color.BLACK);
            }
            if (color != null) {
                return Objects.requireNonNullElse(color[ThemesFactory.getColorMode()], "#000000");
                //return Objects.requireNonNullElse(color[ThemesFactory.getColorMode()], Color.BLACK);
            }
            return "#000000";
        }

        public String getBorderColor() {
            if (checkExperimentalBool()) {
                return setRandomColor(ThemesFactory.getColorMode(), 1);
            }

            if (borderColor != null) {
                return Objects.requireNonNullElse(borderColor[ThemesFactory.getColorMode()], "#000000");
            }
            if (color != null) {
                return Objects.requireNonNullElse(color[ThemesFactory.getColorMode()], "#000000");
            }
            return "#000000";
        }

        public String getInnerFillColor() {
            if (checkExperimentalBool()) {
                return setRandomColor(ThemesFactory.getColorMode(), 2);
            }

            if (innerFillColor != null) {
                return Objects.requireNonNullElse(innerFillColor[ThemesFactory.getColorMode()], "#000000");
            }
            if (color != null) {
                return Objects.requireNonNullElse(color[ThemesFactory.getColorMode()], "#000000");
            }
            return "#000000";
        }

        public String getIconColor() {
            if (checkExperimentalBool()) {
                return setRandomColor(ThemesFactory.getColorMode(), 3);
            }

            if (iconColor != null) {
                return Objects.requireNonNullElse(iconColor[ThemesFactory.getColorMode()], "#000000");
            }
            if (color != null) {
                return Objects.requireNonNullElse(color[ThemesFactory.getColorMode()], "#000000");
            }
            return "#000000";//Return standard black
        }

        public Integer getLayer() {
            return layer;
        }

        public boolean getShouldFill() {
            return shouldFill;
        }

        public Integer getPriority() {
            return priority;
        }

        public Boolean getDashed() {
            return dashed;
        }

        public Boolean getIsRoad() {
            return isRoad;
        }

        public String getIcon() {
            return icon;
        }

        public Boolean getVisible() {
            return visible;
        }

        public Double getWidth() {
            return width;
        }

        public Map<Float, Integer> getNodeSkip() {
            return nodeSkip;
        }

        //for setting random colors to use with colormode 4 (saved random) and 5 (full chaos random),
        // when experimental features are enabled
        public String setRandomColor(int colorMode, int colorType) {
            if (hasDesignatedColor == null || !hasDesignatedColor[colorType] || colorMode == 4) {
                double red = 0.0;
                double green = 0.0;
                double blue = 0.0;
                SecureRandom random = new SecureRandom();
                for (int i = 0; i < 3; i++) {
                    double randomDouble = random.nextDouble(0.999 + 0.001);
                    switch (i) {
                        case (0):
                            red = randomDouble;
                        case (1):
                            green = randomDouble;
                        case (2):
                            blue = randomDouble;
                    }
                }
                Color currentColor = new Color(red, green, blue, 1);
                switch (colorMode) {
                    case (3) -> {
                        if (designatedColor == null) designatedColor = new Color[4];
                        if (hasDesignatedColor == null) hasDesignatedColor = new boolean[4];
                        designatedColor[colorType] = currentColor;
                        hasDesignatedColor[colorType] = true;
                        return designatedColor[colorType].toString();
                    }
                    case (4) -> {
                        designatedColor = null;
                        if (hasDesignatedColor != null) hasDesignatedColor[colorType] = false;
                        return currentColor.toString();
                    }

                }
            }
            return designatedColor[colorType].toString();
        }

        // checks if experimental features are enabled
        public boolean checkExperimentalBool() {
            if (DebugDetailsComponent.getExperimentalBool()) {
                if (ThemesFactory.getColorMode() > 2) {
                    return true;
                }
                hasDesignatedColor = null;
                designatedColor = null;
            }
            return false;

        }
    }

}
