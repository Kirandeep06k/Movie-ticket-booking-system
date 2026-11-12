package com.mbs.util;

import com.mbs.model.Booking;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/** File handling: character streams, byte streams, serialization. */
public final class FileUtil {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private FileUtil() { }

    /** Character stream: write a text receipt. */
    public static void writeReceipt(File file, Booking b, String customerName) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(file))) {
            w.write("=========== THEATRELY TICKET ===========");
            w.newLine();
            w.write("Booking ID : " + b.getId());
            w.newLine();
            w.write("Customer   : " + customerName);
            w.newLine();
            w.write("Movie      : " + b.getMovieTitle());
            w.newLine();
            w.write("Show time  : " + b.getShowTime().format(FMT));
            w.newLine();
            w.write("Seats      : " + b.getSeats());
            w.newLine();
            w.write("Total paid : Rs." + b.getTotal());
            w.newLine();
            w.write("Status     : " + b.getStatus());
            w.newLine();
            w.write("=======================================");
            w.newLine();
        }
    }

    /** Character stream: export bookings to CSV. */
    public static void exportCsv(File file, List<Booking> list) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("id,movie,show_time,seats,total,booked_at,status");
            for (Booking b : list) {
                pw.println(b.getId() + ",\"" + b.getMovieTitle() + "\"," + b.getShowTime() + ",\""
                        + b.getSeats() + "\"," + b.getTotal() + "," + b.getBookedAt() + "," + b.getStatus());
            }
        }
    }

    /** Character stream: read last n lines of a text file (e.g. the log). */
    public static String readLastLines(File file, int n) throws IOException {
        LinkedList<String> lines = new LinkedList<>();
        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = r.readLine()) != null) {
                lines.addLast(line);
                if (lines.size() > n) {
                    lines.removeFirst();
                }
            }
        }
        return String.join("\n", lines);
    }

    /** Byte streams: copy a poster image into the posters folder. */
    public static String copyPoster(File source) throws IOException {
        File dir = new File(AppConfig.get("poster.dir", "posters"));
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Could not create folder " + dir);
        }
        File target = new File(dir, System.currentTimeMillis() + "_" + source.getName());
        try (InputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(target)) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
        return target.getPath();
    }

    /** Serialization: save bookings to a binary file. */
    public static void backup(File file, List<Booking> list) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(new ArrayList<>(list));
        }
    }

    /** Deserialization: read bookings back from a binary file. */
    @SuppressWarnings("unchecked")
    public static List<Booking> restore(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (List<Booking>) in.readObject();
        }
    }
}
