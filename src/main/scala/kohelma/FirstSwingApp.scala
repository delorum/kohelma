package kohelma

import scala.swing._
import scala.swing.event._
import java.awt.Color

object ReactiveSwingApp extends SimpleSwingApplication {
  def top = new MainFrame {
    centerOnScreen()
    title = "Reactive Swing App"
    val button = new Button {
      text = "Click me"
    }
    val label = new Label {
      text = "No button clicks registered"
    }
    contents = new BoxPanel(Orientation.Vertical) {
      contents += button
      contents += label
      border = Swing.EmptyBorder(30, 30, 30, 30)
    }
    listenTo(button)
    var nClicks = 0
    reactions += {
      case ButtonClicked(b) =>
        nClicks += 1
        label.text = "Number of button clicks: "+ nClicks
    }
  }
}

object TempConverter extends SimpleSwingApplication {
  def top = new MainFrame {
    title = "Celsius/Fahrenheit Converter"
    object celsius extends TextField { columns = 5 }
    object fahrenheit extends TextField { columns = 5 }
    contents = new FlowPanel {
      contents += celsius
      contents += new Label(" Celsius = ")
      contents += fahrenheit
      contents += new Label(" Fahrenheit")
      border = Swing.EmptyBorder(15, 10, 10, 10)
    }
    listenTo(celsius, fahrenheit)
    reactions += {
      case EditDone(`fahrenheit`) =>
        val f = fahrenheit.text.toInt
        val c = (f - 32) * 5 / 9
        celsius.text = c.toString
      case EditDone(`celsius`) =>
        val c = celsius.text.toInt
        val f = c * 9 / 5 + 32
        fahrenheit.text = f.toString
    }
  }
}

class BorderedButton(val button:Button) extends BoxPanel(Orientation.NoOrientation) {
  contents += button    
  border = Swing.EmptyBorder(10, 10, 10, 10)
}

object buttons_panel extends BoxPanel(Orientation.Horizontal) {
  object help_button extends Button("Справка")
  object settings_button extends Button("Настройки")
  object learning_button extends Button("Обучение")
  object recognition_button extends Button("Распознавание")

  contents ++= Seq(new BorderedButton(help_button),
                   new BorderedButton(settings_button),
                   new BorderedButton(learning_button),
                   new BorderedButton(recognition_button))
  border = Swing.EmptyBorder(10, 10, 10, 10)
}

object content_panel extends BoxPanel(Orientation.Vertical) {
  contents ++= Seq(help_panel, settings_panel, learning_panel, recognition_panel)
}

object help_panel extends BoxPanel(Orientation.Vertical) {
  contents += new EditorPane {
    editable = false
    text =
"""Handwriting OCR - программа для распознавания рукописного текста и дальнейшего
перевода рукописных символов в печатные.
Для перехода в нужный режим работы нажмите соответствующую клавишу в верхней части окна.

Режим "Настройки" позволяет загрузить предварительно обученную нейронную сеть, либо задать
параметры для обучения новой.

В режиме "Обучение" происходит тренировка новой нейронной сети на готовых наборах данных,
заданных в настройках.

Режим "Распознавание" позволяет использовать предварительно обученную сеть для распознавания
рукописного текста"""
  }
  border = Swing.EmptyBorder(10, 10, 10, 10)
}

object settings_panel extends BoxPanel(Orientation.Vertical) {
  object input_layer extends TextField { text = "5625"; columns = 5 }
  object hidden_layer extends TextField { text = "3"; columns = 5 }
  object output_layer extends TextField { text = "74"; columns = 5 }
  contents ++= Seq(
    new Label("""Сеть""", Swing.EmptyIcon, align = Alignment.Left),
    new Label("""Параметры""", Swing.EmptyIcon, align = Alignment.Left),
    new FlowPanel(alignment = FlowPanel.Alignment.Left)() {
      contents ++= Seq(new Label("""Входной слой:"""), input_layer)
    },
    new FlowPanel(alignment = FlowPanel.Alignment.Left)() {
      contents ++= Seq(new Label("""Скрытый слой:"""), hidden_layer)
    },
    new FlowPanel(alignment = FlowPanel.Alignment.Left)() {
      contents ++= Seq(new Label("""Выходной слой:"""), output_layer)
    },
    new FlowPanel(alignment = FlowPanel.Alignment.Left)() {
      minimumSize = new Dimension(20, 800)
    }
  )
  border = Swing.EmptyBorder(10, 10, 10, 10)
  visible = false
}
object learning_panel extends BoxPanel(Orientation.Vertical) {
  contents += new EditorPane {
    editable = false
    text =
"""Обучение."""
  }
  border = Swing.EmptyBorder(10, 10, 10, 10)
  visible = false
}
object recognition_panel extends BoxPanel(Orientation.Vertical) {
  contents += new EditorPane {
    editable = false
    text =
"""Распознавание."""
  }
  border = Swing.EmptyBorder(10, 10, 10, 10)
  visible = false
}

object Kohelma extends SimpleSwingApplication { 
  def top = new MainFrame {
    title = "Handwriting OCR"
    contents = new BoxPanel(Orientation.Vertical) {
      contents ++= Seq(buttons_panel, content_panel)
      listenTo(buttons_panel.help_button, buttons_panel.settings_button, buttons_panel.learning_button, buttons_panel.recognition_button)
      reactions += {
        case ButtonClicked(buttons_panel.help_button) =>
          content_panel.contents.foreach(_.visible = false)
          help_panel.visible = true
        case ButtonClicked(buttons_panel.settings_button) =>
          content_panel.contents.foreach(_.visible = false)
          settings_panel.visible = true
        case ButtonClicked(buttons_panel.learning_button) =>
          content_panel.contents.foreach(_.visible = false)
          learning_panel.visible = true
        case ButtonClicked(buttons_panel.recognition_button) =>
          content_panel.contents.foreach(_.visible = false)
          recognition_panel.visible = true
      }
      border = Swing.EmptyBorder(10, 10, 10, 10)
    }
    size = new Dimension(1024, 768)
    centerOnScreen()
  }
}
