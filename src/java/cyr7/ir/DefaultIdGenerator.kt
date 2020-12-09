package cyr7.ir

import cyr7.ir.interpret.Configuration

class DefaultIdGenerator : IdGenerator {
    private var labelCounter = 0
    private var tempCounter = 0
    override fun newLabel(): String {
        return String.format("_l%d", labelCounter++)
    }

    override fun newTemp(): String {
        return String.format("_t%d", tempCounter++)
    }

    override fun newTemp(description: String): String {
        return String.format("_t_" + description + "_%d", tempCounter++)
    }

    override fun retTemp(n: Int): String {
        return Configuration.ABSTRACT_RET_PREFIX + n
    }

    override fun argTemp(n: Int): String {
        return Configuration.ABSTRACT_ARG_PREFIX + n
    }
}
