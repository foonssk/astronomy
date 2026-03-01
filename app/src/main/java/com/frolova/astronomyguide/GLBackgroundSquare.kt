package com.frolova.astronomyguide

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import androidx.compose.ui.graphics.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import com.frolova.astronomyguide.R

class GLBackgroundSquare(private val context: Context) {

    private val vertexCordsData: FloatBuffer
    private val textureCordsData: FloatBuffer
    private val program: Int
    private val textureId: Int

    // Вершинный шейдер для работы с каждой вершиной
    private val vertexShaderCode = """
        attribute vec4 aPosition; // переменная вектора из 4 переменных для хранения позиции вершины (x,y,z,w)
        attribute vec2 aTexCoord; // вектор из 2 переменных для привязки к вершине точку из текстуры (u,v)
        uniform mat4 uMVPMatrix; // одинаковая для всех вершин матрица положения объекта, положения камеры и перспективы для расположения на экране 
        varying vec2 vTexCoord; // вектор текстурных координат для определения цветов из текстуры для пикселей в текстурном шейдере
        void main(){
            gl_Position = uMVPMatrix * aPosition; // итоговая позиция в пространстве
            vTexCoord = aTexCoord;
        }
    """.trimIndent()

    // фрагментный шейдер
    private val fragmentShaderCode = """
        precision mediump float; // средняя точность для текстуры
        varying vec2 vTexCoord;
        uniform sampler2D uTexture; // одинаковое для каждого пикселя представление 2D-текстуры. Текстура, откуда брать цвета
        void main(){
            gl_FragColor = texture2D(uTexture, vTexCoord);
        }
    """.trimIndent()

    init {
        val vertices = floatArrayOf(
            -1f,1f,0f, // левый верх
            -1f,-1f,0f, // левый низ
            1f,-1f,0f, // правый низ
            1f,1f,0f // правый верх
        )

        val texCords = floatArrayOf(
            0f,0f, // левый низ
            0f,1f, //
            1f,1f, //
            1f,0f //
        )

        vertexCordsData = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(vertices)
                position(0)
            }

        textureCordsData = ByteBuffer.allocateDirect(texCords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(texCords)
                position(0)
            }
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }

        textureId = loadTexture(context, R.drawable.galaxy)
    }

    fun draw(mvpMatrix: FloatArray){
        GLES20.glUseProgram(program)

        val positionHandle = GLES20.glGetAttribLocation(program,"aPosition")
        val texCordHandle = GLES20.glGetAttribLocation(program,"aTexCoord")
        val mvpHandle = GLES20.glGetUniformLocation(program,"uMVPMatrix")
        val textureHandle = GLES20.glGetUniformLocation(program,"uTexture")

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(
            positionHandle,
            3, // сколько значений на все вершину
            GLES20.GL_FLOAT,
            false, // нормализация
            3*4, // смещение между вершинами
            vertexCordsData // откуда брать данные
        )

        GLES20.glEnableVertexAttribArray(texCordHandle)
        GLES20.glVertexAttribPointer(
            texCordHandle,
            2,
            GLES20.GL_FLOAT,
            false,
            2 * 4,
            textureCordsData
        )


        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0) // загрузка матрицы в вершинный шейдер

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(textureHandle, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCordHandle)
    }
}

fun loadShader(type: Int, code: String) : Int{
    return GLES20.glCreateShader(type).also { shader->
        GLES20.glShaderSource(shader, code)
        GLES20.glCompileShader(shader)
    }
}

fun loadTexture(context: Context, resId: Int) : Int{
    val textureIds = IntArray(1)
    GLES20.glGenTextures(1,textureIds,0)

    val options = BitmapFactory.Options().apply { inScaled = false }
    val bitmap = BitmapFactory.decodeResource(context.resources,resId,options)

    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureIds[0])
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bitmap,0)
    bitmap.recycle()

    return textureIds[0]
}