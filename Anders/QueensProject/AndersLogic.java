import net.sf.javabdd.*;


public class AndersLogic implements IQueensLogic{
  private int size;		// Size of quadratic game board (i.e. size = #rows = #columns)
  private int[][] board;	// Content of the board. Possible values: 0 (empty), 1 (queen), -1 (no queen allowed)
  private BDDFactory fact = JFactory.init(2_000_000,200_000);
  private int nVars;

  //Global BDD that is used to find the (in)valid positions of the queens
  private BDD bdd;


//Constructor kind of
  public void initializeBoard(int size) {
      this.size = size;
      this.board = new int[size][size];
      this.nVars = size * size;
      fact.setVarNum(nVars);
      this.bdd = initializeBDD();

      //For N=6, some positions are not allowed from the start, so we have to
      //do an initial call to updateBoard
      updateBoard();
  }


//Returns the current board
  public int[][] getBoard() {
      return board;
  }


//Inserts a queen in the specified position
  public void insertQueen(int column, int row) {
    board[column][row] = 1;
    updateBdd(column,row);
    updateBoard();
  }

//Used to update this.board with 1, 0 and -1 according to satisfiability
  private void updateBoard(){
    for (int i = 0; i<this.size; i++){
      for (int j=0; j<this.size; j++){
        Boolean[] sat = isSatisfiable(i,j);
        if (sat[0] && !sat[1]){
          board[i][j] = 1;
        }
        if (!sat[0] && sat[1]){
          board[i][j] = -1;
        }
      }
    }
  }



//Returns a boolean array of size 2 that tells whether a solution is
//possible when position (c,r) has a queen and when it does not have a queen
private Boolean[] isSatisfiable(int c, int r){
  Boolean[] ba = {!this.bdd.and(getVar(c,r)).isZero(), !this.bdd.and(getNotVar(c,r)).isZero()};
  return ba;
}

//Updates the global BDD when a queen is inserted
  private void updateBdd(int column, int row) {
    this.bdd = this.bdd.and(getVar(column, row));
  }


//Returns a BDD representing a queen at position column=c and row=r
  private BDD getVar(int c, int r){
    return fact.ithVar(c*this.size + r );
  }


//Returns a BDD representing not a queen at position column=c and row=r
  private BDD getNotVar(int c, int r){
    return fact.nithVar(c*this.size + r );
  }


//Initializes the global BDD with the rules of the game
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
        currentNode = getVar(i,j);

        //Column implication. Only 1 queen per column
        columnImplication = fact.one();
        for (int l = 0; l<N; l++){
          if (l!=j){
              columnImplication = columnImplication.and(getNotVar(i,l));
          }
        }

        //Row implication. Only 1 queen per row
        rowImplication = fact.one();
        for (int k = 0; k<N; k++){
          if (k!=i){
              rowImplication = rowImplication.and(getNotVar(k,j));
          }
        }

        //Diagonal1 implication. Only 1 queen per diagonal
        diagonal1Implication = fact.one();
        for (int k = 0; k<N; k++){
          if (0 <= j+k-i && j+k-i < N && k != i){
            diagonal1Implication = diagonal1Implication.and(getNotVar(k,j+k-i));
          }
        }

        //Diagonal2 implication. Only 1 queen per diagonal
        diagonal2Implication = fact.one();
        for (int k = 0; k<N; k++){
          if (0 <= j+i-k && j+i-k < N && k != i){
            diagonal2Implication = diagonal2Implication.and(getNotVar(k,j+i-k));
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
        columnRestriction = columnRestriction.or(getVar(i,j));
      }
      bdd = bdd.and(columnRestriction);
    }



    return bdd;
  }
}
