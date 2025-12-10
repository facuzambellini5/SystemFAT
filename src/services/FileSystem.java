package services;

import models.Directory;
import models.Disk;
import models.Fat;
import models.MetadataFile;

import java.time.LocalDateTime;
import java.util.List;

import static constants.Constants.*;


public class FileSystem {

    private Fat fat;
    private Disk disk;
    private Directory directory;

    public FileSystem() {
        this.fat = new Fat();
        this.disk = new Disk();
        this.directory = new Directory();

        System.out.println("-----SISTEMA DE ARCHIVOS FAT-----");

        System.out.println("Bloques totales: " + TOTAL_BLOCKS);
        System.out.println("Tamaño por bloque: " + BLOCK_SIZE + " caracteres");
        System.out.println("Capacidad total: " + (TOTAL_BLOCKS * BLOCK_SIZE) + " caracteres\n");
    }

    /**
     * Guarda un archivo. Si existe, anexa contenido.
     */
    public void saveFile(String name, String content) {

        if (directory.exists(name)) {
            appendContent(name, content);
            return;
        }

        createFile(name, content);
    }

    /**
     * Crea un archivo nuevo.
     */
    private void createFile(String name, String content) {
        // 1. Calcular bloques necesarios
        int requiredBlocks = Disk.calculateRequiredBlocks(content);
        System.out.println("Bloques necesarios: " + requiredBlocks);

        // 2. Buscar bloques disponibles
        List<Integer> availableBlocks = fat.searchAvailableBlocks(requiredBlocks);

        // 3. Validar espacio suficiente
        if (availableBlocks.size() < requiredBlocks) {
            System.out.println(MSG_DISK_OUT_OF_SPACE);
            System.out.println("Se necesitan " + requiredBlocks + " bloques, solo hay " + availableBlocks.size());
            return;
        }

        System.out.println("Bloques asignados: " + availableBlocks);

        // 4. Escribir en disco
        disk.writeFragmented(content, availableBlocks);

        // 5. Actualizar FAT
        fat.updateFAT(availableBlocks);

        // 6. Agregar al directorio
        int firstBlock = availableBlocks.getFirst();
        MetadataFile metadata = new MetadataFile(name, content.length(), firstBlock);
        directory.addFile(name, metadata);

        System.out.println(MSG_FILE_SAVED);
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
        int lastBlock = currentBlocks.getLast();

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
                System.out.println(MSG_DISK_OUT_OF_SPACE);
                return false;
            }

            System.out.println("Bloques adicionales: " + newBlocks);

            // Escribir contenido restante
            disk.writeFragmented(remainingContent, newBlocks);

            // Enlazar último bloque anterior con los nuevos
            fat.linkBlocks(lastBlock, newBlocks.getFirst());

            // Actualizar FAT con los nuevos bloques
            fat.updateFAT(newBlocks);
        }

        // 7. Actualizar metadata
        metadata.setSize(metadata.getSize() + content.length());
        metadata.setLastUpdate(LocalDateTime.now());

        System.out.println("Contenido agregado al archivo '" + name + "'.");

        return true;
    }

    public void renameFile(String oldName, String newName) {
        if (!directory.exists(oldName)) {
            System.out.println("El archivo '" + oldName + "' no existe.");
            return;
        }

        if (directory.exists(newName)) {
            System.out.println("Ya existe un archivo con el nombre '" + newName + "'.");
            return;
        }

        boolean success = directory.renameFile(oldName, newName);

        if (success) {
            System.out.println("Archivo renombrado correctamente: " + oldName + " → " + newName);
        } else {
            System.out.println("No se pudo renombrar el archivo.");
        }
    }

    /**
     * Lee un archivo completo.
     */
    public void readFile(String name) {

        if (!directory.exists(name)) {
            System.out.println(MSG_FILE_NOT_FOUND);
            return;
        }

        MetadataFile metadata = directory.getMetadata(name);
        int firstBlock = metadata.getFirstBlock();

        // Obtener cadena de bloques
        List<Integer> blocks = fat.getBlockChain(firstBlock);

        // Leer contenido completo
        String content = disk.readFullContent(blocks);

        System.out.println(content);

        System.out.println("\nTamaño: " + metadata.getSize() + " caracteres");
        System.out.println("Bloques asignados: " + blocks);
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

        if (!directory.exists(name)) {
            System.out.println(MSG_FILE_NOT_FOUND);
            return;
        }

        MetadataFile metadata = directory.getMetadata(name);
        int firstBlock = metadata.getFirstBlock();

        // Obtener cadena de bloques
        List<Integer> blocks = fat.getBlockChain(firstBlock);

        System.out.println("Liberando bloques: " + blocks);

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

        System.out.println("Archivo eliminado correctamente.");
    }

    /**
     * Muestra los bloques que ocupa un archivo.
     */
    public void showBlocks(String name) {
        System.out.println("Bloques del archivo: " + name);

        if (!directory.exists(name)) {
            System.out.println(MSG_FILE_NOT_FOUND);
            return;
        }

        MetadataFile metadata = directory.getMetadata(name);
        int firstBlock = metadata.getFirstBlock();

        List<Integer> blocks = fat.getBlockChain(firstBlock);

        System.out.println("\n-----CADENA FAT-----");

        for (int block : blocks) {
            int next = fat.getNextBlock(block);
            String nextStr = (next == END_OF_FILE) ? "EOF" : String.valueOf(next);
            System.out.printf("Bloque %3d → %s\n", block, nextStr);
        }

        System.out.println("\n-----BLOQUES DEL DISCO-----");

        for (int i = 0; i < blocks.size(); i++) {
            int block = blocks.get(i);
            String content = disk.readBlock(block);
            System.out.printf("Bloque %3d: \"%s\"%n", block, content);
        }

        System.out.println("\nTotal de bloques: " + blocks.size());
    }

    /**
     * Muestra el estado completo del sistema.
     */
    public void showSystemStatus() {
        fat.printStatus();
        disk.printStatus();
        directory.list();
    }
}

