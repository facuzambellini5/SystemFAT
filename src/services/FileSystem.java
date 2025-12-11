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
        System.out.println("Bloques reservados (sistema): " + RESERVED_BLOCKS_COUNT + " (bloques 0-9)");
        System.out.println("Bloques disponibles (archivos): " + (TOTAL_BLOCKS - RESERVED_BLOCKS_COUNT));
        System.out.println("Tamaño de bloque: " + BLOCK_SIZE + " caracteres");
        System.out.println("Capacidad total: " + ((TOTAL_BLOCKS - RESERVED_BLOCKS_COUNT) * BLOCK_SIZE) + " caracteres\n");

    }

    /**
     * Guardar un archivo. Si existe, anexar contenido.
     */
    public void saveFile(String name, String content) {

        if (directory.exists(name)) {
            appendContent(name, content);
            return;
        }

        createFile(name, content);
    }

    /**
     * Crear un archivo nuevo.
     */
    private void createFile(String name, String content) {
        //Calcular bloques necesarios
        int requiredBlocks = Disk.calculateRequiredBlocks(content);
        System.out.println("Bloques necesarios: " + requiredBlocks);

        //Buscar bloques disponibles
        List<Integer> availableBlocks = fat.searchAvailableBlocks(requiredBlocks);

        //Validar espacio suficiente
        if (availableBlocks.size() < requiredBlocks) {
            System.out.println(MSG_DISK_OUT_OF_SPACE);
            System.out.println("Se necesitan " + requiredBlocks + " bloques, solo hay " + availableBlocks.size());
            return;
        }

        System.out.println("Bloques asignados: " + availableBlocks);

        //Escribir en disco
        disk.writeFragmented(content, availableBlocks);

        //Actualizar FAT
        fat.updateFAT(availableBlocks);

        //Agregar al directorio
        int firstBlock = availableBlocks.getFirst();
        MetadataFile metadata = new MetadataFile(name, content.length(), firstBlock);
        directory.addFile(name, metadata);

        System.out.println(MSG_FILE_SAVED);
    }

    /**
     * Anexar contenido a un archivo existente.
     */
    private boolean appendContent(String name, String content) {
        //Obtener metadata del archivo
        MetadataFile metadata = directory.getMetadata(name);
        int firstBlock = metadata.getFirstBlock();

        //Obtener cadena de bloques actual
        List<Integer> currentBlocks = fat.getBlockChain(firstBlock);
        int lastBlock = currentBlocks.getLast();

        //Verificar espacio disponible en el último bloque
        int availableSpace = disk.getAvailableSpace(lastBlock);
        String existingContent = disk.readBlock(lastBlock);

        //Calcular cuánto podemos agregar al último bloque
        int toAppendInLastBlock = Math.min(availableSpace, content.length());
        String remainingContent = content.substring(toAppendInLastBlock);

        //Completar el último bloque
        if (toAppendInLastBlock > 0) {
            disk.write(lastBlock, existingContent + content.substring(0, toAppendInLastBlock));
        }

        //Si queda contenido, necesitamos más bloques
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

        //Actualizar metadata
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
     * Leer un archivo completo.
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
     * Listar todos los archivos.
     */
    public void listFiles() {
        directory.list();
    }

    /**
     * Eliminar un archivo.
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
     * Mostrar los bloques que ocupa un archivo.
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
     * Mostrar el estado completo del sistema.
     */
    public void showStatus() {
        fat.printStatus();
        disk.printStatus();
        directory.list();
    }

    /**
     * Mostrar estadísticas del sistema.
     */
    public void showStats() {
        int totalBlocks = TOTAL_BLOCKS - RESERVED_BLOCKS_COUNT;
        int usedBlocks = fat.countUsedBlocks();
        int freeBlocks = fat.countAvailableBlocks();

        int totalCapacity = totalBlocks * BLOCK_SIZE;
        int usedCapacity = usedBlocks * BLOCK_SIZE;
        int freeCapacity = freeBlocks * BLOCK_SIZE;

        double usagePercentage = (double) usedBlocks / totalBlocks * 100;

        int fileCount = directory.count();
        double avgFragmentation = fileCount > 0 ? (double) usedBlocks / fileCount : 0;


        System.out.println("\n-----ESTADÍSTICAS DEL SISTEMA FAT-----");


        System.out.println("\nCAPACIDAD:");
        System.out.printf("Capacidad total:    %d caracteres\n", totalCapacity);
        System.out.printf("Espacio usado:      %d caracteres (%.1f%%)\n", usedCapacity, usagePercentage);
        System.out.printf("Espacio libre:      %d caracteres (%.1f%%)\n", freeCapacity, 100 - usagePercentage);

        System.out.println("\nBLOQUES:");
        System.out.printf("Total de bloques:   %d bloques\n", totalBlocks);
        System.out.printf("Bloques ocupados:   %d bloques\n", usedBlocks);
        System.out.printf("Bloques libres:     %d bloques\n", freeBlocks);
        System.out.printf("Bloques reservados: %d bloques (sistema)\n", RESERVED_BLOCKS_COUNT);

        System.out.println("\nARCHIVOS:");
        System.out.printf("Total de archivos:  %d\n", fileCount);
        System.out.printf("Fragmentación avg:  %.2f bloques/archivo\n", avgFragmentation);

        System.out.println("\nCONFIGURACIÓN:");
        System.out.printf("Tamaño de bloque:   %d caracteres\n", BLOCK_SIZE);
        System.out.printf("Bloques totales:    %d (10 reservados + 90 disponibles)\n", TOTAL_BLOCKS);

        // Barra de progreso visual
        System.out.println("\nOCUPACIÓN DEL DISCO:");
        printProgressBar(usagePercentage);
    }

    /**
     * Mostrar una barra de progreso visual.
     */
    private void printProgressBar(double percentage) {
        int barLength = 40;
        int filled = (int) (barLength * percentage / 100);

        System.out.print("   [");
        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                System.out.print("█");
            } else {
                System.out.print("░");
            }
        }
        System.out.printf("] %.1f%%\n", percentage);
    }

    /**
     * Formatear el sistema completo.
     */
    public void format() {

        // Limpiar todas las estructuras
        fat.format();
        disk.format();
        directory.clear();

        System.out.println(MSG_SYSTEM_FORMATTED);
    }

}