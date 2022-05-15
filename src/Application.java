import com.hitori.HitoriSolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Application {

    private static final int Rows = 5;
    private static final int Columns = 5;
    private static final String MAP_INDEX = "1";
    private static final String PATH_MAP = "map";

    public static void main(String[] args) throws FileNotFoundException {
        // Đọc file
        int[][] value = new int[Rows][Columns];
        String st = PATH_MAP + "/" + Rows + "x" + Columns + ".ma";
        Scanner scan = new Scanner(new File(st));
        String map = "[" + MAP_INDEX + "]";
        while (scan.hasNext()){
            if (scan.next().startsWith(map)) {
                for (int i = 0; i < Rows; i++)
                    for (int j = 0; j < Columns; j++)
                        value[i][j] = scan.nextInt();
                break;
            }
        }

        // Khởi tạo đối tượng solver
        HitoriSolver hitoriSolver = new HitoriSolver(Rows, Columns, value);
        String[][] valueClone = new String[Rows][Columns];
        for (int i = 0; i < Rows; i++) {
            for (int j = 0; j < Columns; j++) {
                valueClone[i][j] = String.valueOf(value[i][j]);
            }
        }

        // Xuất ra kết quả
        if (hitoriSolver.buildAndSolveHitori()) {
            boolean[][] result = hitoriSolver.getResult();
            for (int i = 0; i < Rows; i++) {
                for (int j = 0; j < Columns; j++) {
                    if (!result[i][j]) {
                        valueClone[i][j] = "[" + valueClone[i][j] + "]";
                    }
                    System.out.print(" " + valueClone[i][j] + " ");
                }
                System.out.println();
            }
        }
    }


}
