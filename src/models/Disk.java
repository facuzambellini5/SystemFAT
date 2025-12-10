package models;

import java.util.Arrays;
import java.util.List;

import static constants.Constants.BLOCK_SIZE;
import static constants.Constants.TOTAL_BLOCKS;

public class Disk {

   private String[] disk;

   //Inicializar disco vacío.
   public Disk(){
       this.disk = new String[TOTAL_BLOCKS];
       Arrays.fill(disk, "");
   }

    public void write(int blockNumber, String content){
        disk[blockNumber] = content;
    }

    public String readBlock(int blockNumber){
        return disk[blockNumber];
    }

    public void delete(int blockNumber){
        disk[blockNumber] = "";
    }

    /**
     * Escribe contenido fragmentado en múltiples bloques.
     */
    public void writeFragmented(String content, List<Integer> availableBlocks){

        int contentPointer = 0;

        for (int block : availableBlocks) {
            int start = contentPointer;
            int end = Math.min(contentPointer + BLOCK_SIZE, content.length());

            String fragment = content.substring(start, end);
            disk[block] = fragment;

            contentPointer += BLOCK_SIZE;
        }
    }

    /**
     * Lee contenido completo de múltiples bloques.
     */
    public String readFullContent(List<Integer> blockNumbers){
        StringBuilder fullContent = new StringBuilder();

        for(int block : blockNumbers){
            fullContent.append(readBlock(block));
        }

        return fullContent.toString();
    }

    /**
     * Calcula cuántos bloques necesita un contenido.
     */
    public static int calculateRequiredBlocks(String content) {
        return (int) Math.ceil((double) content.length() / BLOCK_SIZE);
    }

    /**
     * Obtiene espacio disponible en un bloque.
     */
    public int getAvailableSpace(int blockNumber) {
        return BLOCK_SIZE - disk[blockNumber].length();
    }

    /**
     * Verifica si un bloque está vacío.
     */
    public boolean isEmpty(int blockNumber) {
        return disk[blockNumber].isEmpty();
    }

    /**
     * Imprime el estado del disco (para debugging).
     */
    public void printStatus() {
        System.out.println("\n-----ESTADO DISCO-----");
        for (int i = 0; i < disk.length; i++) {
            if (!isEmpty(i)) {
                System.out.printf("Bloque " + i + ": " + disk[i]+"\n");
            }
        }
    }
}
