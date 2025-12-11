package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class MetadataFile {

    private String name;
    private int size;
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdate;
    private int firstBlock;

    private DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);

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

    /**
     * Formato para listar en el directorio.
     */
    public String toFormattedString() {
        return String.format("%-15s | %6d caracteres | Creado: %s | Modificado: %s | Bloque inicial: %3d",
                name,
                size,
                createdDate.format(formatter),
                lastUpdate.format(formatter),
                firstBlock);
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