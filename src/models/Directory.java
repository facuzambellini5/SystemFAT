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
            System.out.println("\nEl directorio está vacío.");
            return;
        }

        System.out.println("\n-----DIRECTORIO DE ARCHIVOS-----");

        for(MetadataFile metadata : files.values()){
            System.out.println(metadata.toFormattedString());
        }

        System.out.println("\nTotal de archivos: " + count());
    }
}
