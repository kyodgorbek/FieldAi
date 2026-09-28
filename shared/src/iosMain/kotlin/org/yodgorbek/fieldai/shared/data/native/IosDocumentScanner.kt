package org.yodgorbek.fieldai.shared.data.native

import kotlinx.coroutines.suspendCancellableCoroutine
import org.yodgorbek.fieldai.shared.domain.native.DocumentScanner
import platform.Foundation.NSUUID
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.writeToFile
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.darwin.NSObject
import kotlin.coroutines.resume

class IosDocumentScanner : DocumentScanner {
    override suspend fun scanDocument(): String? = suspendCancellableCoroutine { cont ->
        val imagePickerController = UIImagePickerController()
        imagePickerController.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        
        val delegate = object : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
            override fun imagePickerController(
                picker: UIImagePickerController,
                didFinishPickingMediaWithInfo: Map<Any?, *>
            ) {
                val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
                if (image != null) {
                    val data = UIImageJPEGRepresentation(image, 1.0)
                    val filePath = NSTemporaryDirectory() + NSUUID().UUIDString + ".jpg"
                    data?.writeToFile(filePath, atomically = true)
                    picker.dismissViewControllerAnimated(true, null)
                    cont.resume("file://$filePath")
                } else {
                    picker.dismissViewControllerAnimated(true, null)
                    cont.resume(null)
                }
            }

            override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                picker.dismissViewControllerAnimated(true, null)
                cont.resume(null)
            }
        }
        
        imagePickerController.delegate = delegate
        
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootViewController?.presentViewController(imagePickerController, animated = true, completion = null)
        
        cont.invokeOnCancellation {
            rootViewController?.dismissViewControllerAnimated(true, null)
        }
    }
}
