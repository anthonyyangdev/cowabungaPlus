/* The following code was generated by JFlex 1.6.1 */

package cyr7.ir.parse;

import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.ComplexSymbolFactory.ComplexSymbol;
import java_cup.runtime.ComplexSymbolFactory.Location;
import java_cup.runtime.Symbol;

import java.math.BigInteger;

@SuppressWarnings({"unused", "fallthrough", "all"})

/**
 * This class is a scanner generated by 
 * <a href="http://www.jflex.de/">JFlex</a> 1.6.1
 * from the specification file <tt>ir.flex</tt>
 */
public class IRLexer implements java_cup.runtime.Scanner {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = { 
     0, 0
  };

  /** 
   * Translates characters to character classes
   */
  private static final String ZZ_CMAP_PACKED = 
    "\11\0\1\3\1\1\1\0\1\3\1\2\22\0\1\3\3\0\1\5"+
    "\2\0\1\4\1\40\1\41\1\4\2\0\1\10\1\4\1\0\1\7"+
    "\11\11\7\0\1\32\1\33\1\12\1\35\1\24\1\22\1\37\1\36"+
    "\1\20\1\30\1\5\1\31\1\14\1\17\1\13\1\15\1\27\1\34"+
    "\1\26\1\21\1\16\1\23\1\5\1\25\2\5\4\0\1\6\1\0"+
    "\32\5\47\0\4\5\4\0\1\5\12\0\1\5\4\0\1\5\5\0"+
    "\27\5\1\0\37\5\1\0\u01ca\5\4\0\14\5\16\0\5\5\7\0"+
    "\1\5\1\0\1\5\201\0\5\5\1\0\2\5\2\0\4\5\1\0"+
    "\1\5\6\0\1\5\1\0\3\5\1\0\1\5\1\0\24\5\1\0"+
    "\123\5\1\0\213\5\10\0\246\5\1\0\46\5\2\0\1\5\7\0"+
    "\47\5\7\0\1\5\100\0\33\5\5\0\3\5\30\0\1\5\24\0"+
    "\53\5\43\0\2\5\1\0\143\5\1\0\1\5\17\0\2\5\7\0"+
    "\2\5\12\0\3\5\2\0\1\5\20\0\1\5\1\0\36\5\35\0"+
    "\131\5\13\0\1\5\30\0\41\5\11\0\2\5\4\0\1\5\5\0"+
    "\26\5\4\0\1\5\11\0\1\5\3\0\1\5\27\0\31\5\107\0"+
    "\25\5\117\0\66\5\3\0\1\5\22\0\1\5\7\0\12\5\17\0"+
    "\20\5\4\0\10\5\2\0\2\5\2\0\26\5\1\0\7\5\1\0"+
    "\1\5\3\0\4\5\3\0\1\5\20\0\1\5\15\0\2\5\1\0"+
    "\3\5\16\0\4\5\7\0\1\5\11\0\6\5\4\0\2\5\2\0"+
    "\26\5\1\0\7\5\1\0\2\5\1\0\2\5\1\0\2\5\37\0"+
    "\4\5\1\0\1\5\23\0\3\5\20\0\11\5\1\0\3\5\1\0"+
    "\26\5\1\0\7\5\1\0\2\5\1\0\5\5\3\0\1\5\22\0"+
    "\1\5\17\0\2\5\17\0\1\5\7\0\1\5\13\0\10\5\2\0"+
    "\2\5\2\0\26\5\1\0\7\5\1\0\2\5\1\0\5\5\3\0"+
    "\1\5\36\0\2\5\1\0\3\5\17\0\1\5\21\0\1\5\1\0"+
    "\6\5\3\0\3\5\1\0\4\5\3\0\2\5\1\0\1\5\1\0"+
    "\2\5\3\0\2\5\3\0\3\5\3\0\14\5\26\0\1\5\50\0"+
    "\1\5\13\0\10\5\1\0\3\5\1\0\27\5\1\0\20\5\3\0"+
    "\1\5\32\0\3\5\5\0\2\5\43\0\10\5\1\0\3\5\1\0"+
    "\27\5\1\0\12\5\1\0\5\5\3\0\1\5\40\0\1\5\1\0"+
    "\2\5\17\0\2\5\22\0\10\5\1\0\3\5\1\0\51\5\2\0"+
    "\1\5\20\0\1\5\20\0\3\5\30\0\6\5\5\0\22\5\3\0"+
    "\30\5\1\0\11\5\1\0\1\5\2\0\7\5\72\0\60\5\1\0"+
    "\2\5\13\0\10\5\72\0\2\5\1\0\1\5\2\0\2\5\1\0"+
    "\1\5\2\0\1\5\6\0\4\5\1\0\7\5\1\0\3\5\1\0"+
    "\1\5\1\0\1\5\2\0\2\5\1\0\4\5\1\0\2\5\11\0"+
    "\1\5\2\0\5\5\1\0\1\5\25\0\4\5\40\0\1\5\77\0"+
    "\10\5\1\0\44\5\33\0\5\5\163\0\53\5\24\0\1\5\20\0"+
    "\6\5\4\0\4\5\3\0\1\5\3\0\2\5\7\0\3\5\4\0"+
    "\15\5\14\0\1\5\21\0\46\5\1\0\1\5\5\0\1\5\2\0"+
    "\53\5\1\0\u014d\5\1\0\4\5\2\0\7\5\1\0\1\5\1\0"+
    "\4\5\2\0\51\5\1\0\4\5\2\0\41\5\1\0\4\5\2\0"+
    "\7\5\1\0\1\5\1\0\4\5\2\0\17\5\1\0\71\5\1\0"+
    "\4\5\2\0\103\5\45\0\20\5\20\0\126\5\2\0\6\5\3\0"+
    "\u026c\5\2\0\21\5\1\0\32\5\5\0\113\5\3\0\13\5\7\0"+
    "\15\5\1\0\4\5\16\0\22\5\16\0\22\5\16\0\15\5\1\0"+
    "\3\5\17\0\64\5\43\0\1\5\3\0\2\5\103\0\130\5\10\0"+
    "\51\5\1\0\1\5\5\0\106\5\12\0\37\5\61\0\36\5\2\0"+
    "\5\5\13\0\54\5\4\0\32\5\66\0\27\5\11\0\65\5\122\0"+
    "\1\5\135\0\57\5\21\0\7\5\67\0\36\5\15\0\2\5\12\0"+
    "\54\5\32\0\44\5\51\0\3\5\12\0\44\5\153\0\4\5\1\0"+
    "\4\5\3\0\2\5\11\0\300\5\100\0\u0116\5\2\0\6\5\2\0"+
    "\46\5\2\0\6\5\2\0\10\5\1\0\1\5\1\0\1\5\1\0"+
    "\1\5\1\0\37\5\2\0\65\5\1\0\7\5\1\0\1\5\3\0"+
    "\3\5\1\0\7\5\3\0\4\5\2\0\6\5\4\0\15\5\5\0"+
    "\3\5\1\0\7\5\102\0\2\5\23\0\1\5\34\0\1\5\15\0"+
    "\1\5\20\0\15\5\3\0\37\5\103\0\1\5\4\0\1\5\2\0"+
    "\12\5\1\0\1\5\3\0\5\5\6\0\1\5\1\0\1\5\1\0"+
    "\1\5\1\0\4\5\1\0\13\5\2\0\4\5\5\0\5\5\4\0"+
    "\1\5\21\0\51\5\u0a77\0\57\5\1\0\57\5\1\0\205\5\6\0"+
    "\4\5\3\0\2\5\14\0\46\5\1\0\1\5\5\0\1\5\2\0"+
    "\70\5\7\0\1\5\20\0\27\5\11\0\7\5\1\0\7\5\1\0"+
    "\7\5\1\0\7\5\1\0\7\5\1\0\7\5\1\0\7\5\1\0"+
    "\7\5\120\0\1\5\u01d5\0\3\5\31\0\11\5\7\0\5\5\2\0"+
    "\5\5\4\0\126\5\6\0\3\5\1\0\132\5\1\0\4\5\5\0"+
    "\51\5\3\0\136\5\21\0\33\5\65\0\20\5\u0200\0\u19b6\5\112\0"+
    "\u51d6\5\52\0\u048d\5\103\0\56\5\2\0\u010d\5\3\0\20\5\12\0"+
    "\2\5\24\0\57\5\20\0\37\5\2\0\120\5\47\0\11\5\2\0"+
    "\147\5\2\0\43\5\2\0\10\5\77\0\13\5\1\0\3\5\1\0"+
    "\4\5\1\0\27\5\25\0\1\5\7\0\64\5\16\0\62\5\76\0"+
    "\6\5\3\0\1\5\1\0\1\5\14\0\34\5\12\0\27\5\31\0"+
    "\35\5\7\0\57\5\34\0\1\5\20\0\5\5\1\0\12\5\12\0"+
    "\5\5\1\0\51\5\27\0\3\5\1\0\10\5\24\0\27\5\3\0"+
    "\1\5\3\0\62\5\1\0\1\5\3\0\2\5\2\0\5\5\2\0"+
    "\1\5\1\0\1\5\30\0\3\5\2\0\13\5\7\0\3\5\14\0"+
    "\6\5\2\0\6\5\2\0\6\5\11\0\7\5\1\0\7\5\1\0"+
    "\53\5\1\0\12\5\12\0\163\5\35\0\u2ba4\5\14\0\27\5\4\0"+
    "\61\5\u2104\0\u016e\5\2\0\152\5\46\0\7\5\14\0\5\5\5\0"+
    "\1\5\1\0\12\5\1\0\15\5\1\0\5\5\1\0\1\5\1\0"+
    "\2\5\1\0\2\5\1\0\154\5\41\0\u016b\5\22\0\100\5\2\0"+
    "\66\5\50\0\15\5\66\0\2\5\30\0\3\5\31\0\1\5\6\0"+
    "\5\5\1\0\207\5\7\0\1\5\34\0\32\5\4\0\1\5\1\0"+
    "\32\5\13\0\131\5\3\0\6\5\2\0\6\5\2\0\6\5\2\0"+
    "\3\5\3\0\2\5\3\0\2\5\31\0\14\5\1\0\32\5\1\0"+
    "\23\5\1\0\2\5\1\0\17\5\2\0\16\5\42\0\173\5\105\0"+
    "\65\5\u010b\0\35\5\3\0\61\5\57\0\40\5\20\0\33\5\5\0"+
    "\46\5\12\0\36\5\2\0\44\5\4\0\10\5\1\0\5\5\52\0"+
    "\236\5\142\0\50\5\10\0\64\5\234\0\u0137\5\11\0\26\5\12\0"+
    "\10\5\230\0\6\5\2\0\1\5\1\0\54\5\1\0\2\5\3\0"+
    "\1\5\2\0\27\5\12\0\27\5\11\0\37\5\101\0\23\5\1\0"+
    "\2\5\12\0\26\5\12\0\32\5\106\0\70\5\6\0\2\5\100\0"+
    "\1\5\17\0\4\5\1\0\3\5\1\0\33\5\54\0\35\5\3\0"+
    "\35\5\43\0\10\5\1\0\34\5\33\0\66\5\12\0\26\5\12\0"+
    "\23\5\15\0\22\5\156\0\111\5\67\0\63\5\15\0\63\5\u0310\0"+
    "\65\5\113\0\55\5\40\0\31\5\32\0\44\5\51\0\43\5\3\0"+
    "\1\5\14\0\60\5\16\0\4\5\25\0\1\5\1\0\1\5\43\0"+
    "\22\5\1\0\31\5\124\0\7\5\1\0\1\5\1\0\4\5\1\0"+
    "\17\5\1\0\12\5\7\0\57\5\46\0\10\5\2\0\2\5\2\0"+
    "\26\5\1\0\7\5\1\0\2\5\1\0\5\5\3\0\1\5\22\0"+
    "\1\5\14\0\5\5\u011e\0\60\5\24\0\2\5\1\0\1\5\270\0"+
    "\57\5\51\0\4\5\44\0\60\5\24\0\1\5\73\0\53\5\125\0"+
    "\32\5\u0186\0\100\5\37\0\1\5\u01c0\0\71\5\u0507\0\u039a\5\146\0"+
    "\157\5\21\0\304\5\u0abc\0\u042f\5\u0fd1\0\u0247\5\u21b9\0\u0239\5\7\0"+
    "\37\5\161\0\36\5\22\0\60\5\20\0\4\5\37\0\25\5\5\0"+
    "\23\5\u0370\0\105\5\13\0\1\5\102\0\15\5\u4060\0\2\5\u0bfe\0"+
    "\153\5\5\0\15\5\3\0\11\5\7\0\12\5\u1766\0\125\5\1\0"+
    "\107\5\1\0\2\5\2\0\1\5\2\0\2\5\2\0\4\5\1\0"+
    "\14\5\1\0\1\5\1\0\7\5\1\0\101\5\1\0\4\5\2\0"+
    "\10\5\1\0\7\5\1\0\34\5\1\0\4\5\1\0\5\5\1\0"+
    "\1\5\3\0\7\5\1\0\u0154\5\2\0\31\5\1\0\31\5\1\0"+
    "\37\5\1\0\31\5\1\0\37\5\1\0\31\5\1\0\37\5\1\0"+
    "\31\5\1\0\37\5\1\0\31\5\1\0\10\5\u1034\0\305\5\u053b\0"+
    "\4\5\1\0\33\5\1\0\2\5\1\0\1\5\2\0\1\5\1\0"+
    "\12\5\1\0\4\5\1\0\1\5\1\0\1\5\6\0\1\5\4\0"+
    "\1\5\1\0\1\5\1\0\1\5\1\0\3\5\1\0\2\5\1\0"+
    "\1\5\2\0\1\5\1\0\1\5\1\0\1\5\1\0\1\5\1\0"+
    "\1\5\1\0\2\5\1\0\1\5\2\0\4\5\1\0\7\5\1\0"+
    "\4\5\1\0\4\5\1\0\1\5\1\0\12\5\1\0\21\5\5\0"+
    "\3\5\1\0\5\5\1\0\21\5\u1144\0\ua6d7\5\51\0\u1035\5\13\0"+
    "\336\5\2\0\u1682\5\u295e\0\u021e\5\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\u05f0\0";

