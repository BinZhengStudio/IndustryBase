package net.industrybase.util;

import net.industrybase.api.IndustryBaseApi;
import net.minecraft.resources.Identifier;

public class Util {
    public static Identifier withNamespace(String path) {
        return Identifier.fromNamespaceAndPath(IndustryBaseApi.MODID, path);
    }
}
