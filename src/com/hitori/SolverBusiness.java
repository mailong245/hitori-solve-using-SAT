package com.hitori;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import java.io.*;

public class SolverBusiness {

    private static final int HEADER_LENGTH_FILE_CNF = 50;
    private static FileWriter writer;
    private static int numberOfClauses;
    private static final String FILE_PATH_IN = "file/Cnf.in";
    private static final String FILE_PATH_OUT = "file/Cnf.out";
    private int[] result = null;
    private int nConstraints = 0;
    private int nVar = 0;

    public SolverBusiness() {
        try {
            writer = new FileWriter(FILE_PATH_IN);
            writer.write(String.format("%" + HEADER_LENGTH_FILE_CNF + "s", " ") + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        numberOfClauses = 0;
    }

    public void addClause(int... clause) {
        try {
            if (clause.length == 0 || writer == null) return;
            for (int cl : clause)
                writer.write(cl + " ");
            writer.write("0\n");
            numberOfClauses++;
        } catch (IOException ex) {
            System.out.println("IOException in file cnf!");
        }
    }

    public void addVarsAndClauses(int NumberOfVars) {
        String header = "p cnf " + NumberOfVars + " " + numberOfClauses;
        try {
            writer.close();
            RandomAccessFile f = new RandomAccessFile(new File(FILE_PATH_IN), "rw");
            f.seek(0); // to the beginning
            f.writeBytes(header);
        } catch (Exception ex) {
            System.out.println("RandomAccessFile IOException in file cnf!");
        }
    }

    public boolean solve() {
        System.out.println("Start count");
        long start = System.nanoTime();
        ISolver solver = SolverFactory.newLight();
        Reader reader = new DimacsReader(solver);
        try {
            IProblem problem = reader.parseInstance(FILE_PATH_IN);
            if (problem.isSatisfiable()) {
                this.result = problem.model();
                System.out.println("SAT");
                FileWriter myWriter = new FileWriter(FILE_PATH_OUT);
                myWriter.write("SAT\n");
                for (int result : this.result) {
                    myWriter.write(result + " ");
                    System.out.print(result + " ");
                }
                System.out.println();
                myWriter.close();
                this.nConstraints = solver.nConstraints();
                this.nVar = solver.nVars();
                long end = System.nanoTime();
                double total_time = (double) (end - start) / 1_000_000_000;
                System.out.println("Process time: " + total_time);
                System.out.println("Number of constraint (SAT result): " + nConstraints);
                System.out.println("Number of Variables: " + nVar);
                System.out.println("Number of Clauses (Self count): " + numberOfClauses);

                return true;
            } else {
                System.out.println("UnSaT");
            }
        } catch (FileNotFoundException e) {
            System.out.println("The file cannot be found");
        } catch (ParseFormatException e) {
            System.out.println("An error occurs during parsing");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("An I/O error occurs");
        } catch (ContradictionException e) {
            System.out.println("Unsatisfiable (trivial)!");
        } catch (TimeoutException e) {
            System.out.println("Timeout, sorry!");
        }
        return false;
    }

    public int[] getResult() {
        return result;
    }

    public int getnConstraints() {
        return nConstraints;
    }

    public int getnVar() {
        return nVar;
    }

    public static FileWriter getWriter() {
        return writer;
    }

    public static int getnumberOfClauses() {
        return numberOfClauses;
    }


}