  /** 
   * Translates characters to character classes
   */
  private static final char [] ZZ_CMAP = zzUnpackCMap(ZZ_CMAP_PACKED);

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\1\0\1\1\2\2\1\1\1\3\1\4\1\1\1\4"+
    "\20\3\1\5\1\6\1\0\3\3\1\7\11\3\1\10"+
    "\4\3\1\11\12\3\1\12\6\3\1\13\1\14\1\15"+
    "\1\16\3\3\1\17\1\3\1\20\1\21\1\22\1\3"+
    "\1\23\2\3\1\24\1\3\1\25\2\3\1\26\1\3"+
    "\1\27\3\3\1\30\1\31\1\32\1\33\1\34\1\35"+
    "\1\36\5\3\1\37\1\3\1\40\1\41\2\3\1\42"+
    "\5\3\1\43\1\3\1\44\1\45\2\3\1\46\1\47"+
    "\1\3\1\50";

  private static int [] zzUnpackAction() {
    int [] result = new int[125];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /** 
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\42\0\42\0\104\0\146\0\210\0\146\0\252"+
    "\0\314\0\356\0\u0110\0\u0132\0\u0154\0\u0176\0\u0198\0\u01ba"+
    "\0\u01dc\0\u01fe\0\u0220\0\u0242\0\u0264\0\u0286\0\u02a8\0\u02ca"+
    "\0\u02ec\0\42\0\42\0\146\0\u030e\0\u0330\0\u0352\0\210"+
    "\0\u0374\0\u0396\0\u03b8\0\u03da\0\u03fc\0\u041e\0\u0440\0\u0462"+
    "\0\u0484\0\210\0\u04a6\0\u04c8\0\u04ea\0\u050c\0\210\0\u052e"+
    "\0\u0550\0\u0572\0\u0594\0\u05b6\0\u05d8\0\u05fa\0\u061c\0\u063e"+
    "\0\u0660\0\210\0\u0682\0\u06a4\0\u06c6\0\u06e8\0\u070a\0\u072c"+
    "\0\210\0\210\0\210\0\210\0\u074e\0\u0770\0\u0792\0\210"+
    "\0\u07b4\0\210\0\210\0\210\0\u07d6\0\210\0\u07f8\0\u081a"+
    "\0\210\0\u083c\0\210\0\u085e\0\u0880\0\210\0\u08a2\0\210"+
    "\0\u08c4\0\u08e6\0\u0908\0\u092a\0\210\0\210\0\210\0\210"+
    "\0\210\0\210\0\u094c\0\u096e\0\u0990\0\u09b2\0\u09d4\0\210"+
    "\0\u09f6\0\210\0\210\0\u0a18\0\u0a3a\0\210\0\u0a5c\0\u0a7e"+
    "\0\u0aa0\0\u0ac2\0\u0ae4\0\210\0\u0b06\0\210\0\210\0\u0b28"+
    "\0\u0b4a\0\210\0\210\0\u0b6c\0\210";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[125];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /** 
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpackTrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\2\1\3\1\4\1\3\1\5\2\6\1\7\1\10"+
    "\1\11\1\12\1\13\1\14\2\6\1\15\1\6\1\16"+
    "\1\17\1\6\1\20\1\21\1\22\1\6\1\23\1\24"+
    "\1\25\1\6\1\26\1\27\1\30\1\31\1\32\1\33"+
    "\43\0\1\3\44\0\1\34\2\6\3\34\26\6\6\0"+
    "\34\6\6\0\1\34\2\6\2\34\1\11\26\6\6\0"+
    "\1\34\2\6\1\11\1\34\1\11\26\6\6\0\7\6"+
    "\1\35\14\6\1\36\1\6\1\37\5\6\6\0\30\6"+
    "\1\40\3\6\6\0\7\6\1\41\2\6\1\42\5\6"+
    "\1\43\13\6\6\0\20\6\1\44\5\6\1\45\5\6"+
    "\6\0\20\6\1\46\13\6\6\0\12\6\1\47\21\6"+
    "\6\0\21\6\1\50\1\51\1\52\10\6\6\0\7\6"+
    "\1\53\24\6\6\0\12\6\1\54\5\6\1\55\13\6"+
    "\6\0\12\6\1\56\21\6\6\0\15\6\1\57\2\6"+
    "\1\60\1\6\1\61\3\6\1\62\5\6\6\0\13\6"+
    "\1\63\14\6\1\64\1\65\2\6\6\0\20\6\1\66"+
    "\1\6\1\67\11\6\6\0\14\6\1\70\17\6\6\0"+
    "\10\6\1\71\23\6\6\0\15\6\1\72\2\6\1\73"+
    "\13\6\6\0\10\6\1\74\2\6\1\75\20\6\6\0"+
    "\12\6\1\76\21\6\6\0\25\6\1\77\6\6\6\0"+
    "\17\6\1\100\11\6\1\101\2\6\6\0\25\6\1\102"+
    "\6\6\6\0\10\6\1\103\23\6\6\0\23\6\1\104"+
    "\10\6\6\0\10\6\1\105\23\6\6\0\10\6\1\106"+
    "\23\6\6\0\13\6\1\107\20\6\6\0\11\6\1\110"+
    "\22\6\6\0\20\6\1\111\13\6\6\0\30\6\1\112"+
    "\3\6\6\0\27\6\1\113\4\6\6\0\23\6\1\114"+
    "\10\6\6\0\10\6\1\115\23\6\6\0\23\6\1\116"+
    "\10\6\6\0\32\6\1\117\1\6\6\0\27\6\1\120"+
    "\4\6\6\0\31\6\1\121\2\6\6\0\22\6\1\122"+
    "\11\6\6\0\31\6\1\123\2\6\6\0\15\6\1\124"+
    "\16\6\6\0\32\6\1\125\1\6\6\0\17\6\1\126"+
    "\14\6\6\0\12\6\1\127\21\6\6\0\23\6\1\130"+
    "\10\6\6\0\11\6\1\131\22\6\6\0\22\6\1\132"+
    "\11\6\6\0\10\6\1\133\23\6\6\0\25\6\1\134"+
    "\6\6\6\0\20\6\1\135\13\6\6\0\20\6\1\136"+
    "\13\6\6\0\11\6\1\137\22\6\6\0\6\6\1\140"+
    "\25\6\6\0\23\6\1\141\10\6\6\0\11\6\1\142"+
    "\22\6\6\0\14\6\1\143\17\6\6\0\20\6\1\144"+
    "\13\6\6\0\32\6\1\145\1\6\6\0\12\6\1\146"+
    "\21\6\6\0\14\6\1\147\17\6\6\0\25\6\1\150"+
    "\6\6\6\0\12\6\1\151\21\6\6\0\15\6\1\152"+
    "\16\6\6\0\11\6\1\153\22\6\6\0\2\6\1\154"+
    "\31\6\6\0\16\6\1\155\15\6\6\0\25\6\1\156"+
    "\6\6\6\0\14\6\1\157\17\6\6\0\30\6\1\160"+
    "\3\6\6\0\16\6\1\161\15\6\6\0\13\6\1\162"+
    "\20\6\6\0\22\6\1\163\11\6\6\0\15\6\1\164"+
    "\16\6\6\0\16\6\1\165\15\6\6\0\13\6\1\166"+
    "\20\6\6\0\15\6\1\167\16\6\6\0\14\6\1\170"+
    "\17\6\6\0\15\6\1\171\16\6\6\0\15\6\1\172"+
    "\16\6\6\0\15\6\1\173\16\6\6\0\10\6\1\174"+
    "\23\6\6\0\15\6\1\175\16\6\2\0";

  private static int [] zzUnpackTrans() {
    int [] result = new int[2958];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /* error messages for the codes above */
  private static final String ZZ_ERROR_MSG[] = {
    "Unknown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\1\0\2\11\26\1\2\11\1\0\141\1";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[125];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** the input device */
  private java.io.Reader zzReader;

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private char zzBuffer[] = new char[ZZ_BUFFERSIZE];

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /** number of newlines encountered up to the start of the matched text */
  private int yyline;

  /** the number of characters up to the start of the matched text */
  private int yychar;

  /**
   * the number of characters from the last newline up to the start of the 
   * matched text
   */
  private int yycolumn;

  /** 
   * zzAtBOL == true <=> the scanner is currently at the beginning of a line
   */
  private boolean zzAtBOL = true;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /** denotes if the user-EOF-code has already been executed */
  private boolean zzEOFDone;
  
  /** 
   * The number of occupied positions in zzBuffer beyond zzEndRead.
   * When a lead/high surrogate has been read from the input stream
   * into the final zzBuffer position, this will have a value of 1;
   * otherwise, it will have a value of 0.
   */
  private int zzFinalHighSurrogate = 0;

  /* user code: */
    private static ComplexSymbolFactory csf = new ComplexSymbolFactory();

    private Symbol sym(String name, int id) {
        return csf.newSymbol(name, id, beginPos(), endPos());
    }

    private Symbol name(String s) {
        return new Name(s, beginPos(), endPos());
    }

    private Symbol number(String s) {
        BigInteger x = new BigInteger(s);
        if (x.bitLength() > 64) {
            return lexError("Number literal \"" +
                        yytext() + "\" out of range.");
        }
        return new Number(x.longValue(), beginPos(), endPos());
    }

    private Symbol lexError(String msg) {
        System.err.println(msg);
        return new LexErrorToken(beginPos(), endPos());
    }

    private Position beginPos() {
        return new Position(yyline+1, yycolumn+1);
    }

    private Position endPos() {
        int len = yytext().length();
        return new Position(yyline+1, yycolumn+1+len);
    }

private static class Position extends Location {
    public Position(int line, int column) {
        super(line, column);
    }

    @Override
    public String toString() {
        return getLine() + ":" + getColumn();
    }
}

static class Name extends ComplexSymbol {
    public Name(String name, Position left, Position right) {
        super("NAME", IRSym.ATOM, left, right, name);
    }
}

static class Number extends ComplexSymbol {
    public Number(long val, Position left, Position right) {
        super("NUMBER", IRSym.NUMBER, left, right, val);
    }
}

static class LexErrorToken extends ComplexSymbol {
    public LexErrorToken(Position left, Position right) {
        super("error", IRSym.error, left, right);
    }

    @Override
    public String toString() {
        return "lexical error";
    }
}


  /**
   * Creates a new scanner
   *
   * @param   in  the java.io.Reader to read input from.
   */
  public IRLexer(java.io.Reader in) {
    this.zzReader = in;
  }


  /** 
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return         the unpacked character translation table
   */
  private static char [] zzUnpackCMap(String packed) {
    char [] map = new char[0x110000];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    while (i < 2420) {
      int  count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value; while (--count > 0);
    }
    return map;
  }


  /**
   * Refills the input buffer.
   *
   * @return      <code>false</code>, iff there was new input.
   * 
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {

    /* first: make room (if you can) */
    if (zzStartRead > 0) {
      zzEndRead += zzFinalHighSurrogate;
      zzFinalHighSurrogate = 0;
      System.arraycopy(zzBuffer, zzStartRead,
                       zzBuffer, 0,
                       zzEndRead-zzStartRead);

      /* translate stored positions */
      zzEndRead-= zzStartRead;
      zzCurrentPos-= zzStartRead;
      zzMarkedPos-= zzStartRead;
      zzStartRead = 0;
    }

    /* is the buffer big enough? */
    if (zzCurrentPos >= zzBuffer.length - zzFinalHighSurrogate) {
      /* if not: blow it up */
      char newBuffer[] = new char[zzBuffer.length*2];
      System.arraycopy(zzBuffer, 0, newBuffer, 0, zzBuffer.length);
      zzBuffer = newBuffer;
      zzEndRead += zzFinalHighSurrogate;
      zzFinalHighSurrogate = 0;
    }

    /* fill the buffer with new input */
    int requested = zzBuffer.length - zzEndRead;
    int numRead = zzReader.read(zzBuffer, zzEndRead, requested);

    /* not supposed to occur according to specification of java.io.Reader */
    if (numRead == 0) {
      throw new java.io.IOException("Reader returned 0 characters. See JFlex examples for workaround.");
    }
    if (numRead > 0) {
      zzEndRead += numRead;
      /* If numRead == requested, we might have requested to few chars to
         encode a full Unicode character. We assume that a Reader would
         otherwise never return half characters. */
      if (numRead == requested) {
        if (Character.isHighSurrogate(zzBuffer[zzEndRead - 1])) {
          --zzEndRead;
          zzFinalHighSurrogate = 1;
        }
      }
      /* potentially more input available */
      return false;
    }

    /* numRead < 0 ==> end of stream */
    return true;
  }

    
  /**
   * Closes the input stream.
   */
  public final void yyclose() throws java.io.IOException {
    zzAtEOF = true;            /* indicate end of file */
    zzEndRead = zzStartRead;  /* invalidate buffer    */

    if (zzReader != null)
      zzReader.close();
  }


  /**
   * Resets the scanner to read from a new input stream.
   * Does not close the old reader.
   *
   * All internal variables are reset, the old input stream 
   * <b>cannot</b> be reused (internal buffer is discarded and lost).
   * Lexical state is set to <tt>ZZ_INITIAL</tt>.
   *
   * Internal scan buffer is resized down to its initial length, if it has grown.
   *
   * @param reader   the new input stream 
   */
  public final void yyreset(java.io.Reader reader) {
    zzReader = reader;
    zzAtBOL  = true;
    zzAtEOF  = false;
    zzEOFDone = false;
    zzEndRead = zzStartRead = 0;
    zzCurrentPos = zzMarkedPos = 0;
    zzFinalHighSurrogate = 0;
    yyline = yychar = yycolumn = 0;
    zzLexicalState = YYINITIAL;
    if (zzBuffer.length > ZZ_BUFFERSIZE)
      zzBuffer = new char[ZZ_BUFFERSIZE];
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final String yytext() {
    return new String( zzBuffer, zzStartRead, zzMarkedPos-zzStartRead );
  }


  /**
   * Returns the character at position <tt>pos</tt> from the 
   * matched text. 
   * 
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch. 
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBuffer[zzStartRead+pos];
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occured while scanning.
   *
   * In a wellformed scanner (no or only correct usage of 
   * yypushback(int) and a match-all fallback rule) this method 
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  } 


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Contains user EOF-code, which will be executed exactly once,
   * when the end of file is reached
   */
  private void zzDoEOF() throws java.io.IOException {
    if (!zzEOFDone) {
      zzEOFDone = true;
      yyclose();
    }
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public Symbol next_token() throws java.io.IOException {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    char [] zzBufferL = zzBuffer;
    char [] zzCMapL = ZZ_CMAP;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      boolean zzR = false;
      int zzCh;
      int zzCharCount;
      for (zzCurrentPosL = zzStartRead  ;
           zzCurrentPosL < zzMarkedPosL ;
           zzCurrentPosL += zzCharCount ) {
        zzCh = Character.codePointAt(zzBufferL, zzCurrentPosL, zzMarkedPosL);
        zzCharCount = Character.charCount(zzCh);
        switch (zzCh) {
        case '\u000B':
        case '\u000C':
        case '\u0085':
        case '\u2028':
        case '\u2029':
          yyline++;
          yycolumn = 0;
          zzR = false;
          break;
        case '\r':
          yyline++;
          yycolumn = 0;
          zzR = true;
          break;
        case '\n':
          if (zzR)
            zzR = false;
          else {
            yyline++;
            yycolumn = 0;
          }
          break;
        default:
          zzR = false;
          yycolumn += zzCharCount;
        }
      }

      if (zzR) {
        // peek one character ahead if it is \n (if we have counted one line too much)
        boolean zzPeek;
        if (zzMarkedPosL < zzEndReadL)
          zzPeek = zzBufferL[zzMarkedPosL] == '\n';
        else if (zzAtEOF)
          zzPeek = false;
        else {
          boolean eof = zzRefill();
          zzEndReadL = zzEndRead;
          zzMarkedPosL = zzMarkedPos;
          zzBufferL = zzBuffer;
          if (eof) 
            zzPeek = false;
          else 
            zzPeek = zzBufferL[zzMarkedPosL] == '\n';
        }
        if (zzPeek) yyline--;
      }
      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;
  
      zzState = ZZ_LEXSTATE[zzLexicalState];

      // set up zzAction for empty match case:
      int zzAttributes = zzAttrL[zzState];
      if ( (zzAttributes & 1) == 1 ) {
        zzAction = zzState;
      }


      zzForAction: {
        while (true) {
    
          if (zzCurrentPosL < zzEndReadL) {
            zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL, zzEndReadL);
            zzCurrentPosL += Character.charCount(zzInput);
          }
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL, zzEndReadL);
              zzCurrentPosL += Character.charCount(zzInput);
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + zzCMapL[zzInput] ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
        zzAtEOF = true;
            zzDoEOF();
          {     return sym("EOF", IRSym.EOF);
 }
      }
      else {
        switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
          case 1: 
            { return lexError(beginPos() + ": Illegal character \"" +
                                 yytext() + "\"");
            }
          case 41: break;
          case 2: 
            { /* ignore */
            }
          case 42: break;
          case 3: 
            { return name(yytext());
            }
          case 43: break;
          case 4: 
            { return number(yytext());
            }
          case 44: break;
          case 5: 
            { return sym("(", IRSym.LPAREN);
            }
          case 45: break;
          case 6: 
            { return sym(")", IRSym.RPAREN);
            }
          case 46: break;
          case 7: 
            { return sym("OR", IRSym.OR);
            }
          case 47: break;
          case 8: 
            { return sym("EQ", IRSym.EQ);
            }
          case 48: break;
          case 9: 
            { return sym("LT", IRSym.LT);
            }
          case 49: break;
          case 10: 
            { return sym("GT", IRSym.GT);
            }
          case 50: break;
          case 11: 
            { return sym("MOD", IRSym.MOD);
            }
          case 51: break;
          case 12: 
            { return sym("MUL", IRSym.MUL);
            }
          case 52: break;
          case 13: 
            { return sym("MEM", IRSym.MEM);
            }
          case 53: break;
          case 14: 
            { return sym("NEQ", IRSym.NEQ);
            }
          case 54: break;
          case 15: 
            { return sym("EXP", IRSym.EXP);
            }
          case 55: break;
          case 16: 
            { return sym("XOR", IRSym.XOR);
            }
          case 56: break;
          case 17: 
            { return sym("SUB", IRSym.SUB);
            }
          case 57: break;
          case 18: 
            { return sym("SEQ", IRSym.SEQ);
            }
          case 58: break;
          case 19: 
            { return sym("LEQ", IRSym.LEQ);
            }
          case 59: break;
          case 20: 
            { return sym("AND", IRSym.AND);
            }
          case 60: break;
          case 21: 
            { return sym("ADD", IRSym.ADD);
            }
          case 61: break;
          case 22: 
            { return sym("DIV", IRSym.DIV);
            }
          case 62: break;
          case 23: 
            { return sym("GEQ", IRSym.GEQ);
            }
          case 63: break;
          case 24: 
            { return sym("CALL", IRSym.CALL);
            }
          case 64: break;
          case 25: 
            { return sym("MOVE", IRSym.MOVE);
            }
          case 65: break;
          case 26: 
            { return sym("NAME", IRSym.NAME);
            }
          case 66: break;
          case 27: 
            { return sym("TEMP", IRSym.TEMP);
            }
          case 67: break;
          case 28: 
            { return sym("FUNC", IRSym.FUNC);
            }
          case 68: break;
          case 29: 
            { return sym("ESEQ", IRSym.ESEQ);
            }
          case 69: break;
          case 30: 
            { return sym("JUMP", IRSym.JUMP);
            }
          case 70: break;
          case 31: 
            { return sym("HMUL", IRSym.HMUL);
            }
          case 71: break;
          case 32: 
            { return sym("CONST", IRSym.CONST);
            }
          case 72: break;
          case 33: 
            { return sym("CJUMP", IRSym.CJUMP);
            }
          case 73: break;
          case 34: 
            { return sym("LABEL", IRSym.LABEL);
            }
          case 74: break;
          case 35: 
            { return sym("LSHIFT", IRSym.LSHIFT);
            }
          case 75: break;
          case 36: 
            { return sym("RETURN", IRSym.RETURN);
            }
          case 76: break;
          case 37: 
            { return sym("RSHIFT", IRSym.RSHIFT);
            }
          case 77: break;
          case 38: 
            { return sym("ARSHIFT", IRSym.ARSHIFT);
            }
          case 78: break;
          case 39: 
            { return sym("COMPUNIT", IRSym.COMPUNIT);
            }
          case 79: break;
          case 40: 
            { return sym("CALL_STMT", IRSym.CALL_STMT);
            }
          case 80: break;
          default:
            zzScanError(ZZ_NO_MATCH);
        }
      }
    }
  }


}
