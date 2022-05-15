package com.hitori;

import java.util.ArrayList;

public class HitoriSolver {
    protected final int Rows, Columns;
    protected int white[][];
    protected int valueOfCell[][];
    protected boolean[][] result;
    protected boolean isAblePaint[][];
    protected int numberOfVars;
    private final SolverBusiness solverBusiness;


    public HitoriSolver(int Rows, int Columns, int[][] value) {
        this.Rows = Rows;
        this.Columns = Columns;
        valueOfCell = value;
        result = new boolean[Rows][Columns];
        white = new int[Rows][Columns];
        isAblePaint = new boolean[Rows][Columns];
        numberOfVars = 0;
        solverBusiness = new SolverBusiness();
        for (int i = 0; i < Rows; i++) {
            for (int j = 0; j < Columns; j++) {
                this.result[i][j] = true;
                this.white[i][j] = i * Columns + j + 1;
                this.isAblePaint[i][j] = false;
            }
        }
    }

    public boolean buildAndSolveHitori() {
        try {
            cnfRule1();
            cnfRule2();
            cnfRule3();
            solverBusiness.addVarsAndClauses(numberOfVars);
            if (solverBusiness.solve()) {
                this.decode(solverBusiness.getResult());
            }
            return true;
        } catch (Exception e) {
            e.getCause();
            return false;
        }
    }

    protected void cnfRule1() {
        // trên một hàng không thể có 2 valueOfCell cùng một giá trị
        int i, j, k;
        for (i = 0; i < Rows; i++)
            for (j = 0; j < Columns - 1; j++)
                for (k = j + 1; k < Columns; k++)
                    if (valueOfCell[i][j] == valueOfCell[i][k]) {
                        solverBusiness.addClause(-1 * white[i][j], -1 * white[i][k]);
                        isAblePaint[i][j] = true;
                        isAblePaint[i][k] = true;
                    }

        // trên một cột không thể có 2 valueOfCell cùng một giá trị
        for (j = 0; j < Columns; j++)
            for (i = 0; i < Rows - 1; i++)
                for (k = i + 1; k < Rows; k++)
                    if (valueOfCell[i][j] == valueOfCell[k][j]) {
                        solverBusiness.addClause(-1 * white[i][j], -1 * white[k][j]);
                        isAblePaint[i][j] = true;
                        isAblePaint[k][j] = true;
                    }
    }

    // Trên cùng một hàng hoặc cột không được phép có 2 ô bôi đen cạnh nhau
    protected void cnfRule2() {
        for (int i = 0; i < Rows; i++)
            for (int j = 0; j < Columns; j++)
                if (isAblePaint[i][j]) {
                    if ((i - 1 >= 0) && isAblePaint[i - 1][j])
                        solverBusiness.addClause(white[i][j], white[i - 1][j]);
                    if ((j - 1 >= 0) && isAblePaint[i][j - 1])
                        solverBusiness.addClause(white[i][j], white[i][j - 1]);
                }
    }

    protected void cnfRule3() {
        int i, j;
        ArrayList<Integer> cycle = new ArrayList<>();
        // Tìm cycle
        for (i = 0; i < Rows; i++)
            for (j = 0; j < Columns; j++)
                findCycleTmp(i, j, cycle);

        // Tìm chain với những tạo thành 2 cột biên của bảng (2 cạnh ngang)
        for (i = 0; i < Rows; i++) {
            findChainTmp(i, 0, cycle);
            findChainTmp(i, Columns - 1, cycle);
        }

        // Tìm chain với những tạo thành 2 hàng biên của bảng (trên dưới)
        for (j = 0; j < Columns; j++) {
            findChainTmp(0, j, cycle);
            findChainTmp(Rows - 1, j, cycle);
        }
        numberOfVars = Rows * Columns;
    }

