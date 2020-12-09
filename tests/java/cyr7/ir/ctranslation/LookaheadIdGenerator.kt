package cyr7.ir.ctranslation

import cyr7.ir.DefaultIdGenerator
import cyr7.ir.IdGenerator

class LookaheadIdGenerator : IdGenerator {
    private var labelCounter = 0
    private var tempCounter = 0
    override fun newLabel(): String {
        return label(labelCounter++)
    }

    override fun newTemp(): String {
        return temp(tempCounter++)
    }

    override fun newTemp(description: String): String {
        return String.format("_t_%s_%d", description, tempCounter++)
    }

    /**
     * Peek the next label with some offset.
     *
     * peekLabel(0) will return the next label. peekLabel(1) will return the
     * label after that, and so on.
     *
     * @param offset
     * @return
     */
    fun peekLabel(offset: Int): String {
        return label(labelCounter + offset)
    }

    /**
     * Peek the next temp with some offset.
     *
     * peekTemp(0) will return the next temp. peekTemp(1) will return the temp
     * after that, and so on.
     *
     * @param offset
     * @return
     */
    fun peekTemp(offset: Int): String {
        return temp(tempCounter + offset)
    }

    override fun retTemp(n: Int): String {
        return DefaultIdGenerator().retTemp(n)
    }

    override fun argTemp(n: Int): String {
        return DefaultIdGenerator().argTemp(n)
    }

    companion object {
        private fun label(num: Int): String {
            return String.format("_l%d", num)
        }

        private fun temp(num: Int): String {
            return String.format("_t%d", num)
        }
    }
}
