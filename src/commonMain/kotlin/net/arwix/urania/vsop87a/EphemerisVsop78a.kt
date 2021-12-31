package net.arwix.urania.vsop87a

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.yield
import net.arwix.urania.core.calendar.JT
import net.arwix.urania.core.math.vector.RectangularVector
import net.arwix.urania.core.math.vector.Vector
import kotlin.math.cos

suspend fun getEphemerisVsop87ASingleThread(data: VsopRectangularData, jT: JT): Vector {

    val jT10 = jT / 10.0
    val X0 = accumulate(data.X0, jT10)
    val X1 = accumulate(data.X1, jT10) * jT10
    val X2 = accumulate(data.X2, jT10) * jT10 * jT10
    val X3 = accumulate(data.X3, jT10) * jT10 * jT10 * jT10
    val X4 = accumulate(data.X4, jT10) * jT10 * jT10 * jT10 * jT10
    val X5 = accumulate(data.X5, jT10) * jT10 * jT10 * jT10 * jT10 * jT10

    val x = X0 + X1 + X2 + X3 + X4 + X5
    yield()

    val Y0 = accumulate(data.Y0, jT10)
    val Y1 = accumulate(data.Y1, jT10) * jT10
    val Y2 = accumulate(data.Y2, jT10) * jT10 * jT10
    val Y3 = accumulate(data.Y3, jT10) * jT10 * jT10 * jT10
    val Y4 = accumulate(data.Y4, jT10) * jT10 * jT10 * jT10 * jT10
    val Y5 = accumulate(data.Y5, jT10) * jT10 * jT10 * jT10 * jT10 * jT10

    val y = Y0 + Y1 + Y2 + Y3 + Y4 + Y5
    yield()

    val Z0 = accumulate(data.Z0, jT10)
    val Z1 = accumulate(data.Z1, jT10) * jT10
    val Z2 = accumulate(data.Z2, jT10) * jT10 * jT10
    val Z3 = accumulate(data.Z3, jT10) * jT10 * jT10 * jT10
    val Z4 = accumulate(data.Z4, jT10) * jT10 * jT10 * jT10 * jT10
    val Z5 = accumulate(data.Z5, jT10) * jT10 * jT10 * jT10 * jT10 * jT10

    val z = Z0 + Z1 + Z2 + Z3 + Z4 + Z5

    return RectangularVector(x, y, z)
}

suspend fun getEphemerisVsop87A(data: VsopRectangularData, jT: JT): Vector = coroutineScope {

    val jT10 = jT / 10.0
    val X0 = async(Dispatchers.Default) { accumulate(data.X0, jT10) }
    val X1 = async(Dispatchers.Default) { accumulate(data.X1, jT10) * jT10 }
    val X2 = async(Dispatchers.Default) { accumulate(data.X2, jT10) * jT10 * jT10 }
    val X3 = async(Dispatchers.Default) { accumulate(data.X3, jT10) * jT10 * jT10 * jT10 }
    val X4 = async(Dispatchers.Default) { accumulate(data.X4, jT10) * jT10 * jT10 * jT10 * jT10 }
    val X5 = async(Dispatchers.Default) { accumulate(data.X5, jT10) * jT10 * jT10 * jT10 * jT10 * jT10 }

    val x = X0.await() + X1.await() + X2.await() + X3.await() + X4.await() + X5.await()
    yield()

    val Y0 = async(Dispatchers.Default) { accumulate(data.Y0, jT10) }
    val Y1 = async(Dispatchers.Default) { accumulate(data.Y1, jT10) * jT10 }
    val Y2 = async(Dispatchers.Default) { accumulate(data.Y2, jT10) * jT10 * jT10 }
    val Y3 = async(Dispatchers.Default) { accumulate(data.Y3, jT10) * jT10 * jT10 * jT10 }
    val Y4 = async(Dispatchers.Default) { accumulate(data.Y4, jT10) * jT10 * jT10 * jT10 * jT10 }
    val Y5 = async(Dispatchers.Default) { accumulate(data.Y5, jT10) * jT10 * jT10 * jT10 * jT10 * jT10 }

    val y = Y0.await() + Y1.await() + Y2.await() + Y3.await() + Y4.await() + Y5.await()
    yield()

    val Z0 = async(Dispatchers.Default) { accumulate(data.Z0, jT10) }
    val Z1 = async(Dispatchers.Default) { accumulate(data.Z1, jT10) * jT10 }
    val Z2 = async(Dispatchers.Default) { accumulate(data.Z2, jT10) * jT10 * jT10 }
    val Z3 = async(Dispatchers.Default) { accumulate(data.Z3, jT10) * jT10 * jT10 * jT10 }
    val Z4 = async(Dispatchers.Default) { accumulate(data.Z4, jT10) * jT10 * jT10 * jT10 * jT10 }
    val Z5 = async(Dispatchers.Default) { accumulate(data.Z5, jT10) * jT10 * jT10 * jT10 * jT10 * jT10 }

    val z = Z0.await() + Z1.await() + Z2.await() + Z3.await() + Z4.await() + Z5.await()

    RectangularVector(x, y, z)
}


private fun accumulate(data: Array<DoubleArray>, jT10: Double) = data.fold(0.0) { acc, element ->
    acc + element[0] * cos(element[1] + element[2] * jT10)
}