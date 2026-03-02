package com.frolova.astronomyguide

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class SpherePlanet(
    private val color: FloatArray,
    stacks: Int = 24,//Вертикальное разрешение (по широте)
    slices: Int = 24// Горизонтальное разрешение (по долготе)
) { //Чем больше stacks slices тем глаже сфера, но больше вершин и ниже производительность

    private val vertexBuffer: FloatBuffer
    private val indexBuffer: ShortBuffer
    private val program: Int
    private val indexCount: Int

    private val vertexShader = """
        attribute vec4 aPosition;
        uniform mat4 uMVPMatrix;
        uniform vec4 uColor;
        varying vec4 vColor;

        void main() {
            gl_Position = uMVPMatrix * aPosition;
            vColor = uColor;
        }
    """

    private val fragmentShader = """
        precision mediump float;
        varying vec4 vColor;

        void main() {
            gl_FragColor = vColor;
        }
    """

    init {
        val vertices = ArrayList<Float>()
        val indices = ArrayList<Short>()

        // Генерация вершин сферы
        for (i in 0..stacks) {
            val phi = PI * i / stacks
            val y = cos(phi).toFloat()
            val r = sin(phi).toFloat()

            for (j in 0..slices) {
                val theta = 2 * PI * j / slices // Угол по горизонтали (0..2π)
                val x = (r * cos(theta)).toFloat()  // Координата X
                val z = (r * sin(theta)).toFloat()  // Координата Z

                vertices.add(x); vertices.add(y); vertices.add(z)
            }
        }

        // Генерация индексов
        for (i in 0 until stacks) {
            val k1 = i * (slices + 1)
            val k2 = k1 + slices + 1

            for (j in 0 until slices) {
                indices.add((k1 + j).toShort()) // Первый треугольник (нижний левый)
                indices.add((k2 + j).toShort())
                indices.add((k1 + j + 1).toShort())

                indices.add((k1 + j + 1).toShort())// Второй треугольник (верхний правый)
                indices.add((k2 + j).toShort())
                indices.add((k2 + j + 1).toShort())
            }
        }

        indexCount = indices.size

        // Буфер вершин
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(vertices.toFloatArray())
                position(0)
            }

        // Буфер индексов
        indexBuffer = ByteBuffer.allocateDirect(indices.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .apply {
                put(indices.toShortArray())
                position(0)
            }

        // Компиляция шейдеров
        val v = loadShader(GLES20.GL_VERTEX_SHADER, vertexShader)
        val f = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader)

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, v)
            GLES20.glAttachShader(it, f)
            GLES20.glLinkProgram(it)
        }
    }

    fun draw(mvp: FloatArray) {
        GLES20.glUseProgram(program)

        val pos = GLES20.glGetAttribLocation(program, "aPosition")
        val mvpH = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        val colorH = GLES20.glGetUniformLocation(program, "uColor")

        GLES20.glEnableVertexAttribArray(pos)
        GLES20.glVertexAttribPointer(pos, 3, GLES20.GL_FLOAT, false, 3 * 4, vertexBuffer)
        // Передача матрицы и цвета в шейдер
        GLES20.glUniformMatrix4fv(mvpH, 1, false, mvp, 0)
        GLES20.glUniform4fv(colorH, 1, color, 0)

        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            indexCount,
            GLES20.GL_UNSIGNED_SHORT,
            indexBuffer
        )

        GLES20.glDisableVertexAttribArray(pos)
    }

    private fun loadShader(type: Int, code: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, code)
        GLES20.glCompileShader(shader)
        return shader
    }
}
