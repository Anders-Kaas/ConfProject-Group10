/**
 * This class implements a basic logic for the n-queens problem to get you started. 
 * Actually, when inserting a queen, it only puts the queen where requested
 * and does not keep track of which other positions are made illegal by this move.
 * 
 * @author Kristian Nørgaard Jesen
 * @version 04.04.2021
 */


import net.sf.javabdd.*;


public class Group10Logic implements IQueensLogic {

    private int size;		// Size of quadratic game board (i.e. size = #rows = #columns)
    private int[][] board;	// Content of the board. Possible values: 0 (empty), 1 (queen), -1 (no queen allowed)
    private BDDFactory fact = JFactory.init(2_000_000, 200_000);
    private BDD bdd;

    @Override
    public void initializeBoard(int size) {
        // TODO Auto-generated method stub
        this.size = size;
        this.board = new int[size][size];
        this.fact.setVarNum(this.size * this.size);
        // this.fact.printAll();
        this.bdd = initializeBDD();

        checkBoard();
    }

    @Override
    public int[][] getBoard() {
        // TODO Auto-generated method stub
        return this.board;
    }

    @Override
    public void insertQueen(int column, int row) {
        // TODO Auto-generated method stub
        this.board[column][row] = 1;
        this.bdd = updateBDD(column, row);
        checkBoard();
    }

    private int getField(int column, int row) {
        return (column * this.size) + row;
    }

    private void checkBoard() {
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                boolean add = !this.bdd.and(this.fact.ithVar(getField(i, j))).isZero();
                boolean remove = !this.bdd.and(this.fact.nithVar(getField(i, j))).isZero();
                if (add && !remove) {
                    this.board[i][j] = 1;
                } else if (!add && remove) {
                    this.board[i][j] = -1;
                }
            }
        }
    }
    
    private BDD initializeBDD() {
        // Attacking on column and rows
        BDD rules = this.fact.one();

        BDD columnAttack;
        BDD rowAttact;
        BDD diagonal1Attack;
        BDD diagonal2Attack;
        BDD currentField;

        for (int i = 0; i < this.size; i++) { // Row Iteration
            for (int j = 0; j < this.size; j++) { // Column Iteration
                // Column rules
                columnAttack = this.fact.one();
                for (int l = 0; l < this.size; l++) if (l != j) {
                    columnAttack = columnAttack.and(this.fact.nithVar(getField(i, l)));
                }

                // Row rules
                rowAttact = this.fact.one();
                for (int k = 0; k < this.size; k++) if (k != i) {
                    rowAttact = rowAttact.and(this.fact.nithVar(getField(k, j)));
                }

                // Diagonal 1
                diagonal1Attack = this.fact.one();
                for (int k = 0; k < this.size; k++) if (k != i && (j+k-i) >= 0 && (j+k-i) < this.size) {
                    diagonal1Attack = diagonal1Attack.and(this.fact.nithVar(getField(k, j+k-i)));
                }
                
                // Diagonal 2
                diagonal2Attack = this.fact.one();
                for (int k = 0; k < this.size; k++) if (k != i && (j+i-k) >= 0 && (j+i-k) < this.size) {
                    diagonal2Attack = diagonal2Attack.and(this.fact.nithVar(getField(k, j+i-k)));
                }

                // Get current fields var from BDD
                currentField = this.fact.ithVar(getField(i, j));

                rules = rules.and(currentField.imp(columnAttack));
                rules = rules.and(currentField.imp(rowAttact));
                rules = rules.and(currentField.imp(diagonal1Attack));
                rules = rules.and(currentField.imp(diagonal2Attack));
            }
        }

        BDD columnRule;
        for (int i = 0; i < this.size; i++) {
            columnRule = fact.zero();
            for (int j = 0; j < this.size; j++) {
                columnRule = columnRule.or(this.fact.ithVar(getField(i, j)));
            }
            rules = rules.and(columnRule);
        }

        return rules;
    }

    private BDD updateBDD(int column, int row) {
        return this.bdd.and(this.fact.ithVar(getField(column, row)));
    }
}
