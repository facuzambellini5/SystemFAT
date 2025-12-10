package ui;

import services.FileSystem;

import java.util.Scanner;

public class Console {

    private FileSystem fileSystem;
    private Scanner scanner;
    private boolean running;

    public Console() {
        this.fileSystem = new FileSystem();
        this.scanner = new Scanner(System.in);
        this.running = true;
    }

    public void start() {
        showWelcome();
        showHelp();

        while (running) {
            System.out.print("\nFAT> ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) continue;

            processCommand(input);
        }

        scanner.close();
        System.out.println("Saliendo...");
    }

    private void processCommand(String input) {
        String[] parts = input.split("\\s+", 2);
        String command = parts[0].toLowerCase();

        if (command.equals("save")) {
            commandSave(parts);
        }
        else if (command.equals("read")) {
            commandRead(parts);
        }
        else if (command.equals("rename")) {
            commandRename(parts);
        }
        else if (command.equals("list")) {
            commandList();
        }
        else if (command.equals("delete")) {
            commandDelete(parts);
        }
        else if (command.equals("blocks")) {
            commandBlocks(parts);
        }
        else if (command.equals("status")) {
            commandStatus();
        }
        else if (command.equals("help")) {
            showHelp();
        }
        else if (command.equals("clear")) {
            clearConsole();
        }
        else if (command.equals("exit")) {
            running = false;
        }
        else {
            System.out.println("Comando no reconocido. Escribe 'help' para ver lista de comandos.");
        }
    }

    private void commandSave(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Uso: save <nombre> <contenido>");
            return;
        }

        String[] data = parts[1].split(" ", 2);

        if (data.length < 2) {
            System.out.println("Se requiere de nombre y contenido.");
            return;
        }

        fileSystem.saveFile(data[0], data[1]);
    }

    private void commandRead(String[] parts) {

        if (parts.length < 2) {
            System.out.println("Uso: read <nombre>");
            return;
        }

        fileSystem.readFile(parts[1]);
    }

    private void commandRename(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Uso: rename <nombreActual> <nombreNuevo>");
            return;
        }

        String[] names = parts[1].split("\\s+");
        if (names.length < 2) {
            System.out.println("Nombre actual y nombre nuevo requeridos.");
            return;
        }

        fileSystem.renameFile(names[0], names[1]);
    }

    private void commandList() {
        fileSystem.listFiles();
    }

    private void commandDelete(String[] parts) {

        if (parts.length < 2) {
            System.out.println("Uso: delete <nombre>");
            return;
        }

        fileSystem.deleteFile(parts[1]);
    }

    private void commandBlocks(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Uso: blocks <nombre>");
            return;
        }

        fileSystem.showBlocks(parts[1]);
    }

    private void commandStatus() {
        fileSystem.showSystemStatus();
    }

    private void showWelcome() {
        System.out.println("-----SIMULADOR DE SISTEMA DE ARCHIVOS FAT-----");
    }

    private void showHelp() {
        System.out.println("\n        -----COMANDOS DISPONIBLES-----");
        System.out.println(" save <name> <content>      - Guardar archivo");
        System.out.println(" read <name>                - Leer archivo");
        System.out.println(" rename <name> <newName>    - Renombrar archivo");
        System.out.println(" list                       - Listar archivos");
        System.out.println(" delete <name>              - Eliminar archivo");
        System.out.println(" blocks <name>              - Ver bloques");
        System.out.println(" status                     - Estado del sistema");
        System.out.println(" clear                      - Limpiar consola");
        System.out.println(" help                       - Mostrar ayuda");
        System.out.println(" exit                       - Salir");
    }

    private void clearConsole() {
        for (int i = 0; i < 50; i++) System.out.println();
    }
}
