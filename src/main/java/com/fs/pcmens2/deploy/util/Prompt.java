package com.fs.pcmens2.deploy.util;


import java.io.Console;
import java.util.Locale;
import java.util.Scanner;

public class Prompt {
    /** Pregunta yes/no. Si defaultNo=true, ENTER equivale a NO. Acepta y/yes/s/si/sí. */
    public static boolean confirm(String question, boolean defaultNo) {
        String hint = defaultNo ? " [yes/NO] " : " [YES/no] ";
        Console console = System.console();
        String line;
        if (console != null) {
            line = console.readLine(question + hint);
        } else {
            System.out.print(question + hint);
            Scanner sc = new Scanner(System.in);
            line = sc.nextLine();
        }
        if (line == null) return !defaultNo;
        line = line.trim().toLowerCase(Locale.ROOT);
        if (line.isEmpty()) return !defaultNo;
        return line.equals("y") || line.equals("yes")
                || line.equals("s") || line.equals("si") || line.equals("sí");
    }

    public static String ask(String question) {
        Console console = System.console();
        String line;
        if (console != null) {
            line = console.readLine(question + ": ");
        } else {
            System.out.print(question + ": ");
            Scanner sc = new Scanner(System.in);
            line = sc.nextLine();
        }
        if (line == null) return "";
        return line.trim();
    }

}

