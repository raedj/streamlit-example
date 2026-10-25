// SIGNATURE STUB for type-checking only (tools/android-typecheck). Mirrors the public API of the real library; never shipped.
package androidx.activity.result.contract
import android.net.Uri
abstract class ActivityResultContract<I, O>
class ActivityResultContracts {
    class RequestPermission : ActivityResultContract<String, Boolean>()
    open class OpenDocument : ActivityResultContract<Array<String>, Uri?>()
    open class CreateDocument(mimeType: String) : ActivityResultContract<String, Uri?>()
}
