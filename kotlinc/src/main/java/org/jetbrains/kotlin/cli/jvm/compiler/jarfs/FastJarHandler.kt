/*
 * This file is part of Cosmic IDE.
 * Cosmic IDE is a free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Cosmic IDE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Cosmic IDE. If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * This file is part of Cosmic IDE.
 * Cosmic IDE is a free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Cosmic IDE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Cosmic IDE. If not, see <https://www.gnu.org/licenses/>.
 */
package org.jetbrains.kotlin.cli.jvm.compiler.jarfs

import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.io.FileNotFoundException
import java.io.RandomAccessFile
import java.nio.channels.FileChannel

class FastJarHandler(val fileSystem: FastJarFileSystem, path: String) {
    private val myRoot: VirtualFile?
    internal val file = File(path)

    private val cachedManifest: ByteArray?

    init {
        val entries: List<ZipEntryDescription>
        RandomAccessFile(file, "r").use { randomAccessFile ->
            val mappedByteBuffer = randomAccessFile.channel.map(
                FileChannel.MapMode.READ_ONLY,
                0,
                randomAccessFile.length()
            )
            try {
                entries = try {
                    mappedByteBuffer.parseCentralDirectory()
                } catch (e: Exception) {
                    throw IllegalStateException("Error while reading '${file.path}': $e", e)
                }
                cachedManifest =
                    entries.singleOrNull { StringUtil.equals(MANIFEST_PATH, it.relativePath) }
                        ?.let(mappedByteBuffer::contentsToByteArray)
            } finally {
                with(fileSystem) {
                    mappedByteBuffer.unmapBuffer()
                }
            }
        }

        myRoot = FastJarVirtualFile(this, "", -1, parent = null, entryDescription = null)

        // ByteArrayCharSequence should not be used instead of String
        // because the former class does not support equals/hashCode properly
        val filesByRelativePath = HashMap<String, FastJarVirtualFile>(entries.size)
        filesByRelativePath[""] = myRoot

        for (entryDescription in entries) {
            if (!entryDescription.isDirectory) {
                createFile(entryDescription, filesByRelativePath)
            } else {
                getOrCreateDirectory(entryDescription.relativePath, filesByRelativePath)
            }
        }

        for (node in filesByRelativePath.values) {
            node.initChildrenArrayFromList()
        }
    }

    private fun createFile(
        entry: ZipEntryDescription,
        directories: MutableMap<String, FastJarVirtualFile>
    ): FastJarVirtualFile {
        val (parentName, shortName) = entry.relativePath.splitPath()

        val parentFile = getOrCreateDirectory(parentName, directories)
        if ("." == shortName) {
            return parentFile
        }

        return FastJarVirtualFile(
            this, shortName,
            if (entry.isDirectory) -1 else entry.uncompressedSize,
            parentFile,
            entry,
        )
    }

    private fun getOrCreateDirectory(
        entryName: CharSequence,
        directories: MutableMap<String, FastJarVirtualFile>
    ): FastJarVirtualFile {
        return directories.getOrPut(entryName.toString()) {
            val (parentPath, shortName) = entryName.splitPath()
            val parentFile = getOrCreateDirectory(parentPath, directories)

            FastJarVirtualFile(this, shortName, -1, parentFile, entryDescription = null)
        }
    }

    private fun CharSequence.splitPath(): Pair<CharSequence, CharSequence> {
        var slashIndex = this.length - 1

        while (slashIndex >= 0 && this[slashIndex] != '/') {
            slashIndex--
        }

        if (slashIndex == -1) return Pair("", this)
        return Pair(subSequence(0, slashIndex), subSequence(slashIndex + 1, this.length))
    }

    fun findFileByPath(pathInJar: String): VirtualFile? {
        return myRoot?.findFileByRelativePath(pathInJar)
    }

    fun contentsToByteArray(zipEntryDescription: ZipEntryDescription): ByteArray {
        val relativePath = zipEntryDescription.relativePath
        if (StringUtil.equals(relativePath, MANIFEST_PATH)) return cachedManifest
            ?: throw FileNotFoundException("$file!/$relativePath")
        return fileSystem.cachedOpenFileHandles[file].use {
            synchronized(it) {
                it.get().second.contentsToByteArray(zipEntryDescription)
            }
        }
    }
}

private const val MANIFEST_PATH = "META-INF/MANIFEST.MF"