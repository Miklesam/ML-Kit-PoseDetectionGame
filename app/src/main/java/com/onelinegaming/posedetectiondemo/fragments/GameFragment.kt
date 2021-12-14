package com.onelinegaming.posedetectiondemo.fragments

import android.graphics.Point
import android.os.Bundle
import android.util.Size
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.onelinegaming.posedetectiondemo.GraphicOverlay
import com.onelinegaming.posedetectiondemo.PoseGraphic
import com.onelinegaming.posedetectiondemo.R
import kotlinx.android.synthetic.main.fragment_game.*


class GameFragment : Fragment(R.layout.fragment_game) {

    private var width = 0
    private var height = 0
    private lateinit var cameraExecutor: ExecutorService
    private var cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    lateinit var lastResult: Pose
    private var plaing = false

    private var cnt = 0
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val windowManager = requireActivity().windowManager
        val display = windowManager?.defaultDisplay
        val size = Point()
        display?.getSize(size)
        width = size.x
        height = size.y
        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

//            // Preview
//            val preview = Preview.Builder()
//                .build()
//                .also {
//                    it.setSurfaceProvider(view_finder.createSurfaceProvider())
//                }

            val imageCapture = ImageCapture.Builder()
                .setTargetResolution(Size(width, height))
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, ImageAnalyzer(graphic_overlay, cameraSelector))
                }

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, imageCapture, imageAnalyzer
                )

            } catch (exc: Exception) {
                //Log.e(MainActivity.TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private inner class ImageAnalyzer(
        val graphicOverlay: GraphicOverlay,
        val cameraSelector: CameraSelector
    ) : ImageAnalysis.Analyzer {

        private val poseDetector: PoseDetector

        init {
            poseDetector = initPoseRecognition()
        }

        private fun initPoseRecognition(): PoseDetector {
            val options = PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build()

            return PoseDetection.getClient(options)
        }

        override fun analyze(imageProxy: ImageProxy) {
            val isImageFlipped = cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            if (rotationDegrees == 0 || rotationDegrees == 180) {
                graphicOverlay.setImageSourceInfo(
                    imageProxy.width, imageProxy.height, isImageFlipped
                )
            } else {
                graphicOverlay.setImageSourceInfo(
                    imageProxy.height, imageProxy.width, isImageFlipped
                )
            }

            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image =
                    InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                // Pass image to an ML Kit Vision API
                cnt += 1
                if (true) {
                    poseDetector.process(image)
                        .addOnSuccessListener { results ->
                            graphicOverlay.clear()
                            //check(results)
                            if (this@GameFragment::lastResult.isInitialized && plaing) {
                                //checkDebounce(lastResult, results)
                            }
                            lastResult = results
                            graphicOverlay.add(
                                PoseGraphic(
                                    graphicOverlay,
                                    results
                                )
                            )
                        }
                        .addOnFailureListener { e ->
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }


            }
        }
    }

    override fun onDestroy() {
        cameraExecutor.shutdown()
        super.onDestroy()
    }

}