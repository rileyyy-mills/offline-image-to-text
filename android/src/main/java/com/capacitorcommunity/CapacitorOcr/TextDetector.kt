package com.capacitorcommunity.CapacitorOcr

import android.graphics.Bitmap
import android.util.NoSuchPropertyException
import com.getcapacitor.JSObject
import com.getcapacitor.PluginCall
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import org.json.JSONArray

class TextDetector {
    fun detectText(call: PluginCall, bitmap: Bitmap) {
        try {
            // Create ML Kit InputImage from bitmap
            val image = InputImage.fromBitmap(bitmap, 0) // 0 = no rotation
            
            // Initialize ML Kit TextRecognizer
            val textRecognizer: TextRecognizer = TextRecognition.getClient()
            
            // Process image with ML Kit
            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val detectedText = ArrayList<Any>()
                    val width = bitmap.width
                    val height = bitmap.height

                    for (block in visionText.textBlocks) {
                        for (line in block.lines) {
                            // Get bounding box coordinates
                            val cornerPoints = line.cornerPoints ?: 
                                throw NoSuchPropertyException("ML Kit: Could not get bounding coordinates")
                            
                            val topLeft = cornerPoints[0]
                            val topRight = cornerPoints[1]
                            val bottomRight = cornerPoints[2]
                            val bottomLeft = cornerPoints[3]

                            val textDetection = mapOf(
                                "topLeft" to listOf(
                                    topLeft.x.toDouble() / width,
                                    (height - topLeft.y).toDouble() / height
                                ),
                                "topRight" to listOf(
                                    topRight.x.toDouble() / width,
                                    (height - topRight.y).toDouble() / height
                                ),
                                "bottomLeft" to listOf(
                                    bottomLeft.x.toDouble() / width,
                                    (height - bottomLeft.y).toDouble() / height
                                ),
                                "bottomRight" to listOf(
                                    bottomRight.x.toDouble() / width,
                                    (height - bottomRight.y).toDouble() / height
                                ),
                                "text" to line.text
                            )
                            detectedText.add(textDetection)
                        }
                    }
                    call.resolve(JSObject().put("textDetections", JSONArray(detectedText)))
                }
                .addOnFailureListener { e ->
                    call.reject("ML Kit Text Recognition failed", e)
                }
        } catch (e: Exception) {
            e.printStackTrace()
            call.reject(e.localizedMessage, e)
        }
    }
}