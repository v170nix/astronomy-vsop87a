package net.arwix.urania.vsop87.a

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import net.arwix.urania.core.calendar.toJT
import net.arwix.urania.core.ephemeris.Epoch
import net.arwix.urania.core.math.angle.toDec
import net.arwix.urania.core.math.angle.toRA
import net.arwix.urania.core.spherical
import kotlin.test.Test
import kotlin.test.assertEquals

class EphemerisVsop87aTest {

    @Test
    fun createGeocentricEquatorialEphemerisTest() {
        val time = LocalDate(2022, 1, 11).atStartOfDayIn(TimeZone.UTC).toJT()
        val factory = Vsop87AEphemerisFactory(time)
        val sunEphemeris = Vsop87AEphemerisFactory.createSunHeliocentricEclipticJ2000Ephemeris()
        val sunJ2000Ephemeris = factory.createGeocentricEquatorialEphemeris(sunEphemeris, epoch = Epoch.J2000)
        val sunApparentEphemeris = factory.createGeocentricEquatorialEphemeris(sunEphemeris, epoch = Epoch.Apparent)

        val marsEphemeris = Vsop87AEphemerisFactory.createHeliocentricEclipticJ2000Ephemeris(Vsop87AMarsRectangularData)
        val marsJ2000Ephemeris = factory.createGeocentricEquatorialEphemeris(marsEphemeris, epoch = Epoch.J2000)
        val marsApparentEphemeris = factory.createGeocentricEquatorialEphemeris(marsEphemeris, epoch = Epoch.Apparent)

        val uranusEphemeris = Vsop87AEphemerisFactory.createHeliocentricEclipticJ2000Ephemeris(Vsop87AUranusRectangularData)
        val uranusJ2000Ephemeris = factory.createGeocentricEquatorialEphemeris(uranusEphemeris, epoch = Epoch.J2000)
        val uranusApparentEphemeris = factory.createGeocentricEquatorialEphemeris(uranusEphemeris, epoch = Epoch.Apparent)

        runTest {
            sunJ2000Ephemeris(time).let {
                assertEquals("19h 28m 21.97s", it.spherical.phi.toRA().toString())
                assertEquals("-21deg 53m 2.4s", it.spherical.theta.toDec().toString())
            }
            sunApparentEphemeris(time).let {
                assertEquals("19h 29m 38.22s", it.spherical.phi.toRA().toString())
                assertEquals("-21deg 50m 24.6s", it.spherical.theta.toDec().toString())
            }

            marsJ2000Ephemeris(time).let {
                assertEquals("17h 16m 8.64s", it.spherical.phi.toRA().toString())
                assertEquals("-23deg 17m 53.5s", it.spherical.theta.toDec().toString())
            }
            marsApparentEphemeris(time).let {
                assertEquals("17h 17m 26.51s", it.spherical.phi.toRA().toString())
                assertEquals("-23deg 19m 18.2s", it.spherical.theta.toDec().toString())
            }

            uranusJ2000Ephemeris(time).let {
                assertEquals("2h 32m 59.9s", it.spherical.phi.toRA().toString())
                assertEquals("14deg 36m 2.7s", it.spherical.theta.toDec().toString())
            }
            uranusApparentEphemeris(time).let {
                assertEquals("2h 34m 11.92s", it.spherical.phi.toRA().toString())
                assertEquals("14deg 41m 49.2s", it.spherical.theta.toDec().toString())
            }
        }
    }

}