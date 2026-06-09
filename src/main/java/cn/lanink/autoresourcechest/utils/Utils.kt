package cn.lanink.autoresourcechest.utils

import java.text.DecimalFormat
import java.util.*

/**
 * @author lt_name
 */
class Utils {

    companion object {

        @JvmStatic
        fun formatTime(time: Int): String {
            if (time >= 60) {
                val format = DecimalFormat("00")
                return format.format((time / 60).toLong()) + ":" + format.format((time % 60).toLong())
            }
            return time.toString()
        }

        @JvmStatic
        fun bytesToBase64(src: ByteArray): String {
            return if (src.isEmpty()) { "not" } else Base64.getEncoder().encodeToString(src)
        }

        @JvmStatic
        fun base64ToBytes(hexString: String): ByteArray? {
            return if (hexString.isEmpty() || hexString == "not") { null } else Base64.getDecoder().decode(hexString)
        }

    }

}