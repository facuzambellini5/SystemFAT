import models.Disk;
import models.FAT;

import java.util.List;


public class Main {
    public static void main(String[] args) {

        FAT fat = new FAT();

        String content = "Bueno esto es una prueba para ver cómo se comporta el disco y bueno reza malena reza";

        Disk disk = new Disk();

        disk.imprimir();


    }
}
