package services;

import models.Directory;
import models.Disk;
import models.FAT;
import models.MetadataFile;

import java.util.List;

import static models.Constants.*;


public class FileSystem {

    private FAT fat;
    private Disk disk;
    private Directory directory;

    public FileSystem() {
        this.fat = new FAT();
        this.disk = new Disk();
        this.directory = new Directory();

        System.out.println("✅ Sistema FAT inicializado correctamente.");
        System.out.println("   • Bloques totales: " + TOTAL_BLOCKS);
        System.out.println("   • Tamaño por bloque: " + BLOCK_SIZE + " caracteres");
        System.out.println("   • Capacidad total: " + (TOTAL_BLOCKS * BLOCK_SIZE) + " caracteres\n");
    }

    /**
     * Guarda un archivo. Si existe, anexa contenido.
     */
    public boolean saveFile(String name, String content) {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📝 Guardando archivo: " + name);
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (directory.exists(name)) {
            System.out.println("ℹ️  El archivo ya existe. Anexando contenido...");
            return appendContent(name, content);
        }

        return createFile(name, content);
    }

    /**
     * Crea un archivo nuevo.
     */
    private boolean createFile(String name, String content) {
        // 1. Calcular bloques necesarios
        int requiredBlocks = Disk.calculateRequiredBlocks(content);
        System.out.println("   Bloques necesarios: " + requiredBlocks);

        // 2. Buscar bloques disponibles
        List<Integer> availableBlocks = fat.searchAvailableBlocks(requiredBlocks);

        // 3. Validar espacio suficiente
        if (availableBlocks.size() < requiredBlocks) {
            System.out.println("❌ " + MSG_DISK_OUT_OF_SPACE);
            System.out.println("   Se necesitan " + requiredBlocks + " bloques, solo hay " + availableBlocks.size());
            return false;
        }

        System.out.println("   Bloques asignados: " + availableBlocks);

        // 4. Escribir en disco
        disk.writeFragmented(content, availableBlocks);

        // 5. Actualizar FAT
        fat.updateFAT(availableBlocks);

        // 6. Agregar al directorio
        int firstBlock = availableBlocks.get(0);
        MetadataFile metadata = new MetadataFile(name, content.length(), firstBlock);
        directory.addFile(name, metadata);

        System.out.println("✅ " + MSG_FILE_SAVED);
        System.out.println("   Tamaño: " + content.length() + " caracteres");
        System.out.println("   Bloque inicial: " + firstBlock);

        return true;
    }

    /**
     * Anexa contenido a un archivo existente.
     */
    private boolean appendContent(String name, String content) {
        // 1. Obtener metadata del archivo
        MetadataFile metadata = directory.getMetadata(name);
        int firstBlock = metadata.getFirstBlock();

        // 2. Obtener cadena de bloques actual
        List<Integer> currentBlocks = fat.getBlockChain(firstBlock);
        int lastBlock = currentBlocks.get(currentBlocks.size() - 1);

        // 3. Verificar espacio disponible en el último bloque
        int availableSpace = disk.getAvailableSpace(lastBlock);
        String existingContent = disk.readBlock(lastBlock);

        // 4. Calcular cuánto podemos agregar al último bloque
        int toAppendInLastBlock = Math.min(availableSpace, content.length());
        String remainingContent = content.substring(toAppendInLastBlock);

        // 5. Completar el último bloque
        if (toAppendInLastBlock > 0) {
            disk.write(lastBlock, existingContent + content.substring(0, toAppendInLastBlock));
        }

        // 6. Si queda contenido, necesitamos más bloques
        if (!remainingContent.isEmpty()) {
            int additionalBlocks = Disk.calculateRequiredBlocks(remainingContent);
            List<Integer> newBlocks = fat.searchAvailableBlocks(additionalBlocks);

            if (newBlocks.size() < additionalBlocks) {
                System.out.println("❌ " + MSG_DISK_OUT_OF_SPACE);
                return false;
            }

            System.out.println("   Bloques adicionales: " + newBlocks);

            // Escribir contenido restante
            disk.writeFragmented(remainingContent, newBlocks);

            // Enlazar último bloque anterior con los nuevos
            fat.linkBlocks(lastBlock, newBlocks.get(0));

            // Actualizar FAT con los nuevos bloques
            fat.updateFAT(newBlocks);
        }

        // 7. Actualizar metadata
        metadata.setSize(metadata.getSize() + content.length());

        System.out.println("✅ Contenido anexado exitosamente.");
        System.out.println("   Nuevo tamaño: " + metadata.getSize() + " caracteres");

        return true;
    }

    /**
     * Lee un archivo completo.
     */
    public void readFile(String name) {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📖 Leyendo archivo: " + name);
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (!directory.exists(name)) {
            System.out.println("❌ " + MSG_FILE_NOT_FOUND);
            return;
        }

        MetadataFile metadata = directory.getMetadata(name);
        int firstBlock = metadata.getFirstBlock();

        // Obtener cadena de bloques
        List<Integer> blocks = fat.getBlockChain(firstBlock);

        // Leer contenido completo
        String content = disk.readFullContent(blocks);

        System.out.println("\n┌─── Contenido del archivo ───┐");
        System.out.println(content);
        System.out.println("└─────────────────────────────┘");
        System.out.println("\nTamaño: " + metadata.getSize() + " caracteres");
        System.out.println("Bloques utilizados: " + blocks);
    }

    /**
     * Lista todos los archivos.
     */
    public void listFiles() {
        directory.list();
    }

    /**
     * Elimina un archivo.
     */
    public void deleteFile(String name) {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🗑️  Eliminando archivo: " + name);
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (!directory.exists(name)) {
            System.out.println("❌ " + MSG_FILE_NOT_FOUND);
            return;
        }

        MetadataFile metadata = directory.getMetadata(name);
        int firstBlock = metadata.getFirstBlock();

        // Obtener cadena de bloques
        List<Integer> blocks = fat.getBlockChain(firstBlock);

        System.out.println("   Liberando bloques: " + blocks);

        // Liberar bloques en disco
        for (int block : blocks) {
            disk.delete(block);
        }

        // Liberar bloques en FAT
        for (int block : blocks) {
            fat.setAvailable(block);
        }

        // Eliminar del directorio
        directory.deleteFile(name);

        System.out.println("✅ Archivo eliminado exitosamente.");
    }

    /**
     * Muestra los bloques que ocupa un archivo.
     */
    public void showBlocks(String name) {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🔍 Bloques del archivo: " + name);
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (!directory.exists(name)) {
            System.out.println("❌ " + MSG_FILE_NOT_FOUND);
            return;
        }

        MetadataFile metadata = directory.getMetadata(name);
        int firstBlock = metadata.getFirstBlock();

        List<Integer> blocks = fat.getBlockChain(firstBlock);

        System.out.println("\n┌─── Distribución en disco ───┐");
        for (int i = 0; i < blocks.size(); i++) {
            int block = blocks.get(i);
            String content = disk.readBlock(block);
            System.out.printf("  Bloque %3d: \"%s\"%n", block, content);
        }
        System.out.println("└─────────────────────────────┘");
        System.out.println("\nTotal de bloques: " + blocks.size());
        System.out.println("Cadena: " + blocks);
    }

    /**
     * Muestra el estado completo del sistema (debugging).
     */
    public void showSystemStatus() {
        fat.printStatus();
        disk.printStatus();
        directory.list();
    }
}

