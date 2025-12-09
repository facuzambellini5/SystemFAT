package models;

import java.util.*;

import static models.Constants.*;

public class FAT {

    private int[] fat;

    public FAT() {
        this.fat = new int[TOTAL_BLOCKS];
        Arrays.fill(fat, AVAILABLE_BLOCK);
    }

    public void linkBlocks(int actualBlock, int nextBlock){
        fat[actualBlock] = nextBlock;
    }

    public void markAsEndOfFile(int blockNumber){
        fat[blockNumber] = END_OF_FILE;
    }

    public void setAvailable(int blockNumber){
        fat[blockNumber] = AVAILABLE_BLOCK;
    }

    public int getNextBlock(int blockNumber){
        return fat[blockNumber];
    }

    /**
     * Busca bloques disponibles en la FAT.
     */
    public List<Integer> searchAvailableBlocks(int requiredBlocks) {
        List<Integer> availableBlocks = new ArrayList<>();

        for (int i = 0; i < fat.length; i++){
            if (fat[i] == AVAILABLE_BLOCK){
                availableBlocks.add(i);
                if (availableBlocks.size() == requiredBlocks) break;
            }
        }
        return availableBlocks;
    }

    /**
     * Cuenta bloques disponibles totales.
     */
    public int countAvailableBlocks(){
        int count = 0;
        for (int block : fat){
            if (block == AVAILABLE_BLOCK) count++;
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

    public void printStatus() {
        System.out.println("ESTADO FAT");
        for (int i = 0; i < fat.length; i++) {
            if (fat[i] != AVAILABLE_BLOCK) {
                String value = (fat[i] == END_OF_FILE) ? "EOF" : String.valueOf(fat[i]);
                System.out.printf("  Bloque %3d → %s%n", i, value);
            }
        }
        System.out.println("  Bloques libres: " + countAvailableBlocks() + "/" + TOTAL_BLOCKS);
    }
}