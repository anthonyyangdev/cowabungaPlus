package cyr7.cli

import cyr7.typecheck.IxiFileOpener
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.Reader
import java.nio.file.Paths

class BuiltinLibraryLoader(val libRoot: File) {

    fun getIxiFileOpener(): IxiFileOpener {
        return IxiFileOpener{interfaceName -> this.getLibraryReader(interfaceName)}
    }

    private fun getLibraryReader(filename: String): Reader {
        val sourcePath = Paths.get(libRoot.absolutePath, filename)
        CLI.debugPrint("Opening reader to: $sourcePath")
        return BufferedReader(FileReader(sourcePath.toFile()))
    }

}
