package kohelma.swing

import javax.swing.{JFileChooser, JFrame}
import javax.swing.filechooser.FileFilter
import java.awt.event.{FocusEvent, FocusAdapter, ActionEvent, ActionListener}
import javax.imageio.ImageIO
import java.awt.{Color, GraphicsEnvironment, Point}
import kohelma.{MathTools, KohelmaImages, Elman}
import java.io.{FileOutputStream, File}
import net.sourceforge.javaocr.scanner.{DocumentScanner, DocumentScannerListenerAdaptor, PixelImage}
import java.awt.geom.AffineTransform
import java.awt.image.{AffineTransformOp, BufferedImage}
import collection.mutable.{HashMap, ArrayBuffer}

object KohelmaMain extends App {
  var frame: JFrame = new JFrame("Handwriting OCR")
  var gui: KohelmaGUI = new KohelmaGUI
  frame.setContentPane(gui.mainPanel)
  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  frame.setSize(1000, 600)
  var centerPoint: Point = GraphicsEnvironment.getLocalGraphicsEnvironment.getCenterPoint
  frame.setLocation(centerPoint.getX.asInstanceOf[Int] - 400, centerPoint.getY.asInstanceOf[Int] - 300)

  val chooser = new JFileChooser()
  chooser.setCurrentDirectory(new java.io.File("."))

