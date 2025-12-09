package models;

import java.util.*;

import static models.Constants.*;

public class FAT {

    private int[] fat;
    private Disk disk;
    private Directory directory;


    public FAT() {
        this.fat = new int[TOTAL_BLOCKS];
        Arrays.fill(fat, AVAILABLE_BLOCK);

        this.disk = new Disk();
        this.directory = new Directory();

    }

    public boolean isAvailable(int block){
        return fat[block] == AVAILABLE_BLOCK;
    }

    public void linkBlocks (int actualBlock, int nextBlock){
        fat[actualBlock] = nextBlock;
    }

    public void endOfFile(int blockNumber){
        fat[blockNumber] = END_OF_FILE;
    }

    public void setAvailable(int blockNumber){
        fat[blockNumber] = AVAILABLE_BLOCK;
    }

    public int getNextBlock(int blockNumber){
        return fat[blockNumber];
    }



    // Método principal
    public boolean saveFile(String name, String content) {

        if (directory.exists(name)) {
            return addContentToFile(name, content);
        }

        return createFile(name, content);
    }

    // Métodos que acabamos de implementar
    public boolean createFile(String name, String content) {

        //calcular cantidad de bloques requeridos
        int requiredBlocks = calculateRequiredBlocks(content);

        //buscar bloques disponibles en la fat
        List<Integer> availableBlocks = searchAvailableBlocks(requiredBlocks);

        //validar si hay suficientes bloques disponibles
        if (availableBlocks.size() < requiredBlocks){
            System.out.println(MSG_DISK_OUT_OF_SPACE);
            return false;
        }

        //escribir en disco
        writeToDisk(content, availableBlocks);

        //actualizar FAT.FAT
        updateFAT(availableBlocks);

        //Agregar entrada al directorio
        int firstBlock = availableBlocks.getFirst();

        MetadataFile metadata = new MetadataFile(name, content.length(), firstBlock);
        directory.put(name, metadata);


        System.out.println(directory);
        return true;

    }

    public int calculateRequiredBlocks(String content) {
        return (int) Math.ceil((double) content.length() / BLOCK_SIZE);
    }

    private List<Integer> searchAvailableBlocks(int requiredBlocks) {

        List<Integer> availableBlocks = new ArrayList<>();

        for (int i = 0; i < FAT.length; i++){
            if (FAT[i] == AVAILABLE_BLOCK){
                availableBlocks.add(i);
                if (availableBlocks.size() == requiredBlocks) break;
            }
        }
        return availableBlocks;
    }

    public int countAvailableBlocks(){
        int availableBlocks = 0;
        for (int block : fat){
            if (block == AVAILABLE_BLOCK) availableBlocks ++;
        }
        return availableBlocks;
    }

    public void writeToDisk(String content, List<Integer> blocks) {

        int contentPointer = 0;  // Posición actual en el contenido

        for (int blockNumber : blocks) {
            // Calcular el rango a copiar
            int inicio = contentPointer;
            int fin = Math.min(contentPointer + BLOCK_SIZE, content.length());

            // Extraer fragmento y escribir en el disco
            String fragment = content.substring(inicio, fin);
            disk[blockNumber] = fragment;

            contentPointer += BLOCK_SIZE;
        }

    }

    private void updateFAT(List<Integer> blocks) {

        // Enlazar todos los bloques excepto el último
        for (int i = 0; i < blocks.size() - 1; i++) {
            int actualBlock = blocks.get(i);
            int nextBlock = blocks.get(i + 1);

            FAT[actualBlock] = nextBlock;
            System.out.println("  FAT[" + actualBlock + "] = " + nextBlock);
        }

        // El último bloque apunta a -1 (fin de archivo)
        int ultimoBloque = blocks.getLast();
        FAT[ultimoBloque] = END_OF_FILE;
        System.out.println("  FAT[" + ultimoBloque + "] = "+ END_OF_FILE + "(FIN)");

    }

    // Pendiente de implementar
    private boolean addContentToFile(String nombre, String contenido) {
        // TODO: próximo paso
        return false;
    }
}