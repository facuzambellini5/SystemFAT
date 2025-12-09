package models;

import java.util.Arrays;
import java.util.List;

import static models.Constants.BLOCK_SIZE;
import static models.Constants.TOTAL_BLOCKS;

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
       disk[blockNumber]  = "";
   }

   public void writeContent(String content, List<Integer> availableBlocks){
       int contentPointer = 0;  // Posición actual en el contenido

       for (int block : availableBlocks) {
           // Calcular el rango a copiar
           int inicio = contentPointer;
           int fin = Math.min(contentPointer + BLOCK_SIZE, content.length());

           // Extraer fragmento y escribir en el disco
           String fragment = content.substring(inicio, fin);
           disk[block] = fragment;

           contentPointer += BLOCK_SIZE;
       }
   }

   public String readFullContent(List<Integer> blockNumbers){

       StringBuilder fullContent = new StringBuilder();

       for(int block : blockNumbers){
           fullContent.append(readBlock(block));
       }

       return fullContent.toString();
   }

    public int calculateRequiredBlocks(String content) {
        return (int) Math.ceil((double) content.length() / BLOCK_SIZE);
    }


    public void imprimir() {
        System.out.println("ESTADO DEL DISCO");
        System.out.println(Arrays.toString(disk));
    }


}
