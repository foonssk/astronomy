package com.frolova.astronomyguide

import android.content.Context
import android.content.Intent
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.frolova.astronomyguide.GLBackgroundSquare
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class OpenGLRenderer(private val context: Context) : GLSurfaceView.Renderer {
    private lateinit var background: GLBackgroundSquare

    // Планеты
    private lateinit var sun: Planet
    private lateinit var mercury: Planet
    private lateinit var venus: Planet
    private lateinit var earth: Planet
    private lateinit var mars: Planet
    private lateinit var jupiter: Planet
    private lateinit var saturn: Planet
    private lateinit var uranus: Planet
    private lateinit var neptune: Planet

    // Луна
    private lateinit var moonSphere: SpherePlanet

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val vpMatrix = FloatArray(16)

    private var ratio = 0f
    private var angle = 0f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        background = GLBackgroundSquare(context)

        // Планеты (сферы)
        // Planet(сфера, расстояниеОтСолнца, размер, скоростьВращения)
        sun = Planet(SpherePlanet(floatArrayOf(1f, 1f, 0.5f, 1f)), 0f, 0.5f, 0f)      // Яркое золото
        mercury = Planet(SpherePlanet(floatArrayOf(0.6f, 0.6f, 0.7f, 1f)), 0.8f, 0.10f, 4f) // Стальной
        venus = Planet(SpherePlanet(floatArrayOf(1f, 0.4f, 0f, 1f)), 1.2f, 0.15f, 3f)   // Оранжевый
        earth = Planet(SpherePlanet(floatArrayOf(0f, 0.6f, 1f, 1f)), 1.6f, 0.18f, 2f)   // Ярко-синий
        mars = Planet(SpherePlanet(floatArrayOf(1f, 0.1f, 0.1f, 1f)), 2.0f, 0.14f, 1.6f) // Кровавый красный
        jupiter = Planet(SpherePlanet(floatArrayOf(0.9f, 0.7f, 0.3f, 1f)), 2.8f, 0.30f, 1.2f)
        saturn = Planet(SpherePlanet(floatArrayOf(1f, 0.9f, 0.4f, 1f)), 3.6f, 0.26f, 1f)
        uranus = Planet(SpherePlanet(floatArrayOf(0.3f, 1f, 0.9f, 1f)), 4.0f, 0.22f, 0.8f) // Бирюза
        neptune = Planet(SpherePlanet(floatArrayOf(0.2f, 0.2f, 1f, 1f)), 4.4f, 0.20f, 0.6f)
        moonSphere = SpherePlanet(floatArrayOf(0.9f, 0.9f, 0.9f, 1f))

        // Камера фиксированная
        Matrix.setLookAtM(
            viewMatrix, 0,
            0f, 3f, 10f,
            0f, 0f, 0f,
            0f, 1f, 0f
        )
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        ratio = width.toFloat() / height

        Matrix.frustumM( // Настройка проекции: усечённая пирамида создаёт перспективную проекцию — объекты вдали кажутся меньше.
            projectionMatrix, 0,
            -ratio, ratio,
            -1f, 1f,
            1f, 100f
        )
    }

    override fun onDrawFrame(gl: GL10?) {
        angle += 0.5f // плавное вращение: +0.5° за кадр
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        // масштаб всей системы от ориентации
        val systemScale = if (ratio > 1f) {
            2f   // альбомная — ближе
        } else {
            1f   // портретная — дальше
        }
// Отрисовка: сначала фон, потом планеты
        drawBackground()
        drawSolarSystem(systemScale)
    }

    private fun drawBackground() {
        val model = FloatArray(16)
        Matrix.setIdentityM(model, 0)

        // немного наклоняем фон под камеру
        Matrix.rotateM(model, 0, -17f, 1f, 0f, 0f)
        Matrix.translateM(model, 0, 0f, 0f, -30f)

        if (ratio > 1f)// Масштабирование под ориентацию экрана
            Matrix.scaleM(model, 0, 40f * ratio, 40f, 1f)
        else
            Matrix.scaleM(model, 0, 20f, 20f / ratio, 1f)

        val mvp = FloatArray(16)
        Matrix.multiplyMM(mvp, 0, vpMatrix, 0, model, 0)
        background.draw(mvp)
    }

    private fun drawSolarSystem(scale: Float) {// Отрисовка планет с параметрами
        //Каждая планета получает один и тот же angle,
        //но в классе Planet он умножается на индивидуальную speed, поэтому Меркурий вращается быстрее Нептуна.
        sun.drawScaled(vpMatrix, angle, scale)
        mercury.drawScaled(vpMatrix, angle, scale)
        venus.drawScaled(vpMatrix, angle, scale)
        earth.drawScaled(vpMatrix, angle, scale)

        drawMoonScaled(scale)

        mars.drawScaled(vpMatrix, angle, scale)
        jupiter.drawScaled(vpMatrix, angle, scale)
        saturn.drawScaled(vpMatrix, angle, scale)
        uranus.drawScaled(vpMatrix, angle, scale)
        neptune.drawScaled(vpMatrix, angle, scale)
    }

    private fun drawMoonScaled(scale: Float) {
        val model = FloatArray(16)
        Matrix.setIdentityM(model, 0)

        // орбита Земли
        Matrix.rotateM(model, 0, angle * 2f, 0f, 1f, 0f)
        Matrix.translateM(model, 0, 1.6f * scale, 0f, 0f)

        // орбита Луны перпендикулярно эклиптике
        Matrix.rotateM(model, 0, angle * 4f, 0f, 1f, 0f)
        Matrix.translateM(model, 0, 0.3f * scale, 0f, 0f)

        Matrix.scaleM(model, 0, 0.08f * scale, 0.08f * scale, 0.08f * scale)

        val mvp = FloatArray(16)
        Matrix.multiplyMM(mvp, 0, vpMatrix, 0, model, 0)
        moonSphere.draw(mvp)
    }
}