package cyr7.cli

import cyr7.typecheck.IxiFileOpener
import java.io.*
import java.nio.file.Paths
import java.util.jar.JarEntry
import java.util.jar.JarFile


class BuiltinLibraryLoader(val libRoot: File) {

    private val builtinLibraries: Map<String, JarEntry>
    private val jarFile: JarFile
    init {
        val src = this.javaClass.protectionDomain.codeSource
        jarFile = JarFile(src.location.path)

        val jarEntries = jarFile.entries()
        builtinLibraries = jarEntries.toList().mapNotNull { entry ->
            val name = entry.name
            if (name.matches(Regex("^builtin/.+"))) {
                Paths.get(name).fileName.toString() to entry
            } else {
                null
            }
        }.toMap()
    }

    fun getIxiFileOpener(): IxiFileOpener {
        return IxiFileOpener{ interfaceName -> this.getLibraryReader("$interfaceName.ixi")}
    }

    private fun getLibraryReader(filename: String): Reader {
        return builtinLibraries[filename].let { e ->
            if (e != null) {
                val inputStream = jarFile.getInputStream(e)
                InputStreamReader(inputStream)
            } else {
                val sourcePath = Paths.get(libRoot.absolutePath, filename)
                CLI.debugPrint("Opening reader to: $sourcePath")
                BufferedReader(FileReader(sourcePath.toFile()))
            }
        }
    }

}
