package net.arwix.urania.vsop87.a

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.yield
import net.arwix.urania.core.annotation.Ecliptic
import net.arwix.urania.core.annotation.Heliocentric
import net.arwix.urania.core.annotation.J2000
import net.arwix.urania.core.calendar.JT
import net.arwix.urania.core.calendar.jT
import net.arwix.urania.core.ephemeris.*
import net.arwix.urania.core.math.JULIAN_DAYS_PER_CENTURY
import net.arwix.urania.core.math.LIGHT_TIME_DAYS_PER_AU
import net.arwix.urania.core.math.vector.RectangularVector
import net.arwix.urania.core.math.vector.Vector
import net.arwix.urania.core.spherical
import net.arwix.urania.core.transformation.nutation.Nutation
import net.arwix.urania.core.transformation.nutation.createElements
import net.arwix.urania.core.transformation.obliquity.Obliquity
import net.arwix.urania.core.transformation.obliquity.createElements
import net.arwix.urania.core.transformation.precession.Precession
import net.arwix.urania.core.transformation.precession.createElements
import kotlin.math.cos

class Vsop87AEphemerisFactory(
    private val jT0: JT,
    private val earthEphemeris: Ephemeris = createHeliocentricEclipticJ2000Ephemeris(Vsop87AEarthRectangularData),
) {

    private val obliquity by lazy { Obliquity.IAU2006.createElements(JT.J2000) }
    private val precession by lazy { Precession.IAU2006.createElements(jT0) }
    private val nutation by lazy { Nutation.IAU2006.createElements(jT0, Obliquity.IAU2006) }

    init {
        if (earthEphemeris.metadata.plane != Plane.Ecliptic ||
            earthEphemeris.metadata.epoch != Epoch.J2000 ||
            earthEphemeris.metadata.orbit != Orbit.Heliocentric
        ) throw IllegalArgumentException()
    }

    fun createGeocentricEquatorialEphemeris(heliocentricEclipticJ2000Ephemeris: Ephemeris, epoch: Epoch): Ephemeris {
        if (heliocentricEclipticJ2000Ephemeris.metadata.plane != Plane.Ecliptic ||
            heliocentricEclipticJ2000Ephemeris.metadata.epoch != Epoch.J2000 ||
            heliocentricEclipticJ2000Ephemeris.metadata.orbit != Orbit.Heliocentric
        ) throw IllegalArgumentException()

        val currentMetadata = Metadata(
            orbit = Orbit.Geocentric,
            plane = Plane.Equatorial,
            epoch = epoch
        )
        return when (epoch) {
            Epoch.J2000 -> object : Ephemeris {
                override val metadata: Metadata = currentMetadata

                override suspend fun invoke(jT: JT): Vector = coroutineScope {
                    val body = async(Dispatchers.Default) { heliocentricEclipticJ2000Ephemeris(jT) }
                    val earth = async(Dispatchers.Default) { earthEphemeris(jT) }
                    val geoBody = body.await() - earth.await()
                    val oneWayDown = geoBody.spherical.r * LIGHT_TIME_DAYS_PER_AU

                    (heliocentricEclipticJ2000Ephemeris(jT - (oneWayDown / JULIAN_DAYS_PER_CENTURY).jT) - earth.await())
                        .let { obliquity.rotatePlane(it, Plane.Equatorial) }
                }
            }
            Epoch.Apparent -> {
                object : Ephemeris {
                    override val metadata: Metadata = currentMetadata

                    override suspend fun invoke(jT: JT): Vector = coroutineScope {
                        val body = async(Dispatchers.Default) { heliocentricEclipticJ2000Ephemeris(jT) }
                        val earth = async(Dispatchers.Default) { earthEphemeris(jT) }
                        val geoBody = body.await() - earth.await()
                        val oneWayDown = geoBody.spherical.r * LIGHT_TIME_DAYS_PER_AU

                        geoBody
                            .let {
                                val earthVelocity = async(Dispatchers.Default) {
                                    earthEphemeris.getVelocity(
                                        earth.await(),
                                        jT
                                    )
                                }
                                val bodyVelocity = async(Dispatchers.Default) {
                                    heliocentricEclipticJ2000Ephemeris.getVelocity(
                                        body.await(),
                                        jT,
                                    )
                                }
                                it - (bodyVelocity.await() - earthVelocity.await()) * oneWayDown
                            }
                            .let { obliquity.rotatePlane(it, Plane.Equatorial) }
                            .let { precession.changeEpoch(it, Epoch.Apparent) }
                            .let { nutation.apply(it, Plane.Equatorial) }
                    }
                }
            }
        }
    }

    companion object {
        fun createHeliocentricEclipticJ2000Ephemeris(data: VsopRectangularData): Ephemeris {
            return object : Ephemeris {
                override val metadata: Metadata
                    get() = defaultMetadata

                override suspend fun invoke(jT: JT): Vector {
                    return getVectorVsop87A(data, jT)
                }
            }
        }

        fun createSunHeliocentricEclipticJ2000Ephemeris(): Ephemeris {
            return object : Ephemeris {
                override val metadata: Metadata
                    get() = defaultMetadata

                override suspend fun invoke(jT: JT): Vector {
                    return RectangularVector.Zero
                }
            }
        }
    }
}

@Heliocentric
@Ecliptic
@J2000
suspend fun getVectorVsop87A(data: VsopRectangularData, jT: JT): Vector = coroutineScope {

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

private val defaultMetadata = Metadata(
    orbit = Orbit.Heliocentric,
    plane = Plane.Ecliptic,
    epoch = Epoch.J2000
)

@Suppress("NOTHING_TO_INLINE")
private inline fun accumulate(data: Array<DoubleArray>, jT10: Double) = data.fold(0.0) { acc, element ->
    acc + element[0] * cos(element[1] + element[2] * jT10)
}