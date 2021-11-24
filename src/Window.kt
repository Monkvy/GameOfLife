import java.awt.Canvas
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JFrame
import kotlin.math.roundToInt


class Window(windowTitle: String, width: Int, height: Int) : JFrame(windowTitle) {
    private val canvas: Canvas = Canvas()

    private var fps: Double = 0.0
    private var dt: Double = 0.0

    private val resolution: Int = 20
    private val cols: Int = width / resolution
    private val rows: Int = height / resolution
    private var grid: MutableList<MutableList<Int>> = mutableListOf()

    private var running: Boolean = false
    private var nextFrame: Boolean = false

    init {
        // Setting up canvas & window
        setSize(width, height)
        defaultCloseOperation = EXIT_ON_CLOSE
        isResizable = false
        setLocationRelativeTo(null)
        isVisible = true

        canvas.preferredSize = Dimension(width, height)
        canvas.maximumSize = Dimension(width, height)
        canvas.minimumSize = Dimension(width, height)
        canvas.isFocusable = false
        addKeyListener(KeyEvents())
        canvas.addMouseListener(MouseEvents())

        add(canvas)
        pack()

        // Setting up grid
        for (r in 0 until cols) {
            val row = mutableListOf<Int>()

            for (c in 0 until rows) {
                row.add(0)
            }
            grid.add(row)
        }
    }

    // Key events
    inner class KeyEvents: KeyAdapter() {
        override fun keyPressed(e: KeyEvent) {
            if (e.keyCode == KeyEvent.VK_SPACE)
                running = !running
            if (e.keyCode == KeyEvent.VK_RIGHT)
                nextFrame = true
        }
    }

    // Mouse events
    inner class MouseEvents: MouseAdapter() {
        override fun mousePressed(e: MouseEvent) {
            if (!running) {
                val row = e.point.x / resolution
                val col = e.point.y / resolution

                grid[row][col] = if (grid[row][col] == 0) 1 else 0
            }
        }
    }

    // Render
    private fun render(g: Graphics) {
        g.color = Color.BLACK
        g.fillRect(0, 0, width, height)
        for (r in 0 until cols) {
            for (c in 0 until rows) {
                val x = r * resolution
                val y = c * resolution

                g.color = Color.WHITE
                if (grid[r][c] == 0) g.drawRect(x, y, resolution, resolution)
                else g.fillRect(x, y, resolution, resolution)
            }
        }
    }

    // Update
    private fun update() {
        // Updating Title
        title = "Running at ${fps.roundToInt()} FPS  (Running: $running)"

        // Game of life algorithm
        if (running || nextFrame) {
            nextFrame = false
            val next: MutableList<MutableList<Int>> = mutableListOf()
            for (r in 0 until cols) {
                val row = mutableListOf<Int>()
                for (c in 0 until rows) {
                    row.add(0)
                }
                next.add(row)
            }

            for (r in 0 until cols) {
                for (c in 0 until rows) {
                    val state: Int = grid[r][c]

                    if (!(r == 0 || r == cols - 1 || c == 0 || c == rows - 1)) {
                        var sum = 0

                        for (i in -1 until 2) {
                            for (j in -1 until 2) {
                                sum += grid[r + i][c + j]
                            }
                        }
                        sum -= state


                        if (state == 0 && sum == 3)
                            next[r][c] = 1
                        else if (state == 1 && (sum < 2 || sum > 3))
                            next[r][c] = 0
                        else
                            next[r][c] = state

                    }
                }
            }
            grid = next
        }
    }

    // MainLoop
    fun run() {
        while (true) {
            val lastTime = System.nanoTime()

            update()
            val bs =canvas.bufferStrategy

            if (bs == null)
                canvas.createBufferStrategy(3)
            else {
                val g = bs.drawGraphics
                g.clearRect(0, 0, width, height)
                render(g)
                bs.show()
                g.dispose()
            }
            fps = 1000000000.0 / (System.nanoTime() - lastTime)
            dt = 1 / fps
        }
    }

}