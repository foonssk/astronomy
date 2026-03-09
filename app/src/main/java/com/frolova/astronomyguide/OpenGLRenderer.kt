package com.frolova.astronomyguide

import android.content.Context
import android.content.Intent
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.frolova.astronomyguide.GLBackgroundSquare
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.rem

// Рендерер для OpenGL-сцены: управляет отрисовкой 3D-объектов
class OpenGLRenderer(private val context: Context) : GLSurfaceView.Renderer {
    private lateinit var background: GLBackgroundSquare

    lateinit var cubeCursor: CubeCursor

    //  ОБЪЕКТЫ: ПЛАНЕТЫ
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

    // Луна (отдельная сфера, привязанная к Земле)
    private lateinit var moonSphere: SpherePlanet

    //  МАТРИЦЫ И ПАРАМЕТРЫ
    private val projectionMatrix = FloatArray(16) // Матрица проекции (перспектива)
    private val viewMatrix = FloatArray(16)       // Матрица вида (камера)
    private val vpMatrix = FloatArray(16)         // Итоговая матрица (View × Projection)

    private var ratio = 0f  // Соотношение сторон экрана (width/height)
    private var angle = 0f  // Угол вращения системы (для анимации орбит)

    // ИНИЦИАЛИЗАЦИЯ: создаётся один раз при старте
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f) // Чёрный цвет фона при очистке
        GLES20.glEnable(GLES20.GL_DEPTH_TEST) // Включаем тест глубины для 3D

        background = GLBackgroundSquare(context) // Создаём фон-галактику
        cubeCursor = CubeCursor() // Создаём курсор-куб для выделения

        // Планеты (сферы)
        // Параметры: (текстура, радиус_орбиты, размер_планеты, скорость_вращения)
        sun = Planet(SpherePlanet(context,R.drawable.sun1), 0f, 0.5f, 0f)
        mercury = Planet(SpherePlanet(context,R.drawable.mercury1), 0.8f, 0.15f, 4f)
        venus = Planet(SpherePlanet(context,R.drawable.venus1), 1.2f, 0.18f, 3f)
        earth = Planet(SpherePlanet(context,R.drawable.earth1), 1.6f, 0.2f, 2f)
        mars = Planet(SpherePlanet(context,R.drawable.mars1), 2.2f, 0.2f, 1.6f)
        jupiter = Planet(SpherePlanet(context,R.drawable.jupiter1), 2.8f, 0.30f, 1.2f)
        saturn = Planet(SpherePlanet(context,R.drawable.saturn1), 3.6f, 0.26f, 1f)
        uranus = Planet(SpherePlanet(context,R.drawable.uranus1), 4.0f, 0.22f, 0.8f)
        neptune = Planet(SpherePlanet(context,R.drawable.neptun1), 4.4f, 0.20f, 0.6f)

        moonSphere = SpherePlanet(context,R.drawable.moon1) // Луна как отдельная сфера

        // Камера фиксированная
        // Параметры: (позиция камеры, точка взгляда, вектор "вверх")
        Matrix.setLookAtM(
            viewMatrix, 0,
            0f, 3f, 10f,   // Камера: x=0, y=3, z=10
            0f, 0f, 0f,    // Смотрит в центр (Солнце)
            0f, 1f, 0f     // Вектор "вверх" по Y
        )
    }

    // ИЗМЕНЕНИЕ РАЗМЕРА: при повороте экрана или ресайзе
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height) // Область отрисовки на весь экран
        ratio = width.toFloat() / height // Сохраняем соотношение сторон

        // Матрица перспективной проекции (усечение)
        Matrix.frustumM(
            projectionMatrix, 0,
            -ratio, ratio, // Левая/правая границы (с учётом ratio)
            -1f, 1f,       // Нижняя/верхняя границы
            1f, 100f       // Ближняя и дальняя плоскости отсечения
        )
    }

    // КАЖДЫЙ КАДР: основная отрисовка
    override fun onDrawFrame(gl: GL10?) {
        angle += 0.5f // Обновляем угол для анимации вращения
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT) // Очистка экрана и буфера глубины

        // Объединяем матрицы: Projection × View
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        // масштаб всей системы от ориентации
        val systemScale = if (ratio > 1f) {
            2f   // альбомная — ближе
        } else {
            1f   // портретная — дальше
        }

        // Порядок отрисовки: фон → планеты → курсор
        drawBackground()
        drawSolarSystem(systemScale)
        cubeCursor.drawCursor(angle, vpMatrix, systemScale)
    }

    // ОТРИСОВКА ФОНА
    private fun drawBackground() {
        val model = FloatArray(16)
        Matrix.setIdentityM(model, 0) // Сбрасываем матрицу модели

        // немного наклоняем фон под камеру
        Matrix.rotateM(model, 0, -17f, 1f, 0f, 0f) // Наклон по X
        Matrix.translateM(model, 0, 0f, 0f, -30f)  // Сдвигаем фон "вглубь" экрана

        // Масштабируем фон под ориентацию экрана
        if (ratio > 1f)
            Matrix.scaleM(model, 0, 40f * ratio, 40f, 1f)
        else
            Matrix.scaleM(model, 0, 25f, 25f / ratio, 1f)

        // Считаем итоговую матрицу: MVP = ViewProjection × Model
        val mvp = FloatArray(16)
        Matrix.multiplyMM(mvp, 0, vpMatrix, 0, model, 0)
        background.draw(mvp) // Рисуем фон с итоговой матрицей
    }

    // ОТРИСОВКА ПЛАНЕТ
    private fun drawSolarSystem(scale: Float) {
        // Вызываем отрисовку для каждой планеты с общими параметрами
        sun.drawScaled(vpMatrix, angle, scale)
        mercury.drawScaled(vpMatrix, angle, scale)
        venus.drawScaled(vpMatrix, angle, scale)
        earth.drawScaled(vpMatrix, angle, scale)

        drawMoonScaled(scale) // Луна рисуется отдельно (привязана к Земле)

        mars.drawScaled(vpMatrix, angle, scale)
        jupiter.drawScaled(vpMatrix, angle, scale)
        saturn.drawScaled(vpMatrix, angle, scale)
        uranus.drawScaled(vpMatrix, angle, scale)
        neptune.drawScaled(vpMatrix, angle, scale)
    }

    //  ОТРИСОВКА ЛУНЫ (специальная логика)
    private fun drawMoonScaled(scale: Float) {
        val model = FloatArray(16)
        Matrix.setIdentityM(model, 0)

        // орбита Земли: вращение + смещение на радиус орбиты Земли
        Matrix.rotateM(model, 0, angle * 2f, 0f, 1f, 0f)
        Matrix.translateM(model, 0, 1.6f * scale, 0f, 0f)

        // орбита Луны перпендикулярно эклиптике: вращается быстрее + смещение от Земли
        Matrix.rotateM(model, 0, angle * 4f, 0f, 1f, 0f)
        Matrix.translateM(model, 0, 0.3f * scale, 0f, 0f)

        // Масштаб Луны
        Matrix.scaleM(model, 0, 0.08f * scale, 0.08f * scale, 0.08f * scale)

        val mvp = FloatArray(16)
        Matrix.multiplyMM(mvp, 0, vpMatrix, 0, model, 0)
        moonSphere.draw(mvp) // Рисуем сферу Луны
    }

    //  УПРАВЛЕНИЕ: переключение выбранной планеты
    fun selectNext() {
        // Циклический переход к следующей (по модулю размера списка)
        cubeCursor.selectedIndex = (cubeCursor.selectedIndex + 1) % cubeCursor.objectPositions.size
    }

    fun selectPrev() {
        // Циклический переход к предыдущей (с обработкой перехода через 0)
        cubeCursor.selectedIndex = if (cubeCursor.selectedIndex == 0) cubeCursor.objectPositions.size - 1 else cubeCursor.selectedIndex - 1
    }
}