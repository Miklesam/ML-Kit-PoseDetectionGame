package com.onelinegaming.posedetectiondemo.fragments

import android.graphics.Point
import android.os.Bundle
import android.util.Size
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.onelinegaming.posedetectiondemo.GameViewModel
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
    private var playing = false
    lateinit var racetime: String
    protected var animateCount = 0
    protected var imCount = 0
    val gameViewModel: GameViewModel by viewModels()

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

        gameViewModel.gameStartLeft.observe(viewLifecycleOwner) {
            if (it == 4) {
                playing = true
                game_start.visibility = View.GONE
            } else if (it in 1..3) {
                animateCounText(4 - it)
            }
        }

        gameViewModel.time.observe(viewLifecycleOwner) {
            val secs = it / 1000
            val millis = it % 1000
            val time = "$secs:$millis"
            racetime = time
            time_counter.text = time
        }
        gameViewModel.updateFrame.observe(viewLifecycleOwner) {
            if (playing) updateRunningMan()
        }
    }

    private fun animateCounText(value: Int) {
        game_start.text = value.toString()
        game_start.apply {
            this.visibility = View.VISIBLE
            alpha = 0f
            animate().alpha(1f).setDuration(500L).setInterpolator(DecelerateInterpolator()).start()
        }
    }

    private fun updateRunningMan() {
        increaseRunCount()
        when (imCount) {
            0 -> {
                player_view.setImageResource(R.drawable.first_scaled)
            }
            1 -> {
                player_view.setImageResource(R.drawable.second_scaled)
            }
            2 -> {
                player_view.setImageResource(R.drawable.third_scaled)
            }
            3 -> {
                player_view.setImageResource(R.drawable.forth_scaled)
            }
            4 -> {
                player_view.setImageResource(R.drawable.fifth_scaled)
            }
            5 -> {
                player_view.setImageResource(R.drawable.sixth_scaled)
            }
            6 -> {
                player_view.setImageResource(R.drawable.seventh_scaled)
            }
            7 -> {
                player_view.setImageResource(R.drawable.eight_scaled)
            }
        }
    }

    protected fun increaseRunCount() {
        if (animateCount < ANIMATE_COUNT) {
            animateCount++
        } else {
            animateCount = 0
        }
        if (animateCount == ANIMATE_COUNT) {
            if (imCount < 3) {
                imCount++
            } else {
                imCount = 0
            }
        }
    }

    fun checkDebounce(previous: Pose, current: Pose) {
        checkForPoseLandMark(PoseLandmark.LEFT_KNEE, previous, current)
        checkForPoseLandMark(PoseLandmark.RIGHT_KNEE, previous, current)
    }

    private fun checkForPoseLandMark(
        posePoint: Int,
        previous: Pose,
        current: Pose
    ) {
        val previousY =
            previous.getPoseLandmark(posePoint)?.position?.y ?: 0f
        val currentY =
            current.getPoseLandmark(posePoint)?.position?.y ?: 0f

        val diffY = currentY - previousY
        if (diffY > ALLOWED_DEBOUNCE) {
            player_view?.let {
                if (player_view.x + player_view.width / 2 > finish_line.x) {
                    playing = false
                    val bundle = Bundle()
                    bundle.putString(RESULT, racetime)
                    if (findNavController().currentDestination?.id == R.id.gameFragment) {
                        findNavController().navigate(
                            R.id.action_gameFragment_to_resultsFragment,
                            bundle
                        )
                    }
                }
                it.x += SPEED
            }
        }
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
                poseDetector.process(image)
                    .addOnSuccessListener { results ->
                        graphicOverlay.clear()
                        //check(results)
                        if (this@GameFragment::lastResult.isInitialized && playing) {
                            checkDebounce(lastResult, results)
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


            }
        }
    }

    override fun onDestroy() {
        cameraExecutor.shutdown()
        gameViewModel.disposables.dispose()
        super.onDestroy()
    }

    companion object {
        val ANIMATE_COUNT = 4
        val ALLOWED_DEBOUNCE = 4f
        val SPEED = 4f
        val RESULT = "result"
    }

}