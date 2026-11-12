package com.mbs.util;

import com.mbs.model.Movie;

import java.io.File;

/**
 * Finds a movie's poster image. Put files in the posters/ folder named after the movie:
 * title in lower case, anything that is not a letter or digit becomes "-"
 * (Dune: Part Two -> dune-part-two.jpg). JPG or PNG.
 */
public final class PosterLocator {
    private static final String[] EXTENSIONS = {"jpg", "jpeg", "png", "gif", "bmp"};

    private PosterLocator() { }

    public static String slug(String title) {
        String s = title == null ? "" : title.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        return s.replaceAll("^-+|-+$", "");
    }

    /** Returns the poster file, or null if none was added yet. */
    public static File find(Movie m) {
        if (m.getPoster() != null && !m.getPoster().trim().isEmpty()) {
            File uploaded = new File(m.getPoster());
            if (uploaded.isFile()) {
                return uploaded;
            }
        }
        File dir = new File(AppConfig.get("poster.dir", "posters"));
        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }
        String slug = slug(m.getTitle());
        for (String ext : EXTENSIONS) {
            String wanted = slug + "." + ext;
            for (File f : files) {
                if (f.isFile() && f.getName().equalsIgnoreCase(wanted)) {
                    return f;
                }
            }
        }
        return null;
    }
}
