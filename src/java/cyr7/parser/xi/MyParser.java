
//----------------------------------------------------
// The following code was generated by CUP v0.11b 20150326
//----------------------------------------------------

package cyr7.parser.xi;

import cyr7.lexer.MyLexer;

/** CUP v0.11b 20150326 generated parser.
  */
public class MyParser
 extends java_cup.runtime.lr_parser {

  @Override
  public final Class<?> getSymbolContainer() {
    return sym.class;
  }

  /** Default constructor. */
  @Deprecated
  public MyParser() {super();}

  /** Constructor which sets the default scanner. */
  @Deprecated
  public MyParser(java_cup.runtime.Scanner s) {super(s);}

  /** Constructor which sets the default scanner and a SymbolFactory. */
  public MyParser(java_cup.runtime.Scanner s, java_cup.runtime.SymbolFactory sf) {super(s,sf);}

  /** Production table. */
  protected static final short _production_table[][] = 
    unpackFromStrings(new String[] {
    "\000\003\000\002\002\002\000\002\002\004\000\002\002" +
    "\003" });

  /** Access to production table. */
  @Override
  public short[][] production_table() {return _production_table;}

  /** Parse-action table. */
  protected static final short[][] _action_table = 
    unpackFromStrings(new String[] {
    "\000\004\000\006\002\001\053\004\001\002\000\004\002" +
    "\uffff\001\002\000\004\002\006\001\002\000\004\002\000" +
    "\001\002" });

  /** Access to parse-action table. */
  @Override
  public short[][] action_table() {return _action_table;}

  /** {@code reduce_goto} table. */
  protected static final short[][] _reduce_table = 
    unpackFromStrings(new String[] {
    "\000\004\000\004\002\004\001\001\000\002\001\001\000" +
    "\002\001\001\000\002\001\001" });

  /** Access to {@code reduce_goto} table. */
  @Override
  public short[][] reduce_table() {return _reduce_table;}

  /** Instance of action encapsulation class. */
  protected CUP$MyParser$actions action_obj;

  /** Action encapsulation object initializer. */
  @Override
  protected void init_actions()
    {
      action_obj = new CUP$MyParser$actions(this);
    }

  /** Invoke a user supplied parse action. */
  @Override
  public java_cup.runtime.Symbol do_action(
    int                        act_num,
    java_cup.runtime.lr_parser parser,
    java.util.Stack<java_cup.runtime.Symbol> stack,
    int                        top)
    throws java.lang.Exception
  {
    /* call code in generated class */
    return action_obj.CUP$MyParser$do_action(act_num, parser, stack, top);
  }

  /** Indicates start state. */
  @Override
  public int start_state() {return 0;}
  /** Indicates start production. */
  @Override
  public int start_production() {return 1;}

  /** {@code EOF} Symbol index. */
  @Override
  public int EOF_sym() {return 0;}

  /** {@code error} Symbol index. */
  @Override
  public int error_sym() {return 1;}


  /** User initialization code. */
  public void user_init() throws java.lang.Exception
    {


    }

  /** Scan to get the next Symbol. */
  @Override
  public java_cup.runtime.Symbol scan()
    throws java.lang.Exception
    {
 return lexer.next_token(); 
    }


    MyLexer lexer;


/** Cup generated class to encapsulate user supplied action code.*/
class CUP$MyParser$actions {
    private final MyParser parser;

    /** Constructor */
    CUP$MyParser$actions(MyParser parser) {
        this.parser = parser;
    }

    /** Method with the actual generated action code for actions 0 to 2. */
    public final java_cup.runtime.Symbol CUP$MyParser$do_action_part00000000(
            int                        CUP$MyParser$act_num,
            java_cup.runtime.lr_parser CUP$MyParser$parser,
            java.util.Stack<java_cup.runtime.Symbol> CUP$MyParser$stack,
            int                        CUP$MyParser$top)
            throws java.lang.Exception {
            /* Symbol object for return from actions */
            java_cup.runtime.Symbol CUP$MyParser$result;

        /* select the action based on the action number */
        switch (CUP$MyParser$act_num) {
        /*. . . . . . . . . . . . . . . . . . . .*/
        case 0: // program ::= 
            {
                Object RESULT = null;

                CUP$MyParser$result = parser.getSymbolFactory().newSymbol("program",0, CUP$MyParser$stack.peek(), RESULT);
            }
            return CUP$MyParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 1: // $START ::= program EOF 
            {
                Object RESULT = null;
                int start_valleft = CUP$MyParser$stack.elementAt(CUP$MyParser$top-1).left;
                int start_valright = CUP$MyParser$stack.elementAt(CUP$MyParser$top-1).right;
                Object start_val = CUP$MyParser$stack.elementAt(CUP$MyParser$top-1).<Object> value();
                RESULT = start_val;
                CUP$MyParser$result = parser.getSymbolFactory().newSymbol("$START",0, CUP$MyParser$stack.elementAt(CUP$MyParser$top-1), CUP$MyParser$stack.peek(), RESULT);
            }
            /* ACCEPT */
            CUP$MyParser$parser.done_parsing();
            return CUP$MyParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 2: // program ::= LOGICAL_OR 
            {
                Object RESULT = null;

                CUP$MyParser$result = parser.getSymbolFactory().newSymbol("program",0, CUP$MyParser$stack.peek(), CUP$MyParser$stack.peek(), RESULT);
            }
            return CUP$MyParser$result;

        /* . . . . . .*/
        default:
            throw new Exception(
                  "Invalid action number " + CUP$MyParser$act_num + " found in internal parse table");

        }
    } /* end of method */

    /** Method splitting the generated action code into several parts. */
    public final java_cup.runtime.Symbol CUP$MyParser$do_action(
            int                        CUP$MyParser$act_num,
            java_cup.runtime.lr_parser CUP$MyParser$parser,
            java.util.Stack<java_cup.runtime.Symbol> CUP$MyParser$stack,
            int                        CUP$MyParser$top)
            throws java.lang.Exception {
            return CUP$MyParser$do_action_part00000000(
                           CUP$MyParser$act_num,
                           CUP$MyParser$parser,
                           CUP$MyParser$stack,
                           CUP$MyParser$top);
    }
}

}
