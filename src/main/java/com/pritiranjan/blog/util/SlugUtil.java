package com.pritiranjan.blog.util;

public final class SlugUtil {

    private SlugUtil() {
    }

    public static String toSlug(String input) {
        if (input == null) {
            return null;
        }
        String slug = input
                .trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-{2,}", "-");
        return slug.replaceAll("^-|-$", "");
    }
}

