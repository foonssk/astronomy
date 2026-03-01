package com.frolova.astronomyguide

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class OpenGLRenderer(private val context: Context): GLSurfaceView.Renderer{

    private lateinit var backgroundSquare: GLBackgroundSquare
    private lateinit var colorCube: ColorCube
    private val vpMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private var angle = 0f
    private var ratio = 0f

    override fun onDrawFrame(gl: GL10?) {
        angle += 1f
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        val square = FloatArray(16)
        Matrix.setIdentityM(square,0)

        val distance = 5f
        Matrix.translateM(square,0,0f,0f,-distance)

        if (ratio > 1f) {
            // горизонтальная ориентация
            Matrix.scaleM(square, 0, 6f *   ratio, 6f, 1f)
        } else {
            // вертикальная ориентация
            Matrix.scaleM(square, 0, 3f, 3f / ratio, 1f)
        }

        val squareMvp = FloatArray(16)
        Matrix.multiplyMM(squareMvp,0,vpMatrix,0,square,0)
        backgroundSquare.draw(squareMvp)

        val cube = FloatArray(16)
        Matrix.setIdentityM(cube, 0)
        Matrix.translateM(cube,0,0f,0f,-3f)
        Matrix.rotateM(cube,0,angle,0f,1f,0f)
        Matrix.rotateM(cube,0,20f,1f,0f,0f)
        val cubeMvp = FloatArray(16)
        Matrix.multiplyMM(cubeMvp,0,vpMatrix,0,cube,0)
        colorCube.draw(cubeMvp)
    }

    override fun onSurfaceChanged(
        gl: GL10?,
        width: Int,
        height: Int
    ) {
        GLES20.glViewport(0,0,width,height)

        ratio = width.toFloat() / height
        Matrix.frustumM(
            projectionMatrix, 0,
            -ratio, ratio,
            -1f, 1f,
            1f, 10f
        )
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    override fun onSurfaceCreated(
        gl: GL10?,
        config: EGLConfig?
    ) {
        GLES20.glClearColor(0f,0f,0f,1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        backgroundSquare = GLBackgroundSquare(context)
        colorCube = ColorCube()

        Matrix.setLookAtM(
            viewMatrix, 0,
            0f, 0f, 1f,   // позиция камеры
            0f, 0f, 0f,   // точка, куда смотрим
            0f, 1f, 0f    // вектор "вверх"
        )
    }
}