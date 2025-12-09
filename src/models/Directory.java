package models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Directory {

    private Map<String,MetadataFile> files;

    public Directory(){
        this.files = new HashMap<>();
    }

    public void addFile(String name, MetadataFile metadata){
        files.put(name,metadata);
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

    public void list(){
        for(MetadataFile metadata : files.values()){
            System.out.println(metadata.toString());
        }
    }

    public boolean exists(String name){
        return files.containsKey(name);
    }
}
