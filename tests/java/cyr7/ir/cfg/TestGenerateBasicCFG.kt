package cyr7.ir.cfg;

import java.util.HashMap;

import org.junit.jupiter.api.Test;

import cyr7.cfg.ir.constructor.CFGConstructor;
import cyr7.ir.nodes.IRCJump;
import cyr7.ir.nodes.IRCompUnit;
import cyr7.ir.nodes.IRInteger;
import cyr7.ir.nodes.IRFuncDecl;
import cyr7.ir.nodes.IRLabel;
import cyr7.ir.nodes.IRMem;
import cyr7.ir.nodes.IRMove;
import cyr7.ir.nodes.IRReturn;
import cyr7.ir.nodes.IRSeq;
import cyr7.ir.nodes.IRTemp;
import java_cup.runtime.ComplexSymbolFactory.Location;
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class TestGenerateBasicCFG {

    @Test
    fun testEmptyFunction() {
        val loc = Location(-1, -1)
        val func = IRFuncDecl(loc, "empty", IRSeq(loc, IRReturn(loc)))
        val map = HashMap<String, IRFuncDecl>()
        map["empty"] = func

        val comp = IRCompUnit(loc, "base", map)

        val result = CFGConstructor.constructCFG(comp)

        println(result)
    }

    @Test
    fun testAssignmentsFunction() {
        val loc = Location(-1, -1)
        val func = IRFuncDecl(loc, "assign", IRSeq(loc,
                IRMove(loc, IRTemp(loc, "target"), IRInteger(loc, 0)),
                IRMove(loc, IRMem(loc, IRInteger(loc, 0)), IRInteger(loc, 0)),
                IRReturn(loc)))

        val map = HashMap<String, IRFuncDecl>()
        map["assign"] = func
        val comp = IRCompUnit(loc, "base", map)
        val result = CFGConstructor.constructCFG(comp)
        println(result)
    }


    @Test
    fun testIfStmtFunction() {
        val loc = Location(-1, -1)
        val func = IRFuncDecl(loc, "if", IRSeq(loc,
                IRCJump(loc, IRInteger(loc, 0), "Hello_World"),
                IRMove(loc, IRTemp(loc, "target"), IRInteger(loc, 0)),
                IRLabel(loc, "Hello_World"),
                IRReturn(loc)))

        val map = HashMap<String, IRFuncDecl>()
        map["if"] = func

        val comp = IRCompUnit(loc, "base", map)

        val result = CFGConstructor.constructCFG(comp)
        println(result)
    }

}
