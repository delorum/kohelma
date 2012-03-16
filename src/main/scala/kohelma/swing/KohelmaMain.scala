package kohelma.swing

import javax.swing.JFrame
import java.awt.{GraphicsEnvironment, Point}

object KohelmaMain extends App {
  var frame: JFrame = new JFrame("Handwriting OCR")
  var gui: KohelmaGUI = new KohelmaGUI
  frame.setContentPane(gui.mainPanel)


  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)


  frame.setSize(800, 600)
  var centerPoint: Point = GraphicsEnvironment.getLocalGraphicsEnvironment.getCenterPoint
  frame.setLocation(centerPoint.getX.asInstanceOf[Int] - 400, centerPoint.getY.asInstanceOf[Int] - 300)


  frame.setVisible(true)
}
