package cyr7.ir.interpret.exception

import polyglot.util.SerialVersionUID

class OutOfBoundTrap(message: String?) : Trap(message) {
    companion object {
        private val serialVersionUID = SerialVersionUID.generate()
    }
}
