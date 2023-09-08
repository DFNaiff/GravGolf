package io.github.dfnaiff.gravgolf

import kotlin.math.pow
import android.util.Log


class ForceField(private val objects: List<Triple<Float, Float, Float>>) {
    private val epsilon = 1e-3f  // Small constant for numerical stability
    private val normalizer = 1e7f

    fun getBodyForce(x: Float, y: Float): Pair<Float, Float> {
        var totalFx = 0f
        var totalFy = 0f

        for ((xObj, yObj, mObj) in objects) {
            val dx = x - xObj
            val dy = y - yObj

            val r = kotlin.math.sqrt(dx * dx + dy * dy)
            val denominator = (r + epsilon).pow(3)

            val fx = normalizer * mObj * dx / denominator
            val fy = normalizer * mObj * dy / denominator

            totalFx += fx
            totalFy += fy
        }

        return Pair(totalFx, totalFy)
    }
}

class LeapfrogIntegrator(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val dt: Float, // time step
    val field: ForceField
) {

    // Call this to update position and velocity
    fun step() {
        // Get force components from the ForceField
        val force = field.getBodyForce(x, y)
        val fx = force.first
        val fy = force.second

        // Half step update for velocity
        vx += fx * dt / 2
        vy += fy * dt / 2

        // Update positions
        x += vx * dt
        y += vy * dt

        // Another half step for velocity
        vx += fx * dt / 2
        vy += fy * dt / 2
    }
}