    // Tìm cycle trong bảng
    private void findCycle(int x, int y, ArrayList<Integer> cycle) {
        if (validate(x, y, cycle)) {
            return;
        }

        int a, b, c = 0;
        int k[] = new int[3];

        // Kiểm tra xem 3 ô còn lại có ô nào đã tồn tại ở trong cycle hay không
        for (a = -1; a < 2; a = a + 2)
            for (b = -1; b < 2; b = b + 2)
                if (x + a >= 0 && x + a < Rows                                          // x phải nằm trong row sau khi đã +-1
                        && y + b >= 0 && y + b < Columns                                // y phải nằm trong row sau khi đã +-1
                        && !cycle.get(cycle.size() - 1).equals(white[x + a][y + b])) {
                    k[c] = cycle.indexOf(white[x + a][y + b]);
                    c++;
                }

        for (int i = 0; i < c; i++)
            if (k[i] > 0) return;
        for (int i = 0; i < c; i++)
            if (k[i] == 0) {
                int[] ints = new int[cycle.size() + 1];
                for (int j = 0, len = cycle.size(); j < len; j++)
                    ints[j] = cycle.get(j);
                ints[cycle.size()] = white[x][y];
                solverBusiness.addClause(ints);
                return;
            }

        // Add ô hiện tại vào cycle và tiếp tục tìm các ô tiếp theo
        cycle.add(white[x][y]);
        for (a = -1; a < 2; a = a + 2)
            for (b = -1; b < 2; b = b + 2)
                findCycle(x + a, y + b, cycle);
        cycle.remove(cycle.indexOf(white[x][y]));
    }

    private void findChain(int x, int y, ArrayList<Integer> chain) {
        if (validate(x, y, chain)) {
            return;
        }

        int a, b;
        for (a = -1; a < 2; a = a + 2)
            for (b = -1; b < 2; b = b + 2)
                if (x + a >= 0 && x + a < Rows
                        && y + b >= 0 && y + b < Columns
                        && !chain.get(chain.size() - 1).equals(white[x + a][y + b])) {
                    // Tránh tìm ra cycle
                    if (chain.contains(white[x + a][y + b])) {
                        return;
                    }
                }

        if ((x == 0 || y == 0 || x == Rows - 1 || y == Columns - 1)
                && white[x][y] > chain.get(0)) {
            int[] ints = new int[chain.size() + 1];
            for (int i = 0, len = chain.size(); i < len; i++)
                ints[i] = chain.get(i);
            ints[chain.size()] = white[x][y];
            solverBusiness.addClause(ints);
            return;
        }

        chain.add(white[x][y]);
        for (a = -1; a < 2; a = a + 2)
            for (b = -1; b < 2; b = b + 2)
                findChain(x + a, y + b, chain);
        chain.remove(chain.indexOf(white[x][y]));
    }

    private void findCycleTmp(int x, int y, ArrayList<Integer> cycle) {
        if (!isAblePaint[x][y]) return;
        int a, b;
        cycle.add(white[x][y]);
        for (a = -1; a < 2; a = a + 2)
            for (b = -1; b < 2; b = b + 2)
                findCycle(x + a, y + b, cycle);
        cycle.remove(cycle.indexOf(white[x][y]));
    }

    private void findChainTmp(int x, int y, ArrayList<Integer> chain) {
        if (!isAblePaint[x][y]) return;
        int a, b;
        chain.add(white[x][y]);
        for (a = -1; a < 2; a = a + 2)
            for (b = -1; b < 2; b = b + 2)
                findChain(x + a, y + b, chain);
        chain.remove(chain.indexOf(white[x][y]));
    }

    protected void decode(int[] arr) {
        int len = arr.length;
        int k;
        for (int i = 0; i < len; i++) {
            k = arr[i];
            if (k == 0 || k > Rows * Columns || k < -1 * Rows * Columns) break;
            if (k > 0) result[(k - 1) / Columns][(k - 1) % Columns] = true;
            if (k < 0) result[(-k - 1) / Columns][(-k - 1) % Columns] = false;
        }
    }

    private boolean validate(int x, int y, ArrayList<Integer> chainOrCycle) {
        if (!(x >= 0 && x < Rows && y >= 0 && y < Columns)) {
            return true;
        } else if (!isAblePaint[x][y]) {
            return true;
        } else if (chainOrCycle.contains(white[x][y])) {
            return true;
        }
        return false;
    }

    public boolean[][] getResult() {
        return result;
    }
}
