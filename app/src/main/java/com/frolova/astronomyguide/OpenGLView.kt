package com.frolova.astronomyguide

import android.content.Context
import android.opengl.GLSurfaceView

// Custom View для отображения OpenGL-графики
// Наследуется от GLSurfaceView — стандартного контейнера для 3D в Android
class OpenGLView(context: Context): GLSurfaceView(context) {
    private val renderer: OpenGLRenderer // Рендерер, управляющий отрисовкой сцены

    init {
        // Указываем версию OpenGL ES 2.0
        setEGLContextClientVersion(2)

        // Создаём и устанавливаем наш кастомный рендерер
        renderer = OpenGLRenderer(context)
        setRenderer(renderer)

        // Режим отрисовки: непрерывный (кадры рисуются постоянно, даже без изменений)
        // Альтернатива: RENDERMODE_WHEN_DIRTY — только при явном запросе
        renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }

    // Публичный метод для доступа к рендереру извне
    // Нужен, чтобы кнопки из Compose могли управлять сценой (переключение планет)
    fun getRenderer(): OpenGLRenderer = renderer
}