// SIGNATURE STUB for type-checking only (tools/android-typecheck). Mirrors the public API of the real library; never shipped.
package androidx.activity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
open class ComponentActivity : android.app.Activity() {
    fun <I, O> registerForActivityResult(contract: ActivityResultContract<I, O>, callback: ActivityResultCallback<O>): ActivityResultLauncher<I> = TODO()
}
fun ComponentActivity.enableEdgeToEdge() {}
