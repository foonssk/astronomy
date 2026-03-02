package com.frolova.astronomyguide

import android.opengl.Matrix

class Planet(
    val sphere: SpherePlanet,   // модель сферы (сама планета)
    val orbitRadius: Float,     // расстояние от Солнца
    val size: Float,            // масштаб планеты
    val orbitSpeed: Float,      // скорость вращения вокруг Солнца
    val tiltX: Float = 0f,      // наклон оси по X (необязательно)
    val tiltY: Float = 0f       // наклон оси по Y (необязательно)
) {

    fun drawScaled(vp: FloatArray, angle: Float, scale: Float) {
        val model = FloatArray(16)
        Matrix.setIdentityM(model, 0)
//Вращение по орбите
        Matrix.rotateM(model, 0, angle * orbitSpeed, 0f, 1f, 0f)
        //Смещение на орбиту (по оси X)
        Matrix.translateM(model, 0, orbitRadius * scale, 0f, 0f)
        //Масштабирование самой планеты
        Matrix.scaleM(model, 0, size * scale, size * scale, size * scale)

        val mvp = FloatArray(16)
        Matrix.multiplyMM(mvp, 0, vp, 0, model, 0)
        sphere.draw(mvp)// Отрисовка сферы с готовой матрицей
    }

}
