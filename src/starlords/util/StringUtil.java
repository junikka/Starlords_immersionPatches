package starlords.util;

import com.fs.starfarer.api.Global;

public class StringUtil {

    public static String getString(String category, String id) {
        try {
            return Global.getSettings().getString(category, id);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getString(String category, String id, String ... replacements) {
        return starlords.presentation.TextRenderer.positional(getString(category, id), replacements);
    }
}
