package kohelma

import java.io.File
import collection.mutable.{HashMap, ArrayBuffer}

/**
 * Класс предоставляет интерфейс по загрузке наборов картинок и обучению новой сети Элмана на основе этих наборов
 *
 * @param image_width - ширина одной картинки
 * @param image_height - высота картинки
 */
class KohelmaImages(val image_width:Int = 75, val image_height:Int = 75) {
  private val _characters_to_output = HashMap[Char, (Array[Double], Int)]() // char -> (expected_array, expected_neuron_num)
  def charactersToOutput = _characters_to_output.toMap

  private val _output_to_characters = HashMap[Int, Char]() // neuron_num -> char
  def outputToCharacters = _output_to_characters.toMap

  private val _sets = HashMap[String, List[(String, Char, Array[Double])]]() // (directory -> List((image_name, char, image_array)))
  def sets = _sets.toMap

  /**
   * Функция добавляет новый набор картинок
   * @param directory - путь до папки с картинками. В папке должны лежать картинки и текстовый файл patterns, в котором перечисляются
   * символы. Имена картинок должны быть в формате char_<число>.png, они должны быть упорядочены также как и символы в файле patterns
   */
  def addSet(directory:String) {
    val characters:Array[Char] = (for(line <- scala.io.Source.fromFile(directory+(if(directory.endsWith("/")) "" else "/")+"patterns").getLines()) yield {
      line.toCharArray
    }).toArray.flatten

    _characters_to_output ++= (for {
      num <- 0 until characters.length
      char = characters(num)
    } yield (char -> (createExpected(characters.length, num), num))).toMap

    _output_to_characters ++= (for {
      num <- 0 until characters.length
      char = characters(num)
    } yield (num -> char)).toMap

    val training_filenames = new File(directory).list().filter(_ != "patterns").sortWith((a,b) =>
      (a.replaceAll("char_", "").replaceAll(".png", "")).toInt < (b.replaceAll("char_", "").replaceAll(".png", "")).toInt)

    val images = (
      for((filename, num) <- training_filenames.zipWithIndex) yield {
        (filename, characters(num), ImageTools.normalizedGrayscalePixelsFromImage(directory+"/"+filename).map(_.toDouble))
      }
    ).toList

    _sets += (directory -> images)
  }

  /**
   * Удаляет тренировочный набор из списка наборов
   *
   * @param directory - имя папки с тренировочным набором, который требуется удалить
   */
  def removeSet(directory:String) {
    _sets -= directory
  }

  /**
   * Функция создает массив отклика
   *
   * @param expected_length - длина массива
   * @param expected_num - номер ненулевого элемента массива
   * @return - массив чисел длины expected_length, где все элементы равны нулю, кроме элемента с номером expected_num, который
   * равен единице.
   */
  def createExpected(expected_length:Int, expected_num:Int) = {
    require(expected_num >= 0 && expected_num < expected_length)
    val arr = Array.fill(expected_length)(0.0)
    arr(expected_num) = 1.0
    arr
  }

  /**
   * Функция создает новую сеть Элмана и тренирует ее на загруженных наборах картинок.
   *
   * @param learn_rate - коэффициент обучения (величина коррекции весов за одну итерацию обучения)
   * @param context_size -  количество нейронов в контекстном и скрытом слоях
   * @param training_reps - количество повторений тренировочной сессии (предъявлений всех картинок из всех наборов)
   * @param error_threshold - порог ошибки, по достижении которой считаем сеть обученной и прерываем алгоритм обучения.
   * @param reporter - функция для передачи различной отладочной информации из алгоритма обучения в процессе его работы
   * @return обученную сеть Элмана
   */
  def trainNewElman(learn_rate:Double = 0.2, context_size:Int = 3, training_reps:Int = 100000000, error_threshold:Double = 0.09, reporter:String => Unit = {message => println(message)}):Elman = {
    val elman = new Elman(image_width*image_height, context_size, _characters_to_output.keys.size)
    trainElman(elman, learn_rate, training_reps, error_threshold, reporter)
  }

  /**
   * Функция создает новую сеть Элмана и тренирует ее на загруженных наборах картинок.
   *
   * @param elman - сеть Элмана, которую требуется обучить. Переданная в качестве параметра сеть Элмана должна соответствовать требованиям на количество
   * нейронов во входном слое, скрытом слое и в выходном слое
   * @param learn_rate - коэффициент обучения (величина коррекции весов за одну итерацию обучения)
   * @param training_reps - количество повторений тренировочной сессии (предъявлений всех картинок из всех наборов)
   * @param error_threshold - порог ошибки, по достижении которой считаем сеть обученной и прерываем алгоритм обучения.
   * @param reporter - функция для передачи различной отладочной информации из алгоритма обучения в процессе его работы
   * @return обученную сеть Элмана
   */
  def trainElman(elman:Elman, learn_rate:Double = 0.2, training_reps:Int = 100000000, error_threshold:Double = 0.09, reporter:String => Unit = {message => println(message)}):Elman = {
    require(elman.input_neurons == image_width*image_height)
    require(elman.output_neurons == _characters_to_output.keys.size)

    val training_set = (for {
      directory <- _sets.keys
      images_list = _sets(directory)
      images_list_length = images_list.length
      (image_name, char, image_array) <- images_list
      (expected_array, expected_neuron_num) = _characters_to_output(char)
    } yield {
      //println(directory+" "+image_name+" "+char+" "+expected_neuron_num)
      TrainingSet(image_array, expected_array)
    }).toArray

    elman.train(training_set, training_reps, error_threshold, learn_rate, reporter)
    elman
  }
}

/**
 * Программа тестирует работу класса KohelmaImages на реальных наборах картинок с рукописными символами
 */
object TestKohelmaImages extends App {
  val kohelma_images = new KohelmaImages()

  kohelma_images.addSet("train/numbers/javaocr")
  kohelma_images.addSet("train/numbers/vitaly")
  //kohelma_images.addSet("train/russian_alphabet/vitaly2")

  /*val elman =  kohelma_images.trainNewElman(training_reps = 1000000, error_threshold = 0.99)
  elman.save("alphabet.elman")*/

  val elman = Elman("numbers.elman")
  /*kohelma_images.trainElman(elman, training_reps = 1000000, error_threshold = 0.9933)
  elman.save("alphabet.elman")*/

  // тестируем обученную сеть
  /*val images_list = kohelma_images.sets.map(elem => elem._2).flatten
  //var sum = 0
  for {
    num <- 0 until 1000
  } {
    val random_image = (math.random*10).toInt
    val (_, char, image_array) = images_list(random_image)
    val outputs = elman.outputs(image_array)
    val (_, answer_index) = (outputs.zipWithIndex.find {case (output, index) => output == outputs.max}).get
    println("["+num+"] exptected: "+char+"; actual: "+kohelma_images.outputToCharacters(answer_index)/*+"; diff: "+(char.toInt - kohelma_images.outputToCharacters(answer_index).toInt)*/)
    //sum += (char.toInt - kohelma_images.outputToCharacters(answer_index).toInt)
  }*/
  //println("sum: "+sum)
}
