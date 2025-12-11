package models;

import java.util.*;

import static constants.Constants.*;

public class Fat {

    private int[] fat;

    public Fat() {
        this.fat = new int[TOTAL_BLOCKS];
        initializeFAT();
    }

    /**
     * Inicializa la FAT marcando bloques reservados y disponibles.
     */
    private void initializeFAT() {
        // Marcar bloques reservados para el sistema (0-9)
        for (int i = RESERVED_BLOCKS_START; i <= RESERVED_BLOCKS_END; i++) {
            fat[i] = RESERVED_BLOCK;
        }

        // Marcar resto como disponible
        for (int i = FIRST_AVAILABLE_BLOCK; i < TOTAL_BLOCKS; i++) {
            fat[i] = AVAILABLE_BLOCK;
        }
    }

    public void linkBlocks(int actualBlock, int nextBlock){
        fat[actualBlock] = nextBlock;
    }

    public void markAsEndOfFile(int blockNumber){
        fat[blockNumber] = END_OF_FILE;
    }

    public void setAvailable(int blockNumber){
        // No permitir marcar bloques reservados como disponibles
        if (blockNumber >= FIRST_AVAILABLE_BLOCK) {
            fat[blockNumber] = AVAILABLE_BLOCK;
        }
    }

    public int getNextBlock(int blockNumber){
        return fat[blockNumber];
    }

    /**
     * Busca bloques disponibles en la FAT (excluyendo bloques reservados).
     */
    public List<Integer> searchAvailableBlocks(int requiredBlocks) {
        List<Integer> availableBlocks = new ArrayList<>();

        // Comenzar desde el primer bloque disponible (después de los reservados)
        for (int i = FIRST_AVAILABLE_BLOCK; i < fat.length; i++){
            if (fat[i] == AVAILABLE_BLOCK){
                availableBlocks.add(i);
                if (availableBlocks.size() == requiredBlocks) break;
            }
        }
        return availableBlocks;
    }

    /**
     * Cuenta bloques disponibles totales (sin contar reservados).
     */
    public int countAvailableBlocks(){
        int count = 0;
        for (int i = FIRST_AVAILABLE_BLOCK; i < fat.length; i++){
            if (fat[i] == AVAILABLE_BLOCK) count++;
        }
        return count;
    }

    /**
     * Cuenta bloques ocupados por archivos de usuario.
     */
    public int countUsedBlocks() {
        int count = 0;
        for (int i = FIRST_AVAILABLE_BLOCK; i < fat.length; i++) {
            if (fat[i] != AVAILABLE_BLOCK) count++;
        }
        return count;
    }

    /**
     * Obtiene la cadena completa de bloques de un archivo.
     */
    public List<Integer> getBlockChain(int firstBlock) {
        List<Integer> chain = new ArrayList<>();
        int currentBlock = firstBlock;

        while (currentBlock != END_OF_FILE) {
            chain.add(currentBlock);
            currentBlock = fat[currentBlock];
        }

        return chain;
    }

    /**
     * Actualiza la FAT creando una lista enlazada de bloques.
     */
    public void updateFAT(List<Integer> blocks) {
        // Enlazar todos los bloques excepto el último
        for (int i = 0; i < blocks.size() - 1; i++) {
            int actualBlock = blocks.get(i);
            int nextBlock = blocks.get(i + 1);

            fat[actualBlock] = nextBlock;
        }

        // El último bloque marca fin de archivo
        int lastBlock = blocks.getLast();
        fat[lastBlock] = END_OF_FILE;
    }

    /**
     * Reinicia la FAT al estado inicial.
     */
    public void format() {
        initializeFAT();
    }

    public void printStatus() {
        System.out.println("\n-----ESTADO FAT-----");

        // Mostrar bloques reservados
        System.out.println("Bloques reservados del sistema (0-9): RESERVED");

        // Mostrar bloques de archivos
        for (int i = FIRST_AVAILABLE_BLOCK; i < fat.length; i++) {
            if (fat[i] != AVAILABLE_BLOCK) {
                String value = (fat[i] == END_OF_FILE) ? "EOF" : String.valueOf(fat[i]);
                System.out.printf("Bloque %d → %s\n", i, value);
            }
        }

        System.out.println("\nBloques libres: " + countAvailableBlocks() + "/" + (TOTAL_BLOCKS - RESERVED_BLOCKS_COUNT));
    }
}