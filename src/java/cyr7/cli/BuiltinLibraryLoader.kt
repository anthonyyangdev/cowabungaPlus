package cyr7.cli

import cyr7.typecheck.IxiFileOpener
import java.io.*
import java.lang.RuntimeException
import java.nio.file.Path
import java.nio.file.Paths
import java.util.jar.JarEntry
import java.util.jar.JarFile

class BuiltinLibraryLoader(val libRoot: File) {

    sealed class Library {
        abstract fun get(filename: String): Reader?
        class JarLibrary(private val jarFile: JarFile, private val libraries: Map<String, JarEntry>): Library() {
            override fun get(filename: String): Reader? {
                return libraries[filename]?.let { e ->
                    val inputStream = jarFile.getInputStream(e)
                    InputStreamReader(inputStream)
                }
            }
        }
        class FileLibrary(private val libraries: Map<String, File>): Library() {
            override fun get(filename: String): Reader? {
                return libraries[filename]?.let { f ->
                        val inputStream = FileInputStream(f)
                        InputStreamReader(inputStream)
                }
            }
        }
    }

    private val library: Library
    init {
        val cl = this.javaClass.classLoader
        val libraryPath = cl.getResource("builtin")
                ?: throw RuntimeException("Cannot find builtin resource directory")
        when {
            libraryPath.toString().startsWith("jar") -> {
                val codeSource = this.javaClass.protectionDomain.codeSource
                val jarFile = JarFile(codeSource.location.path)
                val jarEntries = jarFile.entries()
                val builtinLibrariesJars = jarEntries.toList().mapNotNull { entry ->
                    val name = entry.name
                    if (name.matches(Regex("^builtin/.+"))) {
                        Paths.get(name).fileName.toString() to entry
                    } else {
                        null
                    }
                }.toMap()
                library = Library.JarLibrary(jarFile, builtinLibrariesJars)
            }
            libraryPath.toString().startsWith("file") -> {
                val builtinLibrariesPath = File(libraryPath.toURI()).listFiles()?.mapNotNull {
                    Path.of(it.toURI()).fileName.toString() to it
                }?.toMap() ?: throw RuntimeException("Cannot find resource")
                library = Library.FileLibrary(builtinLibrariesPath)
            }
            else -> {
                throw RuntimeException("Cannot find builtin resource directory")
            }
        }
    }
    fun getIxiFileOpener(): IxiFileOpener {
        return IxiFileOpener{ interfaceName -> this.getLibraryReader("$interfaceName.ixi")}
    }
    private fun getLibraryReader(filename: String): Reader {
        return this.library.get(filename) ?: getLibraryFromUserSpace(filename)
    }
    private fun getLibraryFromUserSpace(filename: String): Reader {
        val sourcePath = Paths.get(libRoot.absolutePath, filename)
        CLI.debugPrint("Opening reader to: $sourcePath")
        return BufferedReader(FileReader(sourcePath.toFile()))
    }

}
