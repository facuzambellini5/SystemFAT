package models;

import java.time.LocalDateTime;

public class MetadataFile {

    private String name;
    private int size;
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdate;
    private int firstBlock;

    public MetadataFile(String name, int size, int firstBlock) {
        this.name = name;
        this.size = size;
        this.createdDate = LocalDateTime.now();
        this.lastUpdate = LocalDateTime.now();
        this.firstBlock = firstBlock;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public int getFirstBlock() {
        return firstBlock;
    }

    public void setFirstBlock(int firstBlock) {
        this.firstBlock = firstBlock;
    }

    @Override
    public String toString() {
        return "MetadataFile{" +
                "name='" + name + '\'' +
                ", size=" + size +
                ", createdDate=" + createdDate +
                ", lastUpdate=" + lastUpdate +
                ", firstBlock=" + firstBlock +
                '}';
    }
}
