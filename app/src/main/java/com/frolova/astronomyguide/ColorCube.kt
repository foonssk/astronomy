package com.frolova.astronomyguide

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class ColorCube {

    private val vertexBuffer: FloatBuffer
    private val colorBuffer: FloatBuffer
    private val program: Int

    // Цвета для каждой вершины (6 граней × 6 вершин × RGBA)
    private val colors = floatArrayOf(
        // Передняя грань — красная
        1f,0f,0f,1f,
        1f,0f,0f,1f,
        1f,0f,0f,1f,
        1f,0f,0f,1f,
        1f,0f,0f,1f,
        1f,0f,0f,1f,

        0f, 1f, 0f, 1f,
        0f, 1f, 0f, 1f,
        0f, 1f, 0f, 1f,
        0f, 1f, 0f, 1f,
        0f, 1f, 0f, 1f,
        0f, 1f, 0f, 1f,

        0f, 0f, 1f, 1f,
        0f, 0f, 1f, 1f,
        0f, 0f, 1f, 1f,
        0f, 0f, 1f, 1f,
        0f, 0f, 1f, 1f,
        0f, 0f, 1f, 1f,

        1f, 1f, 0f, 1f,
        1f, 1f, 0f, 1f,
        1f, 1f, 0f, 1f,
        1f, 1f, 0f, 1f,
        1f, 1f, 0f, 1f,
        1f, 1f, 0f, 1f,

        1f, 0f, 1f, 1f,
        1f, 0f, 1f, 1f,
        1f, 0f, 1f, 1f,
        1f, 0f, 1f, 1f,
        1f, 0f, 1f, 1f,
        1f, 0f, 1f, 1f,

        0f, 1f, 1f, 1f,
        0f, 1f, 1f, 1f,
        0f, 1f, 1f, 1f,
        0f, 1f, 1f, 1f,
        0f, 1f, 1f, 1f,
        0f, 1f, 1f, 1f,

        )

    // Вершины куба (36 вершин = 12 треугольников)
    private val vertices = floatArrayOf(
        // Передняя грань
        -0.5f,  0.5f,  0.5f,
        -0.5f, -0.5f,  0.5f,
        0.5f, -0.5f,  0.5f,
        -0.5f,  0.5f,  0.5f,
        0.5f, -0.5f,  0.5f,
        0.5f,  0.5f,  0.5f,

        // Задняя грань
        -0.5f,  0.5f, -0.5f,
        -0.5f, -0.5f, -0.5f,
        0.5f, -0.5f, -0.5f,
        -0.5f,  0.5f, -0.5f,
        0.5f, -0.5f, -0.5f,
        0.5f,  0.5f, -0.5f,

        // Левая грань
        -0.5f,  0.5f, -0.5f,
        -0.5f, -0.5f, -0.5f,
        -0.5f, -0.5f,  0.5f,
        -0.5f,  0.5f, -0.5f,
        -0.5f, -0.5f,  0.5f,
        -0.5f,  0.5f,  0.5f,

        // Правая грань
        0.5f,  0.5f, -0.5f,
        0.5f, -0.5f, -0.5f,
        0.5f, -0.5f,  0.5f,
        0.5f,  0.5f, -0.5f,
        0.5f, -0.5f,  0.5f,
        0.5f,  0.5f,  0.5f,

        // Верхняя грань
        -0.5f,  0.5f, -0.5f,
        -0.5f,  0.5f,  0.5f,
        0.5f,  0.5f,  0.5f,
        -0.5f,  0.5f, -0.5f,
        0.5f,  0.5f,  0.5f,
        0.5f,  0.5f, -0.5f,

        // Нижняя грань
        -0.5f, -0.5f, -0.5f,
        -0.5f, -0.5f,  0.5f,
        0.5f, -0.5f,  0.5f,
        -0.5f, -0.5f, -0.5f,
        0.5f, -0.5f,  0.5f,
        0.5f, -0.5f, -0.5f
    )

    private val vertexShaderCode = """
        attribute vec4 aPosition;
        attribute vec4 aColor;
        uniform mat4 uMVPMatrix;
        varying vec4 vColor;

        void main() {
            gl_Position = uMVPMatrix * aPosition;
            vColor = aColor;
        }
    """

    private val fragmentShaderCode = """
        precision mediump float;
        varying vec4 vColor;

        void main() {
            gl_FragColor = vColor;
        }
    """

    init {
        // Буфер вершин
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(vertices)
                position(0)
            }

        // Буфер цветов
        colorBuffer = ByteBuffer.allocateDirect(colors.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(colors)
                position(0)
            }

        // Компиляция шейдеров
        val vShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vShader)
            GLES20.glAttachShader(it, fShader)
            GLES20.glLinkProgram(it)
        }
    }

    fun draw(mvpMatrix: FloatArray) {
        GLES20.glUseProgram(program)

        val posHandle = GLES20.glGetAttribLocation(program, "aPosition")
        val colorHandle = GLES20.glGetAttribLocation(program, "aColor")
        val mvpHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")

        // Вершины
        GLES20.glEnableVertexAttribArray(posHandle)
        GLES20.glVertexAttribPointer(
            posHandle, 3, GLES20.GL_FLOAT, false,
            3 * 4, vertexBuffer
        )

        // Цвета
        GLES20.glEnableVertexAttribArray(colorHandle)
        GLES20.glVertexAttribPointer(
            colorHandle, 4, GLES20.GL_FLOAT, false,
            4 * 4, colorBuffer
        )

        // Матрица
        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0)

        // Рисуем куб
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.size / 3)

        GLES20.glDisableVertexAttribArray(posHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
    }
}
