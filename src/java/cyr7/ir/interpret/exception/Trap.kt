package cyr7.ir.interpret.exception

import polyglot.util.SerialVersionUID

open class Trap(message: String?) : RuntimeException(message) {
    companion object {
        private val serialVersionUID = SerialVersionUID.generate()
    }
}