  gui.training_buttons_loadNet.addActionListener(new ActionListener() {
    def actionPerformed(e:ActionEvent) {
      chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES)
      chooser.setFileFilter(new FileFilter() {
        def accept(file:File) = file.getAbsolutePath.endsWith(".elman")
        def getDescription = ""
      })
      if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        elman = Elman(chooser.getSelectedFile.getAbsolutePath)
        gui.training_labels_netPath.setText(chooser.getSelectedFile.getAbsolutePath)
        gui.training_labels_inputNeurons.setText(""+elman.input_neurons)
        gui.training_textfields_hiddenNeurons.setText(""+elman.hidden_neurons)
        gui.training_labels_outputNeurons.setText(""+elman.output_neurons)
      }
    }
  })

  gui.training_buttons_newNet.addActionListener(new ActionListener {
    def actionPerformed(e: ActionEvent) {
      elman = Elman(gui.training_labels_inputNeurons.getText.toInt, gui.training_textfields_hiddenNeurons.getText.toInt, gui.training_labels_outputNeurons.getText.toInt)
      gui.training_labels_netPath.setText("Новая сеть")
    }
  })

  gui.training_texfields_imageWidth.addActionListener(new ActionListener {
    def actionPerformed(e: ActionEvent) {
      kohelma_images = new KohelmaImages(gui.training_texfields_imageWidth.getText.toInt, kohelma_images.image_height)
      gui.training_comboboxes_setsList.removeAllItems()
      gui.training_labels_inputNeurons.setText(""+(kohelma_images.image_width*kohelma_images.image_height))
      elman = Elman(gui.training_labels_inputNeurons.getText.toInt, gui.training_textfields_hiddenNeurons.getText.toInt, gui.training_labels_outputNeurons.getText.toInt)
      gui.training_labels_netPath.setText("Новая сеть")
    }
  })

  gui.training_texfields_imageWidth.addFocusListener(new FocusAdapter {
    override def focusLost(e:FocusEvent) {
      kohelma_images = new KohelmaImages(gui.training_texfields_imageWidth.getText.toInt, kohelma_images.image_height)
      gui.training_comboboxes_setsList.removeAllItems()
      gui.training_labels_inputNeurons.setText(""+(kohelma_images.image_width*kohelma_images.image_height))
      elman = Elman(gui.training_labels_inputNeurons.getText.toInt, gui.training_textfields_hiddenNeurons.getText.toInt, gui.training_labels_outputNeurons.getText.toInt)
      gui.training_labels_netPath.setText("Новая сеть")
    }
  })

  gui.training_texfields_imageHeight.addActionListener(new ActionListener {
      def actionPerformed(e: ActionEvent) {
        kohelma_images = new KohelmaImages(kohelma_images.image_width, gui.training_texfields_imageHeight.getText.toInt)
        gui.training_comboboxes_setsList.removeAllItems()
        gui.training_labels_inputNeurons.setText(""+(kohelma_images.image_width*kohelma_images.image_height))
        elman = Elman(gui.training_labels_inputNeurons.getText.toInt, gui.training_textfields_hiddenNeurons.getText.toInt, gui.training_labels_outputNeurons.getText.toInt)
        gui.training_labels_netPath.setText("Новая сеть")
      }
    })

  gui.training_texfields_imageHeight.addFocusListener(new FocusAdapter {
    override def focusLost(e:FocusEvent) {
      kohelma_images = new KohelmaImages(kohelma_images.image_width, gui.training_texfields_imageHeight.getText.toInt)
      gui.training_comboboxes_setsList.removeAllItems()
      gui.training_labels_inputNeurons.setText(""+(kohelma_images.image_width*kohelma_images.image_height))
      elman = Elman(gui.training_labels_inputNeurons.getText.toInt, gui.training_textfields_hiddenNeurons.getText.toInt, gui.training_labels_outputNeurons.getText.toInt)
      gui.training_labels_netPath.setText("Новая сеть")
    }
  })

  gui.training_textfields_hiddenNeurons.addActionListener(new ActionListener {
      def actionPerformed(e: ActionEvent) {
        elman = Elman(gui.training_labels_inputNeurons.getText.toInt, gui.training_textfields_hiddenNeurons.getText.toInt, gui.training_labels_outputNeurons.getText.toInt)
        gui.training_labels_netPath.setText("Новая сеть")
      }
    })

  gui.training_textfields_hiddenNeurons.addFocusListener(new FocusAdapter {
    override def focusLost(e:FocusEvent) {
      elman = Elman(gui.training_labels_inputNeurons.getText.toInt, gui.training_textfields_hiddenNeurons.getText.toInt, gui.training_labels_outputNeurons.getText.toInt)
      gui.training_labels_netPath.setText("Новая сеть")
    }
  })

  private def makeObj(item:String):Object = {
    new Object() {override def toString:String = item}
  }
  gui.training_buttons_addSet.addActionListener(new ActionListener {
    def actionPerformed(e: ActionEvent) {
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
      chooser.setFileFilter(null)
      if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        kohelma_images.addSet(chooser.getSelectedFile.getAbsolutePath)
        gui.training_comboboxes_setsList.addItem(makeObj(chooser.getSelectedFile.getAbsolutePath))
        gui.training_labels_outputNeurons.setText(""+kohelma_images.characterToOutput.keys.size)
        if(kohelma_images.characterToOutput.keys.size != elman.output_neurons) {
          elman = Elman(gui.training_labels_inputNeurons.getText.toInt, gui.training_textfields_hiddenNeurons.getText.toInt, gui.training_labels_outputNeurons.getText.toInt)
          gui.training_labels_netPath.setText("Новая сеть")
        }
      }
    }
  })

  gui.training_buttons_removeSet.addActionListener(new ActionListener {
    def actionPerformed(e: ActionEvent) {
      kohelma_images.removeSet(gui.training_comboboxes_setsList.getSelectedItem.toString)
      gui.training_comboboxes_setsList.removeItem(gui.training_comboboxes_setsList.getSelectedItem)
    }
  })

  gui.training_buttons_startStopTraining.addActionListener(new ActionListener {
    def actionPerformed(e: ActionEvent) {
      if(elman.isTrainingStarted) {
        elman.stopTraining()
        gui.training_buttons_startStopTraining.setText("Начать")
      } else if(kohelma_images.sets.keys.size > 0) {
        gui.training_buttons_startStopTraining.setText("Остановить")
        scala.concurrent.ops.spawn {
          kohelma_images.trainElman(elman, gui.training_textfields_learnRate.getText.toDouble, gui.training_textfields_trainingReps.getText.toInt, gui.training_textfields_errorThreshold.getText.toDouble, reporter = {message => gui.training_labels_trainingStatus.setText(message)})
          gui.training_buttons_startStopTraining.setText("Начать")
        }
      }
    }
  })

  gui.training_buttons_saveNet.addActionListener(new ActionListener {
    def actionPerformed(e: ActionEvent) {
      chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES)
      chooser.setFileFilter(new FileFilter() {
        def accept(file:File) = file.getAbsolutePath.endsWith(".elman")
        def getDescription = ""
      })
      if(chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
        elman.outputToCharacter = kohelma_images.outputToCharacter
        elman.save(chooser.getSelectedFile.getAbsolutePath)
        gui.training_labels_netPath.setText(chooser.getSelectedFile.getAbsolutePath)
      }
    }
  })

  private val document_scanner = new DocumentScanner
  private def scaledImage(image:BufferedImage, width:Int, height:Int):BufferedImage = {
      def _scale(image:BufferedImage, dimension:Int, desired_dimension:Int) = {
        val tx = new AffineTransform
        val scale_factor = desired_dimension.toDouble/dimension.toDouble
        tx.scale(scale_factor, scale_factor)
        val op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR)
        op.filter(image, null)
      }

      val width_scaled = if(image.getWidth > width) _scale(image, image.getWidth, width) else image
      if(width_scaled.getHeight > height) _scale(width_scaled, width_scaled.getHeight, height) else width_scaled
  }
  gui.recognition_buttons_loadImage.addActionListener(new ActionListener {
    def actionPerformed(e: ActionEvent) {
      chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES)
      chooser.setFileFilter(new FileFilter() {
        def accept(file:File) = file.getAbsolutePath.endsWith(".png") || file.getAbsolutePath.endsWith(".jpg")
        def getDescription = ""
      })
      if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        val result = new StringBuilder
        val img = ImageIO.read(new File(chooser.getSelectedFile.getAbsolutePath))
        val pixel_image = new PixelImage(img)
        pixel_image.toGrayScale(true)
        pixel_image.filter()
        var current_row_y1 = 0
        var current_row_y2 = 0
        document_scanner.scan(pixel_image, new DocumentScannerListenerAdaptor {
          override def processChar(pixel_image:PixelImage, x1:Int, y1:Int, x2:Int, y2:Int, rowY1:Int, rowY2:Int) {
            if(rowY1 > current_row_y1) {
              result += '\n'
              current_row_y1 = rowY1
            }
            val areaW = x2 - x1
            val areaH = y2 - y1

            val normfac = new Array[Double](1)
            val synth = new Array[Double](1)

            //Extract the character
            val character_image = scaledImage(img.getSubimage(x1, y1, areaW, areaH), kohelma_images.image_width, kohelma_images.image_height)
            val normalized_image = new BufferedImage(kohelma_images.image_width, kohelma_images.image_height, BufferedImage.TYPE_INT_RGB)
            val g = normalized_image.createGraphics
            g.setColor(Color.WHITE)
            g.fillRect(0, 0, kohelma_images.image_width, kohelma_images.image_height)

            //Center scaled image on new canvas
            val x_offset = (kohelma_images.image_width - character_image.getWidth) / 2
            val y_offset = (kohelma_images.image_height - character_image.getHeight) / 2

            g.drawImage(character_image, x_offset, y_offset, null)
            g.dispose()

            val character_pixels = MathTools.normalizedVector(new PixelImage(normalized_image) {
              toGrayScale(true)
              filter()
            }.pixels.map(_.toFloat)).map(_.toDouble)

            val outputs = elman.outputs(character_pixels)
            val outputs_max = outputs.max
            val (_, answer_index) = (outputs.zipWithIndex.find {case (output, index) => output == outputs_max}).get

            val character = kohelma_images.outputToCharacter(answer_index)
            result += character
          }
        }, 0, 0, img.getWidth, img.getHeight)
        gui.recognition_textpanes_text.setText(result.toString())
        gui.recognition_labels_imagePath.setText(chooser.getSelectedFile.getAbsolutePath)
      }
    }
  })

  gui.recognition_buttons_saveText.addActionListener(new ActionListener {
    def actionPerformed(e: ActionEvent) {
      chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES)
      chooser.setFileFilter(new FileFilter() {
        def accept(file:File) = file.getAbsolutePath.endsWith(".txt")
        def getDescription = ""
      })
      if(chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
        val fos = new FileOutputStream(chooser.getSelectedFile)
        fos.write(gui.recognition_textpanes_text.getText.getBytes)
        fos.flush()
        fos.close()
      }
    }
  })

  // -------------- Networks Panel -----------------------
  
  gui.networks_buttons_addNet.addActionListener(new ActionListener {
    def actionPerformed(e: ActionEvent) {
      chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES)
      chooser.setFileFilter(new FileFilter() {
        def accept(file:File) = file.getAbsolutePath.endsWith(".elman")
        def getDescription = ""
      })
      if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        networks += (chooser.getSelectedFile.getAbsolutePath -> Elman(chooser.getSelectedFile.getAbsolutePath))
        gui.networks_comboboxes_netList.addItem(makeObj(chooser.getSelectedFile.getAbsolutePath))
      }
    }
  })

  gui.networks_buttons_removeNet.addActionListener(new ActionListener {
    def actionPerformed(e: ActionEvent) {
      val selected = gui.networks_comboboxes_netList.getSelectedItem
      if(selected != null) {
        networks -= (selected.toString)
        gui.networks_comboboxes_netList.removeItem(selected)
      }
    }
  })

  gui.networks_buttons_loadImage.addActionListener(new ActionListener {
    def actionPerformed(e: ActionEvent) {
      chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES)
      chooser.setFileFilter(new FileFilter() {
        def accept(file:File) = file.getAbsolutePath.endsWith(".png") || file.getAbsolutePath.endsWith(".jpg")
        def getDescription = ""
      })
      if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        val result = new StringBuilder
        val img = ImageIO.read(new File(chooser.getSelectedFile.getAbsolutePath))
        val pixel_image = new PixelImage(img)
        pixel_image.toGrayScale(true)
        pixel_image.filter()
        var current_row_y1 = 0
        var current_row_y2 = 0
        document_scanner.scan(pixel_image, new DocumentScannerListenerAdaptor {
          override def processChar(pixel_image:PixelImage, x1:Int, y1:Int, x2:Int, y2:Int, rowY1:Int, rowY2:Int) {
            if(rowY1 > current_row_y1) {
              result += '\n'
              current_row_y1 = rowY1
            }
            val areaW = x2 - x1
            val areaH = y2 - y1

            val normfac = new Array[Double](1)
            val synth = new Array[Double](1)

            //Extract the character
            val character_image = scaledImage(img.getSubimage(x1, y1, areaW, areaH), kohelma_images.image_width, kohelma_images.image_height)
            val normalized_image = new BufferedImage(kohelma_images.image_width, kohelma_images.image_height, BufferedImage.TYPE_INT_RGB)
            val g = normalized_image.createGraphics
            g.setColor(Color.WHITE)
            g.fillRect(0, 0, kohelma_images.image_width, kohelma_images.image_height)

            //Center scaled image on new canvas
            val x_offset = (kohelma_images.image_width - character_image.getWidth) / 2
            val y_offset = (kohelma_images.image_height - character_image.getHeight) / 2

            g.drawImage(character_image, x_offset, y_offset, null)
            g.dispose()

            val character_pixels = MathTools.normalizedVector(new PixelImage(normalized_image) {
              toGrayScale(true)
              filter()
            }.pixels.map(_.toFloat)).map(_.toDouble)

            val answers = networks.values.toList.zipWithIndex.map{
              case (net, net_number) =>
                println("network "+net_number)
                val outputs = net.outputs(character_pixels)
                println(outputs.mkString("", " : ", ""))
                val outputs_max = outputs.max

                def avg(arr:Array[Double]) = arr.sum/arr.length
                val average_without_max = avg(outputs.filterNot(_ == outputs_max))

                val diff = outputs_max - average_without_max
                val (_, outputs_max_index) = (outputs.zipWithIndex.find {case (output, index) => output == outputs_max}).get
                val character = net.outputToCharacter(outputs_max_index)
                (character, diff)
            }.sortWith {
              case ((char1, diff1), (char2, diff2)) => diff1 > diff2
            }
            if(!answers.isEmpty) {
              val (char, _) = answers.head
              //print(char)
              result += char
            }
          }
        }, 0, 0, img.getWidth, img.getHeight)
        println()
        gui.networks_textpanes_text.setText(result.toString())
        //gui.networks_labels_imagePath.setText(chooser.getSelectedFile.getAbsolutePath)
      }
    }
  })

  gui.networks_buttons_saveText.addActionListener(new ActionListener {
    def actionPerformed(e: ActionEvent) {
      chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES)
      chooser.setFileFilter(new FileFilter() {
        def accept(file:File) = file.getAbsolutePath.endsWith(".txt")
        def getDescription = ""
      })
      if(chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
        val fos = new FileOutputStream(chooser.getSelectedFile)
        fos.write(gui.networks_textpanes_text.getText.getBytes)
        fos.flush()
        fos.close()
      }
    }
  })

  frame.setVisible(true)
  
  private var elman:Elman = Elman(gui.training_labels_inputNeurons.getText.toInt, gui.training_textfields_hiddenNeurons.getText.toInt, gui.training_labels_outputNeurons.getText.toInt)
  private var kohelma_images = new KohelmaImages(gui.training_texfields_imageWidth.getText.toInt, gui.training_texfields_imageHeight.getText.toInt)
  private val networks = HashMap[String, Elman]()
}
