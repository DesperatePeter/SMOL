package util

import net.sf.sevenzipjbinding.*
import org.tinylog.Logger
import java.util.*


class ArchiveExtractToMemoryCallback(
    private val itemsToExtract: IntArray,
    private val inArchive: IInArchive,
    private val onComplete: (Map<Int, String>) -> Unit
) : IArchiveExtractCallback {
    private val startTime = System.currentTimeMillis()
    private var hash = 0
    private var size = 0
    private var index = 0
    private var skipExtraction = false
    private var bytes = byteArrayOf()
    private var total = -1L
    private val results = mutableMapOf<Int, String>()

    override fun getStream(
        index: Int,
        extractAskMode: ExtractAskMode?
    ): ISequentialOutStream? {
        this.index = index
        val isFolder = inArchive
            .getProperty(index, PropID.IS_FOLDER) as Boolean
        skipExtraction = isFolder || index !in itemsToExtract

        if (skipExtraction || extractAskMode !== ExtractAskMode.EXTRACT) {
            return null
        }

        return ISequentialOutStream { data ->
            hash = hash xor Arrays.hashCode(data)
            size += data.size
            bytes += data
            data.size // Return amount of proceed data
        }
    }

    override fun setTotal(total: Long) {
        this.total = total
    }

    override fun setCompleted(complete: Long) {
        if (complete == total) {
            Logger.trace { "Total extraction time: ${System.currentTimeMillis() - startTime}ms." }
            onComplete(results)
        }
    }

    override fun prepareOperation(extractAskMode: ExtractAskMode?) {
    }

    override fun setOperationResult(extractOperationResult: ExtractOperationResult?) {
        if (skipExtraction) return

        if (extractOperationResult != ExtractOperationResult.OK) {
            System.err.println("Extraction error");
        } else {
            val filePath = inArchive.getProperty(index, PropID.PATH) as String

            Logger.trace { String.format("Extracted %10sb | %s", size, filePath) }

            results[index] = bytes.decodeToString()

            hash = 0
            size = 0
            bytes = byteArrayOf()
        }
    }
}