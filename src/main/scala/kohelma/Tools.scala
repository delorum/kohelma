package kohelma

import net.sourceforge.javaocr.scanner.PixelImage
import javax.imageio.ImageIO
import java.io.FileInputStream

object MathTools {
  /**
   * Возвращает длину вектора (корень из суммы квадратов значений элементов вектора)
   * Дополнительно впилена проверка, чтобы функция никогда не возвращала ноль (во избежание проблем при делении)
   */
  def vectorLength(vec:Array[Float]):Float = {
    val vec_len = math.sqrt(vec.reduceLeft((len, elem) => len + elem*elem)).toFloat
    if(vec_len < 1E-30f) 1E-30f else vec_len
  }

  /**
   * Возвращает нормированный вектор: все значения лежат в диапазоне (0; 1)
   */
  def normalizedVector(vec:Array[Float]):Array[Float] = {
    val normalization_factor = 1f/(vectorLength(vec)).toFloat
    vec.map(_ * normalization_factor)
  }

  /**
   * Нормирует вектор, так чтобы все значения лежали в диапазоне (-1; 1)
   * То есть вектор сначала делится на норму (тогда все значения будут лежать в диапазоне (0; 1)),
   * потом все значения умножаются на 2 и из них вычитается 1:
   * 2*(0; 1) - 1 = (-1; 1)
   */
  def bipolarNormalizedVector(vec:Array[Float]) = {
    normalizedVector(vec).map(2 * _ - 1)
  }
}

object ImageTools {
  def filteredGrayscalePixelImageFromFile(image_filename: String) = {
    new PixelImage(ImageIO.read(new FileInputStream(image_filename))) {
      toGrayScale(true)
      filter()
    }
  }

  def bipolarFilteredGrayscalePixelsFromImage(image_filename: String) = {
    val pi = filteredGrayscalePixelImageFromFile(image_filename)
    MathTools.bipolarNormalizedVector(pi.pixels.map(_.toFloat))
  }

  def normalizedGrayscalePixelsFromImage(image_filename: String) = {
    val pi = filteredGrayscalePixelImageFromFile(image_filename)
    MathTools.normalizedVector(pi.pixels.map(_.toFloat))
  }
}