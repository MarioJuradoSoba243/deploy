package com.fs.pcmens2.deploy.cli.cmd;



import picocli.CommandLine;
import java.util.Scanner;

@CommandLine.Command(name="menu", description = "Menú interactivo")
public class MenuCmd implements Runnable {
    @Override public void run() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== pcm-deploy ===");
            System.out.println("1) Preparar version (desde incoming)");
            System.out.println("2) Instalar versión");
            System.out.println("3) Rollback");
            System.out.println("4) Estado");
            System.out.println("5) Verificar checksums");
            System.out.println("0) Salir");
            System.out.print("> ");
            switch (sc.nextLine().trim()) {
                case "1" -> System.out.println("Ejemplo: pcm-deploy prepare --from /opt/pcm-deploy/incoming/platform-2.4.0.zip");
                case "2" -> System.out.println("Ejemplo: pcm-deploy install --version 2.4.0");
                case "3" -> System.out.println("Ejemplo: pcm-deploy rollback  (o --to 2.3.0)");
                case "4" -> new CommandLine(new StatusCmd()).execute();
                case "5" -> System.out.println("Ejemplo: pcm-deploy verify --version 2.4.0");
                case "0" -> { System.out.println("END."); return; }
                default -> System.out.println("Opción no válida.");
            }
        }
    }
}

