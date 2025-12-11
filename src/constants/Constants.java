package constants;

public class Constants {

    // Configuración del disco
    public static final int TOTAL_BLOCKS = 100;
    public static final int BLOCK_SIZE = 10;

    // Bloques reservados para el sistema (concepto educativo)
    public static final int RESERVED_BLOCKS_START = 0;
    public static final int RESERVED_BLOCKS_END = 9;
    public static final int RESERVED_BLOCKS_COUNT = 10;

    // Primer bloque disponible para archivos
    public static final int FIRST_AVAILABLE_BLOCK = 10;

    // Valores especiales de FAT
    public static final int AVAILABLE_BLOCK = 0;
    public static final int END_OF_FILE = -1;
    public static final int RESERVED_BLOCK = -2;  // Marca bloques del sistema

    // Mensajes del sistema
    public static final String MSG_DISK_OUT_OF_SPACE = "ERROR: No hay suficiente espacio en el disco.";
    public static final String MSG_FILE_NOT_FOUND = "ERROR: Archivo no encontrado.";
    public static final String MSG_FILE_SAVED = "Archivo guardado correctamente.";
    public static final String MSG_SYSTEM_FORMATTED = "Sistema formateado correctamente.";
}