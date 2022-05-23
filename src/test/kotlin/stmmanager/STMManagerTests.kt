package stmmanager

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class STMManagerTests {
    private val numberOfThreads = 4
    val executor: ExecutorService = Executors.newFixedThreadPool(numberOfThreads)

    // Tests check that working in one thread works as expected
    @Nested
    @DisplayName("One thread tests")
    inner class TestsInOneThread {

        // Swap operation of two values in one thread
        @Test
        fun `swap of values should work correctly in one thread`() {
            // given
            val a = TVar(BigDecimal(1.07))
            val b = TVar(BigDecimal(2.00))

            // when
            atomic {
                val t = a.read()
                b.write(t)
            }

            // then
            atomic {
                Assertions.assertEquals(a.read(), b.read())
            }
        }
    }

    @Nested
    @DisplayName("Multi threads tests")
    inner class TestsWithMultiThreads {
        // Swap operation of two values in multi thread situation
        @Test
        fun `should be seme value after many swap operations`() {
            // given
            val a = TVar(1)
            val b = TVar(2)

            // when
            for (i in 0..10) {
                (0 until numberOfThreads).map {
                    executor.submit(Callable {
                        atomic {
                            val t = a.read()
                            b.write(t)
                        }
                        println("a to b")
                        atomic {
                            val t = b.read()
                            a.write(t)
                        }
                        println("b to a")
                    })
                }.forEach { it.get() }
            }

            // then
            atomic {
                Assertions.assertEquals(a.read(), b.read())
            }

            executor.shutdown()
        }
    }
}
