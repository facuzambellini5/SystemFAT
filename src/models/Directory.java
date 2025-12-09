package models;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Directory {

    private Map<String,MetadataFile> files;

    public Directory(){
        this.files = new HashMap<>();
    }

    public void addFile(String name, MetadataFile metadata){
        files.put(name, metadata);
    }

    public MetadataFile getMetadata(String name){
        return files.get(name);
    }

    public void deleteFile(String name){
        files.remove(name);
    }

    public Set<String> getFileNames(){
        return files.keySet();
    }

    public boolean exists(String name){
        return files.containsKey(name);
    }

    public boolean isEmpty(){
        return files.isEmpty();
    }

    public int count(){
        return files.size();
    }

    /**
     * Lista todos los archivos con formato.
     */
    public void list(){
        if (isEmpty()) {
            System.out.println("\n📂 El directorio está vacío.");
            return;
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                              DIRECTORIO DE ARCHIVOS                                        ║");
        System.out.println("╠════════════════════════════════════════════════════════════════════════════════════════════╣");

        for(MetadataFile metadata : files.values()){
            System.out.println("║ " + metadata.toFormattedString() + " ║");
        }

        System.out.println("╚════════════════════════════════════════════════════════════════════════════════════════════╝");
        System.out.println("Total de archivos: " + count());
    }
}
