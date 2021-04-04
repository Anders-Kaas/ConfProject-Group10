import net.sf.javabdd.*;
import java.lang.Math;

public class AndreasLogic implements IQueensLogic{
    private int size;		// Size of quadratic game board (i.e. size = #rows = #columns)
    private int[][] board;	// Content of the board. Possible values: 0 (empty), 1 (queen), -1 (no queen allowed)
    private BDDFactory fact = JFactory.init(100000,100000);
    private int nVars;
  
    //Global BDD that is used to find the (in)valid positions of the queens
    private BDD bdd;
    @Override
    public void initializeBoard(int size) {
        this.size = size;
        board = new int[size][size];
        nVars = size * size;
        fact.setVarNum(nVars);
        this.bdd = initializeBDD();
  
        //For N=6, some positions are not allowed from the start, so we have to
        //do an initial call to updateBoard
        updateBoard();
  
        
    }

    @Override
    public int[][] getBoard() {
        // TODO Auto-generated method stub
        return board;
    }

    @Override
    public void insertQueen(int column, int row) {
        // TODO Auto-generated method stub
        board[column][row] = 1;
        this.bdd = this.bdd.and(fact.ithVar(cordtoindex(column, row)));
        updateBoard();
    }
    //cord to index
    private int cordtoindex(int i, int j){
        if(i > size){
            System.out.print("Error, i size" + i);
        }
        if(j > size){
            System.out.print("Error, j size" + j);
        }
        return i*this.size + j;
    }
    private void updateBoard(){
        for (int i = 0; i<this.size; i++){
          for (int j=0; j<this.size; j++){
            //if queen in position i,j will bdd always be fail
            boolean notqueensat = this.bdd.and(fact.ithVar(cordtoindex(i,j))).isZero();
            //if empty in position i,j will bdd always be fail. i.e. queen must be in spot
            boolean notemptysat = this.bdd.and(fact.nithVar(cordtoindex(i,j))).isZero();
            
            //if satisfiable of holding a queen but cannot be empty, it must contain a queen
            if(!notqueensat && notemptysat){
                board[i][j] = 1;
            }
            //if possible to be empty but cannot satisfy a queen, this it must be empty.
            if(!notemptysat && notqueensat){
                board[i][j] = -1;
            }

          }
        }
      }
    //this.bdd.and(getVar(c,r)).isZero(), this.bdd.and(getNotVar(c,r)).isOne()};
    // index0: 
    
    private BDD initializeBDD() {

        //Use same terminology as BDD notes
        int N = this.size;
    
    
        //Final BDD that will be returned in the end after the restrictions/implications
        //have been imposed
        BDD bdd = fact.one();
    
        BDD currentNode = fact.one();
        BDD rowImplication = fact.one();
        BDD columnImplication = fact.one();
        BDD diagonal1Implication = fact.one();
        BDD diagonal2Implication = fact.one();
    
        //We iterate through every position on the board and include all the implications
        //that emerge from placing a queen in that position
        for (int i=0; i<N; i++){
          for (int j=0; j<N; j++){
            currentNode = fact.ithVar(cordtoindex(i,j));
    
            //Column implication. Only 1 queen per column
            columnImplication = fact.one();
            for (int l = 0; l<N; l++){
              if (l!=j){
                  columnImplication = columnImplication.and(fact.nithVar(cordtoindex(i,l)));
              }
            }
    
            //Row implication. Only 1 queen per row
            rowImplication = fact.one();
            for (int k = 0; k<N; k++){
              if (k!=i){
                  rowImplication = rowImplication.and(fact.nithVar(cordtoindex(k,j)));
              }
            }
    
            //Diagonal1 implication. Only 1 queen per diagonal
            diagonal1Implication = fact.one();
            for (int k = 0; k<N-Math.abs(i-j); k++){
                if(i > j){
                    if (k != j){
                        diagonal1Implication = diagonal1Implication.and(fact.nithVar(cordtoindex(k+Math.abs(i-j),k)));
                    }
                }
                else{
                    if (k != i){
                        diagonal1Implication = diagonal1Implication.and(fact.nithVar(cordtoindex(k,k+Math.abs(i-j))));
                    }
                }
            }
            
            //Diagonal2 implication. Only 1 queen per diagonal
            diagonal2Implication = fact.one();
            for (int k = 0; k<N; k++){
              if (0 <= j+i-k && j+i-k < N && k != i){
                diagonal2Implication = diagonal2Implication.and(fact.nithVar(cordtoindex(k,j+i-k)));
              }
            }
    
            //"Add" all the restrictions to the global BDD using a conjunction
            bdd = bdd.and(currentNode.imp(columnImplication));
            bdd = bdd.and(currentNode.imp(rowImplication));
            bdd = bdd.and(currentNode.imp(diagonal1Implication));
            bdd = bdd.and(currentNode.imp(diagonal2Implication));
    
          }
        }
    
    
        //Last restriction: There must be at least one queen per column
        //(Could as well be row)
        BDD columnRestriction = fact.zero();
        for (int i=0; i<N; i++){
          columnRestriction = fact.zero();
          for (int j=0; j<N; j++){
            columnRestriction = columnRestriction.or(fact.ithVar(cordtoindex(i,j)));
          }
          bdd = bdd.and(columnRestriction);
        }
    
    
    
        return bdd;
      }
    
}
