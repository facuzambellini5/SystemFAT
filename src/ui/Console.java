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
        System.out.println("\n👋 Sistema apagado. ¡Hasta luego!");
    }

    private void processCommand(String input) {
        String[] parts = input.split("\\s+", 2);
        String command = parts[0].toLowerCase();

        if (command.equals("save") || command.equals("guardar")) {
            commandSave(parts);
        }
        else if (command.equals("read") || command.equals("leer")) {
            commandRead(parts);
        }
        else if (command.equals("list") || command.equals("listar")) {
            commandList();
        }
        else if (command.equals("delete") || command.equals("borrar")) {
            commandDelete(parts);
        }
        else if (command.equals("blocks") || command.equals("bloques")) {
            commandBlocks(parts);
        }
        else if (command.equals("status") || command.equals("estado")) {
            commandStatus();
        }
        else if (command.equals("help") || command.equals("ayuda")) {
            showHelp();
        }
        else if (command.equals("clear") || command.equals("limpiar")) {
            clearConsole();
        }
        else if (command.equals("exit") || command.equals("salir")) {
            running = false;
        }
        else {
            System.out.println("❌ Comando no reconocido. Escribe 'help' para ver la ayuda.");
        }
    }

    private void commandSave(String[] parts) {
        if (parts.length < 2) {
            System.out.println("❌ Uso: save <nombre> <contenido>");
            System.out.println("   Ejemplo: save carta.txt Hola mundo");
            return;
        }

        String[] data = parts[1].split("\\s+", 2);
        if (data.length < 2) {
            System.out.println("❌ Debes proporcionar nombre Y contenido.");
            return;
        }

        fileSystem.saveFile(data[0], data[1]);
    }

    private void commandRead(String[] parts) {
        if (parts.length < 2) {
            System.out.println("❌ Uso: read <nombre>");
            return;
        }

        fileSystem.readFile(parts[1]);
    }

    private void commandList() {
        fileSystem.listFiles();
    }

    private void commandDelete(String[] parts) {
        if (parts.length < 2) {
            System.out.println("❌ Uso: delete <nombre>");
            return;
        }

        fileSystem.deleteFile(parts[1]);
    }

    private void commandBlocks(String[] parts) {
        if (parts.length < 2) {
            System.out.println("❌ Uso: blocks <nombre>");
            return;
        }

        fileSystem.showBlocks(parts[1]);
    }

    private void commandStatus() {
        fileSystem.showSystemStatus();
    }

    private void showWelcome() {
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║                                                ║");
        System.out.println("║    SIMULADOR DE SISTEMA DE ARCHIVOS FAT       ║");
        System.out.println("║                                                ║");
        System.out.println("╚════════════════════════════════════════════════╝");
    }

    private void showHelp() {
        System.out.println("\n╔═══════════════════════════════════════════════════╗");
        System.out.println("║              COMANDOS DISPONIBLES                 ║");
        System.out.println("╠═══════════════════════════════════════════════════╣");
        System.out.println("║ save <nombre> <contenido>  - Guardar archivo     ║");
        System.out.println("║ read <nombre>              - Leer archivo        ║");
        System.out.println("║ list                       - Listar archivos     ║");
        System.out.println("║ delete <nombre>            - Eliminar archivo    ║");
        System.out.println("║ blocks <nombre>            - Ver bloques         ║");
        System.out.println("║ status                     - Estado del sistema  ║");
        System.out.println("║ clear                      - Limpiar consola     ║");
        System.out.println("║ help                       - Mostrar ayuda       ║");
        System.out.println("║ exit                       - Salir               ║");
        System.out.println("╚═══════════════════════════════════════════════════╝");
    }

    private void clearConsole() {
        for (int i = 0; i < 50; i++) System.out.println();
    }
}
