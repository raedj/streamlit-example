// SIGNATURE STUB for type-checking only (tools/android-typecheck). Mirrors the public API of the real library; never shipped.
package androidx.activity.result
fun interface ActivityResultCallback<O> { fun onActivityResult(result: O) }
abstract class ActivityResultLauncher<I> { fun launch(input: I) {} }
