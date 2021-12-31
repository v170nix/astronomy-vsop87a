@file:Suppress("PropertyName")

package net.arwix.urania.vsop87a

abstract class VsopRectangularData {
    internal abstract val X0: Array<DoubleArray>
    internal abstract val X1: Array<DoubleArray>
    internal abstract val X2: Array<DoubleArray>
    internal abstract val X3: Array<DoubleArray>
    internal abstract val X4: Array<DoubleArray>
    internal abstract val X5: Array<DoubleArray>

    internal abstract val Y0: Array<DoubleArray>
    internal abstract val Y1: Array<DoubleArray>
    internal abstract val Y2: Array<DoubleArray>
    internal abstract val Y3: Array<DoubleArray>
    internal abstract val Y4: Array<DoubleArray>
    internal abstract val Y5: Array<DoubleArray>

    internal abstract val Z0: Array<DoubleArray>
    internal abstract val Z1: Array<DoubleArray>
    internal abstract val Z2: Array<DoubleArray>
    internal abstract val Z3: Array<DoubleArray>
    internal abstract val Z4: Array<DoubleArray>
    internal abstract val Z5: Array<DoubleArray>
}